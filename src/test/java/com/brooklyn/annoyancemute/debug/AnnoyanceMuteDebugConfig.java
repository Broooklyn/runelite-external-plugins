package com.brooklyn.annoyancemute.debug;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

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

	@ConfigItem(
		keyName = "hideConsumedSounds",
		name = "Hide consumed sounds",
		description = "Hides the sounds that are consumed (muted) from the game / area sounds overlay",
		position = 2
	)
	default boolean hideConsumedSounds()
	{
		return false;
	}

	@ConfigItem(
		keyName = "hideSilentAreaSounds",
		name = "Hide out of range area sounds",
		description = "Hides the area sounds that are too far away to be heard from the game / area sounds overlay",
		position = 3
	)
	default boolean hideSilentAreaSounds()
	{
		return false;
	}

	@Range(min = 5, max = 20)
	@ConfigItem(
		keyName = "maxSoundEffectLines",
		name = "Game / area sounds shown",
		description = "How many game and area sounds the overlay can show at a time",
		position = 4
	)
	default int maxSoundEffectLines()
	{
		return 10;
	}
}
