package com.brooklyn.annoyancemute.debug;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import net.runelite.api.AmbientSoundComposition;
import net.runelite.api.AmbientSoundEffect;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.RandomSound;
import net.runelite.api.events.AmbientSoundChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

/**
 * Shows the ambient sounds the client currently has, which is the state AnnoyanceMutePlugin mutes (the plugin edits it
 * in place), so what is listed is what is left after muting. That is the ambient sound the server set
 * (client.getAmbientSound()) and the ambient sound effects of the scene (client.getAmbientSoundEffects()).
 * <p>
 * The primary sounds are listed left aligned, then the random and background sounds that go with them, right aligned.
 * The lists are rebuilt after AnnoyanceMutePlugin has handled the same event, hence the priority.
 */
class AnnoyanceMuteAmbientSoundOverlay extends OverlayPanel
{
	private final static int MAX_ROWS = 20; // per list
	private final static int PRIORITY = -3; // after AnnoyanceMutePlugin, which uses -2 or the default
	private final static Color COLOR_HEADER = Color.CYAN;
	private final static Color COLOR_PRIMARY = Color.WHITE;
	private final static Color COLOR_RANDOM = Color.WHITE;
	private final static Color COLOR_MORE = Color.GRAY;

	private final Client client;
	private final AnnoyanceMuteDebugConfig config;

	private volatile List<String> primarySounds = Collections.emptyList();
	private volatile List<String> randomSounds = Collections.emptyList();

	@Inject
	AnnoyanceMuteAmbientSoundOverlay(Client client, AnnoyanceMuteDebugConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showAmbientSounds())
		{
			return null;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Ambient sounds")
			.leftColor(COLOR_HEADER)
			.build());

		List<String> primary = primarySounds;
		List<String> random = randomSounds;

		// the primary sounds, left aligned
		addRows(primary, false);

		// then the random and background sounds, right aligned
		addRows(random, true);

		return super.render(graphics);
	}

	private void addRows(List<String> list, boolean rightAligned)
	{
		for (int i = 0; i < list.size() && i < MAX_ROWS; i++)
		{
			// the last row says how many did not fit
			boolean more = i == MAX_ROWS - 1 && list.size() > MAX_ROWS;
			String text = more ? "... +" + (list.size() - MAX_ROWS + 1) + " more" : list.get(i);
			Color color = more ? COLOR_MORE : (rightAligned ? COLOR_RANDOM : COLOR_PRIMARY);

			LineComponent.LineComponentBuilder row = LineComponent.builder();
			if (rightAligned)
			{
				row.right(text).rightColor(color);
			}
			else
			{
				row.left(text).leftColor(color);
			}
			panelComponent.getChildren().add(row.build());
		}
	}

	@Subscribe(priority = PRIORITY)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOADING)
		{
			// the scene is being rebuilt, its ambient sounds are created again while it loads
			primarySounds = Collections.emptyList();
			randomSounds = Collections.emptyList();
		}
		else if (gameState == GameState.LOGGED_IN)
		{
			refresh();
		}
	}

	@Subscribe(priority = PRIORITY)
	public void onAmbientSoundChanged(AmbientSoundChanged event)
	{
		refresh();
	}

	// AmbientSoundEffectCreated fires once per effect while a scene loads, so the lists are rebuilt on the next tick
	// instead, which is also when AnnoyanceMutePlugin handles a change of region
	@Subscribe(priority = PRIORITY)
	public void onGameTick(GameTick event)
	{
		refresh();
	}

	private void refresh()
	{
		if (!config.showAmbientSounds())
		{
			return;
		}

		// sets, because a scene has many identical ambient sound effects
		Set<String> primary = new LinkedHashSet<>();
		Set<String> random = new LinkedHashSet<>();

		AmbientSoundComposition ambientSound = client.getAmbientSound();
		if (ambientSound != null)
		{
			addLabels(primary, ambientSound.getSoundEffectIds());
			String name = AmbientSoundNames.get(ambientSound.getId());
			primary.add((name != null ? name : "ambient_sound") + " (" + ambientSound.getId() + ")");

			List<RandomSound> compositionRandomSounds = ambientSound.getRandomSounds();
			if (compositionRandomSounds != null)
			{
				for (RandomSound randomSound : compositionRandomSounds)
				{
					addLabels(random, randomSound.getSoundEffectIds());
				}
			}
		}

		for (AmbientSoundEffect effect : client.getAmbientSoundEffects())
		{
			// -1 is an effect that only has background sounds
			if (effect.getSoundEffectId() != -1)
			{
				primary.add(AnnoyanceMuteSoundEffectOverlay.soundLabel(effect.getSoundEffectId()));
			}

			RandomSound randomSound = effect.getRandomSound();
			if (randomSound != null)
			{
				addLabels(random, randomSound.getSoundEffectIds());
			}
		}

		primarySounds = new ArrayList<>(primary);
		randomSounds = new ArrayList<>(random);
	}

	private static void addLabels(Set<String> labels, int[] soundIds)
	{
		if (soundIds == null)
		{
			return;
		}
		for (int soundId : soundIds)
		{
			labels.add(AnnoyanceMuteSoundEffectOverlay.soundLabel(soundId));
		}
	}
}
