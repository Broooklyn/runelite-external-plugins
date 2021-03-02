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
package com.brooklyn.currentworld;

import java.awt.Color;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("currentworld")
public interface CurrentWorldConfig extends Config
{
	@ConfigSection(
		name = "Overlay Colors",
		description = "Overlay colors",
		closedByDefault = true,
		position = 3
	)
	String colorSection = "colorSection";

	@ConfigItem(
		keyName = "worldSwitcherActivity",
		name = "World Switcher Activity",
		description = "Adds world activity to the Jagex world switcher<br>e.g., 'Sulliuscep cutting' and '2200 Skill total'",
		position = 1
	)
	default boolean worldSwitcherActivity()
	{
		return true;
	}

	@ConfigItem(
		keyName = "showOverlay",
		name = "Overlay",
		description = "Enables the current world overlay",
		position = 2
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "safeWorldColor",
		name = "Safe Worlds",
		description = "The color of the overlay for safe worlds",
		position = 1,
		section = colorSection
	)
	default Color safeWorldColor()
	{
		return Color.GREEN;
	}

	@ConfigItem(
		keyName = "highRiskWorldColor",
		name = "High Risk Worlds",
		description = "The color of the overlay for High Risk worlds",
		position = 2,
		section = colorSection
	)
	default Color highRiskWorldColor()
	{
		return Color.ORANGE;
	}

	@ConfigItem(
		keyName = "pvpWorldColor",
		name = "PVP Worlds",
		description = "The color of the overlay for PVP worlds",
		position = 3,
		section = colorSection
	)
	default Color pvpWorldColor()
	{
		return Color.RED;
	}
}
