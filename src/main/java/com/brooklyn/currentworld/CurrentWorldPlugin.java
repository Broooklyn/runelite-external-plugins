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

import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

@Slf4j
@PluginDescriptor(
	name = "Current World",
	description = "Pins activity to the world switcher, overlays your current world",
	tags = {"hub", "world", "overlay", "pvp", "brooklyn", "activity", "switcher"}
)
public class CurrentWorldPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CurrentWorldConfig config;

	@Inject
	private CurrentWorldOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WorldService worldService;

	private String activity;

	@Provides
	CurrentWorldConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(CurrentWorldConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		getWorld();
		setWorldListTitle();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
		unsetWorldListTitle();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("currentworld"))
		{
			if (config.worldSwitcherActivity())
			{
				setWorldListTitle();
			}
			else
			{
				unsetWorldListTitle();
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			getWorld();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == 841)
		{
			setWorldListTitle();
		}
	}

	private void getWorld()
	{
		WorldResult worldResult = worldService.getWorlds();

		if (worldService != null)
		{
			World world = worldResult.findWorld(client.getWorld());
			activity = world.getActivity();
		}
	}

	private void setWorldListTitle()
	{
		Widget worldListTitleWidget = client.getWidget(69, 2);

		if (worldListTitleWidget != null && config.worldSwitcherActivity())
		{
			worldListTitleWidget.setText(activity.equals("-") ? "World " + client.getWorld() : "World " + client.getWorld() + "<br>" + activity);
		}
	}

	private void unsetWorldListTitle()
	{
		Widget worldListTitleWidget = client.getWidget(69, 2);

		if (worldListTitleWidget != null)
		{
			worldListTitleWidget.setText("Current world - " + client.getWorld());
		}
	}
}
