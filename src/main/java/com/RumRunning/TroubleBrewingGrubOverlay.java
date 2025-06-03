
package com.RumRunning;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.time.Instant;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.ui.overlay.components.ProgressPieComponent;



@Slf4j
public class TroubleBrewingGrubOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	
	private final Config               config;
	
	private double startTime = 0;
	private double endTime;
	private double currTime;
	
	private GameObject[] grubMounds = { null, null, null, null, null, null, null, null, null };
	private NPC[]        swarmNpc   = { null, null, null, null, null, null, null, null, null };
	
	
	@Inject
	private
	TroubleBrewingGrubOverlay(Client               client,
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
		if (!config.enableSweetgrubInfo()) return(null);
		
		//Check if player is ingame
		if (client.getLocalPlayer() == null)
		{
			return(null);
		}
		
		//Check if player is inside of Trouble Brewing
		if (!Utils.inMinigame) return(null);
		
		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		
		for (int i = 0; i < grubMounds.length; ++i)
		{
			if (grubMounds[i] == null) continue;
			
			int dist = grubMounds[i].getWorldLocation().distanceTo(playerLocation);
			if (dist > Utils.DRAW_DISTANCE) continue;
			
			if (grubMounds[i].getId() == ObjectID.BREW_SWEETGRUB_MOUND)
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                grubMounds[i],
				                                config.highlightType(),
				                                Color.GREEN);
			}
			else if (grubMounds[i].getId() == ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED)
			{
				Utils.drawHighlightedGameObject(graphics,
				                                modelOutlineRenderer,
				                                config,
				                                grubMounds[i],
				                                config.highlightType(),
				                                Color.RED);
			}
		}
		
		for (int i = 0; i < swarmNpc.length; ++i)
		{
			if (swarmNpc[i] == null) continue;
			
			GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
			final int radius = 4;
			final int diameter = 9;
			final int startX   = swarmNpc[i].getWorldLocation().getX() - radius;
			final int startY   = swarmNpc[i].getWorldLocation().getY() - radius;
			final int npcPlane = swarmNpc[i].getWorldLocation().getPlane();
			final int[] xs     = new int[4 * diameter + 1];
			final int[] ys     = new int[xs.length];
			
			Color radiusAndPieColour;
			int x = startX;
			int y = startY;
			int z = npcPlane;
			
			int dist = swarmNpc[i].getWorldLocation().distanceTo(playerLocation);
			if (dist > Utils.DRAW_DISTANCE) continue;
			
			
			if (dist >= 5)
			{
				//If swarm is able to despawn
				radiusAndPieColour = Color.GREEN;
			}
			else
			{
				//If swarm is not able to despawn
				radiusAndPieColour = Color.RED;
			}
			
			if (startTime == 0 || currTime >= endTime)
			{
				startTime = Instant.now().toEpochMilli();
				endTime   = startTime + (600 * 20);
			}
			
			currTime = Instant.now().toEpochMilli();
			double current_distance = currTime - startTime; //right now
			double total_distance   = endTime  - startTime; //100%
			
			//Outline Swarm NPC
			modelOutlineRenderer.drawOutline(swarmNpc[i], config.outlineWidth(), Color.RED, 1);
			
			Point piePoint = Perspective.localToCanvas(client, swarmNpc[i].getLocalLocation(), playerLocation.getPlane(), 135);
			
			ProgressPieComponent ppc = new ProgressPieComponent();
			ppc.setBorderColor(radiusAndPieColour);
			ppc.setFill(radiusAndPieColour);
			ppc.setPosition(piePoint);
			ppc.setProgress(1/ total_distance * current_distance);
			ppc.render(graphics);
			
			
			for (int j = 0; j < xs.length; j++)
			{
				if (j < diameter)
				{
					xs[0 * diameter + j] = startX + j;
					xs[1 * diameter + j] = startX + diameter;
					xs[2 * diameter + j] = startX + diameter - j;
					xs[3 * diameter + j] = startX;
					ys[0 * diameter + j] = startY;
					ys[1 * diameter + j] = startY + j;
					ys[2 * diameter + j] = startY + diameter;
					ys[3 * diameter + j] = startY + diameter - j;
				}
				else if (j == diameter)
				{
					xs[xs.length - 1] = xs[0];
					ys[ys.length - 1] = ys[0];
				}
				if (j == 0)
				{
					xs[0 * diameter + j] += 1;
					xs[1 * diameter + j] -= 1;
					xs[2 * diameter + j] -= 1;
					xs[3 * diameter + j] += 1;
					ys[0 * diameter + j] += 1;
					ys[1 * diameter + j] += 1;
					ys[2 * diameter + j] -= 1;
					ys[3 * diameter + j] -= 1;
					x = xs[j];
					y = ys[j];
				}
				
				boolean hasFirst = false;
				if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < Utils.DRAW_DISTANCE)
				{
					hasFirst = moveTo(path, x, y, z);
				}
				
				x = xs[j];
				y = ys[j];
				
				if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < Utils.DRAW_DISTANCE)
				{
					lineTo(path, x, y, z);
				}
				
				//Drawing the radius on the scene
				graphics.setStroke(new BasicStroke((float) 1));
				graphics.setColor(radiusAndPieColour);
				graphics.draw(path);
			}
		}
		
		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			for (int i = 0; i < grubMounds.length; ++i)
			{
				grubMounds[i] = null;
				swarmNpc  [i] = null;
			}
		}
		
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		if (!(gameObject.getId() == ObjectID.BREW_SWEETGRUB_MOUND ||
		      gameObject.getId() == ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED))
			return;
		
		for (int i = 0; i < grubMounds.length; ++i)
		{
			if (grubMounds[i]  == null)
			{
				grubMounds[i] = gameObject;
				return;
			}
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		for (int i = 0; i < grubMounds.length; ++i)
		{
			if ((grubMounds[i] != null) && (gameObject.equals(grubMounds[i])))
			{
				 grubMounds[i]  = null;
			}
		}
	}

	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		
		// Check if spawned NPC is Swarm
		if (!(npc.getId() == NpcID.BREW_SWARM))
			return;
		
		for (int i = 0; i < swarmNpc.length; ++i)
		{
			if (swarmNpc[i]  == null)
			{
				swarmNpc[i] = npc;;
				return;
			}
		}
	}
	
	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();
		
		for (int i = 0; i < swarmNpc.length; ++i)
		{
			if ((swarmNpc[i] != null) && (npc.equals(swarmNpc[i])))
			{
				 swarmNpc[i]  = null;;
			}
		}
	}
	
	private boolean moveTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.moveTo(point.getX(), point.getY());
			return true;
		}
		return false;
	}
	
	private void lineTo(GeneralPath path, final int x, final int y, final int z)
	{
		Point point = XYToPoint(x, y, z);
		if (point != null)
		{
			path.lineTo(point.getX(), point.getY());
		}
	}
	
	private Point XYToPoint(int x, int y, int z)
	{
		LocalPoint localPoint = LocalPoint.fromWorld(client.getTopLevelWorldView(), x, y);
		
		if (localPoint == null)
		{
			return(null);
		}
		
		return Perspective.localToCanvas(client, new LocalPoint(
				localPoint.getX() - Perspective.LOCAL_TILE_SIZE / 2,
				localPoint.getY() - Perspective.LOCAL_TILE_SIZE / 2,
				client.getTopLevelWorldView()),
				z,
				30);
	}
	
}



