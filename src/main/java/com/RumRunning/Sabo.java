
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class Sabo
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	private final ChatMessageManager   chatManager;
	
	private final TroubleBrewingPlugin plugin;
	private final Config               config;
	
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
		PIPES_R (15837, 8930, true),
		PIPES_B (15863, 8930, false),
		HOPPER_R(15847, 8932, true),
		HOPPER_B(15873, 8932, false),
		BRIDGE_R(15855, 8979, true),
		BRIDGE_B(15881, 8979, true),
		
		/* Both sides have the same ID, and a different repair system, so this will
		 * have to be handled differently throughout the code. */
		PUMP(15936, 8930, false);
		
		public final int     working_id;
		public final int     repair_item_id;
		public final boolean on_red_side;
		
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
			on_red_side    = redSide;
			
		}
	};
	
	private List<GameObject> brokenObjs          = new ArrayList<>();
	private List<WorldPoint> trackForSaboMessage = new ArrayList<>();
	
	/*
	 * TODO:
	 * > Have a notification when saboed
	 * > Add other flammable objects like trees
	 * > Highlight the required items on the table UI
	 * > Add a highlight / background to the icons, its hard to tell which icon
	 *   is showing bcus its the same colour as its surroundings - mostly true
	 *   for the bridge section
	 * > Add a timer (or something) so the chatbox doesnt get nuked with messages
	 *   if the player joins midgame to a room half saboed
	 * > Fix the weird perspective issue with icons
	 * > Fix the arrangement of icons in DrawIconThree(...) (pipes overlap)
	 * > Test, test, test
	 * */
	
	
	
	@Inject
	private
	Sabo(Client               client,
	     ModelOutlineRenderer modelOutlineRenderer,
	     ItemManager          itemManager,
	     ChatMessageManager   chatManager,
	     TroubleBrewingPlugin plugin,
	     Config               config)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		this.chatManager          = chatManager;
		this.plugin               = plugin;
		this.config               = config;
		
		setLayer(OverlayLayer.ABOVE_SCENE);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		final Player player = client.getLocalPlayer();
		
		if (!Utils.inMinigame) return(null);
		
		for (int i = 0; i < brokenObjs.size(); ++i)
		{
			final int id = brokenObjs.get(i).getId();
			
			BufferedImage icon         = null;
			int           iconAmount   = 0;
			int           workingID    = 0;
			
			if (brokenObjs.get(i).getWorldLocation().distanceTo(player.getWorldLocation()) >
			    Utils.DRAW_DISTANCE ||
			    brokenObjs.get(i).getWorldLocation().getPlane() !=
			    player.getWorldLocation().getPlane())
			{
				continue;
			}
		
			/* THE DEFAULT "Outline" OPTION DOES NOT WOKR FOR SOME OBJECTS (bridge)*/
			Utils.drawHighlightedGameObject(graphics,
			                                modelOutlineRenderer,
			                                config,
			                                brokenObjs.get(i),
			                                config.highlightType(),
			                                Color.RED);
			
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
				icon = Utils.ICON_LUMBER_PATCH;
			}
			else if (workingID == Type.BRIDGE_R.working_id ||
			         workingID == Type.BRIDGE_B.working_id)
			{
				icon = Utils.ICON_BRIDGE_SECTION;
			}
			else if (workingID == Type.PUMP.working_id &&
			         id == Type.PUMP.working_id + 2)
			{
				icon = Utils.ICON_PIPE_SECTION;
			}
			
			DrawIcon(graphics, icon, brokenObjs.get(i), iconAmount);
		}
		
		return(null);
	}
	
	private void
	DrawIconThree(Graphics2D graphics, BufferedImage icon, GameObject gameObject)
	{
		Point p = gameObject.getCanvasLocation(150);
		if (icon == null || p == null) return;
		
		p = new Point(p.getX() - 11, p.getY());
		DrawIcon(graphics, icon, p);
		p = new Point(p.getX() + 11, p.getY() - 6);
		DrawIcon(graphics, icon, p);
		p = new Point(p.getX() + 15, p.getY() + 6);
		DrawIcon(graphics, icon, p);
	}
	
	private void
	DrawIconTwo(Graphics2D graphics, BufferedImage icon, GameObject gameObject)
	{
		Point p = gameObject.getCanvasLocation(150);
		if (icon == null || p == null) return;
		
		p = new Point(p.getX() - 4, p.getY());
		DrawIcon(graphics, icon, p);
		p = new Point(p.getX() + 8, p.getY() - 1);
		DrawIcon(graphics, icon, p);
	}
	
	private void
	DrawIcon(Graphics2D graphics, BufferedImage icon, Point point)
	{
		if (icon == null || point == null) return;
		graphics.drawImage(icon, point.getX(), point.getY(), null);
	}
	
	private void
	DrawIcon(Graphics2D graphics, BufferedImage icon, GameObject gameObject,
	         int amount)
	{
		if (amount == 1)
		{
			DrawIcon(graphics, icon, gameObject.getCanvasLocation(150));
		}
		else if (amount == 2)
		{
			DrawIconTwo(graphics, icon, gameObject);
		}
		else if (amount == 3)
		{
			DrawIconThree(graphics, icon, gameObject);
		}
		else {  }
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
		
		if (!Utils.getTeamsHalf().contains(wp)) return;
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



