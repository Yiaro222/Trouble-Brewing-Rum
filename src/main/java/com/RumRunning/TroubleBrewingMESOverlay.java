
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingMESOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;
	private final TroubleBrewingUtils  utils;
	
	/* Create a list of GameObjects to highlight / whatever here */
	
	
	
	@Inject
	private
	TroubleBrewingMESOverlay(Client               client,
	                         ModelOutlineRenderer modelOutlineRenderer,
	                         ItemManager          itemManager,
	                         TroubleBrewingPlugin plugin,
	                         TroubleBrewingConfig config,
	                         TroubleBrewingUtils  utils)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		this.plugin               = plugin;
		this.config               = config;
		this.utils                = utils;
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		/* Check utils.inMinigame and whatnot */
		
		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		/* if (gameStateChanged.getGameState() == GameState.LOADING)
		 * or whatever - & clear your game ObjectList here */
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		/* Add to GameObject list here */
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		/* Remove from GameObject list here */
	}
}



