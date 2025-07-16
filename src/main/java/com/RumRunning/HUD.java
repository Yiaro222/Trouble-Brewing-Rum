
package com.RumRunning;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.ProgressBarComponent;
import net.runelite.client.ui.overlay.components.SplitComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.util.ImageUtil;



@Slf4j
public class HUD
extends      OverlayPanel
{
	private final Client      client;
	private final ItemManager itemManager;
	
	private final Config config;
	
	/* Timer and many of the subclasses below are what I'm dubbing a "struct-styled
	 * class", similar to a C++ struct. They exist independently of each other
	 * and are supposed to be simple, and group together functionality for a
	 * feature or utility. */
	public class Timer
	{
		private int     mins       = -1;
		private long    secs       = -1;
		private Instant start_time = null;
		
		/* This has to be called at least every second. widgetMins is +1 in that
		 * it's 20 (+59secs, I assume) - 1 mins (+0secs). I don't want this. I want
		 * 19 (+59secs) - 0 (+0secs), so I minus 1 from this value.
		 * */
		public void
		update(int widgetMins)
		{
			Duration dur;
			
			widgetMins -= 1;
			
			if (widgetMins < 0)
			{
				reset();
				return;
			}
			
			if (mins == -1)
			{
				/* Store this start time although it is likely to be wrong. When the
				 * player joins the game the default UIs will all display the wrong
				 * values for a few seconds, which means this will likely be set to
				 * 20(-1) even if there are less than that remaining. */
				mins = widgetMins;
				return;
			}
			/* Jumped over a minute because the initial value was wrong, as explained
			 * in the previous comment */
			else if (mins == 19 && widgetMins < 18)
			{
				mins       = widgetMins;
				start_time = null;
			}
			
			if (start_time == null && widgetMins < mins)
			{
				mins       = widgetMins;
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
				mins       = mins - 1 >= 0 ? mins - 1 : 0;
				start_time = Instant.now();
				
				/* Stop the timer if it reaches 00:00 */
				if (secs == 0 && mins == 0)
				{
					reset();
				}
			}
		}
		
		public boolean
		running()
		{
			if (start_time != null) return(true);
			return(false);
		}
		
		public String
		get()
		{
			if (mins == -1)
			{
				return("--:--");
			}
			else if (start_time == null)
			{
				return(String.format("%02d:--", mins));
			}
			else
			{
				return(String.format("%02d:%02d", mins, secs));
			}
		}
		
		/* Reset and don't run */
		public void reset() { reset(false, 0); }
		
		/* Resets with options to run
		 * 
		 * run - Determines whether the timer will run after resetting. Set to true
		 *       if you know the starting seconds will be aligned with the in game
		 *       time (like joining at the start of the game or midpoint).
		 *       Otherwise set it to null so update() can start it when it sees
		 *       the minute tick over.
		 * 
		 * startingMins - If run = true, it will use this as the starting minutes
		 * */
		public void
		reset(boolean run, int startingMins)
		{
			mins       = -1;
			secs       = -1;
			start_time = null;
			
			if (run)
			{
				mins = startingMins - 1;
				start_time = Instant.now();
			}
		}
		
	}
	private Timer timer = new Timer();
	
	/* Struct-styled class
	 * This contains information about item rewards and stores which the player
	 * has already obtained (loaded in from, and written to, the cache elsewhere)
	 * */
	public static class Rewards
	{
		public enum Shop
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
			
			public static Shop
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
			
			Shop(int item_id, int eight_price)
			{
				this.item_id     = item_id;
				this.eight_price = eight_price;
			}
		};
		
		public List<Shop> clogs        = new ArrayList<>();
		public int        eight_amount = 0;
	}
	public static Rewards rewards = new Rewards();
	
	/* Struct-styled class
	 * This stores data which is needed across draws/ticks */
	public class CacheData
	{
		public GameState prev_gamestate;
		public boolean   awaiting_scene_load = false;
		public boolean   clog_read           = false;
		public boolean   clog_menu_open      = false;
	}
	private CacheData cacheData = new CacheData();
	
	/* I couldn't figure how to have this be its own UI (separate from the lobby/
	 * in-game UI) so I just made it its own class. */
	public class ItemWarning
	extends      OverlayPanel
	{
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
			ItemID.TRAIL_GILDED_AXE,
			ItemID.TRAILBLAZER_AXE_EMPTY, ItemID.TRAILBLAZER_AXE,
			ItemID.TRAILBLAZER_AXE_NO_INFERNAL
		};
		
		private BufferedImage  bufferedImage;
		private ImageComponent imageComponent;
		
		ItemWarning()
		{
			bufferedImage  = ImageUtil.loadImageResource(getClass(), "/cancel_icon.png");
			imageComponent = new ImageComponent(bufferedImage);
			
			setPriority(Overlay.PRIORITY_MED);
			setLayer   (OverlayLayer.ABOVE_SCENE);
			setPosition(OverlayPosition.BOTTOM_RIGHT);
			setSnappable (true);
			setMovable   (true);
			setResettable(true);
		}
		
		@Override
		public Dimension
		render(Graphics2D graphics)
		{
			WorldPoint playerPos;
			boolean    drawn = false;
			
			playerPos = client.getLocalPlayer().getWorldLocation();
			if (!Utils.NEAR_LOBBY.contains(playerPos))
			{
				return(null);
			}
			
			panelComponent.getChildren().clear();
			panelComponent.setPreferredSize(new Dimension(100, 30));
			panelComponent.setWrap(false);
			
			if (config.displayMSpeakAmuletReminder() &&
			    (Quest.MONKEY_MADNESS_II.getState(client) != QuestState.FINISHED))
			{
				if (!HasEquippedOrInInventory(ItemID.MM_AMULET_OF_MONKEY_SPEAK,
				                              KitType.AMULET))
				{
					panelComponent.getChildren().add(TitleComponent.builder()
					                            .text("Item Reminder")
					                            .build());
					DrawWarningUI(graphics, "M'Speak Amulet");
					drawn = true;
				}
			}
			if (config.displayAxeReminder())
			{
				if (!(IntStream.of(AxeItemIDs).anyMatch(x ->
				      HasEquippedOrInInventory(x, KitType.WEAPON))))
				{
					if (!drawn)
					{
						panelComponent.getChildren().add(TitleComponent.builder()
						                            .text("Item Reminder")
						                            .build());
					}
					
					DrawWarningUI(graphics, "Axe");
				}
			}
			
			return(super.render(graphics));
		}
		
		private Dimension
		DrawWarningUI(Graphics2D graphics, String message)
		{
			panelComponent.getChildren()
			              .add(SplitComponent.builder()
			              .orientation(ComponentOrientation.HORIZONTAL)
			              .first(imageComponent)
			              .second(LineComponent.builder().right(message).build())
			              .build());
			
			return(null);
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
		
	}
	
	/* Struct-styled class
	 * Reads and stores the relevant varbits. eight_count is called upon logging
	 * in, as well as at the end of the game. The others are called within the
	 * minigame */
	public static class VarbitValues
	{
		/* This consists of the relevant TB varbits */
		enum TroubleBrewingVarbit
		{
			/* _DAN_ = red team, _SAN_ = blue team */
			BREW_DAN_BOTTLE_1   (VarbitID.BREW_DAN_BOTTLE_1,    "BREW_DAN_BOTTLE_1",    false),
			BREW_SAN_BOTTLE_1   (VarbitID.BREW_SAN_BOTTLE_1,    "BREW_SAN_BOTTLE_1",    false),
			BREW_DAN_PLAYER_LOAD(VarbitID.BREW_DAN_PLAYER_LOAD, "BREW_DAN_PLAYER_LOAD", false),
			BREW_SAN_PLAYER_LOAD(VarbitID.BREW_SAN_PLAYER_LOAD, "BREW_SAN_PLAYER_LOAD", false),
		
			BREW_PLAYER_REWARD  (VarbitID.BREW_PLAYER_REWARD,   "BREW_PLAYER_REWARD",   false),
			BREW_PLAYER_MIDPOINT(VarbitID.BREW_PLAYER_MIDPOINT, "BREW_PLAYER_MIDPOINT", false),
		
			BREW_LOADS_AVAILABLE  (VarbitID.BREW_LOADS_AVAILABLE,   "BREW_LOADS_AVAILABLE",   false),
		
			BREW_PIECES(VarPlayerID.BREW_PIECES, "BREW_PIECES", true);
		
			int     id;
			String  name;
			boolean player_varbit;
		
			TroubleBrewingVarbit(int id, String name, boolean playerVarbit)
			{
				this.id            = id;
				this.name          = name;
				this.player_varbit = playerVarbit;
			}
		}
		
		public boolean rum_varbit_triggered   = false;
		public boolean eight_varbit_triggered = false;
		
		/* points          - "contrib",
		 * player_loads    - I believe this determines if the player can see the rum
		 * loads_available - this is to do with activity, if its 0 you don't see
		 *                   any other varbits 
		 * eight_count     - the reward points/currency
		 * */
		public int points          = 0;
		public int player_loads    = 0;
		public int loads_available = 0;
		public int eight_count     = 0;
		
		public void
		reset()
		{
			rum_varbit_triggered   = false;
			eight_varbit_triggered = false;
			
			points          = 0;
			player_loads    = 0;
			loads_available = 0;
			eight_count     = 0;
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
			
			if (tbVar == TroubleBrewingVarbit.BREW_PIECES)
			{
				eight_count            = value;
				eight_varbit_triggered = true;
			}
			else if (tbVar == TroubleBrewingVarbit.BREW_PLAYER_REWARD)
			{
				points = value;
			}
			
			if ((tbVar == TroubleBrewingVarbit.BREW_DAN_BOTTLE_1 ||
			     tbVar == TroubleBrewingVarbit.BREW_SAN_BOTTLE_1) &&
			    value == 2)
			{
				rum_varbit_triggered = true;
			}
			
			if (tbVar == TroubleBrewingVarbit.BREW_DAN_PLAYER_LOAD ||
			    tbVar == TroubleBrewingVarbit.BREW_SAN_PLAYER_LOAD)
			{
				player_loads = value;
			}
			
			if (tbVar == TroubleBrewingVarbit.BREW_LOADS_AVAILABLE)
			{
				loads_available = value;
			}
		}
		
	}
	public static VarbitValues varbits = new VarbitValues();
	
	/* Struct-styled class
	 * Reads and stores the values read from the default UI widgets.
	 * The two subsubclasses follow the same structure. The status variable is for
	 * checking whether the player is within the lobby or within the game. It's
	 * set with validate(), and reset is for resetting the state when entering and
	 * inbetween games and such.
	 * The prev_ and curr_ states are for detecting and grabbing changes in the
	 * stored values.
	 * The subsubclass values are then written to in DefaultWidgetValue itself.
	 * */
	public class DefaultWidgetValue
	{
		public class LobbyWidgetData
		{
			public boolean status;
			
			public boolean midpoint;
			public boolean waiting_for_more_players;
			public int     next_game_timer;
			
			public void
			reset()
			{
				status                   = false;
				midpoint                 = false;
				waiting_for_more_players = false;
				next_game_timer          = -1;
			}
			
			public void
			validate()
			{
				status = false;
				if (next_game_timer > -1 || waiting_for_more_players)
				{
					status = true;
				}
			}
			
		}
		
		public class IngameWidgetData
		{
			public boolean status;
			
			public int mins_timer;
			public int score_red;
			public int score_blue;
			
			public int boiler_1;
			public int boiler_2;
			public int boiler_3;
			
			public int bitternut;
			public int sweetgrub;
			public int water_bucket;
			public int coloured_water;
			public int scrapey_bark;
			
			public void
			reset()
			{
				status         = false;
				mins_timer     = -1;
				score_red      = -1;
				score_blue     = -1;
				boiler_1       = -1;
				boiler_2       = -1;
				boiler_3       = -1;
				bitternut      = -1;
				sweetgrub      = -1;
				water_bucket   = -1;
				coloured_water = -1;
				scrapey_bark   = -1;
			}
			
			public void
			validate()
			{
				status = true;
				if (mins_timer     == -1 || score_red    == -1 || score_blue   == -1 ||
				    boiler_1       == -1 || boiler_2     == -1 || boiler_3     == -1 ||
				    bitternut      == -1 || sweetgrub    == -1 || water_bucket == -1 ||
				    coloured_water == -1 || scrapey_bark == -1)
				{
					status = false;
				}
			}
			
		}
		
		public LobbyWidgetData  lobby_state       = new LobbyWidgetData();
		public IngameWidgetData prev_ingame_state = new IngameWidgetData();
		public IngameWidgetData curr_ingame_state = new IngameWidgetData();
		
		public void
		updateLobbyData()
		{
			String contents;
			int    mins;
			int    duration;
			
			contents = GetWidgetContent(InterfaceID.BrewWaitingRoomOverlay.TIME_TEXT);
			
			lobby_state.waiting_for_more_players = contents.contains("Waiting for");
			
			if (contents == null || (mins = GetExtractedValue(contents)) == -1)
			{
				lobby_state.validate();
				lobby_state.next_game_timer = -1;
				return;
			}
			
			/* I feel like the logic here is wrong, but it's consistently wrong, so
			 * it works? :D */
			
			if ((lobby_state.midpoint = contents.contains("Midpoint")))
			{
				duration = 10 + 1 - mins;
			}
			else
			{
				/* To fix the 6->3mins time jump */
				if (mins < 6)
				{
					mins += 2;
				}
				
				duration = 15 + 1 - mins + 10;
			}
			
			lobby_state.next_game_timer = 23 - duration;
			lobby_state.validate();
		}
		
		public void
		updateIngameData()
		{
			IngameWidgetData result = new IngameWidgetData();
			
			updatePrevious();
			
			result.mins_timer = GetValue(InterfaceID.BrewOverlay.BREW_TIME_DISPLAY);
			
			result.score_red  = GetValue(InterfaceID.BrewOverlay.RED_SCORE);
			result.score_blue = GetValue(InterfaceID.BrewOverlay.BLUE_SCORE);
			
			result.boiler_1 = GetValue(InterfaceID.BrewOverlay.BOILER1_COUNT);
			result.boiler_2 = GetValue(InterfaceID.BrewOverlay.BOILER2_COUNT);
			result.boiler_3 = GetValue(InterfaceID.BrewOverlay.BOILER3_COUNT);
			
			result.bitternut      = GetValue(InterfaceID.BrewOverlay.BITTERNUT_COUNT);
			result.sweetgrub      = GetValue(InterfaceID.BrewOverlay.SWEETGRUB_COUNT);
			result.water_bucket   = GetValue(InterfaceID.BrewOverlay.BUCKET_COUNT);
			result.coloured_water = GetValue(InterfaceID.BrewOverlay.COLOURWATER_COUNT);
			result.scrapey_bark   = GetValue(InterfaceID.BrewOverlay.BARK_COUNT);
			
			result.validate();
			curr_ingame_state = result;
		}
		
		public boolean
		resourcesDecreased()
		{
			if ((curr_ingame_state.bitternut      < prev_ingame_state.bitternut)      ||
			    (curr_ingame_state.sweetgrub      < prev_ingame_state.sweetgrub)      ||
			    (curr_ingame_state.coloured_water < prev_ingame_state.coloured_water) ||
			    (curr_ingame_state.scrapey_bark   < prev_ingame_state.scrapey_bark))
			{
				return(true);
			}
			
			return(false);
		}
		
		public void
		updatePrevious()
		{
			prev_ingame_state = curr_ingame_state;
		}
		
		private int
		GetValue(int interfaceID)
		{
			return(GetExtractedValue(GetWidgetContent(interfaceID)));
		}
		
		private String
		GetWidgetContent(int interfaceID)
		{
			Widget widget;
			
			if ((widget = client.getWidget(interfaceID)) == null) return(null);
			
			return(widget.getText());
		}
		
		private int
		GetExtractedValue(String widgetText)
		{
			int     result = -1;
			Matcher matcher;
			
			if (widgetText == null || widgetText.length() < 1) return(result);
			
			/* I don't understand regex. I got ChatGPT to generate this. */
			matcher = Pattern.compile("\\d+").matcher(widgetText);
			
			if (matcher.find())
			{
				String numerals;
				
				numerals = matcher.group();
				if (!numerals.chars().allMatch(Character::isDigit))
				{
					return(result);
				}
				
				result = Integer.parseInt(numerals);
			}
			
			return(result);
		}
		
	}
	private DefaultWidgetValue defaultWidgetValues = new DefaultWidgetValue();
	
	/* Struct-styled class
	 * This does a few different similar things.
	 * > It tracks how long a rum has been brewing for
	 * Then for the following, it only tracks if the player joined at the beginning
	 * of the game.
	 * > When the game started
	 * > The timestamps are for when each resource was first depoed.
	 * */
	public class MinigameTimers
	{
		Instant rum        = null;
		Instant game_start = null;
		
		String bitternut_timestamp      = "";
		String sweetgrub_timestamp      = "";
		String coloured_water_timestamp = "";
		String scrapey_bark_timestamp   = "";
		String water_bucket_timestamp   = "";
		String boiler_timestamp         = "";
		
		public void
		reset()
		{
			rum        = null;
			game_start = null;
			
			bitternut_timestamp      = "";
			sweetgrub_timestamp      = "";
			coloured_water_timestamp = "";
			scrapey_bark_timestamp   = "";
			water_bucket_timestamp   = "";
			boiler_timestamp         = "";
		}
		
		public String
		get()
		{
			if (game_start == null) return("-1");
			
			final Instant now  = Instant.now();
			final int     secs = (int) Duration.between(game_start, now).toSeconds();
			
			return(secs + "s");
		}
	}
	private MinigameTimers timers = new MinigameTimers();
	
	/* Stores icons displayed on the UI */
	private Map<Integer, ImageComponent> icons = new HashMap<>();
	
	
	
	@Inject
	private
	HUD(Client         client,
	    Config         config,
	    OverlayManager overlayManager,
	    ItemManager    itemManager)
	{
		this.client      = client;
		this.config      = config;
		this.itemManager = itemManager;
		
		setPriority(Overlay.PRIORITY_MED);
		setLayer   (OverlayLayer.ABOVE_SCENE);
		setPosition(OverlayPosition.DYNAMIC);
		setSnappable (true);
		setMovable   (true);
		setResettable(true);
		
		cacheData.prev_gamestate = client.getGameState();
		
		overlayManager.add(new ItemWarning());
	}
	
	public Dimension
	render(Graphics2D graphics)
	{
		WorldPoint playerPos;
		
		panelComponent.getChildren().clear();
		panelComponent.setPreferredSize(new Dimension(110, 200));
		panelComponent.setWrap(false);
		
		playerPos = client.getLocalPlayer().getWorldLocation();
		
		if (Utils.inMinigame(client))
		{
			DefaultWidgetValue.LobbyWidgetData LS;
			/* (Can't use Utils.onRedTeam bcus that var is updated after this
			 * function or next tick) */
			boolean                            ORT = Utils.onRedTeam(client);
			
			defaultWidgetValues.updateIngameData();
			LS = defaultWidgetValues.lobby_state;
			
			/* Just joined the game */
			if (LS.status)
			{
				/* So blue team can only join at two points, the start of the game and
				 * at the mid point. So these timers are set. Red team's timer is only
				 * known/set at the start of them game. Joining late will require the
				 * widget's timer to tickdown before the timer can be started properly.
				 * */
				/* Joined at midpoint on blue side */
				if (!ORT && LS.midpoint)
				{
					timer.reset(true, 10);
				}
				/* TB has a slight bug where you don't always get put into the game
				 * on the same tick that the timer ends - sometimes it will reset to
				 * "10 minutes until midpoint" (or whatever) before putting the player
				 * into the game the following tick. The next_game_timer == 22 case
				 * is to deal with this. */
				/* Joined at the start on blue side */
				else if (!ORT && (!LS.midpoint || LS.waiting_for_more_players) ||
				         (LS.next_game_timer == 22))
				{
					timer.reset(true, 20);
					timers.game_start = Instant.now();
				}
				/* The second line is to address the same bug as mentioned in the
				 * previous comment. I'm not certain it fixes it because it's so hard
				 * to replicate the issue, so testing is an issue */
				/* Join at the start on red side */
				else if (ORT && (LS.next_game_timer < 1 || LS.waiting_for_more_players ||
				         (timer.running() && timer.mins == 0)))
				{
					timer.reset(true, 20);
					timers.game_start = Instant.now();
				}
				/* This is called if the player joins late on red side */
				else
				{
					timer.mins -= 3;
				}
				
				defaultWidgetValues.lobby_state.reset();
			}
			else
			{
				timer.update(defaultWidgetValues.curr_ingame_state.mins_timer + 1);
			}
			
			/* If the player was present at the start of the game then update the
			 * first-resource depo times */
			if (timers.game_start != null && config.displayIngredientTimes())
			{
				CheckFirstResources();
			}
			
			return(DrawMinigameHUD(graphics));
		}
		else if (Utils.NEAR_LOBBY.contains(playerPos))
		{
			/* Game just finished */
			if (defaultWidgetValues.curr_ingame_state.status)
			{
				defaultWidgetValues.curr_ingame_state.reset();
				defaultWidgetValues.prev_ingame_state.reset();
				defaultWidgetValues.lobby_state.reset();
				timers.reset();
				timer.reset();
			}
			
			/* Update lobby state and timer */
			if (Utils.LOBBY.contains(playerPos))
		  {
				final DefaultWidgetValue.LobbyWidgetData LS;
				
				defaultWidgetValues.updateLobbyData();
				LS = defaultWidgetValues.lobby_state;
				
				/* Check if game started while player was on skip tiles & reset timer */
				if (Utils.BLUE_TEAM_LOBBY_SKIP_TILES.contains(playerPos) &&
				    timer.mins == 0 && LS.next_game_timer == 22)
				{
					timer.reset(true, 23);
				}
				
				timer.update(defaultWidgetValues.lobby_state.next_game_timer + 1);
			}
			/* If the player joins the lobby, timer caches the widget's mins, then 
			 * they leave, the widget timer would have decreased, then they join the
			 * lobby again, the timer will think that it *just* decreased and will
			 * start the seconds timer. To prevent this, this resets the timer if the
			 * secs timer wasn't started. */
			else if (timer.mins > 0 && !timer.running() && !Utils.LOBBY.contains(playerPos))
			{
				timer.reset();
			}
		  else if (timer.running())
		  {
		  	/* (Any large number (>23?) just so it continues to update) */
				timer.update(10000);
		  }
			
			return(DrawLobbyHUD(graphics));
		}
		/* Not in minigame or in/near the lobby */
		else
		{
			defaultWidgetValues.updatePrevious();
			defaultWidgetValues.curr_ingame_state.reset();
			defaultWidgetValues.lobby_state.reset();
			timers.reset();
			timer.reset();
		}
		
		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged, Cache o_cache)
	{
		final var currGameState = gameStateChanged.getGameState();
		
		/* Player logged-in */
		if (cacheData.prev_gamestate == GameState.LOGGING_IN &&
		    currGameState            == GameState.LOGGED_IN)
		{
			/* So the scene isn't loaded until a few ticks after logging in, so I
			 * need to read in the data from the cache after that happens. */
			cacheData.awaiting_scene_load = true;
		}
		/* Player logged-out - write to the cache if needed */
		else if ((cacheData.prev_gamestate == GameState.LOGGED_IN        ||
		          cacheData.prev_gamestate == GameState.CONNECTION_LOST) &&
		          currGameState            == GameState.LOGIN_SCREEN)
		{
			defaultWidgetValues.updatePrevious();
			defaultWidgetValues.curr_ingame_state.reset();
			defaultWidgetValues.lobby_state.reset();
			timers.reset();
			timer.reset();
			
			o_cache.data.eight_count = rewards.eight_amount;
			if (cacheData.clog_read)
			{
				o_cache.data.collection_log_ids.clear();
				for (int i = 0; i < rewards.clogs.size(); ++i)
				{
					o_cache.data.collection_log_ids.add(rewards.clogs.get(i).item_id);
				}
			}
			
			o_cache.write(client.getLocalPlayer().getName());
		}
		
		if (currGameState != GameState.LOADING)
		{
			cacheData.prev_gamestate = currGameState;
		}
	}
	
	public void
	gameTick(GameTick gameTick, Cache i_cache)
	{
		ReadSceneData(i_cache);
		ReadClogData();
	}
	
	public void
	varbitChanged(VarbitChanged event)
	{
		varbits.varbitChanged(event);
		
		if (varbits.eight_varbit_triggered)
		{
			rewards.eight_amount           = varbits.eight_count;
			varbits.eight_varbit_triggered = false;
		}
	}
	
	
	
	private Dimension
	DrawMinigameHUD(Graphics2D graphics)
	{
		ImageComponent icon      = null;
		int            rumCount  = 0;
		int            rp        = 0;
		String         msg       = "";
		Color          colour    = Color.WHITE;
		float          totalSecs = 0.0f;
		
		final boolean active = varbits.loads_available > 0 ? true : false;
		
		totalSecs = timer.mins * 60 + (int) timer.secs;
		if (active && timers.rum != null)
		{
			Instant now;
			long    seconds;
			
			now        = Instant.now();
			seconds    = Duration.between(timers.rum, now).toSeconds();
			totalSecs += seconds;
		}
		
		/* rum duration, remaining rum */
		final float RD = 40.0f;
		final int   RR = (int) Math.floor(totalSecs / RD);
		
		if (!Utils.inMinigame(client)) return(null);
		
		icon = GetIcon(0, "clock.png");
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(timer.get()).build())
		              .build());
		
		panelComponent.getChildren().add(LineComponent.builder().left("").build());
		
		rumCount = defaultWidgetValues.curr_ingame_state.score_blue;
		if (Utils.onRedTeam(client))
		{
			rumCount = defaultWidgetValues.curr_ingame_state.score_red;
		}
		
		if (varbits.points >= 100)
		{
			colour = Color.GREEN;
		}
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left("Points: ")
		                            .right(varbits.points + " (+" + rumCount * 10 + ")")
		                            .rightColor(colour)
		                            .build());
		
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left("Rum: ")
		                            .right(Integer.toString(rumCount))
		                            .build());
		
		msg = "Inactive";
		if ((rp = (int) Math.ceil((float) varbits.loads_available / 2.0f)) > 0)
		{
			msg = Integer.toString(rp);
		}
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left("Potential: ")
		                            .right(msg)
		                            .build());
		
		if (active && timers.rum != null)
		{
			Instant              now;
			long                 seconds;
			ProgressBarComponent bar;
			
			now     = Instant.now();
			seconds = Duration.between(timers.rum, now).toSeconds();
			
			panelComponent.getChildren().add(LineComponent.builder().left("").build());
			
			bar = new ProgressBarComponent();
			bar.setLabelDisplayMode(ProgressBarComponent.LabelDisplayMode.FULL);
			bar.setMinimum(0);
			bar.setMaximum(40);
			bar.setValue((int) seconds);
			panelComponent.getChildren().add(bar);
		}
		
		panelComponent.getChildren().add(LineComponent.builder().left("").build());
		
		colour = Color.WHITE;
		if (defaultWidgetValues.curr_ingame_state.bitternut >= RR) colour = Color.GREEN;
		icon = GetIcon(ItemID.BREW_BITTERNUT, "");
		msg  = "";
		if (config.displayIngredientTimes() && !timers.bitternut_timestamp.isEmpty())
		{
			msg += "(" + timers.bitternut_timestamp + ") ";
		}
		msg += defaultWidgetValues.curr_ingame_state.bitternut;
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(msg).rightColor(colour).build())
		              .build());
		
		colour = Color.WHITE;
		if (defaultWidgetValues.curr_ingame_state.sweetgrub >= RR) colour = Color.GREEN;
		icon = GetIcon(ItemID.BREW_SWEETGRUBS, "");
		msg  = "";
		if (config.displayIngredientTimes() && !timers.sweetgrub_timestamp.isEmpty())
		{
			msg += "(" + timers.sweetgrub_timestamp + ") ";
		}
		msg += defaultWidgetValues.curr_ingame_state.sweetgrub;
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(msg).rightColor(colour).build())
		              .build());
		
		colour = Color.WHITE;
		if (defaultWidgetValues.curr_ingame_state.coloured_water / 3 >= RR) colour = Color.GREEN;
		if (Utils.onRedTeam(client)) icon = GetIcon(ItemID.BREW_BOWL_RED, "");
		else                         icon = GetIcon(ItemID.BREW_BOWL_BLUE, "");
		msg = "";
		if (config.displayIngredientTimes() && !timers.coloured_water_timestamp.isEmpty())
		{
			msg += "(" + timers.coloured_water_timestamp + ") ";
		}
		msg += defaultWidgetValues.curr_ingame_state.coloured_water;
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(msg).rightColor(colour).build())
		              .build());
		
		colour = Color.WHITE;
		if (defaultWidgetValues.curr_ingame_state.scrapey_bark >= RR) colour = Color.GREEN;
		icon = GetIcon(ItemID.BREW_SCRAPEY_BARK, "");
		msg  = "";
		if (config.displayIngredientTimes() && !timers.scrapey_bark_timestamp.isEmpty())
		{
			msg += "(" + timers.scrapey_bark_timestamp + ") ";
		}
		msg += defaultWidgetValues.curr_ingame_state.scrapey_bark;
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(msg).rightColor(colour).build())
		              .build());
		
		colour = Color.WHITE;
		if (defaultWidgetValues.curr_ingame_state.water_bucket / 5 >= RR) colour = Color.GREEN;
		icon = GetIcon(ItemID.BUCKET_WATER, "");
		msg  = "";
		if (config.displayIngredientTimes() && !timers.water_bucket_timestamp.isEmpty())
		{
			msg += "(" + timers.water_bucket_timestamp + ") ";
		}
		msg += defaultWidgetValues.curr_ingame_state.water_bucket;
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(msg).rightColor(colour).build())
		              .build());
		
		
		if (config.displayIngredientTimes())
		{
			final int b0       = defaultWidgetValues.curr_ingame_state.boiler_1;
			final int b1       = defaultWidgetValues.curr_ingame_state.boiler_2;
			final int b2       = defaultWidgetValues.curr_ingame_state.boiler_3;
			final int logCount = (b0 < b1 ? b0 : b1) < b2 ? (b0 < b1 ? b0 : b1) : b2;
			
			panelComponent.getChildren().add(LineComponent.builder().left("").build());
			
			icon = GetIcon(0, "boiler.png");
			msg  = "";
			if (!timers.boiler_timestamp.isEmpty())
			{
				msg += "(" + timers.boiler_timestamp + ") ";
			}
			msg += logCount;
			panelComponent.getChildren().add(SplitComponent.builder()
			              .orientation(ComponentOrientation.HORIZONTAL)
			              .first(icon)
			              .second(LineComponent.builder().right(msg).build())
			              .build());
		}
		
		if (!active && timers.rum != null)
		{
			timers.rum = null;
		}
		
		if (varbits.rum_varbit_triggered)
		{
			if (timers.rum != null)
			{
				Instant now;
				long    seconds;
				
				now     = Instant.now();
				seconds = Duration.between(timers.rum, now).toSeconds();
				
				if (seconds > 20)
				{
					log.debug("Rum took " + seconds + " seconds to create");
					
					log.info("Stopping timers.rum");
					timers.rum = null;
				}
			}
			else
			{
				log.info("RUM PRODUCED BUT TIMER WASN'T RUNNING");
				/* This is fine tbh */
			}
			varbits.rum_varbit_triggered = false;
		}
		
		if (timers.rum != null)
		{
			long mins;
			
			mins = Duration.between(timers.rum, Instant.now()).toMinutes();
			if (mins > 1)
			{
				log.info("timers.rum is dead, stopping timer");
				timers.rum = null;
			}
		}

		if (defaultWidgetValues.resourcesDecreased() && active)
		{
			if (timers.rum != null)
			{
				log.info("timers.rum already running!!!");
			}
			else
			{
				timers.rum = Instant.now();
				log.info("timers.rum started");
			}
		}
		
		{
			Widget widget;
			
			/* Content Top   - boilers and ingrediets (hides the exact same as
			 *                 InterfaceID.BrewOverlay.INGREDIENTS_AND_BOILERS) */
			/* Content Left  - Sabo icons */
			/* Content Right - Ribbon UI  */
			if ((widget = client.getWidget(InterfaceID.BrewOverlay.CONTENT_TOP))   != null) widget.setHidden(!config.hideContentTop());
			if (widget != null) widget.revalidate();
			if ((widget = client.getWidget(InterfaceID.BrewOverlay.CONTENT_LEFT))  != null) widget.setHidden(!config.hideContentLeft());
			if (widget != null) widget.revalidate();
			if ((widget = client.getWidget(InterfaceID.BrewOverlay.CONTENT_RIGHT)) != null) widget.setHidden(!config.hideContentRight());
			if (widget != null) widget.revalidate();
		}
		
		return(super.render(graphics));
	}
	
	private Dimension
	DrawLobbyHUD(Graphics2D graphics)
	{
		int            remainderCost = 0;
		ImageComponent icon;
		Widget         widget;
		
		if ((widget = client.getWidget(InterfaceID.BrewWaitingRoomOverlay.CONTENTS))
		    != null)
		{
			widget.setHidden(!config.hideLobbyWidget());
			widget.revalidate();
		}
		
		for (int i = 0; i < rewards.clogs.size(); ++i)
		{
			remainderCost += rewards.clogs.get(i).eight_price;
		}
		
		remainderCost = Rewards.Shop.getTotalCost() - remainderCost;
		
		panelComponent.getChildren().add(TitleComponent.builder()
		                            .text("Lobby")
		                            .build());
		
		icon = GetIcon(0, "clock.png");
		panelComponent.getChildren().add(SplitComponent.builder()
		              .orientation(ComponentOrientation.HORIZONTAL)
		              .first(icon)
		              .second(LineComponent.builder().right(timer.get()).build())
		              .build());
		
		panelComponent.getChildren().add(LineComponent.builder().left("").build());
		
		icon = GetIcon(ItemID.BREW_PIECE_OF_EIGHT, "");
		if (remainderCost == 0)
		{
			panelComponent.getChildren().add(SplitComponent.builder()
			              .orientation(ComponentOrientation.HORIZONTAL)
			              .first(icon)
			              .second(LineComponent.builder()
			              .right(Integer.toString(rewards.eight_amount)).build())
			              .build());
		}
		else
		{
			panelComponent.getChildren().add(SplitComponent.builder()
			              .orientation(ComponentOrientation.HORIZONTAL)
			              .first(icon)
			              .second(LineComponent.builder()
			              .right(rewards.eight_amount + "/" + remainderCost).build())
			              .build());
		}
		
		return(super.render(graphics));
	}
	
	private void
	CheckFirstResources()
	{
		final DefaultWidgetValue.IngameWidgetData CS = defaultWidgetValues.curr_ingame_state;
		
		final boolean brewStarted = defaultWidgetValues.resourcesDecreased();
		
		final int b0       = defaultWidgetValues.curr_ingame_state.boiler_1;
		final int b1       = defaultWidgetValues.curr_ingame_state.boiler_2;
		final int b2       = defaultWidgetValues.curr_ingame_state.boiler_3;
		final int logCount = (b0 < b1 ? b0 : b1) < b2 ? (b0 < b1 ? b0 : b1) : b2;
		
		if (timers.bitternut_timestamp.isEmpty() && (brewStarted || CS.bitternut > 0))
		{
			timers.bitternut_timestamp = timers.get();
		}
		
		if (timers.sweetgrub_timestamp.isEmpty() && (brewStarted || CS.sweetgrub > 0))
		{
			timers.sweetgrub_timestamp = timers.get();
		}
		
		if (timers.coloured_water_timestamp.isEmpty() && (brewStarted || CS.coloured_water >= 3))
		{
			timers.coloured_water_timestamp = timers.get();
		}
		
		if (timers.scrapey_bark_timestamp.isEmpty() && (brewStarted || CS.scrapey_bark > 0))
		{
			timers.scrapey_bark_timestamp = timers.get();
		}
		
		if (timers.water_bucket_timestamp.isEmpty() && (brewStarted || CS.water_bucket >= 5))
		{
			timers.water_bucket_timestamp = timers.get();
		}
		
		if (timers.boiler_timestamp.isEmpty() && (brewStarted || logCount >= 3))
		{
			timers.boiler_timestamp = timers.get();
		}
	}
	
	private ImageComponent
	GetIcon(int itemID, String localImage)
	{
		ImageComponent result;
		BufferedImage  defaultImage;
		BufferedImage  resizedImage;
		Graphics2D     graphics;
		
		final int width  = 20;
		final int height = 20;
		
		if (localImage.length() > 0)
		{
			itemID = localImage.hashCode();
		}
		
		if (icons.containsKey(itemID))
		{
			return(icons.get(itemID));
		}
		
		if (localImage.length() > 0)
		{
			defaultImage = ImageUtil.loadImageResource(getClass(), "/" + localImage);
		}
		else
		{
			defaultImage = itemManager.getImage(itemID, 1, false);
		}
		resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		graphics     = resizedImage.createGraphics();
		
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		                          RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.drawImage(defaultImage, 0, 0, width, height, null);
		graphics.dispose();
		
		result = new ImageComponent(resizedImage);
		icons.put(itemID, result);
		
		return(result);
	}
	
	private void
	ReadSceneData(Cache i_cache)
	{
		if (cacheData.awaiting_scene_load)
		{
			final Player player = client.getLocalPlayer();
			if (player == null || player.getName() == null) return;
			
			i_cache.read(player.getName());
			
			if (rewards.eight_amount == 0)
			{
				rewards.eight_amount = i_cache.data.eight_count;
			}
			
			if (rewards.clogs.isEmpty())
			{
				for (int i = 0; i < i_cache.data.collection_log_ids.size(); ++i)
				{
					rewards.clogs.add(Rewards.Shop.valueOf(i_cache.data.collection_log_ids.get(i)));
				}
			}
			
			cacheData.awaiting_scene_load = false;
		}
	}
	
	private void
	ReadClogData()
	{
		Widget   tbClogWidget;
		Widget[] itemWidgets;
		
		if ((tbClogWidget =
		     client.getWidget(InterfaceID.Collection.ITEMS_CONTENTS)) == null)
		{
			cacheData.clog_menu_open = false;
			return;
		}
		
		if ((itemWidgets = tbClogWidget.getChildren()) == null)// return;
		{
			return;
		}
		
		/* Not on tb tab */
		if (!Rewards.Shop.containsID(itemWidgets[0].getItemId()))// return;
		{
			return;
		}
		
		rewards.clogs.clear();
		
		/* Populate list of item IDs that the player already has */
		for (int i = 0; i < itemWidgets.length; ++i)
		{
			final int id = itemWidgets[i].getItemId();
			
			if (itemWidgets[i].getOpacity() > 0) continue;
			
			final Rewards.Shop clogItem = Rewards.Shop.valueOf(id);
			if (!rewards.clogs.contains(clogItem))
			{
				rewards.clogs.add(clogItem);
			}
		}
		
		client.addChatMessage(ChatMessageType.GAMEMESSAGE,
		                      "TroubleBrewingRum Plugin",
		                      "Trouble Brewing Rum: Collection Log read", "");
		cacheData.clog_read      = true;
		cacheData.clog_menu_open = true;
	}
	
}



