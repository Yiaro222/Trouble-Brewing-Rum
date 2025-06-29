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
		name            = "General",
		description     = "General Options.",
		position        = 0,
		closedByDefault = false
	)
	String GeneralSection = "General";
	
	@ConfigSection
	(
		name            = "Base",
		description     = "Options relating to the team's base.",
		position        = 1,
		closedByDefault = false
	)
	String BaseSection = "Base";
	
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
		description = "The type that is used for hightling objects",
		position    = 0,
		section     = BaseSection
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
		section     = BaseSection
	)
	default int outlineWidth()
	{
		return 3;
	}
	
	@ConfigItem
	(
		keyName     = "FontSize",
		name        = "Font Size",
		position    = 0,
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
		position    = 1,
		section     = GeneralSection
	)
	default Color fontColour()
	{
		return Color.GRAY;
	}
	
	//
	@ConfigItem
	(
		keyName     = "contentTop",
		name        = "contentTop",
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
		name        = "contentLeft",
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
		name        = "contentRight",
		description = "",
		position    = 4,
		section     = GeneralSection
	)
	default boolean hideContentRight()
	{
		return true;
	}
	//
	
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
		description = "A timer which includes seconds (when known) and shows the correct time until next game.",
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
		description = "Timestamps for when the required amount of a resource to make a rum has been deposited. Only displays if player joined at the start of the game.",
		position    = 8,
		section     = LobbySection
	)
	default boolean displayIngredientTimes()
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
}



