
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingBoilerOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;

	private       boolean    onRedTeam;
	private final WorldPoint redSideLocation;
	private final WorldPoint blueSideLocation;
	
	private final BufferedImage logsIcon;
	private final BufferedImage tinderboxIcon;
	
	private final int BOILER_EMPTY_IDS  [] = { 15903, 15909, 15912 };
	private final int BOILER_HAS_LOG_IDS[] = { 15904, 15910, 15913 };
	private final int BOILER_LIT_IDS    [] = { 15905, 15911, 15914 };
	
	private GameObject boilers[] = { null, null, null };
	
	private boolean IN_LOBBY_TRIGGERED = false;
	
	/*
	 * TODO:
	 * > Highlight empty boilers red (have an option for outline/hull/click box)
	 * >> Highlight orange for yellow for unlit ("Logs")
	 * >> They should all turn red if the pipes have been saboed
	 * > Draw the icons after the outline bcus if its filled then you can't see the icons
	 * > Determine the team and choose the correct boilers from that
	 * > only draw inside tb on plane 0
	 * > I believe these are the interface IDs - https://github.com/runelite/runelite/blob/b95ceaf6a62f9a5c4ef1b0af695f9f8df74bd043/runelite-api/src/main/java/net/runelite/api/widgets/WidgetID.java#L191
	 * >> I would have expected there to be mulitple tho, like the resource UI "feelings" different to the scroll one
	 * > I believe I get the UI by calling Widget w = client.getWidget(...) but the args are confusing to me
	 * > 
	 * */
	
	
	
	@Inject
	private
	TroubleBrewingBoilerOverlay(Client               client,
	                            ModelOutlineRenderer modelOutlineRenderer,
	                            ItemManager          itemManager,
	                            TroubleBrewingPlugin plugin,
	                            TroubleBrewingConfig config)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		this.plugin               = plugin;
		this.config               = config;
		
		redSideLocation  = new WorldPoint(3815, 3000, 0);
		blueSideLocation = new WorldPoint(3815, 2950, 0);
		onRedTeam = true; // TEMP
		
		logsIcon      = itemManager.getImage(ItemID.LOGS);
		tinderboxIcon = itemManager.getImage(ItemID.TINDERBOX);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		//if (!(player.getWorldLocation().getRegionID() == 15150 &&
		//      player.getWorldLocation().getPlane()    == 0))
		{
			//return(null);
		}
		
		if (IN_LOBBY_TRIGGERED)
		{
			Widget widget;

			widget = client.getWidget(InterfaceID.BREW_WAITING_ROOM_OVERLAY,
			                          InterfaceID.BrewWaitingRoomOverlay.TIME_TEXT);
			if (widget != null)
			{
				log.info(widget.getText());
			}
			else
			{
				log.info("could not find text");
			}
		}
		
		for (int i = 0; i < 3; ++i)
		{
			Color colour;
			Point pos;
			
			if (boilers[i] == null) continue;

			colour = Color.RED;
			if (boilers[i].getId() == BOILER_EMPTY_IDS[i])
			{
				if (config.displayBoilerIcons())
				{
					pos = Perspective.getCanvasImageLocation(client,
					                                         boilers[i].getLocalLocation(),
					                                         tinderboxIcon, 150);
					graphics.drawImage(logsIcon, pos.getX(), pos.getY(), null);
				}
			}
			else if (boilers[i].getId() == BOILER_HAS_LOG_IDS[i])
			{
				colour = Color.YELLOW;
				if (config.displayBoilerIcons())
				{
					pos = Perspective.getCanvasImageLocation(client,
					                                         boilers[i].getLocalLocation(),
					                                         logsIcon, 150);
					graphics.drawImage(tinderboxIcon, pos.getX(), pos.getY(), null);
				}
			}
			else if (boilers[i].getId() == BOILER_LIT_IDS[i])
			{
				colour = Color.GREEN;
			}
			
			DrawHighlightedGameObject(graphics, boilers[i], config.highlightType(), colour);
		}
		
		return(null);
	}
	
	private void
	DrawHighlightedGameObject(Graphics2D                         graphics,
	                          GameObject                         obj,
	                          TroubleBrewingConfig.HighlightType type,
	                          Color                              colour)
	{
		if (type == TroubleBrewingConfig.HighlightType.NONE)
		{
			return;
		}
		else if (type == TroubleBrewingConfig.HighlightType.OUTLINE)
		{
			modelOutlineRenderer.drawOutline(obj, config.outlineWidth(), colour, 1);
		}
		else if (type == TroubleBrewingConfig.HighlightType.HULL_OUTLINE)
		{
			graphics.setColor(colour);
			graphics.draw(obj.getConvexHull());
		}
		else if (type == TroubleBrewingConfig.HighlightType.HULL_FILLED)
		{
			graphics.setColor(colour);
			graphics.fill(obj.getConvexHull());
		}
		else if (type == TroubleBrewingConfig.HighlightType.CLICKBOX_OUTLINE)
		{
			graphics.setColor(colour);
			graphics.draw(obj.getClickbox());
		}
		else if (type == TroubleBrewingConfig.HighlightType.CLICKBOX_FILLED)
		{
			graphics.setColor(colour);
			graphics.fill(obj.getClickbox());
		}
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
//			boolean isBlueTeam = client.getItemContainer(InventoryID.EQUIPMENT)
//					.getItem(EquipmentInventorySlot.HEAD
//					.getSlotIdx()).getId() == ItemID.PIRATE_BANDANA_8949;
			for (int i = 0; i < 3; ++i)
			{
				boilers[i] = null;
			}
		}
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final int dist = gameObject.getWorldLocation().distanceTo(redSideLocation);
		
		if (dist > 20) return; // TEMP: ONLY DRAW RED SIDE FOR NOW
		
		for (int i = 0; i < 3; ++i)
		{
			if      (gameObject.getId() == BOILER_EMPTY_IDS  [i]) boilers[i] = gameObject;
			else if (gameObject.getId() == BOILER_HAS_LOG_IDS[i]) boilers[i] = gameObject;
			else if (gameObject.getId() == BOILER_LIT_IDS    [i]) boilers[i] = gameObject;
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		for (int i = 0; i < 3; ++i)
		{
			if ((boilers[i] != null) && (gameObject.equals(boilers[i])))
			{
				boilers[i] = null;
			}
		}
	}
	
	public void
	widgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.BREW_WAITING_ROOM_OVERLAY)
		{
			log.info("IN LOBBY");
			IN_LOBBY_TRIGGERED = true;
		}
	}
	
	public void
	widgetClosed(WidgetClosed event)
	{
		//
	}
}



