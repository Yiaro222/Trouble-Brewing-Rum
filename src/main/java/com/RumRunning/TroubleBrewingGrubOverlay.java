
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
	private final TroubleBrewingConfig config;

    private WorldPoint swarmTile;
	private int ticksSinceLogin = 0;
	private int ticksWhileNoAggroAndDistance = 0;
	private boolean once = false;
	private boolean aggro = true;
	private int dist = 0;
	private boolean swarmExists = false;

	private double startTime = 0;
	private double endTime;
	private double currTime;

    final int diameter = 9;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private NPC HonestJimmy;

	private GameObject[] grubMounds = { null, null, null, null, null, null, null, null, null };
	private NPC[]   swarmNpc        = { null, null, null, null, null, null, null, null, null };


	@Inject
	private TroubleBrewingGrubOverlay(Client               client,
                                      ModelOutlineRenderer modelOutlineRenderer,
                                      TroubleBrewingConfig config)
	{
		this.client               = client;
        this.modelOutlineRenderer = modelOutlineRenderer;
		this.config               = config;
    }
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		if (client.getLocalPlayer() == null)
		{
			return(null);
		}

		GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        final int radius = 4;
        final int startX   = HonestJimmy.getWorldLocation().getX() - radius;
		final int startY   = HonestJimmy.getWorldLocation().getY() - radius;
		final int npcPlane = HonestJimmy.getWorldLocation().getPlane();
		final int[] xs = new int[4 * diameter + 1];
		final int[] ys = new int[xs.length];

        int x = startX;
		int y = startY;
		int z = npcPlane;

		if (startTime == 0 || currTime >= endTime)
		{
			startTime = Instant.now().toEpochMilli();
			endTime   = startTime + (600 * 20);
		}

		currTime = Instant.now().toEpochMilli();
        double current_distance = currTime - startTime; //right now
        double total_distance   = endTime  - startTime; //100%

        Point npcPointOnScene = Perspective.localToCanvas(client, HonestJimmy.getLocalLocation(), playerLocation.getPlane());

		ProgressPieComponent ppc = new ProgressPieComponent();
		ppc.setBorderColor(Color.ORANGE);
		ppc.setFill(Color.YELLOW);
		ppc.setPosition(npcPointOnScene);
		ppc.setProgress(1/ total_distance * current_distance);
		ppc.render(graphics);


		for (int i = 0; i < xs.length; i++)
		{
			if (i < diameter)
			{
				xs[0 * diameter + i] = startX + i;
				xs[1 * diameter + i] = startX + diameter;
				xs[2 * diameter + i] = startX + diameter - i;
				xs[3 * diameter + i] = startX;
				ys[0 * diameter + i] = startY;
				ys[1 * diameter + i] = startY + i;
				ys[2 * diameter + i] = startY + diameter;
				ys[3 * diameter + i] = startY + diameter - i;
			}
			else if (i == diameter)
			{
				xs[xs.length - 1] = xs[0];
				ys[ys.length - 1] = ys[0];
			}
			if (i == 0)
			{
				xs[0 * diameter + i] += 1;
				xs[1 * diameter + i] -= 1;
				xs[2 * diameter + i] -= 1;
				xs[3 * diameter + i] += 1;
				ys[0 * diameter + i] += 1;
				ys[1 * diameter + i] += 1;
				ys[2 * diameter + i] -= 1;
				ys[3 * diameter + i] -= 1;
				x = xs[i];
				y = ys[i];
			}

			boolean hasFirst = false;
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < TroubleBrewingUtils.DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}

			x = xs[i];
			y = ys[i];

			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < TroubleBrewingUtils.DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}

			//Drawing the radius on the scene
			graphics.setStroke(new BasicStroke((float) 1));
			graphics.setColor(Color.CYAN);
			graphics.draw(path);
		}

		//Check if player is inside of Trouble Brewing
		if (!TroubleBrewingUtils.inMinigame) return(null);

		for (int i = 0; i < 9; ++i)
		{
			if (grubMounds[i] == null) continue;

			dist = grubMounds[i].getWorldLocation().distanceTo(playerLocation);
			if (dist > TroubleBrewingUtils.DRAW_DISTANCE) continue;

			if (grubMounds[i].getId() == ObjectID.BREW_SWEETGRUB_MOUND)
			{
				TroubleBrewingUtils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						grubMounds[i],
						config.highlightType(),
						Color.GREEN);
			}
			else if (grubMounds[i].getId() == ObjectID.BREW_SWEETGRUB_MOUND_DEPELETED)
			{
				TroubleBrewingUtils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						grubMounds[i],
						config.highlightType(),
						Color.RED);
			}
		}

		for (int i = 0; i < 9; ++i)
		{
			if (swarmNpc[i] == null) continue;

			if (!aggro)
			{
				dist = swarmNpc[i].getWorldLocation().distanceTo(playerLocation);
				if (dist > TroubleBrewingUtils.DRAW_DISTANCE) continue;
			}
			else if (aggro)
			{
				dist = swarmTile.distanceTo(playerLocation);
				if (dist > TroubleBrewingUtils.DRAW_DISTANCE) continue;
			}

			modelOutlineRenderer.drawOutline(swarmNpc[i], config.outlineWidth(), Color.RED, 1);

			//log.info(String.valueOf(dist));

			if ((dist >= 5) && !once)
			{
				log.info("Swarm lost aggro on: " + ticksSinceLogin);
				once = true;
				aggro = false;
			}
		}

		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			for (int i = 0; i < 9; ++i)
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

		for (int i = 0; i < 9; ++i)
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

		for (int i = 0; i < 9; ++i)
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

		if (npc.getId() == NpcID.HONEST_JIMMY)
		{
			HonestJimmy = npc;
		}

		// Check if spawned NPC is Swarm
		if (!(npc.getId() == NpcID.BREW_SWARM))
			return;

		for (int i = 0; i < 9; ++i)
		{
			if (swarmNpc[i]  == null)
			{
				swarmNpc[i] = npc;
				swarmTile = npc.getWorldLocation();
				log.info("Swarm spawned on: " + ticksSinceLogin + ", on tile " + swarmTile);
				swarmExists = true;
				return;
			}
		}
	}

	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();

		for (int i = 0; i < 9; ++i)
		{
			if ((swarmNpc[i] != null) && (npc.equals(swarmNpc[i])))
			{
				 swarmNpc[i]  = null;
				 log.info("Swarm despawned on: " + ticksSinceLogin);
				 log.info("Swarm stayed out of distance for " + ticksWhileNoAggroAndDistance + " ticks");
				 log.info("-----------------------------------------------");
				 ticksWhileNoAggroAndDistance = 0;
				 once = false;
				 swarmExists = false;
				 aggro = true;
			}
		}
	}

	public void gameTick(GameTick tick)
	{
		ticksSinceLogin++;

		if ((dist >= 5) && once && swarmExists)
		{
			ticksWhileNoAggroAndDistance++;
			log.info("Swarm is out of range for " + ticksWhileNoAggroAndDistance + " ticks");
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

		return Perspective.localToCanvas(
				client,
				new LocalPoint(localPoint.getX() - LOCAL_TILE_SIZE / 2, localPoint.getY() - LOCAL_TILE_SIZE / 2, client.getTopLevelWorldView()),
				z);
	}
}



