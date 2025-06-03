
package com.RumRunning;

import javax.inject.Inject;

import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;



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
	private ItemManager itemManager;
	
	@Inject
	private Config config;
	@Inject
	private Utils  utils;
	@Inject
	private Boiler boiler;
	@Inject
	private MES    mes;
	@Inject
	private TroubleBrewingBarkOverlay barkOverlay;
	
	
	
	@Override
	protected void
	startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(utils);
		overlayManager.add(boiler);
		overlayManager.add(barkOverlay);
		
		mes = new MES(client, config);
	}
	
	@Override
	protected void
	shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
		overlayManager.remove(boiler);
		overlayManager.remove(barkOverlay);
		overlayManager.remove(utils);
	}
	
	@Subscribe
	public void
	onGameStateChanged(GameStateChanged gameStateChanged)
	{
		boiler.gameStateChanged(gameStateChanged);
		barkOverlay.gameStateChanged(gameStateChanged);
	}
	
	@Subscribe
	public void
	onPostMenuSort(PostMenuSort postMenuSort)
	{
		mes.postMenuSort(postMenuSort);
	}
	
	@Subscribe
	public void
	onClientTick(ClientTick clientTick)
	{
	}
	
	@Subscribe
	public void
	onGameObjectSpawned(GameObjectSpawned event)
	{
		boiler.gameObjectSpawned(event);
		barkOverlay.gameObjectSpawned(event);
	}
	
	@Subscribe
	public void
	onGameObjectDespawned(GameObjectDespawned event)
	{
		boiler.gameObjectDespawned(event);
		barkOverlay.gameObjectDespawned(event);
	}
	
	@Subscribe
	public void
	onConfigChanged(ConfigChanged event)
	{
		utils.configChanged(event);
	}
	
	@Provides
	Config
	provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Config.class);
	}
	
}



