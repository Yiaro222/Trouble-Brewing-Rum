
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
public class Bitternut
		extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final Config config;

	private WorldPoint nullSpawnTile;
	private int ticksSinceLogin = 0;
	private int ticksWhileNoAggroAndDistance = 0;
	private boolean once = false;
	private boolean aggro = true;
	private int dist = 0;
	private boolean nullExists = false;

	private double startTime = 0;
	private double endTime;
	private double currTime;

	final int diameter = 9;
	private static final int LOCAL_TILE_SIZE = Perspective.LOCAL_TILE_SIZE;

	private NPC HonestJimmy;

	private GameObject[] bitternutTree    = { null, null, null };
	private NPC[]        nullNpc          = { null, null, null, null, null, null, null, null, null, null, null, null };
	private int[]		 nullNpcIDs	      = { 1819, 1820, 1821, 1822, 1823, 1824 };
	private String[]	 nullNpcNames     = { "brew_tree_red",  "brew_tree_red_careful", "brew_tree_red_angry", "brew_tree_blue", "brew_tree_blue_careful", "brew_tree_blue_angry"};

	@Inject
	private Bitternut(Client               client,
					  ModelOutlineRenderer modelOutlineRenderer,
					  Config config)
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
			if (playerLocation.distanceTo(new WorldPoint(x, y, z)) < Utils.DRAW_DISTANCE)
			{
				hasFirst = moveTo(path, x, y, z);
			}

			x = xs[i];
			y = ys[i];

			if (hasFirst && playerLocation.distanceTo(new WorldPoint(x, y, z)) < Utils.DRAW_DISTANCE)
			{
				lineTo(path, x, y, z);
			}

			//Drawing the radius on the scene
			graphics.setStroke(new BasicStroke((float) 1));
			graphics.setColor(Color.CYAN);
			graphics.draw(path);
		}

		//Check if player is inside of Trouble Brewing
		if (!Utils.inMinigame) return(null);

		for (int i = 0; i < 9; ++i)
		{
			if (bitternutTree[i] == null) continue;

			dist = bitternutTree[i].getWorldLocation().distanceTo(playerLocation);
			if (dist > Utils.DRAW_DISTANCE) continue;

			if (bitternutTree[i].getId() == ObjectID.BREW_BITTERNUT_TREE)
			{
				Utils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						bitternutTree[i],
						config.highlightType(),
						Color.GREEN);
			}
			else if (bitternutTree[i].getId() == ObjectID.BREW_BITTERNUT_TREE_READY_RED)
			{
				Utils.drawHighlightedGameObject(graphics,
						modelOutlineRenderer,
						config,
						bitternutTree[i],
						config.highlightType(),
						Color.RED);
			}
		}

		for (int i = 0; i < 9; ++i)
		{
			if (nullNpc[i] == null) continue;

			if (!aggro)
			{
				dist = nullNpc[i].getWorldLocation().distanceTo(playerLocation);
				if (dist > Utils.DRAW_DISTANCE) continue;
			}
			else if (aggro)
			{
				dist = nullSpawnTile.distanceTo(playerLocation);
				if (dist > Utils.DRAW_DISTANCE) continue;
			}

			modelOutlineRenderer.drawOutline(nullNpc[i], config.outlineWidth(), Color.RED, 1);

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
				bitternutTree[i] = null;
				nullNpc      [i] = null;
			}
		}

	}

	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final int gameObjectId = gameObject.getId();

		if (!(gameObjectId == ObjectID.BREW_BITTERNUT_TREE 			  		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_THROW_BLUE 		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_THROW_RED  		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_CLIMB_BLUE 		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_CLIMB_RED  		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_READY_BLUE 		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_READY_RED  		 ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_FIGHT_BLUE_DEFEND ||
			  gameObjectId == ObjectID.BREW_BITTERNUT_TREE_FIGHT_RED_DEFEND))
			return;

		for (int i = 0; i < 9; ++i)
		{
			if (bitternutTree[i]  == null)
			{
				bitternutTree[i] = gameObject;
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
			if ((bitternutTree[i] != null) && (gameObject.equals(bitternutTree[i])))
			{
				bitternutTree[i]  = null;
			}
		}
	}

	public void onNpcSpawned(NpcSpawned event)
	{
		final NPC npc = event.getNpc();
		final int npcId = npc.getId();

		if (npcId == NpcID.HONEST_JIMMY)
		{
			HonestJimmy = npc;
		}

		// Check if spawned NPC is Swarm
		if (!(npcId == NpcID.BREW_TREE_RED))
			return;

		if (!(npcId == NpcID.BREW_TREE_RED 			||
			  npcId == NpcID.BREW_TREE_RED_CAREFUL  ||
			  npcId == NpcID.BREW_TREE_RED_ANGRY    ||
			  npcId == NpcID.BREW_TREE_BLUE  		||
			  npcId == NpcID.BREW_TREE_BLUE_CAREFUL ||
			  npcId == NpcID.BREW_TREE_BLUE_ANGRY))
			return;

		for (int i = 0; i < 9; ++i)
		{
			if (nullNpc[i]  == null)
			{
				nullNpc[i] = npc;
				nullSpawnTile = npc.getWorldLocation();
				log.info("Swarm spawned on: " + ticksSinceLogin + ", on tile " + nullSpawnTile);
				nullExists = true;
				return;
			}
		}
	}

	public void onNpcDespawned(NpcDespawned event)
	{
		NPC npc = event.getNpc();

		for (int i = 0; i < 9; ++i)
		{
			if ((nullNpc[i] != null) && (npc.equals(nullNpc[i])))
			{
				nullNpc[i]  = null;
				log.info("Swarm despawned on: " + ticksSinceLogin);
				log.info("Swarm stayed out of distance for " + ticksWhileNoAggroAndDistance + " ticks");
				log.info("-----------------------------------------------");
				ticksWhileNoAggroAndDistance = 0;
				once = false;
				nullExists = false;
			}
		}
	}

	public void gameTick(GameTick tick)
	{
		ticksSinceLogin++;

		if ((dist >= 5) && once && nullExists)
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