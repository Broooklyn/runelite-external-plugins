package com.brooklyn.annoyancemute;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.events.ConfigChanged;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

	@Mock
	@Bind
	private ConfigManager configManager;

	@Inject
	private AnnoyanceMutePlugin plugin;

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);

		when(config.soundsToMute()).thenReturn("");
		when(config.ambientSoundsToMute()).thenReturn("");
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
	public void muteFollowerSounds()
	{
		when(config.mutePetSounds()).thenReturn(true);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		NPCComposition followerComposition = mock(NPCComposition.class);
		when(followerComposition.isFollower()).thenReturn(true);
		NPC follower = mock(NPC.class);
		when(follower.getComposition()).thenReturn(followerComposition);

		NPCComposition otherComposition = mock(NPCComposition.class);
		when(otherComposition.isFollower()).thenReturn(false);
		NPC other = mock(NPC.class);
		when(other.getComposition()).thenReturn(otherComposition);

		// a sound that is not on any list is still muted when a pet makes it
		int unlistedSound = 1;
		assertTrue(plugin.shouldMute(unlistedSound, SoundEffectType.EITHER, follower));
		assertFalse(plugin.shouldMute(unlistedSound, SoundEffectType.EITHER, other));
		assertFalse(plugin.shouldMute(unlistedSound, SoundEffectType.EITHER, null));
	}

	@Test
	public void keepFollowerSoundsWhenPetsNotMuted()
	{
		when(config.mutePetSounds()).thenReturn(false);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		NPCComposition followerComposition = mock(NPCComposition.class);
		when(followerComposition.isFollower()).thenReturn(true);
		NPC follower = mock(NPC.class);
		when(follower.getComposition()).thenReturn(followerComposition);

		assertFalse(plugin.shouldMute(1, SoundEffectType.EITHER, follower));
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

	private void configChanged()
	{
		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("soundsToMute");
		plugin.onConfigChanged(configChanged);
	}

	@Test
	public void mutedSoundsAcceptIdsAndNames()
	{
		when(config.soundsToMute()).thenReturn("chaos_elemental_madness_impact");
		configChanged();
		assertTrue(plugin.shouldMute(353, SoundEffectType.EITHER, null));

		when(config.soundsToMute()).thenReturn("353");
		configChanged();
		assertTrue(plugin.shouldMute(353, SoundEffectType.EITHER, null));

		// names are not case sensitive and entries can be mixed
		when(config.soundsToMute()).thenReturn("1, Chaos_Elemental_Madness_Impact");
		configChanged();
		assertTrue(plugin.shouldMute(353, SoundEffectType.EITHER, null));
		assertTrue(plugin.shouldMute(1, SoundEffectType.EITHER, null));
		assertFalse(plugin.shouldMute(354, SoundEffectType.EITHER, null));
	}

	@Test
	public void mutedSoundsIgnoreInvalidNames()
	{
		when(config.soundsToMute()).thenReturn("not_a_real_sound,,  ,353:,chaos_elemental_madness_impact");
		configChanged();
		assertTrue(plugin.shouldMute(353, SoundEffectType.EITHER, null));
		assertFalse(plugin.shouldMute(354, SoundEffectType.EITHER, null));
	}

	@Test
	public void mutedAmbientSoundsAcceptIdsAndNames()
	{
		when(config.ambientSoundsToMute()).thenReturn("chaos_elemental_madness_impact, 12, nope");
		assertEquals(Arrays.asList(353, 12), plugin.getGenericAmbientSounds());
	}

	@Test
	public void regionalMutedAmbientSoundsAcceptIdsAndNames()
	{
		when(config.ambientSoundsToMute()).thenReturn("4020:chaos_elemental_madness_impact,4021:12,4022:nope,nope:353,4023,1:2:3,12,353");
		List<int[]> regional = plugin.getRegionalAmbientSounds();
		assertEquals(2, regional.size());
		assertArrayEquals(new int[]{4020, 353}, regional.get(0));
		assertArrayEquals(new int[]{4021, 12}, regional.get(1));

		// entries without a region are everywhere ambient sounds, the regional ones are not
		assertEquals(Arrays.asList(4023, 12, 353), plugin.getGenericAmbientSounds());
	}

	@Test
	public void migrateMovesRegionalAmbientSoundsIntoAmbientSounds()
	{
		when(config.ambientSoundsToMute()).thenReturn("12, 4020:353");
		when(configManager.getConfiguration("annoyancemute", "ambientSoundsToMuteRegional")).thenReturn("4020:353,4021:12");

		plugin.migrate();

		verify(configManager).setConfiguration("annoyancemute", "ambientSoundsToMute", "12,4020:353,4021:12");
		verify(configManager).unsetConfiguration("annoyancemute", "ambientSoundsToMuteRegional");
	}

	@Test
	public void migrateDoesNothingWithoutRegionalAmbientSounds()
	{
		plugin.migrate();

		verify(configManager, never()).setConfiguration(any(), any(), anyString());
		verify(configManager, never()).unsetConfiguration(any(), any());
	}
}
