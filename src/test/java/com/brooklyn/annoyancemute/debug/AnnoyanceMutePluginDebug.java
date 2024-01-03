package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
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
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Annoyance Mute Debug",
	description = "Annoyance Mute Debug mode",
	tags = {"sound", "volume", "mute", "hub", "brooklyn", "pet", "stomp"}
)
@PluginDependency(AnnoyanceMutePlugin.class)
public class AnnoyanceMutePluginDebug extends Plugin
{
	@Inject
	Client client;

	@Inject
	EventBus eventBus;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AnnoyanceMuteOverlay annoyanceMuteOverlay;

	@Inject
	private AnnoyanceMuteSoundEffectOverlay annoyanceMuteSoundEffectOverlay;


	@Getter(AccessLevel.PACKAGE)
	private final List<AmbientSoundTileMarker> points = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<AmbientSoundPoint> groundMarkerPoints = new ArrayList<>();

	@Override
	protected void startUp()
	{
		overlayManager.add(annoyanceMuteOverlay);
		overlayManager.add(annoyanceMuteSoundEffectOverlay);
		eventBus.register(annoyanceMuteSoundEffectOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(annoyanceMuteOverlay);
		overlayManager.remove(annoyanceMuteSoundEffectOverlay);
		eventBus.unregister(annoyanceMuteSoundEffectOverlay);
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
	 * @param points {@link AmbientSoundPoint}s to be converted to {@link AmbientSoundTileMarker}s
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
