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
import static org.mockito.ArgumentMatchers.eq;
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
		when(config.muteWoodcuttingMode()).thenReturn(MuteMode.ALL);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("annoyancemute");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.EITHER, null));
	}

	@Test
	public void muteWoodcuttingOnlyOthers()
	{
		Player localPlayer = mock(Player.class);
		Player otherPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(config.muteWoodcuttingMode()).thenReturn(MuteMode.ONLY_OTHERS);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("muteWoodcutting");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.AREA_SOUND_EFFECT, otherPlayer));
		assertFalse(plugin.shouldMute(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.SOUND_EFFECT, localPlayer));
		// the source is sometimes missing when another player does it
		assertTrue(plugin.shouldMute(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.SOUND_EFFECT, null));
	}

	@Test
	public void muteFishingAllMutesLocalPlayerAndOthers()
	{
		Player localPlayer = mock(Player.class);
		Player otherPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(config.muteFishingMode()).thenReturn(MuteMode.ALL);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("muteFishing");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.SOUND_EFFECT, localPlayer));
		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.AREA_SOUND_EFFECT, otherPlayer));
		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.AREA_SOUND_EFFECT, null));
	}

	@Test
	public void muteFishing()
	{
		when(config.muteFishingMode()).thenReturn(MuteMode.ALL);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("muteFishing");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.EITHER, null));
	}

	@Test
	public void muteFishingOnlyOthers()
	{
		Player localPlayer = mock(Player.class);
		Player otherPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(config.muteFishingMode()).thenReturn(MuteMode.ONLY_OTHERS);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("muteFishing");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.AREA_SOUND_EFFECT, otherPlayer));
		assertFalse(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.SOUND_EFFECT, localPlayer));
		// the source is sometimes missing when another player does it
		assertTrue(plugin.shouldMute(SoundEffectID.FISHING_SOUND, SoundEffectType.SOUND_EFFECT, null));
	}

	@Test
	public void muteMiningOnlyOthers()
	{
		Player localPlayer = mock(Player.class);
		Player otherPlayer = mock(Player.class);
		when(client.getLocalPlayer()).thenReturn(localPlayer);
		when(config.muteMiningMode()).thenReturn(MuteMode.ONLY_OTHERS);

		ConfigChanged configChanged = new ConfigChanged();
		configChanged.setGroup("annoyancemute");
		configChanged.setKey("muteMining");
		plugin.onConfigChanged(configChanged);

		assertTrue(plugin.shouldMute(SoundEffectID.MINING_PICK_SWING_1, SoundEffectType.AREA_SOUND_EFFECT, otherPlayer));
		assertTrue(plugin.shouldMute(SoundEffectID.MINING_PICK_SWING_2, SoundEffectType.AREA_SOUND_EFFECT, otherPlayer));
		assertFalse(plugin.shouldMute(SoundEffectID.MINING_PICK_SWING_1, SoundEffectType.SOUND_EFFECT, localPlayer));
		// the source is sometimes missing when another player does it
		assertTrue(plugin.shouldMute(SoundEffectID.MINING_PICK_SWING_1, SoundEffectType.SOUND_EFFECT, null));
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
		when(config.muteTeleportMode()).thenReturn(MuteMode.ONLY_OTHERS);

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
		when(config.muteTeleportMode()).thenReturn(MuteMode.ONLY_OTHERS);

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
		when(config.muteTeleportMode()).thenReturn(MuteMode.ALL);

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
		when(config.muteTeleportMode()).thenReturn(MuteMode.ALL);

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
		when(config.muteTeleportMode()).thenReturn(MuteMode.OFF);

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
	public void mutedSoundsAcceptWildcardNames()
	{
		when(config.soundsToMute()).thenReturn("Stone_Armour*");
		configChanged();
		// stone_armour1..5
		for (int id : new int[]{31, 32, 33, 2846, 2847})
		{
			assertTrue(plugin.shouldMute(id, SoundEffectType.EITHER, null));
		}
		assertFalse(plugin.shouldMute(353, SoundEffectType.EITHER, null));

		// a lone * or an unknown prefix matches nothing
		when(config.soundsToMute()).thenReturn("*, not_a_real_sound*");
		configChanged();
		assertFalse(plugin.shouldMute(31, SoundEffectType.EITHER, null));
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

	private void legacyConfig(String key, String value)
	{
		when(configManager.getConfiguration("annoyancemute", key)).thenReturn(value);
	}

	private void verifyMigrated(String newKey, MuteMode mode, String... oldKeys)
	{
		verify(configManager).setConfiguration("annoyancemute", newKey, mode);
		for (String oldKey : oldKeys)
		{
			verify(configManager).unsetConfiguration("annoyancemute", oldKey);
		}
	}

	@Test
	public void migrateTeleportOthersEnabledMutesOnlyOthers()
	{
		legacyConfig("muteTeleport", "true");
		legacyConfig("muteTeleportOthers", "true");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.ONLY_OTHERS, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportOthersEnabledWithAllDisabledMutesOnlyOthers()
	{
		legacyConfig("muteTeleport", "false");
		legacyConfig("muteTeleportOthers", "true");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.ONLY_OTHERS, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportAllMutesAll()
	{
		legacyConfig("muteTeleport", "true");
		legacyConfig("muteTeleportOthers", "false");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.ALL, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportNeitherIsOff()
	{
		legacyConfig("muteTeleport", "false");
		legacyConfig("muteTeleportOthers", "false");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.OFF, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportOnlyAllChangedUsesOldOthersDefault()
	{
		// others was never changed, so it is still on
		legacyConfig("muteTeleport", "true");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.ONLY_OTHERS, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportOnlyOthersTurnedOffIsOff()
	{
		// teleport was never changed, so it is still off
		legacyConfig("muteTeleportOthers", "false");

		plugin.migrate();

		verifyMigrated("muteTeleportMode", MuteMode.OFF, "muteTeleport", "muteTeleportOthers");
	}

	@Test
	public void migrateTeleportDoesNothingWhenNeverChanged()
	{
		plugin.migrate();

		verify(configManager, never()).setConfiguration(any(), eq("muteTeleportMode"), any());
		verify(configManager, never()).unsetConfiguration(any(), eq("muteTeleport"));
		verify(configManager, never()).unsetConfiguration(any(), eq("muteTeleportOthers"));
	}

	@Test
	public void migrateHumidifyAllMutesAll()
	{
		legacyConfig("muteAreaOfEffectSpells", "true");
		legacyConfig("muteOthersHumidfy", "true");

		plugin.migrate();

		verifyMigrated("muteHumidifyMode", MuteMode.ALL, "muteAreaOfEffectSpells", "muteOthersHumidfy");
	}

	@Test
	public void migrateHumidifyOthersOnlyMutesOnlyOthers()
	{
		legacyConfig("muteAreaOfEffectSpells", "false");
		legacyConfig("muteOthersHumidfy", "true");

		plugin.migrate();

		verifyMigrated("muteHumidifyMode", MuteMode.ONLY_OTHERS, "muteAreaOfEffectSpells", "muteOthersHumidfy");
	}

	@Test
	public void migrateHumidifyNeitherIsOff()
	{
		legacyConfig("muteAreaOfEffectSpells", "false");
		legacyConfig("muteOthersHumidfy", "false");

		plugin.migrate();

		verifyMigrated("muteHumidifyMode", MuteMode.OFF, "muteAreaOfEffectSpells", "muteOthersHumidfy");
	}

	@Test
	public void migrateHumidifyOnlyAllTurnedOffUsesOldOthersDefault()
	{
		// others was never changed, so it is still on
		legacyConfig("muteAreaOfEffectSpells", "false");

		plugin.migrate();

		verifyMigrated("muteHumidifyMode", MuteMode.ONLY_OTHERS, "muteAreaOfEffectSpells", "muteOthersHumidfy");
	}

	@Test
	public void migrateHumidifyOnlyOthersTurnedOffMutesAll()
	{
		// humidify was never changed, so it is still on
		legacyConfig("muteOthersHumidfy", "false");

		plugin.migrate();

		verifyMigrated("muteHumidifyMode", MuteMode.ALL, "muteAreaOfEffectSpells", "muteOthersHumidfy");
	}

	@Test
	public void migrateHumidifyDoesNothingWhenNeverChanged()
	{
		plugin.migrate();

		verify(configManager, never()).setConfiguration(any(), eq("muteHumidifyMode"), any());
		verify(configManager, never()).unsetConfiguration(any(), eq("muteAreaOfEffectSpells"));
		verify(configManager, never()).unsetConfiguration(any(), eq("muteOthersHumidfy"));
	}

	@Test
	public void migrateMiningAndWoodcuttingFromCheckbox()
	{
		legacyConfig("muteMining", "true");
		legacyConfig("muteWoodcutting", "false");
		legacyConfig("muteFishing", "false");

		plugin.migrate();

		verifyMigrated("muteMiningMode", MuteMode.ALL, "muteMining");
		verifyMigrated("muteWoodcuttingMode", MuteMode.OFF, "muteWoodcutting");
		verifyMigrated("muteFishingMode", MuteMode.OFF, "muteFishing");
	}

	@Test
	public void migrateMiningAndWoodcuttingDoesNothingWithoutOldValues()
	{
		plugin.migrate();

		verify(configManager, never()).setConfiguration(any(), eq("muteMiningMode"), any());
		verify(configManager, never()).setConfiguration(any(), eq("muteWoodcuttingMode"), any());
		verify(configManager, never()).setConfiguration(any(), eq("muteFishingMode"), any());
		verify(configManager, never()).unsetConfiguration(any(), eq("muteFishing"));
		verify(configManager, never()).unsetConfiguration(any(), eq("muteMining"));
		verify(configManager, never()).unsetConfiguration(any(), eq("muteWoodcutting"));
	}
}
