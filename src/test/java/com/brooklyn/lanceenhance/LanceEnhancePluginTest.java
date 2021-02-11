package com.brooklyn.lanceenhance;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LanceEnhancePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LanceEnhancePlugin.class);
		RuneLite.main(args);
	}
}