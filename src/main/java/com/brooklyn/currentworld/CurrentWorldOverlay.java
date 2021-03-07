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

import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.client.game.WorldService;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CurrentWorldOverlay extends OverlayPanel
{
	private final Client client;
	private final CurrentWorldConfig config;
	private final WorldService worldService;

	@Inject
	private CurrentWorldOverlay(Client client, CurrentWorldConfig config, WorldService worldService)
	{
		this.client = client;
		this.config = config;
		this.worldService = worldService;
		setPriority(OverlayPriority.HIGH);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay())
		{
			return null;
		}

		if (!panelComponent.getChildren().isEmpty())
		{
			panelComponent.getChildren().remove(0);
		}

		WorldResult worldResult = worldService.getWorlds();
		final boolean pvpWorld = client.getWorldType().contains(WorldType.PVP) || client.getWorldType().contains(WorldType.DEADMAN);
		final boolean highRiskWorld = client.getWorldType().contains(WorldType.HIGH_RISK);
		final Color textColor = pvpWorld ? config.pvpWorldColor() : highRiskWorld ? config.highRiskWorldColor() : config.safeWorldColor();
		final List<String> textToDisplay = new ArrayList<>();

		if (!config.overlayActivity() && (pvpWorld || highRiskWorld))
		{
			textToDisplay.add(pvpWorld ? "PVP WORLD" : "HIGH RISK");
		}

		textToDisplay.add("World " + client.getWorld());

		if (config.overlayActivity() && worldResult != null)
		{
			final World currentWorld = worldResult.findWorld(client.getWorld());

			if (!"-".equals(currentWorld.getActivity()))
			{
				textToDisplay.add(currentWorld.getActivity());
			}
		}

		final int overlayWidth = calculateWidth(graphics, textToDisplay) + 10;
		panelComponent.setOrientation(ComponentOrientation.VERTICAL);
		panelComponent.setPreferredSize(new Dimension(overlayWidth, 0));

		for (String text : textToDisplay)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text(text)
				.color(textColor)
				.build());
		}

		return super.render(graphics);
	}

	private int calculateWidth(Graphics2D graphics, Collection<String> textToDisplay)
	{
		return textToDisplay.stream()
			.mapToInt(line -> graphics.getFontMetrics().stringWidth(line))
			.max()
			.orElseThrow(() -> new IllegalArgumentException("Can't calculate overlay width"));
	}
}
