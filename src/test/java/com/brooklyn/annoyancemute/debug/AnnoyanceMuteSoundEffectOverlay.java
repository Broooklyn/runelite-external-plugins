package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import com.brooklyn.annoyancemute.SoundEffectType;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.api.Client;
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
	private final AnnoyanceMutePlugin plugin;

	@Inject
	AnnoyanceMuteSoundEffectOverlay(Client client, AnnoyanceMutePlugin plugin)
	{
		this.client = client;
		this.plugin = plugin;

		panelComponent.getChildren().add(LineComponent.builder()
			.left("Sound Effects")
			.leftColor(Color.CYAN)
			.build());

		setClearChildren(false);
		setPosition(OverlayPosition.TOP_LEFT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		return super.render(graphics);
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		int soundId = event.getSoundId();

		String text = "G: " + soundId;

		String action = ALLOWED;
		Color actionColor = COLOR_ALLOWED;

		if (plugin.shouldMute(soundId, SoundEffectType.SOUND_EFFECT))
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
		String text = "A: " + soundId;
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
					text = "SA: " + soundId;
				}
			}
		}

		if (plugin.shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT))
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

	private void checkMaxLines()
	{
		while (panelComponent.getChildren().size() > MAX_LINES)
		{
			panelComponent.getChildren().remove(1);
		}
	}
}