package com.brooklyn.toblightup;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TobLightUpPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TobLightUpPlugin.class);
		RuneLite.main(args);
	}
}