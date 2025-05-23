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



