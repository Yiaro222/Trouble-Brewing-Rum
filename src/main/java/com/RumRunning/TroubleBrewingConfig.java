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
	
	@ConfigSection
	(
		name            = "Resources",
		description     = "Resource Section",
		position        = 1,
		closedByDefault = false
	)
	String ResourceSection = "Resources";
	
	@ConfigSection
	(
		name            = "Information",
		description     = "Information Section",
		position        = 2,
		closedByDefault = false
	)
	String InformationSection = "Information";
	
	@ConfigSection
	(
		name            = "example",
		description     = "example Section",
		position        = 2,
		closedByDefault = true
	)
	String ExamplesSection = "examples";
	// TODO: a section for "Rewards"? and move score/contrib one there
	
	
	
	//############################   Config Items   ############################
	
	// TODO: probs shouldn't be part of Base
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
	
	// TODO: probs shouldn't be part of Base
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
		keyName     = "Display Boiler Icons",
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
		keyName     = "DisplayScore",
		name        = "Display Contribution",
		position    = 0,
		description = "Display contribution points.",
		section     = InformationSection
	)
	default boolean displayScore()
	{
		return true;
	}
	
	
	//Integer example
	@ConfigItem
	(
			keyName     = "IntNumber1",
			name        = "Integer number box",
			position    = 1,
			description = "Integer user number box",
			section     = ExamplesSection
	)
	default int IntNumber1()
	{
		return 0;
	}
	
	
	//ColourPicking example
	@ConfigItem
	(
			keyName     = "ColourPicking1",
			name        = "Colour picking",
			description = "Letting the user pick a colour.",
			position    = 3,
			section     = ExamplesSection
	)
	default Color ColourPicking1()
	{
		return new Color(255, 0, 0, 255);
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



