/*
 * Copyright (c) 2021, Brooklyn <https://github.com/Broooklyn>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.brooklyn.tobnoticeboard;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.awt.*;

@ConfigGroup("tobnoticeboard")
public interface TobNoticeBoardConfig extends Config
{
	@ConfigItem(
		keyName = "highlightFriends",
		name = "Highlight Friends",
		description = "Whether or not to highlight friends' names on the notice board",
		position = 1
	)
	default boolean highlightFriends()
	{
		return true;
	}

	@ConfigItem(
		keyName = "friendColor",
		name = "Friend color",
		description = "The color with which to highlight names from your friends list",
		position = 2
	)
	default Color friendColor()
	{
		return new Color(0, 200, 83);
	}

	@ConfigItem(
		keyName = "highlightClan",
		name = "Highlight Clan members",
		description = "Whether or not to highlight clan chat members' names on the notice board",
		position = 3
	)
	default boolean highlightClan()
	{
		return true;
	}

	@ConfigItem(
		keyName = "clanColor",
		name = "Clan member color",
		description = "The color with which to highlight names from your current clan chat",
		position = 4
	)
	default Color clanColor()
	{
		return new Color(170, 0, 255);
	}

	@ConfigItem(
		keyName = "highlightIgnored",
		name = "Highlight Ignored players",
		description = "Whether or not to highlight ignored players on the notice board",
		position = 5
	)
	default boolean highlightIgnored()
	{
		return true;
	}

	@ConfigItem(
		keyName = "ignoredColor",
		name = "Ignored color",
		description = "The color with which to highlight names from your ignore list",
		position = 6
	)
	default Color ignoredColor()
	{
		return new Color(182, 0, 0);
	}
}
