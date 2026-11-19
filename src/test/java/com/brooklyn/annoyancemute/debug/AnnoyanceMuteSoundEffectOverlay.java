package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import com.brooklyn.annoyancemute.SoundEffectType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

class AnnoyanceMuteSoundEffectOverlay extends OverlayPanel
{
	private final static int MAX_LINES = 10;
	private final static Color COLOR_SOUND_EFFECT = Color.WHITE;
	private final static Color COLOR_AREA_SOUND_EFFECT = Color.YELLOW;
	private final static Color COLOR_SILENT_SOUND_EFFECT = Color.GRAY;
	private final static Color COLOR_ALLOWED = Color.GREEN;
	private final static Color COLOR_CONSUMED = Color.RED;
	public static final String ALLOWED = "Allowed";
	public static final String CONSUMED = "Consumed";
	private final Client client;
	private final AnnoyanceMuteDebugConfig config;
	private AnnoyanceMutePlugin plugin;

	@Inject
	AnnoyanceMuteSoundEffectOverlay(Client client, AnnoyanceMuteDebugConfig config)
	{
		this.client = client;
		this.config = config;

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Game / Area Sounds")
			.leftColor(Color.CYAN)
			.build());

		setClearChildren(false);
		setPosition(OverlayPosition.TOP_LEFT);
	}

	void setMutePlugin(AnnoyanceMutePlugin plugin)
	{
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showSoundEffects())
		{
			return null;
		}
		return super.render(graphics);
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		int soundId = event.getSoundId();

		String text = "G: " + soundLabel(soundId) +
			" - S: " + sourceLabel(event.getSource()) +
			" - D: " + event.getDelay();

		String action = ALLOWED;
		Color actionColor = COLOR_ALLOWED;

		if (plugin.shouldMute(soundId, SoundEffectType.SOUND_EFFECT, event.getSource()))
		{
			action = CONSUMED;
			actionColor = COLOR_CONSUMED;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(text)
			.leftColor(COLOR_SOUND_EFFECT)
			.right(action)
			.rightColor(actionColor)
			.build());

		checkMaxLines();
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		Color textColor = COLOR_AREA_SOUND_EFFECT;
		Color actionColor = COLOR_ALLOWED;

		int soundId = event.getSoundId();
		String text =
			"Id: " + soundLabel(soundId) +
				" - S: " + sourceLabel(event.getSource()) +
				" - L: " + event.getSceneX() + "," + event.getSceneY() +
				" - R: " + event.getRange() +
				" - D: " + event.getDelay();
		String action = ALLOWED;

		// Check if the player is within range to hear the sound
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			LocalPoint lp = localPlayer.getLocalLocation();
			if (lp != null)
			{
				int sceneX = lp.getSceneX();
				int sceneY = lp.getSceneY();
				int distance = Math.abs(sceneX - event.getSceneX()) + Math.abs(sceneY - event.getSceneY());
				if (distance > event.getRange())
				{
					textColor = COLOR_SILENT_SOUND_EFFECT;
					text = "SA: " + soundLabel(soundId);
				}
			}
		}

		if (plugin.shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT, event.getSource()))
		{
			action = CONSUMED;
			actionColor = COLOR_CONSUMED;
		}

		panelComponent.getChildren().add(LineComponent.builder()
			.left(text)
			.leftColor(textColor)
			.right(action)
			.rightColor(actionColor)
			.build());

		checkMaxLines();
	}

	static String soundLabel(int soundId)
	{
		String name = SoundNames.get(soundId);
		return name != null ? name + " (" + soundId + ")" : String.valueOf(soundId);
	}

	static String sourceLabel(@Nullable Actor source)
	{
		if (source == null)
		{
			return "<none>";
		}
		String id = source instanceof NPC ? "id " + ((NPC) source).getId() + ", " : "";
		return source.getName() + " (" + id + "cb " + source.getCombatLevel() + ")";
	}

	private void checkMaxLines()
	{
		while (panelComponent.getChildren().size() > MAX_LINES)
		{
			panelComponent.getChildren().remove(1);
		}
	}
}