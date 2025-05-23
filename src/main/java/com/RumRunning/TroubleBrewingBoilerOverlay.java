
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
import net.runelite.client.ui.FontManager;
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
	
	/*
	 * TODO:
	 * > The boilers should all turn/flash red if the pipes have been saboed.
	 * > Have options for colour thresholds. some people want red 0-4, 5-9 yellow
	 *   and green at 10.
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
		
		logsIcon      = itemManager.getImage(ItemID.LOGS);
		tinderboxIcon = itemManager.getImage(ItemID.TINDERBOX);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		final Player player    = client.getLocalPlayer();
		final int    DRAW_DIST = 40; /* TODO: find how to properly do this */
		      Widget widget;
		      int    logCount;
		
		if (!(player.getWorldLocation().getRegionID() == 15150 &&
		      player.getWorldLocation().getPlane()    == 0))
		{
			return(null);
		}
		
		onRedTeam = client.getItemContainer(InventoryID.EQUIPMENT)
		                  .getItem(EquipmentInventorySlot.HEAD
		                  .getSlotIdx()).getId() == ItemID.PIRATE_HAT;
		
		for (int i = 0; i < 3; ++i)
		{
			Color colour;
			Point pos;
			int   dist;
			
			if (boilers[i] == null) continue;
			
			/* Don't draw if they're out of view */
			dist = boilers[i].getWorldLocation().distanceTo(player.getWorldLocation());
			if (dist > DRAW_DIST) continue;
			
			colour = Color.RED;
			if      (boilers[i].getId() == BOILER_HAS_LOG_IDS[i]) colour = Color.YELLOW;
			else if (boilers[i].getId() == BOILER_LIT_IDS    [i]) colour = Color.GREEN;
			
			DrawHighlightedGameObject(graphics, boilers[i], config.highlightType(), colour);
			
			if (config.displayBoilerIcons() &&
			    boilers[i].getId() == BOILER_EMPTY_IDS[i])
			{
				if (config.displayBoilerIcons())
				{
					pos = Perspective.getCanvasImageLocation(client,
					                                         boilers[i].getLocalLocation(),
					                                         tinderboxIcon, 150);
					if (pos != null)
					{
						graphics.drawImage(logsIcon, pos.getX(), pos.getY(), null);
					}
				}
			}
			else if (config.displayBoilerIcons() &&
			         boilers[i].getId() == BOILER_HAS_LOG_IDS[i])
			{
				if (config.displayBoilerIcons())
				{
					pos = Perspective.getCanvasImageLocation(client,
					                                         boilers[i].getLocalLocation(),
					                                         logsIcon, 150);
					if (pos != null)
					{
						graphics.drawImage(tinderboxIcon, pos.getX(), pos.getY(), null);
					}
				}
			}
			
			if (!config.displayBoilerLogCount()) continue;
			widget = client.getWidget(InterfaceID.BrewOverlay.BOILER1_COUNT + i);
			pos    = boilers[i].getCanvasTextLocation(graphics, "00/00", 0);
			if (widget != null && pos != null)
			{
				/* to centre better */
				pos = new Point(pos.getX() + config.fontSize() / 2, pos.getY());
				/* Do this really have to be called every render call?... */
				graphics.setFont(new Font(FontManager.getRunescapeFont().getName(),
				                          Font.PLAIN, config.fontSize()));
				graphics.setColor(config.fontColour());
				logCount = Integer.parseInt(widget.getText());
				OverlayUtil.renderTextLocation(graphics, pos, logCount + "/10", Color.GRAY);
			}
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
		final int dist = onRedTeam ?
		                 gameObject.getWorldLocation().distanceTo(redSideLocation) :
		                 gameObject.getWorldLocation().distanceTo(blueSideLocation);
		
		if (dist > 20) return;
		
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
}



