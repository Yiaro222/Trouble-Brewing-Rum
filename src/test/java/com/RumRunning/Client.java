package com.RumRunning;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class Client
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TroubleBrewingPlugin.class);
		RuneLite.main(args);
	}
}