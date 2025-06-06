
package com.RumRunning;

import javax.inject.Inject;

import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.Client;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;



@Slf4j
@PluginDescriptor(
	name = "Trouble Brewing Rum"
)
public class TroubleBrewingPlugin
extends      Plugin
{
	@Inject
	private Client             client;
	@Inject
	private OverlayManager     overlayManager;
	@Inject
	private ChatMessageManager chatManager;
	@Inject
	private PluginManager      pluginManager;
	
	@Inject
	private Config config;
	@Inject
	private Utils  utils;
	@Inject
	private Boiler boiler;
	@Inject
	private MES    mes;
	@Inject
	private Sabo   sabo;
	@Inject
	private TroubleBrewingBarkOverlay barkOverlay;
	@Inject
	private TroubleBrewingGrubOverlay grubOverlay;
	
	
	
	@Override
	protected void
	startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(utils);
		overlayManager.add(boiler);
		overlayManager.add(grubOverlay);
		overlayManager.add(barkOverlay);
		overlayManager.add(sabo);
		
		mes = new MES(client, config);
	}
	
	@Override
	protected void
	shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
		overlayManager.remove(boiler);
		overlayManager.remove(barkOverlay);
		overlayManager.remove(sabo);
		overlayManager.remove(grubOverlay);
		overlayManager.remove(utils);
	}
	
	@Subscribe
	public void
	onGameStateChanged(GameStateChanged gameStateChanged)
	{
		boiler.gameStateChanged(gameStateChanged);
		barkOverlay.gameStateChanged(gameStateChanged);
		grubOverlay.gameStateChanged(gameStateChanged);
		sabo.gameStateChanged(gameStateChanged);
	}
	
	@Subscribe
	public void
	onPostMenuSort(PostMenuSort postMenuSort)
	{
		mes.postMenuSort(postMenuSort);
	}
	
	@Subscribe
	public void
	onGameTick(GameTick gameTick)
	{
		utils.gameTick(gameTick);
	}
	
	@Subscribe
	public void
	onGameObjectSpawned(GameObjectSpawned event)
	{
		boiler.gameObjectSpawned(event);
		barkOverlay.gameObjectSpawned(event);
		grubOverlay.gameObjectSpawned(event);
		sabo.gameObjectSpawned(event);
	}
	
	@Subscribe
	public void
	onGameObjectDespawned(GameObjectDespawned event)
	{
		boiler.gameObjectDespawned(event);
		barkOverlay.gameObjectDespawned(event);
		grubOverlay.gameObjectDespawned(event);
		sabo.gameObjectDespawned(event);
	}

	@Subscribe
	public void
	onNpcSpawned(NpcSpawned event)
	{
		grubOverlay.onNpcSpawned(event);
	}

	@Subscribe
	public void
	onNpcDespawned(NpcDespawned event)
	{
		grubOverlay.onNpcDespawned(event);
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



