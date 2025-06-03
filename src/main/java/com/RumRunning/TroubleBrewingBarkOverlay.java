
package com.RumRunning;

import java.awt.*;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingBarkOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	
	private final Config config;

	private GameObject[] scrapyTrees = { null, null, null, null, null, null, null, null };
	
	@Inject
	private TroubleBrewingBarkOverlay(Client               client,
	                                  ModelOutlineRenderer modelOutlineRenderer,
	                                  Config               config)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.config               = config;
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		final Player player = client.getLocalPlayer();

		if (!Utils.inMinigame) return(null);

		for (int i = 0; i < scrapyTrees.length; ++i)
		{
			int dist;

			if (scrapyTrees[i] == null) continue;

			dist = scrapyTrees[i].getWorldLocation().distanceTo(player.getWorldLocation());
			if (dist > Utils.DRAW_DISTANCE) continue;

			if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE)
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                scrapyTrees[i],
				                                config.highlightType(),
				                                Color.GREEN);
			}
			else if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE_STUMP)
			{
				Utils.drawHighlightedGameObject(graphics,
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
			for (int i = 0; i < scrapyTrees.length; ++i)
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

		for (int i = 0; i < scrapyTrees.length; ++i)
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

		for (int i = 0; i < scrapyTrees.length; ++i)
		{
			if ((scrapyTrees[i] != null) && (gameObject.equals(scrapyTrees[i])))
			{
				scrapyTrees[i] = null;
			}
		}
	}
	
}



