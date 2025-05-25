
package com.RumRunning;

import java.awt.*;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingBarkOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;
	private final TroubleBrewingUtils  utils;

	private GameObject[] scrapyTrees = { null, null, null, null, null, null, null, null };
	
	@Inject
	private TroubleBrewingBarkOverlay(Client               client,
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
		final Player player    = client.getLocalPlayer();

		if (!TroubleBrewingUtils.inMinigame) return(null);

		for (int i = 0; i < 8; ++i)
		{
			int dist;

			if (scrapyTrees[i] == null) continue;

			dist = scrapyTrees[i].getWorldLocation().distanceTo(player.getWorldLocation());
			if (dist > TroubleBrewingUtils.DRAW_DISTANCE) continue;

			if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE)
			{
				TroubleBrewingUtils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						scrapyTrees[i],
						config.highlightType(),
						Color.GREEN);
			}
			else if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE_STUMP)
			{
				TroubleBrewingUtils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						scrapyTrees[i],
						config.highlightType(),
						Color.RED);
			}
		}

		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			for (int i = 0; i < 8; ++i)
			{
				scrapyTrees[i] = null;
			}
		}

	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		/* Add to GameObject list here */

		if (!(gameObject.getId() == ObjectID.BREW_SCRAPEY_TREE ||
		      gameObject.getId() == ObjectID.BREW_SCRAPEY_TREE_STUMP))
			return;

		for (int i = 0; i < 8; ++i)
		{
			if (scrapyTrees[i]  == null)
			{
				scrapyTrees[i] = gameObject;
				return;
			}
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();

		for (int i = 0; i < 8; ++i)
		{
			if ((scrapyTrees[i] != null) && (gameObject.equals(scrapyTrees[i])))
			{
				scrapyTrees[i] = null;
			}
		}
	}
}



