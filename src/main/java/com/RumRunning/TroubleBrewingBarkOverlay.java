
package com.RumRunning;

import java.awt.*;
import java.time.Instant;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.ObjectID;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingBarkOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	
	private final Config config;
	
	private double startTime = 0;
	private double endTime;
	private double currTime;
	
	private GameObject[] scrapyTrees = { null, null, null, null, null, null, null, null };
	
	@Inject
	private TroubleBrewingBarkOverlay(Client               client,
	                                  ModelOutlineRenderer modelOutlineRenderer,
	                                  Config               config)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.config               = config;
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		//Check if player is ingame
		if (client.getLocalPlayer() == null)
		{
			return(null);
		}
		
		if (!config.enableTreeInfo()) return(null);
		
		//Check if player is inside of Trouble Brewing
		if (!Utils.inMinigame) return(null);
		
		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		for (int i = 0; i < scrapyTrees.length; ++i)
		{
			int dist;
			
			if (scrapyTrees[i] == null) continue;
			
			dist = scrapyTrees[i].getWorldLocation().distanceTo(playerLocation);
			if (dist > Utils.DRAW_DISTANCE) continue;
			
			if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE)
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                scrapyTrees[i],
				                                config.highlightType(),
				                                config.treeActiveColour());
			}
			else if (scrapyTrees[i].getId() == ObjectID.BREW_SCRAPEY_TREE_STUMP)
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                scrapyTrees[i],
				                                config.highlightType(),
				                                config.treeInactiveColour());
				
				if (startTime == 0 || currTime >= endTime)
				{
					startTime = Instant.now().toEpochMilli();
					endTime   = startTime + (600 * 24);
				}
				
				currTime = Instant.now().toEpochMilli();
				double current_distance = currTime - startTime; //right now
				double total_distance   = endTime  - startTime; //100%
				
				Point piePoint = Perspective.localToCanvas(client, scrapyTrees[i].getLocalLocation(), playerLocation.getPlane(), 20);
				
				ProgressPieComponent ppc = new ProgressPieComponent();
				ppc.setBorderColor(Color.ORANGE);
				ppc.setFill(Color.YELLOW);
				ppc.setPosition(piePoint);
				ppc.setProgress(1/ total_distance * current_distance);
				ppc.render(graphics);
				
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



