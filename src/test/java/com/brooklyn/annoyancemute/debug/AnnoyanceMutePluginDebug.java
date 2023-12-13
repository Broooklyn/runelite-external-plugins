package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMuteConfig;
import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import com.google.inject.Provides;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.AmbientSoundEffect;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AmbientSoundEffectCreated;
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
	private final List<AmbientSoundTileMarker> points = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<AmbientSoundPoint> groundMarkerPoints = new ArrayList<>();

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

	@Subscribe(priority = -3) // priority -3 to run after AnnoyanceMutePlugin
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOGGED_IN)
		{
			addSounds();
		}
	}

	@Subscribe(priority = -3) // priority -3 to run after AnnoyanceMutePlugin
	public void onAmbientSoundEffectCreated(AmbientSoundEffectCreated ambientSoundEffectCreated)
	{
		addSounds();
	}

	private void addSounds()
	{
		points.clear();
		groundMarkerPoints.clear();
		for (AmbientSoundEffect ambientSoundEffect : client.getAmbientSoundEffects())
		{
			markTile(ambientSoundEffect);
		}

		Collection<AmbientSoundTileMarker> colorTileMarkers = translateToColorTileMarker(groundMarkerPoints);
		points.addAll(colorTileMarkers);
	}

	private void markTile(AmbientSoundEffect ambientSoundEffect)
	{
		if (ambientSoundEffect == null)
		{
			return;
		}

		int[] b = ambientSoundEffect.getBackgroundSoundEffectIds();

		StringBuilder stringBuilder = new StringBuilder();
		if (b == null)
		{
			stringBuilder.append(",");
		}
		else
		{
			for (int i : b)
			{
				stringBuilder.append("," + i);
			}
		}

		markTile(ambientSoundEffect.getMinPosition(), String.valueOf(ambientSoundEffect.getSoundEffectId()) + "min (" + stringBuilder.toString().substring(1) + ")");
		markTile(ambientSoundEffect.getMaxPosition(), String.valueOf(ambientSoundEffect.getSoundEffectId()) + "max (" + stringBuilder.toString().substring(1) + ")");
	}

	private void markTile(LocalPoint localPoint, String label)
	{
		if (localPoint == null)
		{
			return;
		}

		WorldPoint worldPoint = WorldPoint.fromLocalInstance(client, localPoint);

		int regionId = worldPoint.getRegionID();
		AmbientSoundPoint point = new AmbientSoundPoint(regionId, worldPoint.getRegionX(), worldPoint.getRegionY(), worldPoint.getPlane(), Color.RED, label);

		groundMarkerPoints.add(point);
	}

	/**
	 * Translate a collection of ambient sound points to ambient sound tiles, accounting for instances
	 *
	 * @param points {@link AmbientSoundTileMarker}s to be converted to {@link AmbientSoundPoint}s
	 * @return A collection of ambient sound tiles, converted from the passed ambient sound points, accounting for local
	 * instance points. See {@link WorldPoint#toLocalInstance(Client, WorldPoint)}
	 */
	public Collection<AmbientSoundTileMarker> translateToColorTileMarker(Collection<AmbientSoundPoint> points)
	{
		if (points.isEmpty())
		{
			return Collections.emptyList();
		}

		return points.stream()
			.map(point -> new AmbientSoundTileMarker(
				WorldPoint.fromRegion(point.getRegionId(), point.getRegionX(), point.getRegionY(), point.getZ()),
				point.getColor(), point.getLabel()))
			.flatMap(colorTile ->
			{
				final Collection<WorldPoint> localWorldPoints = WorldPoint.toLocalInstance(client, colorTile.getWorldPoint());
				return localWorldPoints.stream().map(wp -> new AmbientSoundTileMarker(wp, colorTile.getColor(), colorTile.getLabel()));
			})
			.collect(Collectors.toList());
	}
}
