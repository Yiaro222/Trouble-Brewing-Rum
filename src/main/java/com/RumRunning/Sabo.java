
package com.RumRunning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



public class Sabo
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	private final ChatMessageManager   chatManager;
	
	private final Config config;
	
	private enum Offsets
	{
		FIRE_1 (1), /* Fire cycle 2 */
		FIRE_2 (2), /* Fire cycle 1 */
		BURNT_1(3), /* Burnt for 2 cycles */
		BURNT_2(4), /* Burnt for 1 cycle  */
		WET_1  (5), /* Burnt for 2 cycles, just put out */
		WET_2  (6), /* Burnt for 1 cycle,  just put out */
		BURNT_3(7); /* Burnt for 3 cycles / fully burnt, wasn't put out */
		
		public final int id;
		Offsets(int id) { this.id = id; }
	}
	
	private enum Type
	{
		PIPES_R (ObjectID.BREW_PIPES_RED,   ItemID.BREW_PIPE_SECTION,   true),
		PIPES_B (ObjectID.BREW_PIPES_BLUE,  ItemID.BREW_PIPE_SECTION,   false),
		HOPPER_R(ObjectID.BREW_HOPPER_RED,  ItemID.BREW_LUMBER_PATCH,   true),
		HOPPER_B(ObjectID.BREW_HOPPER_BLUE, ItemID.BREW_LUMBER_PATCH,   false),
		BRIDGE_R(ObjectID.BREW_BRIDGE_RED,  ItemID.BREW_BRIDGE_SECTION, true),
		BRIDGE_B(ObjectID.BREW_BRIDGE_BLUE, ItemID.BREW_BRIDGE_SECTION, true),
		
		/* Both sides have the same ID, and a different repair system, so this will
		 * have to be handled differently throughout the code. */
		PUMP(ObjectID.BREW_WATER_PUMP, ItemID.BREW_PIPE_SECTION, false);
		
		public final int working_id;
		public final int repair_item_id;
		
		public static Type
		getWorkingType(int id)
		{
			for (int i = 0; i < values().length; ++i)
			{
				if (id >= values()[i].working_id &&
			    	id <= values()[i].working_id + Offsets.BURNT_3.id)
				{
					return(values()[i]);
				}
			}
			
			return (null);
		}

		public static boolean
		containsWorkingID(int id)
		{
			for (int i = 0; i < Type.values().length; ++i)
			{
				if (values()[i].working_id == id) return(true);
			}
			
			return(false);
		}
		
		Type(int workingID, int repairItemID, boolean redSide)
		{
			working_id     = workingID;
			repair_item_id = repairItemID;
		}
	};
	
	private List<GameObject> brokenObjs          = new ArrayList<>();
	private List<WorldPoint> trackForSaboMessage = new ArrayList<>();
	
	/*
	 * TODO:
	 * > Have a notification when saboed
	 * > Highlight the required items on the table UI
	 * > Add a timer (or something) so the chatbox doesnt get nuked with messages
	 *   if the player joins midgame to a base which is half saboed
	 * > It re-displays the "X has been sabotaged" message when repairing
	 * */
	
	
	
	@Inject
	private
	Sabo(Client               client,
	     ModelOutlineRenderer modelOutlineRenderer,
	     ItemManager          itemManager,
	     ChatMessageManager   chatManager,
	     Config               config)
	{
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		this.chatManager          = chatManager;
		this.config               = config;
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		final WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
		
		if (!Utils.inMinigame) return(null);
		
		for (int i = 0; i < brokenObjs.size(); ++i)
		{
			final int id = brokenObjs.get(i).getId();
			
			BufferedImage icon          = null;
			Color         outlineColour = null;
			int           iconAmount    = 0;
			int           workingID     = 0;
			WorldPoint    objPos;
			
			objPos = brokenObjs.get(i).getWorldLocation();
			if (objPos.distanceTo(playerPos) > Utils.DRAW_DISTANCE ||
			    objPos.getPlane() != playerPos.getPlane())
			{
				continue;
			}
			
			if (config.displaySaboOutline())
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                brokenObjs.get(i),
				                                config.highlightType(),
				                                Color.RED);
			}
			
			for (Type x : Type.values())
			{
				workingID = x.working_id;
				if (id >= Type.PUMP.working_id && id <= Type.PUMP.working_id + 2)
				{
					workingID  = Type.PUMP.working_id;
					iconAmount = 1;
					break;
				}
				
				if ((id == workingID + Offsets.FIRE_1.id  ||
				     id == workingID + Offsets.FIRE_2.id) ||
				    (id == workingID + Offsets.BURNT_2.id ||
				     id == workingID + Offsets.WET_2.id))
				{
					iconAmount = 1;
					break;
				}
				else if (id == workingID + Offsets.BURNT_1.id ||
				         id == workingID + Offsets.WET_1.id)
				{
					iconAmount = 2;
					break;
				}
				else if (id == workingID + Offsets.BURNT_3.id)
				{
					iconAmount = 3;
					break;
				}
			}
			
			if (iconAmount == 0) continue;
			
			if (id == workingID + Offsets.FIRE_1.id  ||
			    (id == workingID + Offsets.FIRE_2.id &&
			     workingID != Type.PUMP.working_id)  ||
			    (workingID == Type.PUMP.working_id   &&
			     id == Type.PUMP.working_id + 1))
			{
				icon = Utils.ICON_BUCKET_OF_WATER;
			}
			else if (workingID == Type.PIPES_R.working_id ||
			         workingID == Type.PIPES_B.working_id)
			{
				icon = Utils.ICON_PIPE_SECTION;
			}
			else if (workingID == Type.HOPPER_R.working_id ||
			         workingID == Type.HOPPER_B.working_id)
			{
				icon          = Utils.ICON_LUMBER_PATCH;
				outlineColour = Color.WHITE;
			}
			else if (workingID == Type.BRIDGE_R.working_id ||
			         workingID == Type.BRIDGE_B.working_id)
			{
				icon          = Utils.ICON_BRIDGE_SECTION;
				outlineColour = Color.WHITE;
			}
			else if (workingID == Type.PUMP.working_id &&
			         id == Type.PUMP.working_id + 2)
			{
				icon = Utils.ICON_PIPE_SECTION;
			}
			
			if (config.displayRepairItems())
			{
				DrawIcon(graphics, icon, brokenObjs.get(i), iconAmount, outlineColour);
			}
		}
		
		return(null);
	}
	
	private void
	DrawIconThree(Graphics2D graphics, BufferedImage icon, GameObject gameObject,
	              Color outlineColour)
	{
		if (icon == null) return;
		
		DrawIcon(graphics, icon, GetPosition(gameObject, icon, -22,   0), outlineColour);
		DrawIcon(graphics, icon, GetPosition(gameObject, icon,   0, -15), outlineColour);
		DrawIcon(graphics, icon, GetPosition(gameObject, icon,  25,  -2), outlineColour);
	}
	
	private void
	DrawIconTwo(Graphics2D graphics, BufferedImage icon, GameObject gameObject,
	            Color outlineColour)
	{
		if (icon == null) return;
		
		DrawIcon(graphics, icon, GetPosition(gameObject, icon, -15, 0), outlineColour);
		DrawIcon(graphics, icon, GetPosition(gameObject, icon,  15, 0), outlineColour);
	}
	
	private void
	DrawIcon(Graphics2D graphics, BufferedImage icon, Point point,
	         Color outlineColour)
	{
		BufferedImage outline;
		
		if (icon == null || point == null) return;
		graphics.drawImage(icon, point.getX(), point.getY(), null);
		
		if (outlineColour != null)
		{
			outline = itemManager.getItemOutline(ItemID.BREW_BRIDGE_SECTION, 2,
			                                     outlineColour);
			graphics.drawImage(outline, point.getX(), point.getY(), null);
		}
	}
	
	private void
	DrawIcon(Graphics2D graphics, BufferedImage icon, GameObject gameObject,
	         int amount, Color outlineColour)
	{
		if (amount == 1)
		{
			DrawIcon(graphics, icon, GetPosition(gameObject, icon, 0, 0), outlineColour);
		}
		else if (amount == 2)
		{
			DrawIconTwo(graphics, icon, gameObject, outlineColour);
		}
		else if (amount == 3)
		{
			DrawIconThree(graphics, icon, gameObject, outlineColour);
		}
		else {  }
	}
	
	private Point
	GetPosition(GameObject obj, BufferedImage image, int offsetX, int offsetY)
	{
		LocalPoint lp;
		
		lp = obj.getLocalLocation();
		lp = new LocalPoint(lp.getX() + offsetX, lp.getY() + offsetY,
		                    lp.getWorldView());
		
		return(Perspective.getCanvasImageLocation(client, lp, image, 100));
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			brokenObjs.clear();
		}
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final WorldPoint wp         = gameObject.getWorldLocation();
		
		if (!Utils.getTeamsHalf().contains(wp))         return;
		if (Type.containsWorkingID(gameObject.getId())) return;
		
		if (trackForSaboMessage.stream().anyMatch(x ->
		    x.getX() == wp.getX() && x.getY() == wp.getY()))
		{
			final Type workingType = Type.getWorkingType(gameObject.getId());
			
			String message = "";
			String formattedMsg;
			
			if (workingType == Type.PIPES_R || workingType == Type.PIPES_B)
			{
				message = "Your team's Pipes have been sabotaged!";
			}
			else if (workingType == Type.HOPPER_R || workingType == Type.HOPPER_B)
			{
				message = "One of your team's Hoppers has been sabotaged!";
			}
			else if (workingType == Type.BRIDGE_R || workingType == Type.BRIDGE_B)
			{
				if (Utils.MIDDLE_WEST.contains(wp))
				{
					message = "Your team's Western Bridge has been sabotaged!";
				}
				else if (Utils.MIDDLE_CENTRE.contains(wp))
				{
					message = "Your team's Middle Bridge has been sabotaged!";
				}
				else if (Utils.MIDDLE_EAST.contains(wp))
				{
					message = "Your team's Eastern Bridge has been sabotaged!";
				}
			}
			else if (workingType == Type.PUMP)
			{
				message = "Your team's Water Pump has been sabotaged!";
			}

			if (message.length() > 0)
			{
				formattedMsg = new ChatMessageBuilder().append(ChatColorType.HIGHLIGHT)
				                                       .append(message)
				                                       .build();
				chatManager.queue(QueuedMessage.builder()
				           .type(ChatMessageType.CONSOLE)
				           .runeLiteFormattedMessage(formattedMsg)
				           .build());
			}
			
			trackForSaboMessage.remove(wp);
		}
		
		if (brokenObjs.contains(gameObject)) return;
		
		for (int i = 0; i < Type.values().length; ++i)
		{
			if (gameObject.getId() >= Type.values()[i].working_id + Offsets.FIRE_1.id  &&
			    gameObject.getId() <= Type.values()[i].working_id + Offsets.BURNT_3.id)
			{
				brokenObjs.add(gameObject);
			}
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		brokenObjs.remove(gameObject);
		
		if (Utils.inMinigame(client) &&
		    Type.containsWorkingID(gameObject.getId()))
		{
			trackForSaboMessage.add(gameObject.getWorldLocation());
		}
	}
	
}



