package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMuteConfig;
import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.AmbientSoundEffect;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Annoyance Mute Debug",
	description = "Selectively mute annoying game sounds",
	tags = {"sound", "volume", "mute", "hub", "brooklyn", "pet", "stomp"}
)
public class AnnoyanceMutePluginDebug extends Plugin
{
	@Inject
	Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AnnoyanceMuteOverlay overlay;

	@Inject
	private AnnoyanceMutePlugin plugin;

	@Getter(AccessLevel.PACKAGE)
	private final List<ColorTileMarker> points = new ArrayList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
	}

	@Provides
	AnnoyanceMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnnoyanceMuteConfig.class);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe(priority = -3) // priority -2 to run after music plugin
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOGGED_IN)
		{
			points.clear();
			for (AmbientSoundEffect ambientSoundEffect : client.getAmbientSoundEffects())
			{
				markTile(ambientSoundEffect);
			}
		}
	}

	private void markTile(AmbientSoundEffect ambientSoundEffect)
	{
		if (ambientSoundEffect == null)
		{
			return;
		}

		WorldPoint worldPointMin = WorldPoint.fromLocalInstance(client, ambientSoundEffect.getMinPosition());
		WorldPoint worldPointMax = WorldPoint.fromLocalInstance(client, ambientSoundEffect.getMaxPosition());

		int[] b = ambientSoundEffect.getBackgroundSoundEffectIds();

		StringBuilder stringBuilder = new StringBuilder();
		if (b == null)
		{
			stringBuilder.append(",");
		} else {
			for (int i: b)
			{
				stringBuilder.append("," + i);
			}
		}

		ColorTileMarker pointMin = new ColorTileMarker(worldPointMin, Color.RED, String.valueOf(ambientSoundEffect.getSoundEffectId()) + "min (" + stringBuilder.toString().substring(1) + ")");
		ColorTileMarker pointMax = new ColorTileMarker(worldPointMax, Color.RED, String.valueOf(ambientSoundEffect.getSoundEffectId()) + "max (" + stringBuilder.toString().substring(1) + ")");

		points.add(pointMin);
		points.add(pointMax);
	}
}
