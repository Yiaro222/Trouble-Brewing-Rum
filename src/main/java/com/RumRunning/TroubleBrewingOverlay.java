package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.inject.Inject;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import lombok.Setter;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.api.events.*;



@Slf4j
public class TroubleBrewingOverlay
extends      Overlay
{
	private final Client               client;
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	
	@Setter
	private GameObject renderableJunglePlant;
	
	private final BufferedImage tinderboxIcon;
	
	
	@Inject
	private
	TroubleBrewingOverlay(Client client,
	                      TroubleBrewingPlugin plugin,
	                      TroubleBrewingConfig config,
	                      ModelOutlineRenderer modelOutlineRenderer,
	                      ItemManager          itemManager)
	{
		this.client               = client;
		this.plugin               = plugin;
		this.config               = config;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		
		tinderboxIcon = itemManager.getImage(ItemID.TINDERBOX);
	 }
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		Player localPlayerData;
		Point  overheadTextTestPosition;
		Point  iconTestPosition;
		
		if (renderableJunglePlant == null)
		{
			log.info("NOTHING TO DRAW");
			return null;
		}
		
		log.info("DRAWING PLANT");
		
		localPlayerData = client.getLocalPlayer();
		overheadTextTestPosition = localPlayerData.getCanvasTextLocation
		(
			graphics, "HeightOffset",
			localPlayerData.getLogicalHeight() + 40
		);
		iconTestPosition = Perspective.getCanvasImageLocation
		(
			client,
			renderableJunglePlant.getLocalLocation(),
			tinderboxIcon, 150
		);
		
		modelOutlineRenderer.drawOutline(renderableJunglePlant, 3, Color.ORANGE, 1);
		graphics.draw(renderableJunglePlant.getConvexHull());
		
		modelOutlineRenderer.drawOutline(localPlayerData, 3, Color.MAGENTA,1);
		OverlayUtil.renderTextLocation(graphics, overheadTextTestPosition,
		                               "Certified Rum Runner", Color.MAGENTA);
		
		graphics.drawImage(tinderboxIcon,
		                   iconTestPosition.getX(), iconTestPosition.getY(),
		                   null);
		
		return null;
	 }
}
