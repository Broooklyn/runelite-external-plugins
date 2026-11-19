package com.brooklyn.annoyancemute.debug;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(AnnoyanceMuteDebugConfig.GROUP)
public interface AnnoyanceMuteDebugConfig extends Config
{
	String GROUP = "annoyancemutedebug";

	@ConfigItem(
		keyName = "showAmbientSounds",
		name = "Ambient sounds overlay",
		description = "Shows the ambient sounds the client currently has, each primary sound followed by its random and background sounds in brackets",
		position = 0
	)
	default boolean showAmbientSounds()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showSoundEffects",
		name = "Game / area sounds overlay",
		description = "Shows the game and area sound effects as they play and whether they are muted",
		position = 1
	)
	default boolean showSoundEffects()
	{
		return true;
	}
}
