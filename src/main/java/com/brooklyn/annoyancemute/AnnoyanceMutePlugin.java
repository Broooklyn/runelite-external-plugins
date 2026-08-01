/*
 * Copyright (c) 2020, Brooklyn <https://github.com/Broooklyn>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.brooklyn.annoyancemute;

import com.brooklyn.annoyancemute.soundeffects.ActorCombatSoundEffect;
import com.brooklyn.annoyancemute.soundeffects.AnimationSoundEffect;
import com.brooklyn.annoyancemute.soundeffects.AmbientSoundEffect;
import com.brooklyn.annoyancemute.soundeffects.SoundEffect;
import com.brooklyn.annoyancemute.soundeffects.GenericSoundEffect;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
import net.runelite.api.events.AmbientSoundEffectCreated;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Annoyance Mute",
	description = "Selectively mute annoying game sounds",
	tags = {"sound", "volume", "mute", "hub", "brooklyn", "pet", "stomp"}
)
public class AnnoyanceMutePlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AnnoyanceMuteConfig config;

	@VisibleForTesting
	public HashSet<SoundEffect> soundEffects = new HashSet<>();

	@Getter(AccessLevel.PUBLIC)
	public HashSet<SoundEffect> ambientSoundsToMute = new HashSet<>();

	ArrayList<net.runelite.api.AmbientSoundEffect> soundsToKeep;

	@Provides
	AnnoyanceMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnnoyanceMuteConfig.class);
	}

	@Override
	public void startUp()
	{
		setUpMutes();
		soundsToKeep = new ArrayList<>();

		clientThread.invoke(() ->
		{
			// Reload the scene to reapply ambient sounds
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Override
	public void shutDown()
	{
		soundEffects.clear();
		soundsToKeep = new ArrayList<>();

		clientThread.invoke(() ->
		{
			// Reload the scene to reapply ambient sounds
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getGroup().equals("annoyancemute"))
		{
			setUpMutes();

			switch (configChanged.getKey())
			{
				case "muteMagicTrees":
				case "muteHousePortal":
				case "muteWhiteNoise":
				case "muteChirps":
				case "muteWater":
				case "muteRanges":
				case "muteFortisColosseum":
				case "muteStranglewoodHowls":
					soundsToKeep = new ArrayList<>();
					clientThread.invoke(() ->
					{
						// Reload the scene to reapply ambient sounds
						if (client.getGameState() == GameState.LOGGED_IN)
						{
							client.setGameState(GameState.LOADING);
						}
					});
			}
		}
	}


	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onAmbientSoundEffectCreated(AmbientSoundEffectCreated ambientSoundEffectCreated)
	{
		if (ambientSoundsToMute.isEmpty())
			return;
		// some ambient sounds are created as -1, just remove them imo.
		if (ambientSoundEffectCreated.getAmbientSoundEffect().getSoundEffectId() == -1)
		{
			client.getAmbientSoundEffects().clear();

			// add the sounds not black listed back in
			for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : soundsToKeep)
			{
				client.getAmbientSoundEffects().addLast(ambientSoundEffect);
			}
		}
		if (ambientSoundsToMute.stream().anyMatch(s -> s.getId() ==  ambientSoundEffectCreated.getAmbientSoundEffect().getSoundEffectId()))
		{
			client.getAmbientSoundEffects().clear();

			// add the sounds not black listed back in
			for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : soundsToKeep)
			{
				client.getAmbientSoundEffects().addLast(ambientSoundEffect);
			}
		}
	}

	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// if nothing to mute then return
		if (ambientSoundsToMute.isEmpty())
		{
			return;
		}

		GameState gameState = gameStateChanged.getGameState();

		// on map load mute ambient sounds
		if (gameState == GameState.LOGGED_IN)
		{
			soundsToKeep = new ArrayList<>();
			muteAmbientSounds();
		}
	}

	// Check the ambient sounds currently being played and remove the ones that should be muted
	private void muteAmbientSounds()
	{
		if (ambientSoundsToMute.isEmpty())
			return;
		Deque<net.runelite.api.AmbientSoundEffect> ambientSoundEffects = client.getAmbientSoundEffects();

		for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : ambientSoundEffects)
		{
			List<AmbientSoundEffect> mutedAmbientsSameID = ambientSoundsToMute.stream().filter(AmbientSoundEffect.class::isInstance)
				.map(AmbientSoundEffect.class::cast).filter(mutedSounds -> mutedSounds.getId() == ambientSoundEffect.getSoundEffectId()).collect(Collectors.toList());
			boolean muteSound = false;
			for (int i = 0; i < mutedAmbientsSameID.size() && !muteSound; i++)
			{
				int[] backgroundSounds = ambientSoundEffect.getBackgroundSoundEffectIds();
				int[] backgroundSoundsToMute = mutedAmbientsSameID.get(i).getBackgroundSoundEffects();

				if (backgroundSounds == null && backgroundSoundsToMute.length == 0)
				{
					muteSound = true;
				}
				if (backgroundSounds != null && backgroundSoundsToMute.length == 0)
				{
					muteSound = true;
				}
				if (backgroundSounds != null && backgroundSoundsToMute.length != 0 && arraysLikeEnough(backgroundSounds, backgroundSoundsToMute))
				{
					muteSound = true;
				}
			}
			if (!muteSound)// && !soundsToKeep.contains(ambientSoundEffect))
			{
				soundsToKeep.add(ambientSoundEffect);
			}
		}
		// clear the deque (mutes all sounds)
		client.getAmbientSoundEffects().clear();

		// add the sounds not black listed back in
		for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : soundsToKeep)
		{
			client.getAmbientSoundEffects().addLast(ambientSoundEffect);
		}
	}

	// i can't be asked to find every single instance of 2184's ambient sound and all unique arrays
	// so let's find similar ones and include them as mutes
	private boolean arraysLikeEnough(int[] array1, int[] array2)
	{
		int total = 0;
		int totalSimilar = 0;

		for (int int1 : array1)
		{
			total++;
			for (int int2 : array2)
			{
				if (int1 == int2)
				{
					totalSimilar++;
				}
			}
		}

		return ((double) totalSimilar / total > 0.75);
	}

	@VisibleForTesting
	public void setUpMutes()
	{
		soundEffects = new HashSet<>();

		// this
		if (config.muteREEEE())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ACB_REEEE, SoundEffectType.EITHER));
		}
		if (config.muteCannon())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CANNON_SPIN, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SHATTERED_CANNON_SPIN, SoundEffectType.EITHER));
		}
		if (config.muteIceSpells())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ICE_BARRAGE_CAST, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ICE_BLITZ_CAST, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ICE_BURST_CAST, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ICE_SPELL_LAND, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ICE_RUSH_CAST, SoundEffectType.EITHER));
		}
		if (config.muteThralls())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.WATER_STRIKE_CAST, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.WATER_STRIKE_LAND, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ZOMBIE_THRALL_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_RANGED_THRALL_ATTACK_1, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_RANGED_THRALL_ATTACK_2, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MAGE_THRALL_ATTACK_1, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MAGE_THRALL_ATTACK_2, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MAGE_THRALL_ATTACK_3, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MELEE_THRALL_ATTACK_1, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MELEE_THRALL_ATTACK_2, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMP_MELEE_THRALL_ATTACK_3, SoundEffectType.EITHER));
		}

		// ------- NPCs -------
		if (config.muteCaveHorrors())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CAVE_HORROR, SoundEffectType.EITHER));
		}
		if (config.muteCows())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MOO_MOO, SoundEffectType.EITHER));
		}
		if (config.muteDemons())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.GREATER_DEMON_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.GREATER_DEMON_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.GREATER_DEMON_PARRY, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DEMON_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DEMON_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DEMON_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteDustDevils())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DUST_DEVIL_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DUST_DEVIL_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DUST_DEVIL_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteWyverns())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_69, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_71, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_73, SoundEffectType.EITHER));
		}
		if (config.muteJellies())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.JELLY_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.JELLY_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.JELLY_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteNailBeasts())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NAIL_BEAST_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NAIL_BEAST_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NAIL_BEAST_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteNechryael())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NECHRYAEL_ATTACK, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NECHRYAE_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NECHRYAEL_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteNightmare())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NIGHTMARE_SOUND, SoundEffectType.EITHER));
		}
		if (config.mutePetSounds())
		{
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.SNAKELING_METAMORPHOSIS, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.CLOCKWORK_CAT_CLICK_CLICK, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.PET_KREEARRA_WING_FLAP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.ELECTRIC_HYDRA_IN, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.ELECTRIC_HYDRA_OUT, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.IKKLE_HYDRA_RIGHT_FOOT_LETS_STOMP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.IKKLE_HYDRA_LEFT_FOOT_LETS_STOMP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.PET_WALKING_THUMP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.VETION_JR_RIGHT_FOOT_LETS_STOMP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.VETION_JR_LEFT_FOOT_LETS_STOMP, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.NOON_FLAP_1, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.NOON_FLAP_2, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.BEEF_NOISE_1, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.BEEF_NOISE_2, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.GULLIVER_EMOTE_1, SoundEffectType.EITHER, 0));
			soundEffects.add(new ActorCombatSoundEffect(SoundEffectID.GULLIVER_EMOTE_2, SoundEffectType.EITHER, 0));
		}
		if (config.mutePetSounds() || config.muteRandoms())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CAT_HISS, SoundEffectType.EITHER));
		}

		// Applicable to both pet sounds and random event sounds
		if (config.muteRandoms())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NPC_TELEPORT_WOOSH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DRUNKEN_DWARF, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.POSTIE_PETE, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FROG_SPLASH, SoundEffectType.EITHER));
		}
		if (config.muteScarabs())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SCARAB_ATTACK_SOUND, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SCARAB_SPAWN_SOUND, SoundEffectType.EITHER));
		}
		if (config.muteSire())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SIRE_SPAWNS, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SIRE_SPAWNS_DEATH, SoundEffectType.EITHER));
		}
		if (config.muteSpectres())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SPECTRE_ATTACK_SHOOT, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SPECTRE_ATTACK_HIT, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SPECTRE_DEATH, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SPECTRE_PARRY, SoundEffectType.EITHER));
		}
		if (config.muteTekton())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.METEOR, SoundEffectType.EITHER));
		}
		if (config.muteTownCrierSounds())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TOWN_CRIER_BELL_DING, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TOWN_CRIER_BELL_DONG, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TOWN_CRIER_SHOUT_SQUEAK, SoundEffectType.EITHER));
		}

		// ------- Skilling -------
		if (config.muteAlchemy())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HIGH_ALCHEMY, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.LOW_ALCHEMY, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TRAIL_BLAZERS_HIGH_ALCHEMY, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TRAIL_BLAZERS_LOW_ALCHEMY, SoundEffectType.EITHER));
		}
		if (config.muteChopChop())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHOP_CHOP, SoundEffectType.EITHER));
		}
		if (config.muteSmashing())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SMASHING, SoundEffectType.AREA_SOUND_EFFECT));
		}
		if (config.muteDenseEssence())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHISEL, SoundEffectType.EITHER));
		}
		if (config.muteFiremaking())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FIREMAKING_LOG_BURN, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FIREMAKING_LOG_LIGHT, SoundEffectType.EITHER));
		}
		if (config.muteFishing())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FISHING_SOUND, SoundEffectType.EITHER));
		}
		if (config.muteFletching())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FLETCHING_CUT, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.AMETHYST_FLETCHING, SoundEffectType.EITHER));
		}
		if (config.muteAOESounds())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HUMIDIFY_SOUND, SoundEffectType.EITHER));
		}
		else if (config.muteOthersHumidify())
		{
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.HUMIDIFY_SOUND, SoundEffectType.EITHER, 6294));
		}
		if (config.mutePickpocket())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PICKPOCKET_PLOP, SoundEffectType.EITHER));
		}
		if (config.mutePickpocketStun())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PICKPOCKET_STUN, SoundEffectType.EITHER));
		}
		if (config.muteMining())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MINING_PICK_SWING_1, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MINING_PICK_SWING_2, SoundEffectType.EITHER));
		}
		if (config.mutePlankMake())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PLANK_MAKE, SoundEffectType.EITHER));
		}
		if (config.muteStringJewellery())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.STRING_JEWELLERY, SoundEffectType.EITHER));
		}
		if (config.muteWoodcutting())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.EITHER));
		}
		if (config.muteChargeOrb())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHARGE_EARTH_ORB, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHARGE_AIR_ORB, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHARGE_FIRE_ORB, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHARGE_WATER_ORB, SoundEffectType.EITHER));
		}
		// ------- Prayers -------
		if (config.muteThickSkin())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.THICK_SKIN, SoundEffectType.EITHER));
		}
		if (config.muteBurstofStrength())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.BURST_OF_STRENGTH, SoundEffectType.EITHER));
		}
		if (config.muteClarityOfThought())
		{

			soundEffects.add(new GenericSoundEffect(SoundEffectID.CLARITY_OF_THOUGHT, SoundEffectType.EITHER));
		}
		if (config.muteRockSkin())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ROCK_SKIN, SoundEffectType.EITHER));
		}
		if (config.muteSuperhumanStrength())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SUPERHUMAN_STRENGTH, SoundEffectType.EITHER));
		}
		if (config.muteImprovedReflexes())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.IMPROVED_REFLEXES, SoundEffectType.EITHER));
		}
		if (config.muteRapidHeal())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.RAPID_HEAL, SoundEffectType.EITHER));
		}
		if (config.muteProtectItem())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PROTECT_ITEM, SoundEffectType.EITHER));
		}
		if (config.muteHawkEye())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HAWK_EYE, SoundEffectType.EITHER));
		}
		if (config.muteMysticLore())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MYSTIC_LORE, SoundEffectType.EITHER));
		}
		if (config.muteSteelSkin())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.STEEL_SKIN, SoundEffectType.EITHER));
		}
		if (config.muteUltimateStrength())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ULTIMATE_STRENGTH, SoundEffectType.EITHER));
		}
		if (config.muteIncredibleReflexes())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.INCREDIBLE_REFLEXES, SoundEffectType.EITHER));
		}
		if (config.muteProtectFromMagic())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PROTECT_FROM_MAGIC, SoundEffectType.EITHER));
		}
		if (config.muteProtectFromRange())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PROTECT_FROM_RANGE, SoundEffectType.EITHER));
		}
		if (config.muteProtectFromMelee())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PROTECT_FROM_MELEE, SoundEffectType.EITHER));
		}
		if (config.muteEagleEye())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.EAGLE_EYE, SoundEffectType.EITHER));
		}
		if (config.muteMysticMight())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MYSTIC_MIGHT, SoundEffectType.EITHER));
		}
		if (config.muteRetribution())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.RETRIBUTION, SoundEffectType.EITHER));
		}
		if (config.muteRedemption())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.REDEMPTION, SoundEffectType.EITHER));
		}
		if (config.muteSmite())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SMITE, SoundEffectType.EITHER));
		}
		if (config.mutePreserve())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PRESERVE, SoundEffectType.EITHER));
		}
		if (config.muteChivalry())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.CHIVALRY, SoundEffectType.EITHER));
		}
		if (config.mutePiety())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.PIETY, SoundEffectType.EITHER));
		}
		if (config.muteRigour())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.RIGOUR, SoundEffectType.EITHER));
		}
		if (config.muteAugury())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.AUGURY, SoundEffectType.EITHER));
		}
		if (config.muteMysticVigour())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.MYSTIC_VIGOUR, SoundEffectType.EITHER));
		}
		if (config.muteDeadeye())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DEADEYE, SoundEffectType.EITHER));
		}
		if (config.muteDeactivatePrayer())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.DEACTIVATE_PRAYER, SoundEffectType.EITHER));
		}

		// ------- Miscellaneous -------
		if (config.muteFishingExplosive())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.FISHING_EXPLOSIVE, SoundEffectType.EITHER));
		}
		if (config.muteHealOther())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HEAL_OTHER_2, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HEAL_OTHER_3, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HEAL_OTHER_4, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.HEAL_OTHER_5, SoundEffectType.EITHER));
		}
		if (config.muteItemDrop())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.ITEM_DROP, SoundEffectType.EITHER));
		}
		if (config.muteLevelUp())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.LEVEL_UP_1, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.LEVEL_UP_2, SoundEffectType.EITHER));
		}
		if (config.muteNPCContact())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.NPC_CONTACT, SoundEffectType.EITHER));
		}
		if (config.muteSnowballSounds())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SNOWBALL_HIT, SoundEffectType.EITHER));
			soundEffects.add(new GenericSoundEffect(SoundEffectID.SNOWBALL_THROW, SoundEffectType.EITHER));
		}
		if (config.muteTeleother())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.TELEOTHER, SoundEffectType.EITHER));
		}
		if (config.muteTeleport())
		{
			if (config.muteTeleportOthers())
			{
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 714)); // Normal
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 1816)); // Lunar
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3864)); // Scroll
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3865)); // Xeric
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3867)); // Wilderness
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3869)); // Cabbage
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3872)); // Ardougne
				soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3874)); // Burgh
			}
			else
			{
				soundEffects.add(new GenericSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER));
			}
		}
		else if (config.muteTeleportOthers())
		{
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 714)); // Normal
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 1816)); // Lunar
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3864)); // Scroll
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3865)); // Xeric
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3867)); // Wilderness
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3869)); // Cabbage
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3872)); // Ardougne
			soundEffects.add(new AnimationSoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.EITHER, 3874)); // Burgh
		}
		if (config.muteRubberChickenSounds())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.WHACK, SoundEffectType.EITHER));
		}
		if (config.muteObelisk())
		{
			soundEffects.add(new GenericSoundEffect(SoundEffectID.WILDY_OBELISK, SoundEffectType.EITHER));
		}

		// Ambient Sounds
		ambientSoundsToMute = new HashSet<>();

		if (config.muteMagicTrees())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.MAGIC_TREE, SoundEffectType.AMBIENT));
		}
		if (config.muteHousePortal())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.HOUSE_PORTAL, SoundEffectType.AMBIENT));
		}
		if (config.muteWhiteNoise())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_STATIC_1));

			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.STATIC_1, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.STATIC_2, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.STATIC_3, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.STATIC_4, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.STATIC_5, SoundEffectType.AMBIENT));
		}
		if (config.muteChirps())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_1));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_2));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_3));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_4));

			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_1, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_2, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_3, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_4, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_5, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.CRICKET_6, SoundEffectType.AMBIENT));
		}
		if (config.muteWater())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_WATER_1));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectType.AMBIENT, SoundEffectID.COMMON_BACKGROUND_2184_WATER_2));

			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_1, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_2, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_3, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_4, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_5, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_6, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_7, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_8, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_9, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_10, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_11, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.WATER_12, SoundEffectType.AMBIENT));
		}
		if (config.muteRanges())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.RANGE_1, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.RANGE_2, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COOKING_POT, SoundEffectType.AMBIENT));
		}
		if (config.muteFortisColosseum())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.FORTIS_COLOSSEUM_AMBIENT_1, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.FORTIS_COLOSSEUM_AMBIENT_2, SoundEffectType.AMBIENT));
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.FORTIS_COLOSSEUM_FIRE, SoundEffectType.AMBIENT));
		}

		if (config.muteStranglewoodHowls())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(SoundEffectID.COMMON_BACKGROUND_2, SoundEffectType.AMBIENT, SoundEffectID.VARDORVIS_AREA));
		}

		// add the user defined ambient sounds to mute to the list manually
		for (int i : getSelectedAmbientSounds())
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(i, SoundEffectType.AMBIENT));
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed areaSoundEffectPlayed)
	{
		Actor source = areaSoundEffectPlayed.getSource();
		int soundId = areaSoundEffectPlayed.getSoundId();
		if (source != client.getLocalPlayer() && source instanceof Player)
		{
			if (config.muteOthersAreaSounds())
			{
				areaSoundEffectPlayed.consume();
			}
			else if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT, source))
			{
				areaSoundEffectPlayed.consume();
			}
		}
		else if (source == null)
		{
			if (soundId == SoundEffectID.PET_WALKING_THUMP && client.getVarbitValue(Varbits.IN_RAID) == 1)
			{
				return;
			}
			if (soundId == SoundEffectID.SKELETON_THRALL_ATTACK && config.muteThralls())
			{
				areaSoundEffectPlayed.consume();
			}
			if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT, null))
			{
				areaSoundEffectPlayed.consume();
			}
		}
		else if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT, source))
		{
			areaSoundEffectPlayed.consume();
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed)
	{
		int soundId = soundEffectPlayed.getSoundId();
		if (shouldMute(soundId, SoundEffectType.SOUND_EFFECT, soundEffectPlayed.getSource()))
		{
			soundEffectPlayed.consume();
		}
	}

	@VisibleForTesting
	public boolean shouldMute(int soundId, SoundEffectType type, @Nullable Actor source)
	{
		if (getSelectedSounds().contains(Integer.toString(soundId)))
		{
			return true;
		}

		// filter to generics
		List<SoundEffect> genericSoundEffects = soundEffects.stream()
			.filter(GenericSoundEffect.class::isInstance)
			.map(GenericSoundEffect.class::cast)
			.filter(s -> s.getId() == soundId
				&& (s.getSoundEffectType() == SoundEffectType.EITHER || s.getSoundEffectType() == type))
			.collect(Collectors.toCollection(ArrayList::new));

		// filter to animations
		List<SoundEffect> animationSoundEffects = soundEffects.stream()
			.filter(AnimationSoundEffect.class::isInstance)
			.map(AnimationSoundEffect.class::cast)
			.filter(s -> s.getId() == soundId
				&& (s.getSoundEffectType() == SoundEffectType.EITHER || s.getSoundEffectType() == type))
			.collect(Collectors.toCollection(ArrayList::new));

		// filter to combat levels
		List<SoundEffect> actorSoundEffects = soundEffects.stream()
			.filter(ActorCombatSoundEffect.class::isInstance)
			.map(ActorCombatSoundEffect.class::cast)
			.filter(s -> s.getId() == soundId
				&& (s.getSoundEffectType() == SoundEffectType.EITHER || s.getSoundEffectType() == type)
				&& (source != null && s.getActorCombatLevel() == source.getCombatLevel()))
			.collect(Collectors.toCollection(ArrayList::new));

		List<SoundEffect> combinedList = new ArrayList<>();
		combinedList.addAll(genericSoundEffects);
		combinedList.addAll(animationSoundEffects);
		combinedList.addAll(actorSoundEffects);

		if (combinedList.isEmpty())
		{
			return false;
		}
		return !genericSoundEffects.isEmpty()
			&& !actorSoundEffects.isEmpty()
			&& animationSoundEffects.stream().filter(AnimationSoundEffect.class::isInstance)
			.map(AnimationSoundEffect.class::cast).anyMatch(s -> s.getAnimationID() == -1) ||
			animationSoundEffects.stream().filter(AnimationSoundEffect.class::isInstance)
				.map(AnimationSoundEffect.class::cast).noneMatch(s -> s.getAnimationID() == client.getLocalPlayer().getAnimation());
	}

	public List<String> getSelectedSounds()
	{
		final String configSounds = config.soundsToMute().toLowerCase();

		if (configSounds.isEmpty())
		{
			return Collections.emptyList();
		}

		return Text.fromCSV(configSounds);
	}

	public List<Integer> getSelectedAmbientSounds()
	{
		final String configSounds = config.ambientSoundsToMute().toLowerCase();

		List<String> configValue = Text.fromCSV(configSounds);

		ArrayList<Integer> returnValues = new ArrayList<>();
		for (String str : configValue)
		{
			if (tryParseInt(str))
			{
				returnValues.add(Integer.parseInt(str));
			}
		}

		return returnValues;
	}

	private boolean tryParseInt(String value)
	{
		try
		{
			Integer.parseInt(value);
			return true;
		}
		catch (NumberFormatException e)
		{
			return false;
		}
	}
}