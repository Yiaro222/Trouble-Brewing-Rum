
package com.RumRunning;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.api.events.*;



@Slf4j
@PluginDescriptor(
	name = "Trouble Brewing Rum"
)
public class TroubleBrewingPlugin
extends      Plugin
{
	@Inject
	private Client         client;
	@Inject
	private OverlayManager overlayManager;
	
	@Inject
	private TroubleBrewingConfig           config;
	@Inject
	private TroubleBrewingOverlay          overlay;
	@Inject
	private TroubleBrewingInterfaceOverlay interfaceOverlay;
	
	
	
	@Override
	protected void
	startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(overlay);
		overlayManager.add(interfaceOverlay);
	}
	
	@Override
	protected void
	shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
		overlayManager.remove(overlay);
		overlayManager.remove(interfaceOverlay);
	}
	
	@Subscribe
	public void
	onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			overlay.setRenderableJunglePlant(null);
		}
	}
	
	@Subscribe
	public void
	onGameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		if (gameObject.getId() == 1204                   &&
		    gameObject.getWorldLocation().getX() == 3815 &&
		    gameObject.getWorldLocation().getY() == 3024)
		{
			overlay.setRenderableJunglePlant(gameObject);
		}
	}
	
	@Subscribe
	public void
	onGameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		if (gameObject.getId() == 1204                   &&
		    gameObject.getWorldLocation().getX() == 3815 &&
		    gameObject.getWorldLocation().getY() == 3024)
		{
			overlay.setRenderableJunglePlant(null);
		}
	}
	
	@Provides
	TroubleBrewingConfig
	provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TroubleBrewingConfig.class);
	}
}



