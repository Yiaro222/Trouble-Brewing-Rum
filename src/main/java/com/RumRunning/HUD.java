
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.List;
import java.util.ArrayList;

import javax.inject.Inject;

import ch.qos.logback.core.recovery.ResilientOutputStreamBase;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.PlayerDespawned;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.gameval.InterfaceID.BrewOverlay;
import net.runelite.api.gameval.InterfaceID.BrewWaitingRoomOverlay;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;


@Slf4j
public class HUD
extends      OverlayPanel
{
	private final Client client;
	
	private final TroubleBrewingPlugin plugin;
	private final Config               config;
	
	public class
	Timer
	{
		private int     mins       = -1;
		private long    secs       = -1;
		private Instant start_time = null;
		
		/*
		 * This has to be called at least every second. widgetMins is +1 in that
		 * it's 20 (+59secs, I assume) - 1 mins (+0secs). I don't want this. I want
		 * 19 (+59secs) - 0 (+0secs), so I minus 1 from this value.
		 * */
		public void
		update(int widgetMins)
		{
			Duration dur;
			
			widgetMins--;
			
			if (mins == -1)
			{
				mins = widgetMins;
				log.info("cached mins: " + mins);
				return;
			}
			
			if (start_time == null && widgetMins < mins)
			{
				log.info("starting timer via update - mins: " + mins +
				         ", widgetMins: " + widgetMins);
				start_time = Instant.now();
			}
			
			if (start_time == null)
			{
				return;
			}
			
			dur  = Duration.between(start_time, Instant.now());
			secs = 59 - dur.getSeconds();
			
			if (secs < 0)
			{
				// start_time = start_time.minus(Duration.ofMinutes(1));
				start_time = Instant.now();
				mins = mins - 1 >= 0 ? mins - 1 : 0;
			}
		}
		
		public boolean
		running()
		{
			if (start_time != null) return(true);
			return(false);
		}
		
		/*
		 * Gets formatted time as a String
		 * */
		public String
		get()
		{
			String result = "";
			
			if (start_time == null)
			{
				result = String.format("%02dM", mins);
			}
			else
			{
				result = String.format("%02dM %02dS", mins, secs);
			}
			
			return(result);
		}
		
		/*
		 * Reset and don't run
		 * */
		public void reset() { reset(false, 0); }
		
		/*
		 * Resets with options to run
		 * 
		 * run - Determines whether the timer will run after resetting. Set to true
		 *       if you know the starting seconds will be aligned with the in game
		 *       time (like joining at the start of the game or midpoint).
		 *       Otherwise set it to null so update() can start it when it sees
		 *       the minute tick over.
		 * 
		 * startingMins - If continuing, it will use this as the starting minutes
		 * */
		public void
		reset(boolean run, int startingMins)
		{
			mins       = -1;
			secs       = -1;
			start_time = null;
			
			log.info("reset timer");
			if (run)
			{
				mins = startingMins;
				log.info("Starting timer from: " + startingMins);
				start_time = Instant.now();
			}
		}
	}
	private Timer timer = new Timer();
	
	public class EntryState
	{
		boolean pre_midpoint = false;
		boolean was_in_lobby = false;
		boolean on_red_team  = false;
		/* Not strictly an inversion of was_in_lobby */
		boolean is_in_game   = true;
	}
	private int        prevMins;
	private EntryState prevEntryState = new EntryState();
	
	enum ShopRewards
	{
		BLUE_SHIRT  (ItemID.BREW_UNIFORM_BLUE,       1000),
		BLUE_LEGS   (ItemID.BREW_NAVY_SLACKS_BLUE,   1000),
		BLUE_HAT    (ItemID.BREW_TRICORN_BLUE,       500),
		GREEN_SHIRT (ItemID.BREW_UNIFORM_GREEN,      1000),
		GREEN_LEGS  (ItemID.BREW_NAVY_SLACKS_GREEN,  1000),
		GREEN_HAT   (ItemID.BREW_TRICORN_GREEN,      500),
		RED_SHIRT   (ItemID.BREW_UNIFORM_RED,        1000),
		RED_LEGS    (ItemID.BREW_NAVY_SLACKS_RED,    1000),
		RED_HAT     (ItemID.BREW_TRICORN_RED,        500),
		BROWN_SHIRT (ItemID.BREW_UNIFORM_BROWN,      1000),
		BROWN_LEGS  (ItemID.BREW_NAVY_SLACKS_BROWN,  1000),
		BROWN_HAT   (ItemID.BREW_TRICORN_BROWN,      500),
		BLACK_SHIRT (ItemID.BREW_UNIFORM_BLACK,      1000),
		BLACK_LEGS  (ItemID.BREW_NAVY_SLACKS_BLACK,  1000),
		BLACK_HAT   (ItemID.BREW_TRICORN_BLACK,      500),
		PURPLE_SHIRT(ItemID.BREW_UNIFORM_PURPLE,     1000),
		PURPLE_LEGS (ItemID.BREW_NAVY_SLACKS_PURPLE, 1000),
		PURPLE_HAT  (ItemID.BREW_TRICORN_PURPLE,     500),
		GREY_SHIRT  (ItemID.BREW_UNIFORM_GREY,       1000),
		GREY_LEGS   (ItemID.BREW_NAVY_SLACKS_GREY,   1000),
		GREY_HAT    (ItemID.BREW_TRICORN_GREY,       500),
		
		FLAG0(ItemID.BREW_FLAG_1, 2000),
		FLAG1(ItemID.BREW_FLAG_2, 2000),
		FLAG2(ItemID.BREW_FLAG_3, 3000),
		FLAG3(ItemID.BREW_FLAG_4, 4000),
		FLAG4(ItemID.BREW_FLAG_5, 5000),
		FLAG5(ItemID.BREW_FLAG_6, 6000),
		
		THE_STUFF(ItemID.BREW_HYPER_YEAST, 50),
		
		RUM_RED (ItemID.BREW_RED_RUM,  20),
		RUM_BLUE(ItemID.BREW_BLUE_RUM, 20);
		
		
		
		public final int item_id;
		public final int eight_price;
		
		public static ShopRewards
		valueOf(int id)
		{
			for (int i = 0; i < values().length; ++i)
			{
				if (values()[i].item_id == id) return(values()[i]);
			}
			return(null);
		}
		
		public static boolean
		containsID(int id)
		{
			for (int i = 0; i < values().length; ++i)
			{
				if (values()[i].item_id == id) return(true);
			}
			
			return(false);
		}
		
		public static int
		getTotalCost()
		{
			int result = 0;
			for (int i = 0; i < values().length; ++i)
			{
				result += values()[i].eight_price;
			}
			
			return(result);
		}
		
		ShopRewards(int item_id, int eight_price)
		{
			this.item_id     = item_id;
			this.eight_price = eight_price;
		}
	};
	
	private List<ShopRewards> playerClogs = new ArrayList<>();
	
	enum TroubleBrewingVarbit
	{
		BREW_DAN_BOTTLE_1   (VarbitID.BREW_DAN_BOTTLE_1,    "BREW_DAN_BOTTLE_1",    false),
		BREW_SAN_BOTTLE_1   (VarbitID.BREW_SAN_BOTTLE_1,    "BREW_SAN_BOTTLE_1",    false),
		BREW_DAN_PLAYER_LOAD(VarbitID.BREW_DAN_PLAYER_LOAD, "BREW_DAN_PLAYER_LOAD", false),
		BREW_SAN_PLAYER_LOAD(VarbitID.BREW_SAN_PLAYER_LOAD, "BREW_SAN_PLAYER_LOAD", false),
		BREW_PLAYER_REWARD  (VarbitID.BREW_PLAYER_REWARD,   "BREW_PLAYER_REWARD",   false),
		BREW_TREE_VAR       (VarbitID.BREW_TREE_VAR,        "BREW_TREE_VAR",        false),
		BREW_PLAYER_MONKEY  (VarbitID.BREW_PLAYER_MONKEY,   "BREW_PLAYER_MONKEY",   false),
		BREW_PLAYER_MIDPOINT(VarbitID.BREW_PLAYER_MIDPOINT, "BREW_PLAYER_MIDPOINT", false),
		/* Idk */
		BREW_HYPER_YEAST_BREWERY_1(VarbitID.BREW_HYPER_YEAST_BREWERY_1, "BREW_HYPER_YEAST_BREWERY_1", false),
		BREW_HYPER_YEAST_BREWERY_2(VarbitID.BREW_HYPER_YEAST_BREWERY_2, "BREW_HYPER_YEAST_BREWERY_2", false),
		
		BREW_STORE_BOILER_POS      (VarbitID.BREW_STORE_BOILER_POS,       "BREW_STORE_BOILER_POS",       false),
		BREW_DAN_MULTI_BOTTLER     (VarbitID.BREW_DAN_MULTI_BOTTLER,      "BREW_DAN_MULTI_BOTTLER",      false),
		BREW_SAN_MULTI_BOTTLER     (VarbitID.BREW_SAN_MULTI_BOTTLER,      "BREW_SAN_MULTI_BOTTLER",      false),
		BREW_COLOURED_WATER_NUMBERS(VarbitID.BREW_COLOURED_WATER_NUMBERS, "BREW_COLOURED_WATER_NUMBERS", false),
		BREW_WATER_NUMBERS         (VarbitID.BREW_WATER_NUMBERS,          "BREW_WATER_NUMBERS",          false),
		BREW_LOADS_AVAILABLE       (VarbitID.BREW_LOADS_AVAILABLE,        "BREW_LOADS_AVAILABLE",        false),
		/* Unlikely to be related */
		BREW_OVERVIEWVAR_1        (VarbitID.BREW_OVERVIEWVAR_1,         "BREW_OVERVIEWVAR_1",         false),
		BREW_HYPER_YEAST_BREWERY_3(VarbitID.BREW_HYPER_YEAST_BREWERY_3, "BREW_HYPER_YEAST_BREWERY_3", false),
		
		BREW_VAR_1       (VarPlayerID.BREW_VAR_1,        "BREW_VAR_1",        true),
		BREW_VAR_2       (VarPlayerID.BREW_VAR_2,        "BREW_VAR_2",        true),
		BREW_VAR_3       (VarPlayerID.BREW_VAR_3,        "BREW_VAR_3",        true),
		BREW_VAR_NO_RESET(VarPlayerID.BREW_VAR_NO_RESET, "BREW_VAR_NO_RESET", true),
		BREW_PIECES      (VarPlayerID.BREW_PIECES,       "BREW_PIECES",       true);
		
		
		
		int     id;
		String  name;
		boolean player_varbit;
		
		TroubleBrewingVarbit(int id, String name, boolean playerVarbit)
		{
			this.id = id;
			this.name = name;
			this.player_varbit = playerVarbit;
		}
	}
	
	private int eightAmount = 0;
	
	private int[] AxeItemIDs = 
	{
		ItemID.BRONZE_AXE,           ItemID.BRONZE_AXE_2H,
		ItemID.IRON_AXE,             ItemID.IRON_AXE_2H,
		ItemID.STEEL_AXE,            ItemID.STEEL_AXE_2H,
		ItemID.BLACK_AXE,            ItemID.BLACK_AXE_2H,
		ItemID.MITHRIL_AXE,          ItemID.MITHRIL_AXE_2H,
		ItemID.ADAMANT_AXE,          ItemID.ADAMANT_AXE_2H,
		ItemID.RUNE_AXE,             ItemID.RUNE_AXE_2H,
		ItemID.DRAGON_AXE,           ItemID.DRAGON_AXE_2H,
		ItemID.CRYSTAL_AXE,          ItemID.CRYSTAL_AXE_2H,
		ItemID.CRYSTAL_AXE_INACTIVE, ItemID.CRYSTAL_AXE_2H_INACTIVE,
		ItemID._3A_AXE,              ItemID._3A_AXE_2H,
		ItemID.INFERNAL_AXE,         ItemID.INFERNAL_AXE_EMPTY,
		ItemID.TRAIL_GILDED_AXE
		/* What about dragon and infernal ornament ids? */
	};
	boolean MOVE_TO_CONFIG_mspeakReminder = true;
	boolean MOVE_TO_CONFIG_axeReminder    = true;
	
	private int points = 0;
	
	/* TODO:
	 * Timer:
	 * > A huge problem is that when a game ends the lobby timer jumps from like
	 *   "6 minutes until next game" (or some bs like that) can change to "3 minutes"
	 * > If you join a game late it displays 20mins for like 10 secs, which leads
	 *   to the timer caching the wrong value...
	 * 
	 * 
	 * Default UI changes
	 * > Option to hide parts of the default UI
	 * > Make it scalable
	 * >> Trying to resize the ribbon widget just makes it disappear. i imagine
	 *    it will be the same for the in-game widgets too.
	 * 
	 * 
	 * Custom UIs
	 * > Add chat mesage if player doesn't have score 100 by a certain time (and
	 *   let the time by set in the config) + screen flash notification
	 * > Cache eightAmount and playerClogs
	 * > Maybe split it up into multiple classes, like item warnings can be part
	 *   of an "info" UI, idk... maybe.
	 * 
	 * */
	
	
	
	@Inject
	private
	HUD(Client               client,
	    TroubleBrewingPlugin plugin,
	    Config               config)
	{
		this.client               = client;
		this.plugin               = plugin;
		this.config               = config;
		
		setSnappable(true);
		setMovable(true);
		setResettable(true);
		setPriority(Overlay.PRIORITY_MED);
		setLayer(OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.TOP_LEFT);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		WorldPoint playerPos;
		int        currMins;
		EntryState currEntryState;
		
		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(200, 30));
		panelComponent.setWrap(false);
		
		playerPos      = client.getLocalPlayer().getWorldLocation();
		currEntryState = new EntryState();
		
		if (Utils.NEAR_LOBBY.contains(playerPos))
		{
			DrawEightUI(graphics);
			
			if (MOVE_TO_CONFIG_mspeakReminder &&
			    (Quest.MONKEY_MADNESS_II.getState(client) != QuestState.FINISHED))
			{
				if (!HasEquippedOrInInventory(ItemID.MM_AMULET_OF_MONKEY_SPEAK,
				                              KitType.AMULET))
				{
					DrawWarningUI(graphics, "Bring your M'Speak amulet");
				}
			}
			if (MOVE_TO_CONFIG_axeReminder)
			{
				if (!(IntStream.of(AxeItemIDs).anyMatch(x ->
				      HasEquippedOrInInventory(x, KitType.WEAPON))))
				{
					DrawWarningUI(graphics, "Bring your axe");
				}
			}
		}
		
		/* Currently in lobby */
		if (Utils.RED_TEAM_LOBBY.contains(playerPos) ||
		    Utils.BLUE_TEAM_LOBBY.contains(playerPos))
		{
			if (prevEntryState.was_in_lobby == false)
			{
				timer.reset();
			}
			
			currMins = GetDefaultWidgetTime(false, currEntryState);
			timer.update(currMins);
			prevEntryState = currEntryState;
			
			DrawLobbyWidget(graphics);
			return(super.render(graphics));
		}
		/* If it's not in the minigame either, then return. */
		else if (!Utils.inMinigame)
		{
			/* Here currEntyState wasn't even wrote to, so I'm just resetting
			 * prevEntyState. I'm not sure what I actually want to do here. */
			prevEntryState = currEntryState;
			if (timer.running()) timer.reset();
			return(super.render(graphics));
		}
		
		/* In minigame */
		currMins = GetDefaultWidgetTime(true, currEntryState);
		
		/* *Just* entered the minigame */
		if (prevEntryState.was_in_lobby && currEntryState.is_in_game)
		{
			/* So blue team can only join at two points, the start of the game and
			 * at the mid point. So these timers are set. Red team's timer is only
			 * known/set at the start of them game. Joining late will require the
			 * widget's timer to tickdown before the timer can be started properly.
			 * */
			if (!prevEntryState.on_red_team && prevEntryState.pre_midpoint)
			{
				timer.reset(true, 10);
			}
			else if (!prevEntryState.on_red_team && !prevEntryState.pre_midpoint)
			{
				timer.reset(true, 20);
			}
			else if (prevEntryState.on_red_team && prevMins == 1 && currMins == 20)
			{
				timer.reset(true, 20);
			}
			else
			{
				timer.reset();
			}
			
			DrawGameWidget(graphics);
		}
		else if (!prevEntryState.was_in_lobby && currEntryState.is_in_game)
		{
			timer.update(currMins);
			DrawGameWidget(graphics);
		}
		
		super.render(graphics);
		prevMins       = currMins;
		prevEntryState = currEntryState;
		
		return(null);
	}
	
	private Dimension
	DrawLobbyWidget(Graphics2D graphics)
	{
		if (timer.running())
		{
			panelComponent.getChildren().add(LineComponent.builder()
			                            .left(timer.get())
			                            .build());
		}
		
		return(null);
	}
	
	private Dimension
	DrawGameWidget(Graphics2D graphics)
	{
		DrawPoints(graphics);
		
		if (timer.running())
		{
			panelComponent.getChildren().add(LineComponent.builder()
			                            .left(timer.get())
			                            .build());
		}
		
		return(null);
	}
	
	private Dimension
	DrawEightUI(Graphics2D graphics)
	{
		int remainderCost = 0;
		
		for (int i = 0; i < playerClogs.size(); ++i)
		{
			remainderCost += playerClogs.get(i).eight_price;
		}
		
		remainderCost = ShopRewards.getTotalCost()- remainderCost;
		
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left("Eight needed:")
		                            .right(eightAmount + "/" + remainderCost)
		                            .build());
		
		return(null);
	}
	
	private Dimension
	DrawPoints(Graphics2D graphics)
	{
		if (points >= 100)
		{
			panelComponent.getChildren().add(LineComponent.builder()
			                            .left("Points: ")
			                            .right(Integer.toString(points))
			                            .rightColor(Color.GREEN)
			                            .build());
		}
		else
		{
			panelComponent.getChildren().add(LineComponent.builder()
			                            .left("Points: ")
			                            .right(Integer.toString(points))
			                            .build());
		}
		
		return(null);
	}
	
	private Dimension
	DrawWarningUI(Graphics2D graphics, String message)
	{
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left(message)
		                            .build());
		
		return(null);
	}
	
	/*
	 * This will not work if the default widget has already been written to.
	 * inGame opposed to "inLobby"
	 * entryState is optional. Data is written into it.
	 * */
	private int
	GetDefaultWidgetTime(boolean inGame, EntryState o_entryState)
	{
		int    result = -1;
		Widget timeWidget;
		String widgetText;
		int    iID;
		
		iID = inGame ? InterfaceID.BrewOverlay.BREW_TIME_DISPLAY :
		               InterfaceID.BrewWaitingRoomOverlay.TIME_TEXT;
		
		timeWidget = client.getWidget(iID);
		if (timeWidget == null)
		{
			return(result);
		}
		
		if ((widgetText = timeWidget.getText()) != null)
		{
			Matcher matcher;
			
			/* I don't understand regex. I got ChatGPT to generate this. */
			matcher = Pattern.compile("\\d+").matcher(widgetText);
			
			if (matcher.find())
			{
				String mins;
				
				mins = matcher.group();
				if (!mins.chars().allMatch(Character::isDigit))
				{
					log.info("string does not contain only digits: " + mins);
					return(result);
				}
				
				result = Integer.parseInt(mins);
			}
			
			if (o_entryState != null)
			{
				o_entryState.pre_midpoint = widgetText.contains("Midpoint");
			}
		}
		else
		{
			log.info("text is null");
		}
		
		/* (It wouldn't have made it this far into the function if it's not either 
		 *  in game or in lobby.) */
		if (o_entryState != null)
		{
			o_entryState.was_in_lobby = !inGame;
			o_entryState.is_in_game   = Utils.inMinigame(client);
			o_entryState.on_red_team  = Utils.onRedTeam(client);
		}
		
		return(result);
	}
	
	private boolean
	HasEquippedOrInInventory(int id, KitType slot)
	{
		int           wearingID;
		ItemContainer inventory;
		
		wearingID = client.getLocalPlayer()
		                  .getPlayerComposition().getEquipmentId(slot);
		
		if (wearingID == id)
		{
			return(true);
		}
		
		if ((inventory = client.getItemContainer(InventoryID.INV)) == null)
		{
			return(false);
		}
		
		if (inventory.contains(id))
		{
			return(true);
		}
		
		return(false);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		/* if (gameStateChanged.getGameState() == GameState.LOADING)
		 * or whatever - & clear your game ObjectList here */
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		/* Add to GameObject list here */
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		/* Remove from GameObject list here */
	}
	
	public void
	varbitChanged(VarbitChanged event)
	{
		TroubleBrewingVarbit tbVar = null;
		int                  value = 0;
		
		for (int i = 0; i < TroubleBrewingVarbit.values().length; ++i)
		{
			final int id  = TroubleBrewingVarbit.values()[i].id;
			final int vID = TroubleBrewingVarbit.values()[i].player_varbit ?
			                event.getVarpId() : event.getVarbitId();
			if (id == vID)
			{
				tbVar = TroubleBrewingVarbit.values()[i];
				value = event.getValue();
				break;
			}
		}
		
		if (tbVar == null) return;
		
		log.info("Var] " + tbVar.name + ": " + Integer.toString(value));
		
		if (tbVar == TroubleBrewingVarbit.BREW_PIECES)
		{
			eightAmount = value;
		}
		else if (tbVar == TroubleBrewingVarbit.BREW_PLAYER_REWARD)
		{
			points = value;
		}
		
	}
	
	public void
	playerSpawned(PlayerSpawned event)
	{
		// log.info("Player " + event.getActor().getName() + " appeared.");
	}
	
	public void
	playerDespawned(PlayerDespawned event)
	{
	}
	
	public void
	widgetLoaded(WidgetLoaded event)
	{
		Widget   tbClogWidget;
		Widget[] itemWidgets;
		
		if ((tbClogWidget =
		     client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS)) == null)
		{
			// log.info("Could not find TB Clog UI");
			return;
		}
		
		if ((itemWidgets = tbClogWidget.getChildren()) == null)
		{
			return;
		}
		
		/* Not on tb tab */
		if (!ShopRewards.containsID(itemWidgets[0].getItemId()))
		{
			return;
		}
		
		for (int i = 0; i < itemWidgets.length; ++i)
		{
			final int id = itemWidgets[i].getItemId();
			
			if (itemWidgets[i].getOpacity() > 0) continue;
			
			/* Populate list of item IDs that the player already has */
			if (!playerClogs.contains(id))
			{
				final ShopRewards clogItem = ShopRewards.valueOf(id);
				playerClogs.add(clogItem);
			}
		}
	}
	
	public void
	gameTick()
	{
	}
	
}



