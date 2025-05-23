
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
	private TroubleBrewingConfig        config;
	@Inject
	private TroubleBrewingBoilerOverlay boilerOverlay;
	
	
	
	@Override
	protected void
	startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(boilerOverlay);
	}
	
	@Override
	protected void
	shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
		overlayManager.remove(boilerOverlay);
	}
	
	@Subscribe
	public void
	onGameStateChanged(GameStateChanged gameStateChanged)
	{
		boilerOverlay.gameStateChanged(gameStateChanged);
	}
	
	@Subscribe
	public void
	onGameObjectSpawned(GameObjectSpawned event)
	{
		boilerOverlay.gameObjectSpawned(event);
	}
	
	@Subscribe
	public void
	onGameObjectDespawned(GameObjectDespawned event)
	{
		boilerOverlay.gameObjectDespawned(event);
	}
	
	@Subscribe
	public void
	onWidgetLoaded(WidgetLoaded event)
	{
		boilerOverlay.widgetLoaded(event);
	}
	
	@Subscribe
	public void
	onWidgetClosed(WidgetClosed event)
	{
		boilerOverlay.widgetClosed(event);
	}
	
	@Provides
	TroubleBrewingConfig
	provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TroubleBrewingConfig.class);
	}
}



