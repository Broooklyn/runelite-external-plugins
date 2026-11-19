package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
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
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.util.ColorUtil;

/**
 * Shows the ambient sounds the client currently has, which is the state AnnoyanceMutePlugin mutes (the plugin edits it
 * in place), so what is listed is what is left after muting. That is the ambient sound the server set
 * (client.getAmbientSound()) and the ambient sound effects of the scene (client.getTopLevelWorldView().getAmbientSoundEffects()).
 * <p>
 * Laid out like the ambient sounds overlay of the Visual Sounds plugin: each primary sound is followed by the random and
 * background sounds that go with it in brackets, and the text wraps to the width of the panel.
 * The lists are rebuilt after AnnoyanceMutePlugin has handled the same event, hence the priority.
 */
class AnnoyanceMuteAmbientSoundOverlay extends OverlayPanel
{
	private final static int PANEL_WIDTH = 250;
	private final static int PANEL_BORDER = 0;
	private final static int PRIORITY = -3; // after AnnoyanceMutePlugin, which uses -2 or the default
	private final static Color COLOR_HEADER = Color.YELLOW;
	private final static Color COLOR_PRIMARY = Color.WHITE;
	private final static Color COLOR_CONTAINER = Color.CYAN;
	private final static Color COLOR_SUB = new Color(0xA0A0A0);
	private final static Color COLOR_SUB_ALTERNATE = new Color(0xFFC864);

	private final Client client;
	private final AnnoyanceMuteDebugConfig config;
	private AnnoyanceMutePlugin plugin;

	// primary sound -> the random and background sounds that go with it
	private volatile Map<String, List<String>> ambientSounds = Collections.emptyMap();

	@Inject
	AnnoyanceMuteAmbientSoundOverlay(Client client, AnnoyanceMuteDebugConfig config)
	{
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.BOTTOM_LEFT);
		setMovable(true);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(PRIORITY_LOW);
		setPreferredSize(new Dimension(PANEL_WIDTH, 0));
	}

	void setMutePlugin(AnnoyanceMutePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showAmbientSounds())
		{
			return null;
		}

		List<LayoutableRenderableEntity> rows = panelComponent.getChildren();
		rows.add(LineComponent.builder()
			.left("Ambient sounds")
			.leftColor(COLOR_HEADER)
			.build());

		FontMetrics metrics = graphics.getFontMetrics();
		// the overlay is resizable and RuneLite restores the saved size, so wrap to the width it is drawn at
		Dimension panelSize = getPreferredSize();
		int maxTextWidth = (panelSize != null ? panelSize.width : PANEL_WIDTH) - PANEL_BORDER;
		int spaceWidth = metrics.stringWidth(" ");

		for (Map.Entry<String, List<String>> entry : ambientSounds.entrySet())
		{
			List<String> subs = entry.getValue();
			boolean hasSubs = !subs.isEmpty();

			// each token is { text with color tags, plain text to measure }
			List<String[]> tokens = new ArrayList<>();
			tokens.add(new String[]{
				ColorUtil.prependColorTag(entry.getKey(), COLOR_PRIMARY) + (hasSubs ? ColorUtil.prependColorTag(",", COLOR_CONTAINER) : ""),
				entry.getKey() + (hasSubs ? "," : "")});

			for (int i = 0; i < subs.size(); i++)
			{
				String open = i == 0 ? "[" : "";
				String close = i == subs.size() - 1 ? "]" : ",";
				Color color = i % 2 == 0 ? COLOR_SUB : COLOR_SUB_ALTERNATE;
				tokens.add(new String[]{
					ColorUtil.prependColorTag(open, COLOR_CONTAINER)
						+ ColorUtil.prependColorTag(subs.get(i), color)
						+ ColorUtil.prependColorTag(close, COLOR_CONTAINER),
					open + subs.get(i) + close});
			}

			// greedy wrap between tokens, a sound is never split across lines
			StringBuilder line = new StringBuilder();
			int lineWidth = 0;
			for (String[] token : tokens)
			{
				int width = metrics.stringWidth(token[1]);
				if (line.length() > 0 && lineWidth + spaceWidth + width > maxTextWidth)
				{
					rows.add(LineComponent.builder().left(line.toString()).build());
					line.setLength(0);
					lineWidth = 0;
				}
				if (line.length() > 0)
				{
					line.append(' ');
					lineWidth += spaceWidth;
				}
				line.append(token[0]);
				lineWidth += width;
			}
			if (line.length() > 0)
			{
				rows.add(LineComponent.builder().left(line.toString()).build());
			}
		}

		return super.render(graphics);
	}

	@Subscribe(priority = PRIORITY)
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();
		if (gameState == GameState.LOADING)
		{
			// the scene is being rebuilt, its ambient sounds are created again while it loads
			ambientSounds = Collections.emptyMap();
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
		Map<String, Set<String>> sounds = new LinkedHashMap<>();

		AmbientSoundComposition ambientSound = client.getAmbientSound();
		if (ambientSound != null)
		{
			String name = plugin.getSoundNames().getAmbientSoundName(ambientSound.getId());
			Set<String> subs = sounds.computeIfAbsent((name != null ? name : "ambient_sound") + " (" + ambientSound.getId() + ")", k -> new LinkedHashSet<>());
			addLabels(subs, ambientSound.getSoundEffectIds());

			List<RandomSound> compositionRandomSounds = ambientSound.getRandomSounds();
			if (compositionRandomSounds != null)
			{
				for (RandomSound randomSound : compositionRandomSounds)
				{
					addLabels(subs, randomSound.getSoundEffectIds());
				}
			}
		}

		for (AmbientSoundEffect effect : client.getTopLevelWorldView().getAmbientSoundEffects())
		{
			RandomSound randomSound = effect.getRandomSound();

			// -1 is an effect that only has background sounds, which have no primary sound to go under
			String primary = effect.getSoundEffectId() != -1
				? AnnoyanceMuteSoundEffectOverlay.soundLabel(plugin.getSoundNames(), effect.getSoundEffectId())
				: "background (-1)";
			Set<String> subs = sounds.computeIfAbsent(primary, k -> new LinkedHashSet<>());
			if (randomSound != null)
			{
				addLabels(subs, randomSound.getSoundEffectIds());
			}
		}

		Map<String, List<String>> result = new LinkedHashMap<>();
		sounds.forEach((primary, subs) -> result.put(primary, new ArrayList<>(subs)));
		ambientSounds = result;
	}

	private void addLabels(Set<String> labels, int[] soundIds)
	{
		if (soundIds == null)
		{
			return;
		}
		for (int soundId : soundIds)
		{
			labels.add(AnnoyanceMuteSoundEffectOverlay.soundLabel(plugin.getSoundNames(), soundId));
		}
	}
}
