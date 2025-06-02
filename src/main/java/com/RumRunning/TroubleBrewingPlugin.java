
package com.RumRunning;

import com.google.inject.Provides;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
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
	private Config config;
	@Inject
	private Utils  utils;
	@Inject
	private HUD    hud;
	
	
	
	@Override
	protected void
	startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(utils);
		overlayManager.add(hud);
	}
	
	@Override
	protected void
	shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
		overlayManager.add(utils);
		overlayManager.remove(hud);
	}
	
	@Subscribe
	public void
	onGameStateChanged(GameStateChanged gameStateChanged)
	{
		/* Call your class's gameStateChanged here, if it has one */
		hud.gameStateChanged(gameStateChanged);
	}
	
	@Subscribe
	public void
	onGameObjectSpawned(GameObjectSpawned event)
	{
		/* Call your class's gameObjectSpawned here, if it has one */
		hud.gameObjectSpawned(event);
	}
	
	@Subscribe
	public void
	onGameObjectDespawned(GameObjectDespawned event)
	{
		/* Call your class's gameObjectDespawned here, if it has one */
		hud.gameObjectDespawned(event);
	}
	
	@Subscribe
	public void
	onGameTick(GameTick gameTick)
	{
		hud.gameTick();
	}
	
	@Subscribe
	public void
	onConfigChanged(ConfigChanged event)
	{
		utils.configChanged(event);
		/* Call your class's configChanged here (always call after utils), if
		 * it has one */
	}
	
	@Subscribe
	public void
	onVarbitChanged(VarbitChanged event)
	{
		hud.varbitChanged(event);
	}
	
	@Subscribe
	public void
	onPlayerSpawned(PlayerSpawned event)
	{
		hud.playerSpawned(event);
	}
	
	@Subscribe
	public void
	onPlayerDespawned(PlayerDespawned event)
	{
		hud.playerDespawned(event);
	}
	
	@Subscribe
	public void
	onWidgetLoaded(WidgetLoaded event)
	{
		hud.widgetLoaded(event);
	}
	
	@Provides
	Config
	provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(Config.class);
	}
}



