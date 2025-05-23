package com.RumRunning;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;



@ConfigGroup("TroubleBrewingRumConfig")
public interface TroubleBrewingConfig
extends          Config
{
	//############################   Sections       ############################
	
	@ConfigSection
	(
		name            = "Base",
		description     = "Options relating to the team's base.",
		position        = 0,
		closedByDefault = false
	)
	String BaseSection = "Base";
	
	
	
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
		keyName     = "DisplayBoilerIcons",
		name        = "Display Boiler Icons",
		position    = 2,
		description = "Display Log/Tinderbox icon over Boilers.",
		section     = BaseSection
	)
	default boolean displayBoilerIcons()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "DisplayBoilerLogCount",
		name        = "Boiler Log Count",
		position    = 3,
		description = "Display log count over boilers.",
		section     = BaseSection
	)
	default boolean displayBoilerLogCount()
	{
		return true;
	}
	
	@ConfigItem
	(
		keyName     = "FontSize",
		name        = "Font Size",
		position    = 4,
		description = "Used for boiler log count.",
		section     = BaseSection
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
		position    = 5,
		section     = BaseSection
	)
	default Color fontColour()
	{
		return Color.GRAY;
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



