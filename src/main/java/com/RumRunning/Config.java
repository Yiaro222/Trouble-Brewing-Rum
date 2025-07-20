
package com.RumRunning;

import java.awt.Color;

import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;



@ConfigGroup("TroubleBrewingRumConfig")
public interface Config
extends          net.runelite.client.config.Config
{
	//############################   Sections       ############################
	
	@ConfigSection
	(
		name            = "General Settings",
		description     = "",
		position        = 0,
		closedByDefault = true
	)
	String GeneralSection = "General Settings";
	
	@ConfigSection
	(
		name            = "Lobby Settings",
		description     = "",
		position        = 1,
		closedByDefault = true
	)
	String LobbySection = "Lobby Settings";
	
	@ConfigSection
	(
		name            = "Interfaces",
		description     = "",
		position        = 2,
		closedByDefault = true
	)
	String InterfaceSection = "Interfaces";
	
	@ConfigSection
	(
		name            = "Menu Entry Swapper",
		description     = "",
		position        = 3,
		closedByDefault = true
	)
	String MESSection = "Menu Entry Swapper";
	
	@ConfigSection
	(
		name            = "Resource Gathering",
		description     = "",
		position        = 4,
		closedByDefault = true
	)
	String ResourceSection = "Resoure Gathering";
	
	@ConfigSection
	(
		name            = "Sabotage Settings",
		description     = "",
		position        = 5,
		closedByDefault = true
	)
	String SaboSection = "Sabotage Settings";
	
	@ConfigSection
	(
		name            = "Boiler Settings",
		description     = "",
		position        = 6,
		closedByDefault = true
	)
	String BoilerSection = "Boiler Settings";
	
	
	
	//############### Config Items[0]: General Settings Section   ###############
	
	@ConfigItem
	(
		keyName     = "HightlightType",
		name        = "Hightlight Type",
		description = "The type that is used for highlighting all objects",
		position    = 0,
		section     = GeneralSection
	)
	default HighlightType highlightType()
	{
		return HighlightType.OUTLINE;
	}
	
	@ConfigItem
	(
		keyName     = "OutlineWidth",
		name        = "Outline Width",
		position    = 1,
		description = "Number of pixels used when outlining objects.",
		section     = GeneralSection
	)
	default int outlineWidth()
	{
		return 3;
	}
	
	@ConfigItem
	(
		keyName     = "FontSize",
		name        = "Font Size",
		position    = 2,
		description = "Used for boiler log count.",
		section     = GeneralSection
	)
	default int fontSize()
	{
		return 18;
	}
	
	@ConfigItem
	(
		keyName     = "FontColour",
		name        = "Font Colour",
		description = "",
		position    = 3,
		section     = GeneralSection
	)
	default Color fontColour()
	{
		return Color.GRAY;
	}
	
	
	
	//############### Config Items[1]: Lobby Settings Section     ###############
	
	@ConfigItem
	(
		keyName     = "RedTeamHatType",
		name        = "Red Team Hat Type",
		description = "May cause temporary baldness",
		position    = 0,
		section     = LobbySection
	)
	default RedTeamHatType redTeamHatType()
	{
		return RedTeamHatType.DEFAULT;
	}
	
	@ConfigItem
	(
		keyName     = "BlueTeamHatType",
		name        = "Blue Team Hat Type",
		description = "",
		position    = 1,
		section     = LobbySection
	)
	default BlueTeamHatType blueTeamHatType()
	{
		return BlueTeamHatType.DEFAULT;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayAxeReminder",
		name        = "Axe Reminder",
		description = "Show a reminder to bring an Axe.",
		position    = 2,
		section     = LobbySection
	)
	default boolean displayAxeReminder()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayMSpeakReminder",
		name        = "M'Speak Reminder",
		description = "Useful when running nuts. Only displays if MM2 is not " +
		              "completed.",
		position    = 3,
		section     = LobbySection
	)
	default boolean displayMSpeakAmuletReminder()
	{
		return false;
	}
	
	
	
	//############### Config Items[2]: Interfaces Section         ###############
	
	@ConfigItem
	(
		keyName     = "MinigameHUD",
		name        = "Minigame UI",
		description = "",
		position    = 0,
		section     = InterfaceSection
	)
	default boolean enableMinigameHUD()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayFirstIngredientTimes",
		name        = "Display Deposit Timers",
		description = "Timestamps for when the required amount of a resource to " +
		              "make a rum has been deposited. Only displays if player "   +
		              "joined at the start of the game.",
		position    = 1,
		section     = InterfaceSection
	)
	default boolean displayIngredientTimes()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "ColourMaxResources",
		name        = "Display Adequate Resources",
		description = "Turns resource counts green when adequate resources have " +
		              "been reached. This assumes each rum takes 40 secs to brew "+
		              " (it's usually less)",
		position    = 2,
		section     = InterfaceSection
	)
	default boolean colourMaxResources()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "HideDefaultUI",
		name        = "Hide Default UI",
		description = "",
		position    = 3,
		section     = InterfaceSection
	)
	default boolean hideDefaultMinigameUI()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "LobbyHUD",
		name        = "Lobby UI",
		description = "",
		position    = 4,
		section     = InterfaceSection
	)
	default boolean enableLobbyHUD()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayRemainder",
		name        = "Remaining Eight",
		description = "Show remaining eight needed until green log. You may " +
		              "need to open your Trouble Brewing collection log for " +
		              "this to be accurate.",
		position    = 5,
		section     = InterfaceSection
	)
	default boolean displayRemainder()
	{
		return false;
	}
	
	@ConfigItem
	(
		keyName     = "LobbyHideDefaultUI",
		name        = "Hide Default UI",
		description = "Hide the default lobby ribbon UI",
		position    = 6,
		section     = InterfaceSection
	)
	default boolean hideDefaultLobbyUI()
	{
		return true;
	}
	
	
	
	//############### Config Items[3]: Menu Entry Swapper Section ###############
	
	@ConfigItem
	(
		keyName     = "EnableMES",
		name        = "Misclick Prevention",
		description = "Prevents certain items like monkeys and flowers from being " +
		              "used on easily misclickable things such as trees and players",
		position    = 0,
		section     = MESSection
	)
	default boolean enableMES()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "SwapJoinCrewOptions",
		name        = "Swap Join Options",
		description = "Swaps San Fan and Fancy Dan's left click option to Join-" +
		              "Crew, and swaps the door option while outside the lobby.",
		position    = 1,
		section     = MESSection
	)
	default boolean swapJoinTeam()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "HonestJimmyType",
		name        = "Honest Jimmy",
		description = "",
		position    = 2,
		section     = MESSection
	)
	default HonestJimmyType jimmyType()
	{
		return HonestJimmyType.DEFAULT;
	}
	
	@ConfigItem
	(
		keyName     = "SwapBucketAmount",
		name        = "Bucket Take 5",
		description = "Swaps the Tool Table's withdrawal amount to 5 for Buckets",
		position    = 3,
		section     = MESSection
	)
	default boolean swapBucketAmount()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "SwapBowlAmount",
		name        = "Bowl Take 5",
		description = "Swaps the Tool Table's withdrawal amount to 5 for Bowl",
		position    = 4,
		section     = MESSection
	)
	default boolean swapBowlAmount()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "SwapMeatAmount",
		name        = "Meat Take 5",
		description = "Swaps the Tool Table's withdrawal amount to 5 for Meat",
		position    = 5,
		section     = MESSection
	)
	default boolean swapMeatAmount()
	{
		return true;
	}
	
	
	
	//############### Config Items[4]: Resource Gathering Section ###############
	
	@ConfigItem
	(
		keyName     = "EnableScrapeyTree",
		name        = "Enable Scrapey Tree",
		position    = 0,
		description = "Adds outlines and spawn timers",
		section     = ResourceSection
	)
	default boolean enableTreeInfo()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "EnableSweetgrub",
		name        = "Enable Sweetgrub",
		position    = 1,
		description = "Adds outlines to mounds and aggression zone for sweetgrubs",
		section     = ResourceSection
	)
	default boolean enableSweetgrubInfo()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "TreeOutlineColour",
		name        = "Tree Active",
		description = "",
		position    = 2,
		section     = ResourceSection
	)
	default Color treeActiveColour()
	{
		return Color.GREEN;
	}
	
	@ConfigItem
	(
		keyName     = "TreeStumpOutlineColour",
		name        = "Tree Inactive",
		description = "Tree stump outline colour",
		position    = 3,
		section     = ResourceSection
	)
	default Color treeInactiveColour()
	{
		return Color.RED;
	}
	
	@ConfigItem
	(
		keyName     = "MoundOutlineColour",
		name        = "Sweetgrub Mound Active",
		description = "",
		position    = 4,
		section     = ResourceSection
	)
	default Color sweetgrubMoundActiveColour()
	{
		return Color.GREEN;
	}
	
	@ConfigItem
	(
		keyName     = "MoundOutlineColour",
		name        = "Sweetgrub Mound Inactive",
		description = "",
		position    = 5,
		section     = ResourceSection
	)
	default Color sweetgrubMoundInactiveColour()
	{
		return Color.RED;
	}
	
	
	
	//############### Config Items[5]: Sabotage Settings Section  ###############
	
	@ConfigItem
	(
		keyName     = "DisplaySaboOutline",
		name        = "Outline Sabotaged Objects",
		position    = 0,
		description = "",
		section     = SaboSection
	)
	default boolean displaySaboOutline()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplaySaboRepairItems",
		name        = "Display Required Repair Items",
		position    = 1,
		description = "",
		section     = SaboSection
	)
	default boolean displayRepairItems()
	{
		return true;
	}
	
	
	
	//############### Config Items[6]: Boiler Settings Section    ###############
	
	@ConfigItem
	(
		keyName     = "DisplayBoilerIcons",
		name        = "Boiler Icons",
		position    = 0,
		description = "Display Log/Tinderbox icons over Boilers.",
		section     = BoilerSection
	)
	default boolean displayBoilerIcons()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayBoilerLogCount",
		name        = "Boiler Log Count",
		position    = 1,
		description = "Display log count over boilers.",
		section     = BoilerSection
	)
	default boolean displayBoilerLogCount()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "OutlineBoilers",
		name        = "Outline Boilers",
		position    = 2,
		description = "Draws a coloured outline around the boilers depending on " +
		              " their log count.",
		section     = BoilerSection
	)
	default boolean displayBoilerOutline()
	{
		return true;
	}
	
	
	
	//############################   Enums          ############################
	
	enum HighlightType
	{
		NONE,
		OUTLINE,
		HULL_OUTLINE,
		HULL_FILLED,
		CLICKBOX_OUTLINE,
		CLICKBOX_FILLED
	}
	
	enum RedTeamHatType
	{
		DEFAULT,
		BANDANA
	}
	
	enum BlueTeamHatType
	{
		DEFAULT,
		PIRATE_HAT
	}
	
	enum HonestJimmyType
	{
		DEFAULT,
		TRADE,
		JOIN
	}
}



