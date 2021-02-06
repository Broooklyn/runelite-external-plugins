package com.brooklyn.currentworld;

import net.runelite.api.Client;
import net.runelite.api.WorldType;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ComponentOrientation;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class CurrentWorldOverlay extends OverlayPanel
{
	@Inject
	Client client;

	@Inject
	private CurrentWorldOverlay(CurrentWorldPlugin plugin)
	{
		setPriority(OverlayPriority.LOW);
		setPosition(OverlayPosition.TOP_RIGHT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!panelComponent.getChildren().isEmpty())
		{
			panelComponent.getChildren().remove(0);
		}

		final int currentWorld = client.getWorld();
		final boolean pvpWorld = client.getWorldType().contains(WorldType.PVP);
		final boolean highRiskWorld = client.getWorldType().contains(WorldType.HIGH_RISK);

		panelComponent.setOrientation(ComponentOrientation.VERTICAL);
		panelComponent.setPreferredSize(new Dimension(67,0));

		if (pvpWorld || highRiskWorld)
		{
			panelComponent.getChildren().add(TitleComponent.builder()
				.text(pvpWorld ? "PVP WORLD" : "HIGH RISK")
				.color(pvpWorld ? Color.RED : Color.ORANGE)
				.build());
		}

		panelComponent.getChildren().add(TitleComponent.builder()
			.text("World " + currentWorld)
			.color(pvpWorld ? Color.RED : highRiskWorld ? Color.ORANGE : Color.GREEN)
			.build());

		return super.render(graphics);
	}
}
