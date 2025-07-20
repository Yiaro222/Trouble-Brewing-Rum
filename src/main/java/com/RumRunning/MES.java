
package com.RumRunning;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import net.runelite.api.Client;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.ObjectComposition;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.PostMenuSort;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.util.Text;



public class MES
{
	private final Client client;
	
	private final Config config;
	
	private final List<Integer> useItemIDs    = List.of
	(
		ItemID.BREW_RED_FLOWER, ItemID.BREW_BLUE_FLOWER,
		ItemID.LOGS,            ItemID.TINDERBOX,
		
		ItemID.BREW_RED_MONKEY, ItemID.BREW_BLUE_MONKEY,
		ItemID.RAW_RAT_MEAT,
		
		ItemID.BREW_BOWL_RED,  ItemID.BREW_BOWL_BLUE,
		ItemID.BREW_BITTERNUT, ItemID.BREW_SWEETGRUBS, ItemID.BREW_SCRAPEY_BARK,
		
		ItemID.BUCKET_WATER,      ItemID.BREW_PIPE_SECTION,
		ItemID.BREW_LUMBER_PATCH, ItemID.BREW_BRIDGE_SECTION
	);
	private final List<String>  hideItemNames = List.of
	(
		"Jungle Grass", "Jungle tree"
	);
	
	
	
	@Inject
	public
	MES(Client client,
	    Config config)
	{
		this.client = client;
		this.config = config;
	}
	
	public void
	postMenuSort(PostMenuSort postMenuSort)
	{
		SwapMinigameOptions();
	}
	
	public void
	menuEntryAdded(MenuEntryAdded event)
	{
		String target = Text.removeTags(event.getTarget());
		
		SwapLobbyAreaOptions(target);
		SwapToolUI(target);
	}
	
	private void
	SwapMinigameOptions()
	{
		Menu            menu;
		MenuEntry[]     defaultLayout;
		List<MenuEntry> layout;
		Widget          selectedItemWidget;
		int             selectedItemID;
		
		if (!config.enableMES())                                       return;
		if (client.isMenuOpen())                                       return;
		if ((menu = client.getMenu()) == null)                         return;
		if ((defaultLayout = menu.getMenuEntries()) == null)           return;
		if ((selectedItemWidget = client.getSelectedWidget()) == null) return;
		
		layout         = new ArrayList<MenuEntry>();
		selectedItemID = selectedItemWidget.getItemId();
		if (!useItemIDs.contains(selectedItemID)) return;
		
		for (int i = 0; i < defaultLayout.length; ++i)
		{
			ObjectComposition objComp;
			String            objName;
			int               objID;
			
			if (defaultLayout[i].getType() == MenuAction.WIDGET_TARGET_ON_GAME_OBJECT)
			{
				if ((objID   = defaultLayout[i].getIdentifier())  <  0   ) continue;
				if ((objComp = client.getObjectDefinition(objID)) == null) continue;
				if ((objName = objComp.getName())                 == null) continue;
				
				if (hideItemNames.contains(objName))
				{
					continue;
				}
			}
			else if (defaultLayout[i].getType() == MenuAction.WIDGET_TARGET_ON_PLAYER)
			{
				continue;
			}
			
			layout.add(defaultLayout[i]);
		}
		
		client.getMenu().setMenuEntries(layout.toArray(new MenuEntry[0]));
	}
	
	private void
	SwapLobbyAreaOptions(String target)
	{
		final WorldPoint wp = client.getLocalPlayer().getWorldLocation();
		
		if (!(Utils.NEAR_LOBBY.contains(wp) && !Utils.LOBBY.contains(wp))) return;
		
		if (config.swapJoinTeam() && target.equals("Waiting Room Door"))
		{
			for (var e: client.getMenu().getMenuEntries())
			{
				if (e.getOption().equals("Open"))
				{
					e.setDeprioritized(true);
				}
			}
		}
		else if (config.swapJoinTeam()     &&
		         (target.equals("San Fan") || target.equals("Fancy Dan")))
		{
			for (var e: client.getMenu().getMenuEntries())
			{
				if (e.getOption().equals("Talk-To"))
				{
					e.setDeprioritized(true);
				}
			}
		}
		else if (target.equals("Honest Jimmy"))
		{
			for (var e: client.getMenu().getMenuEntries())
			{
				if (config.jimmyType() == Config.HonestJimmyType.JOIN)
				{
					if (!e.getOption().equals("Join-Team"))
					{
						e.setDeprioritized(true);
					}
				}
				else if (config.jimmyType() == Config.HonestJimmyType.TRADE)
				{
					if (!e.getOption().equals("Trade"))
					{
						e.setDeprioritized(true);
					}
				}
			}
		}
		
	}
	
	private void
	SwapToolUI(String target)
	{
		if (client.getWidget(InterfaceID.BrewTools.UNIVERSE) == null) return;
		
		for (var e: client.getMenu().getMenuEntries())
		{
			if (target.equals("Bucket")    && config.swapBucketAmount())
			{
				if (!e.getOption().equals("Take-5"))
				{
					e.setDeprioritized(true);
				}
			}
			else if (target.equals("Bowl") && config.swapBowlAmount())
			{
				if (!e.getOption().equals("Take-5"))
				{
					e.setDeprioritized(true);
				}
			}
			else if (target.equals("Meat") && config.swapMeatAmount())
			{
				if (!e.getOption().equals("Take-5"))
				{
					e.setDeprioritized(true);
				}
			}
		}
	}
	
}



