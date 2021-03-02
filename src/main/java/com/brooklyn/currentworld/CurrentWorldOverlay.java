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
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class CurrentWorldOverlay extends OverlayPanel
{
	private final Client client;
	private final CurrentWorldConfig config;

	@Inject
	private CurrentWorldOverlay(Client client, CurrentWorldConfig config)
	{
		this.client = client;
		this.config = config;
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

		final int currentWorld = client.getWorld();
		final boolean pvpWorld = client.getWorldType().contains(WorldType.PVP) || client.getWorldType().contains(WorldType.DEADMAN);
		final boolean highRiskWorld = client.getWorldType().contains(WorldType.HIGH_RISK);

		panelComponent.setOrientation(ComponentOrientation.VERTICAL);
		panelComponent.setPreferredSize(new Dimension(67,0));

		if (pvpWorld || highRiskWorld)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text(pvpWorld ? "PVP WORLD" : "HIGH RISK")
				.color(pvpWorld ? config.pvpWorldColor() : config.highRiskWorldColor())
				.build());
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("World " + currentWorld)
			.color(pvpWorld ? config.pvpWorldColor() : highRiskWorld ? config.highRiskWorldColor() : config.safeWorldColor())
			.build());

		return super.render(graphics);
	}
}
