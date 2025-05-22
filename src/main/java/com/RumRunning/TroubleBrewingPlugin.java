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
public class TroubleBrewingPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private TroubleBrewingConfig config;

	@Inject
	private TroubleBrewingOverlay TroubleBrewingOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		log.info(" ##### Plugin started! ##### ");
		overlayManager.add(TroubleBrewingOverlay);
		Player player = client.getLocalPlayer();
		//log.info(player.getName());

	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info(" ##### Plugin stopped! ##### ");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.StringText1(), null);
		}
	}


	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		GameObject gameObject = event.getGameObject();
		if (gameObject.getId() == 1204 && gameObject.getWorldLocation().getX() == 3815 && gameObject.getWorldLocation().getY() == 3024){
			log.info("Plant found");
			TroubleBrewingOverlay.renderableJunglePlant = gameObject;

			int testvariable;
			testvariable = gameObject.getId();
			log.info("testvariable: "+ testvariable);
			//scene location: 39, 16
		}
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		log.info("GameObjectDespawned called");
		GameObject gameObject = event.getGameObject();
		if (gameObject.getId() == 1204 && gameObject.getWorldLocation().getX() == 3815 && gameObject.getWorldLocation().getY() == 3024){
			log.info("Plant unfound");
			TroubleBrewingOverlay.renderableJunglePlant = null;
		} else {
			TroubleBrewingOverlay.renderableJunglePlant = null;
		}
	}

	@Subscribe
	public void onGraphicsObjectCreated(GraphicsObjectCreated event)
	{
		log.info("GraphicsObjectCreated called");
	}

	@Provides
	TroubleBrewingConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(TroubleBrewingConfig.class);
	}
}
