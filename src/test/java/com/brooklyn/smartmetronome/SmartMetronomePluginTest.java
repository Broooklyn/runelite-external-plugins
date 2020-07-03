package com.brooklyn.smartmetronome;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class SmartMetronomePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(SmartMetronomePlugin.class);
		RuneLite.main(args);
	}
}