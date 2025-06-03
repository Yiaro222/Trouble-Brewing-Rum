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
	
	
	
	//############################   Config Items   ############################
	
	@ConfigItem
	(
		keyName     = "HightlightType",
		name        = "Hightlight Type",
		description = "The type that is used for hightling all objects",
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
		name        = "Outlinte Boilers",
		position    = 2,
		description = "Draws a coloured outline around the boilers depending on their log count.",
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
}



