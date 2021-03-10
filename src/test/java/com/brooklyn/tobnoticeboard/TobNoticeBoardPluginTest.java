package com.brooklyn.tobnoticeboard;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class TobNoticeBoardPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(TobNoticeBoardPlugin.class);
		RuneLite.main(args);
	}
}