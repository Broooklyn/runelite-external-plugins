package com.brooklyn.annoyancemute;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.events.ConfigChanged;
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
@RunWith(MockitoJUnitRunner.Silent.class)
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
		when(config.ambientSoundsToMute()).thenReturn("");
		when(config.ambientSoundsToMuteRegional()).thenReturn("");
	}

	@Test
	public void muteWoodcutting()
	{
		when(config.muteWoodcutting()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.EITHER, null));
	}

	@Test
	public void muteDemons()
	{
		when(config.muteDemons()).thenReturn(false);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		assertFalse(plugin.shouldMute(SoundEffectID.DEMON_ATTACK, SoundEffectType.EITHER, null));
	}

	@Test
	public void muteNoonPet()
	{
		when(config.mutePetSounds()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		NPC noonpet = mock(NPC.class);
		when(noonpet.getCombatLevel()).thenReturn(0);

		assertTrue(plugin.shouldMute(SoundEffectID.NOON_FLAP_1, SoundEffectType.EITHER, noonpet));
		assertTrue(plugin.shouldMute(SoundEffectID.NOON_FLAP_2, SoundEffectType.EITHER, noonpet));
	}

	@Test
	public void muteNoon()
	{
		when(config.mutePetSounds()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		NPC noon = mock(NPC.class);
		when(noon.getCombatLevel()).thenReturn(222);

		assertFalse(plugin.shouldMute(SoundEffectID.NOON_FLAP_1, SoundEffectType.EITHER, noon));
		assertFalse(plugin.shouldMute(SoundEffectID.NOON_FLAP_2, SoundEffectType.EITHER, noon));
	}

	@Test
	public void muteTeleportOthers()
	{
		// when true/true like this, we only mute the sound when another player is doing it
		when(config.muteTeleport()).thenReturn(true);
		when(config.muteTeleportOthers()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(7284);

		assertTrue(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, localPlayer));
	}

	@Test
	public void muteTeleportOthers2()
	{
		// when true/false like this, we only mute the sound when another player is doing it
		when(config.muteTeleport()).thenReturn(false);
		when(config.muteTeleportOthers()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(7284);

		assertTrue(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, localPlayer));
	}

	@Test
	public void muteTeleports()
	{
		// when true/false like this, we will always mute the sounds
		when(config.muteTeleport()).thenReturn(true);
		when(config.muteTeleportOthers()).thenReturn(false);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(714);

		assertTrue(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, localPlayer));
	}

	@Test
	public void muteTeleports2()
	{
		// when true/false like this, we will always mute the sounds
		when(config.muteTeleport()).thenReturn(true);
		when(config.muteTeleportOthers()).thenReturn(false);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(9999); // new teleport animation id, same sound so it is muted because this configuration always mutes VWOOP sound

		assertTrue(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, localPlayer));
	}

	@Test
	public void teleportMakesNoise()
	{
		// when false/false like this, we won't mute
		when(config.muteTeleport()).thenReturn(false);
		when(config.muteTeleportOthers()).thenReturn(false);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		Player localPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(localPlayer.getAnimation()).thenReturn(7284);

		assertFalse(plugin.shouldMute(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, localPlayer));
	}
}
