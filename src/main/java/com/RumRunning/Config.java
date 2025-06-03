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
		name            = "Sabotage",
		description     = "Sabotage Options",
		position        = 1,
		closedByDefault = true
	)
	String SaboSection = "Sabo";
	
	
	
	//############################   Config Items   ############################
	
	@ConfigItem
	(
		keyName     = "HightlightType",
		name        = "Hightlight Type",
		description = "The type that is used for hightling objects",
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
		keyName     = "DisplaySaboOutline",
		name        = "Outline Sabotaged Objects",
		position    = 4,
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
		position    = 5,
		description = "",
		section     = SaboSection
	)
	default boolean displayRepairItems()
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



