
package com.RumRunning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayUtil;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



public class Boiler
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	
	private final Config config;
	
	enum BoilerData
	{
		BOILER_1_RED (0, ObjectID.BREW_STILL_BOILER_CORNER_MIRROR, ObjectID.BREW_STILL_BOILER_CORNER_LOGS_MIRROR, ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR, new Point(3811, 2999), true),
		BOILER_2_RED (1, ObjectID.BREW_STILL_BOILER_CORNER,        ObjectID.BREW_STILL_BOILER_CORNER_LOGS,        ObjectID.BREW_STILL_BOILER_CORNER_FIRE,        new Point(3811, 3003), true),
		BOILER_3_RED (2, ObjectID.BREW_STILL_BOILER,               ObjectID.BREW_STILL_BOILER_LOGS,               ObjectID.BREW_STILL_BOILER_FIRE,               new Point(3816, 3003), true),
		BOILER_1_BLUE(0, ObjectID.BREW_STILL_BOILER,               ObjectID.BREW_STILL_BOILER_LOGS,               ObjectID.BREW_STILL_BOILER_FIRE,               new Point(3816, 2947), false),
		BOILER_2_BLUE(1, ObjectID.BREW_STILL_BOILER_CORNER,        ObjectID.BREW_STILL_BOILER_CORNER_LOGS,        ObjectID.BREW_STILL_BOILER_CORNER_FIRE,        new Point(3821, 2947), false),
		BOILER_3_BLUE(2, ObjectID.BREW_STILL_BOILER_CORNER_MIRROR, ObjectID.BREW_STILL_BOILER_CORNER_LOGS_MIRROR, ObjectID.BREW_STILL_BOILER_CORNER_FIRE_MIRROR, new Point(3821, 2951), false);
		
		public int        id;
		public int        empty_id;
		public int        has_log_id;
		public int        lit_id;
		public Point      position;
		public boolean    red_side;
		public GameObject game_object = null;
		
		public static BoilerData
		match(GameObject obj)
		{
			final int id = obj.getId();
			final Point objPosition = new Point(obj.getWorldLocation().getX(),
			                                    obj.getWorldLocation().getY());
			for (int i = 0; i < values().length; ++i)
			{
				if ((values()[i].empty_id == id  || values()[i].has_log_id == id ||
				     values()[i].lit_id   == id) &&
				    (values()[i].position.equals(objPosition)))
				{
					return(values()[i]);
				}
			}
			
			return(null);
		}
		
		BoilerData(int id, int emptyID, int hasLogID, int litID, Point position,
		           boolean redSide)
		{
			this.id         = id;
			this.empty_id   = emptyID;
			this.has_log_id = hasLogID;
			this.lit_id     = litID;
			this.position   = position;
			this.red_side   = redSide;
		}
	}
	private List<BoilerData> boilers = new ArrayList<>();
	
	
	
	@Inject
	private
	Boiler(Client               client,
	       ModelOutlineRenderer modelOutlineRenderer,
	       Config               config)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.config               = config;
		
		setLayer(OverlayLayer.ABOVE_SCENE);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		final WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
		
		if (!Utils.inMinigame) return(null);
		
		for (int i = 0; i < boilers.size(); ++i)
		{
			final BoilerData boiler = boilers.get(i);
			
			WorldPoint boilerPos;
			Color      colour;
			int        logCount;
			
			if (boiler.game_object == null) continue;
			
			boilerPos = boiler.game_object.getWorldLocation();
			if ((boilerPos.distanceTo(playerPos) > Utils.DRAW_DISTANCE)   ||
			    (boilerPos.getPlane() != playerPos.getPlane())            ||
			    ( Utils.onRedTeam && Utils.BLUE_HALF.contains(boilerPos)) ||
			    (!Utils.onRedTeam && Utils.RED_HALF .contains(boilerPos)))
			{
				continue;
			}
			
			logCount = GetLogCount(boiler.id);
			colour   = Color.RED;
			
			if      (logCount >= 3 && logCount <= 7) colour = Color.YELLOW;
			else if (logCount >= 8)                  colour = Color.GREEN;
			
			if (config.displayBoilerOutline())
			{
				Utils.drawHighlightedGameObject(graphics, modelOutlineRenderer, config,
				                                boiler.game_object, config.highlightType(),
				                                colour);
			}
			
			if (config.displayBoilerIcons() &&
			    boiler.game_object.getId() == boiler.has_log_id)
			{
				DrawIcon(graphics, boiler.game_object, Utils.ICON_TINDERBOX);
			}
			else if (config.displayBoilerIcons() && logCount < 3)
			{
				DrawIcon(graphics, boiler.game_object, Utils.ICON_LOGS);
			}
			
			if (config.displayBoilerLogCount())
			{
				Point pos;
				
				pos = boiler.game_object.getCanvasTextLocation(graphics, "00/00", 0);
				pos = new Point(pos.getX() + config.fontSize() / 2, pos.getY());
				
				graphics.setFont(Utils.FONT);
				OverlayUtil.renderTextLocation(graphics, pos, logCount + "/10",
				                               config.fontColour());
			}
		}
		
		return(null);
	}
	
	private int
	GetLogCount(int id)
	{
		int    result = 0;
		Widget widget;
		
		widget = client.getWidget(InterfaceID.BrewOverlay.BOILER1_COUNT + id);
		if (widget != null)
		{
			result = Integer.parseInt(widget.getText());
		}
		
		return(result);
	}
	
	private void
	DrawIcon(Graphics2D graphics, GameObject obj, BufferedImage image)
	{
		Point pos;
		
		pos = Perspective.getCanvasImageLocation(client, obj.getLocalLocation(),
		                                         image, 150);
		
		if (pos != null)
		{
			graphics.drawImage(image, pos.getX(), pos.getY(), null);
		}
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			boilers.clear();
		}
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		      BoilerData boiler;
		
		if ((boiler = BoilerData.match(gameObject)) == null) return;
		
		boiler.game_object = gameObject;
		if (!boilers.contains(boiler))
		{
			boilers.add(boiler);
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		      BoilerData boiler;
		
		if ((boiler = BoilerData.match(gameObject)) == null) return;
		
		boilers.remove(boiler);
	}
	
}



