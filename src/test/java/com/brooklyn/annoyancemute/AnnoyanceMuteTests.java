package com.brooklyn.annoyancemute;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.config.RuneLiteConfig;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class AnnoyanceMuteTests
{
	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private RuneLiteConfig runeLiteConfig;

	@Mock
	@Bind
	private AnnoyanceMuteConfig config;

	@Inject
	private AnnoyanceMutePlugin plugin;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

		when(config.soundsToMute()).thenReturn("");
	}

	@Test
	public void muteTeleportOthers()
	{
		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(7284);

		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 714)); // Normal
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 1816)); // Lunar
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3864)); // Scroll
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3865)); // Xeric
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3867)); // Wilderness
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3869)); // Cabbage
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3872)); // Ardougne
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3874)); // Burgh

		assertTrue(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either));
	}

	@Test
	public void muteTeleport()
	{
		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(714);

		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 714)); // Normal
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 1816)); // Lunar
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3864)); // Scroll
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3865)); // Xeric
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3867)); // Wilderness
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3869)); // Cabbage
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3872)); // Ardougne
		plugin.soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3874)); // Burgh

		assertFalse(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either));
	}
}
