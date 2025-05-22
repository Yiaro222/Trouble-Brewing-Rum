package com.RumRunning;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("TroubleBrewingRumConfig")
public interface TroubleBrewingConfig extends Config
{

	//#####		Sections	 #####

	@ConfigSection(
			name = "Plugin",
			description = "Plugin config options.",
			position = 0,
			closedByDefault = true
	)
	String PluginSection = "Examples";


	@ConfigSection(
			name = "Examples",
			description = "Examples of config options for development.",
			position = 1,
			closedByDefault = false
	)
	String ExamplesSection = "Examples";


	//#####		Config Items	 #####

	//Boolean example
	@ConfigItem(
			keyName = "Boolean1",
			name = "Boolean T/F Box",
			position = 0,
			description = "True/False user clickable box",
			section = ExamplesSection
	)
	default boolean Boolean1()
	{
		return true;
	}


	//Integer example
	@ConfigItem(
			keyName = "IntNumber1",
			name = "Integer number box",
			position = 1,
			description = "Integer user number box",
			section = ExamplesSection
	)
	default int IntNumber1()
	{
		return 0;
	}


	//Drop-down menu example
	@ConfigItem(
			keyName = "Drop-down1",
			name = "Drop-down menu",
			description = "Drop-down ",
			position = 2,
			section = ExamplesSection
	)
	default EnableState npcHighlightEnableState()
	{
		return EnableState.Option1;
	}

	enum EnableState
	{
		Option1,
		Option2,
		Option3
	}


	//ColourPicking example
	@ConfigItem(
			keyName = "ColourPicking1",
			name = "Colour picking",
			description = "Letting the user pick a colour.",
			position = 3,
			section = ExamplesSection
	)
	default Color ColourPicking1()
	{
		return new Color(255, 0, 0, 255);
	}


	//String example
	@ConfigItem(
		keyName = "StringText1",
		name = "String Text Box",
		position = 4,
		description = "Area for user to put text into.",
		section = ExamplesSection
	)
	default String StringText1()
	{
		return "User text";
	}
}
