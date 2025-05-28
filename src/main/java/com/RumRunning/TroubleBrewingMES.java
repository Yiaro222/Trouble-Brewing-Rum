
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Menu;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import static net.runelite.api.MenuAction.*;


@Slf4j
public class TroubleBrewingMES
{
	private final Client               client;
	private final ItemManager          itemManager;
	
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;
	private final TroubleBrewingUtils  utils;
	
	/* Boiler/Kettle Items */
	private final int RED_FLOWERS  = 8936;
	private final int BLUE_FLOWERS = 8938;
	private final int LOGS         = 1511;
	
	/* Use on Scenery */
	private final int RED_MONKEY0  = 8946;
	private final int RED_MONKEY1  = 8947;
	private final int RED_MONKEY2  = 8948;
	private final int BLUE_MONKEY0 = 8943;
	private final int BLUE_MONKEY1 = 8944;
	private final int BLUE_MONKEY2 = 8945;
	private final int RAW_RAT_MEAT = 2134;
	
	/* Hopper Items */
	private final int BOWL_WATER_RED  = 8972;
	private final int BOWL_WATER_BLUE = 8974;
	private final int BITTERNUT       = 8976;
	private final int SCRAPEY_BARK    = 8977;
	private final int SWEETGRUBS      = 8981;
	private final int BUCKET_OF_WATER = 1929;
	
	/* Repair Items */
	private final int PIPE_SECTION   = 8930;
	private final int LUMBER_PATCH   = 8932;
	private final int BRIDGE_SECTION = 8979;
	
	private final List<Integer> useItemIDs    = List.of
	(
		RED_FLOWERS, BLUE_FLOWERS, LOGS,
		RED_MONKEY0, RED_MONKEY1, RED_MONKEY2,
		BLUE_MONKEY0, BLUE_MONKEY1, BLUE_MONKEY2,
		RAW_RAT_MEAT,
		BOWL_WATER_RED, BOWL_WATER_BLUE, BITTERNUT, SCRAPEY_BARK, SWEETGRUBS,
		BUCKET_OF_WATER,
		PIPE_SECTION, LUMBER_PATCH, BRIDGE_SECTION
	);
	private final List<String>  hideItemNames = List.of
	(
		"Jungle Grass", "Plant"
	);
	private final boolean       hidePlayers   = true;
	
	
	
	@Inject
	public
	TroubleBrewingMES(Client               client,
	                  ItemManager          itemManager,
	                  TroubleBrewingPlugin plugin,
	                  TroubleBrewingConfig config,
	                  TroubleBrewingUtils  utils)
	{
		this.client               = client;
		this.itemManager          = itemManager;
		this.plugin               = plugin;
		this.config               = config;
		this.utils                = utils;
	}

	public void
	postMenuSort(PostMenuSort postMenuSort)
	{
		Menu            menu;
		MenuEntry[]     defaultLayout;
		List<MenuEntry> layout;
		Widget          selectedItemWidget;
		int             selectedItemID;
		
		if (client.isMenuOpen())                                       return;
		if ((menu = client.getMenu()) == null)                         return;
		if ((defaultLayout = menu.getMenuEntries()) == null ||
		    defaultLayout.length < 0)                                  return;
		if ((selectedItemWidget = client.getSelectedWidget()) == null) return;
		
		layout         = new ArrayList<MenuEntry>();
		selectedItemID = selectedItemWidget.getItemId();
		if (!useItemIDs.contains(selectedItemID)) return;
		
		for (int i = 0; i < defaultLayout.length; ++i)
		{
			ObjectComposition objComp;
			String            objName;
			int               objID;
			
			if (defaultLayout[i].getType() == WIDGET_TARGET_ON_PLAYER && hidePlayers)
			{
				continue;
			}
			else if (defaultLayout[i].getType() == WIDGET_TARGET_ON_GAME_OBJECT)
			{
				if ((objID   = defaultLayout[i].getIdentifier())  <  0   ) continue;
				if ((objComp = client.getObjectDefinition(objID)) == null) continue;
				if ((objName = objComp.getName())                 == null) continue;
				
				if (hideItemNames.contains(objName))
				{
					continue;
				}
			}
			
			layout.add(defaultLayout[i]);
		}
		
		client.getMenu().setMenuEntries(layout.toArray(new MenuEntry[0]));
	}
}



