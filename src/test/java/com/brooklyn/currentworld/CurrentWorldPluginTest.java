package com.brooklyn.currentworld;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class CurrentWorldPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(CurrentWorldPlugin.class);
		RuneLite.main(args);
	}
}