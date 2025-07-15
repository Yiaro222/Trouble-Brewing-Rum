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
		closedByDefault = false
	)
	String GeneralSection = "General Settings";
	
	@ConfigSection
	(
		name            = "Boilers",
		description     = "Boiler options.",
		position        = 1,
		closedByDefault = true
	)
	String BoilerSection = "Boiler Settings";
	
	@ConfigSection
	(
		name            = "Menu Entry Swap Options",
		description     = "",
		position        = 2,
		closedByDefault = true
	)
	String MESSection = "MES";
	
	@ConfigSection
	(
		name            = "Resource Gathering",
		description     = "",
		position        = 3,
		closedByDefault = true
	)
	String ResourceSection = "Resoure Gathering";
	
	@ConfigSection
	(
		name            = "Sabotage",
		description     = "Sabotage Options",
		position        = 1,
		closedByDefault = true
	)
	String SaboSection = "Sabo";
	
	@ConfigSection
	(
		name            = "Lobby",
		description     = "",
		position        = 2,
		closedByDefault = false
	)
	String LobbySection = "Lobby";
	
	
	
	//############################   Config Items   ############################
	
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
		description = "Used for the boiler's font.",
		position    = 3,
		section     = GeneralSection
	)
	default Color fontColour()
	{
		return Color.GRAY;
	}
	
	
	
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
		name        = "Repair Items",
		position    = 1,
		description = "",
		section     = SaboSection
	)
	default boolean displayRepairItems()
	{
		return true;
	}
	
	
	
	@ConfigItem
	(
		keyName     = "MES",
		name        = "Enable",
		position    = 0,
		description = "Left click swaps",
		section     = MESSection
	)
	default boolean enableMES()
	{
		return true;
	}
	
	
	
	@ConfigItem
	(
		keyName     = "EnableScrapeyTreeInfo",
		name        = "Enable Scrapey Tree Info",
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
		keyName     = "EnableSweetgrubInfo",
		name        = "Enable Sweetgrub Info",
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
		keyName     = "contentTop",
		name        = "Hide Default Resource UI",
		description = "",
		position    = 2,
		section     = GeneralSection
	)
	default boolean hideContentTop()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "contentLeft",
		name        = "Hide Default Sabo UI",
		description = "",
		position    = 3,
		section     = GeneralSection
	)
	
	default boolean hideContentLeft()
	{
		return true;
	}
	@ConfigItem
	(
		keyName     = "contentRight",
		name        = "Hide Default Ingame Ribbon UI",
		description = "",
		position    = 4,
		section     = GeneralSection
	)
	default boolean hideContentRight()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "LobbyHUD",
		name        = "Enable",
		description = "Toggle for all Lobby UI options.",
		position    = 0,
		section     = LobbySection
	)
	default boolean enableLobbyHUD()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayTimer",
		name        = "Timer",
		description = "A timer which includes seconds (when known) and shows the " +
		              "correct time until next game.",
		position    = 1,
		section     = LobbySection
	)
	default boolean displayTimer()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayEight",
		name        = "Eight Count",
		description = "Shows current Pieces of Eight count while in Lobby.",
		position    = 2,
		section     = LobbySection
	)
	default boolean displayEight()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayRemainder",
		name        = "Remaining Eight",
		description = "Show remaining eight needed until green log. You may need to" +
		              "open your Trouble Brewing collection log for this to be accurate.",
		position    = 3,
		section     = LobbySection
	)
	default boolean displayRemainder()
	{
		return false;
	}
	
	@ConfigItem
	(
		keyName     = "HideDefaultUI",
		name        = "Hide Default UI",
		description = "Hide the default ribbon UI.",
		position    = 4,
		section     = LobbySection
	)
	default boolean hideDefaultWidget()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "HideLobbyUI",
		name        = "Hide Lobby UI",
		description = "Hide the default lobby ribbon UI.",
		position    = 5,
		section     = LobbySection
	)
	default boolean hideLobbyWidget()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayAxeReminder",
		name        = "Axe Reminder",
		description = "Show a reminder to bring an Axe.",
		position    = 6,
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
		description = "Useful when running nuts. Only displays if MM2 is not completed.",
		position    = 7,
		section     = LobbySection
	)
	default boolean displayMSpeakAmuletReminder()
	{
		return false;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayFirstIngredientTimes",
		name        = "Ingredient Timers",
		description = "Timestamps for when the required amount of a resource to " +
		              "make a rum has been deposited. Only displays if player "   +
		              "joined at the start of the game.",
		position    = 2,
		section     = ResourceSection
	)
	default boolean displayIngredientTimes()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "RedTeamHatType",
		name        = "Red Team Hat Type",
		description = "Can cause temporary baldness",
		position    = 5,
		section     = GeneralSection
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
		position    = 6,
		section     = GeneralSection
	)
	default BlueTeamHatType blueTeamHatType()
	{
		return BlueTeamHatType.DEFAULT;
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
}



