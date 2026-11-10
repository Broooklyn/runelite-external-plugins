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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Actor;
import net.runelite.api.AmbientSoundComposition;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.RandomSound;
import net.runelite.api.Varbits;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AmbientSoundChanged;
import net.runelite.api.events.AmbientSoundEffectCreated;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.SoundEffectPlayed;
import static net.runelite.api.gameval.AnimationID.DREAM_PLAYER_HUMIDIFY_SPELL;
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
	// Animations that play when a player teleports
	private static final int NORMAL_TELEPORT_ANIMATION = 714;
	private static final int LUNAR_TELEPORT_ANIMATION = 1816;
	private static final int SCROLL_TELEPORT_ANIMATION = 3864;
	private static final int XERIC_TELEPORT_ANIMATION = 3865;
	private static final int WILDERNESS_TELEPORT_ANIMATION = 3867;
	private static final int CABBAGE_TELEPORT_ANIMATION = 3869;
	private static final int ARDOUGNE_TELEPORT_ANIMATION = 3872;
	private static final int BURGH_TELEPORT_ANIMATION = 3874;

	private static final int[] TELEPORT_ANIMATIONS = {
		NORMAL_TELEPORT_ANIMATION,
		LUNAR_TELEPORT_ANIMATION,
		SCROLL_TELEPORT_ANIMATION,
		XERIC_TELEPORT_ANIMATION,
		WILDERNESS_TELEPORT_ANIMATION,
		CABBAGE_TELEPORT_ANIMATION,
		ARDOUGNE_TELEPORT_ANIMATION,
		BURGH_TELEPORT_ANIMATION
	};

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private AnnoyanceMuteConfig config;

	@VisibleForTesting
	public HashSet<SoundEffect> soundEffects = new HashSet<>();

	// The same effects as soundEffects, grouped by sound id so a sound can be checked with one lookup
	private Map<Integer, List<SoundEffect>> soundEffectsById = new HashMap<>();

	// The user's custom "sounds to mute" list, parsed once per config change
	private Set<String> selectedSounds = Collections.emptySet();

	@Getter(AccessLevel.PUBLIC)
	public HashSet<SoundEffect> ambientSoundsToMute = new HashSet<>();

	// The same effects as ambientSoundsToMute, grouped by sound id
	private Map<Integer, List<AmbientSoundEffect>> ambientSoundsToMuteById = new HashMap<>();

	// Only the ids the user entered in the ambient sound config, these can mute the whole ambient sound (composition)
	private List<AmbientSoundEffect> customAmbientSoundsToMute = new ArrayList<>();

	private final List<net.runelite.api.AmbientSoundEffect> soundsToKeep = new ArrayList<>();

	int currentPlayerRegionId = -1;

	@Provides
	AnnoyanceMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnnoyanceMuteConfig.class);
	}

	@Override
	public void startUp()
	{
		setUpMutes();
		reloadScene();
	}

	@Override
	public void shutDown()
	{
		soundEffects.clear();
		soundEffectsById.clear();
		reloadScene();
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
				case "ambientSoundsToMute":
				case "ambientSoundsToMuteRegional":
					reloadScene();
			}
		}
	}

	// Reload the scene to reapply ambient sounds
	private void reloadScene()
	{
		soundsToKeep.clear();
		clientThread.invoke(() ->
		{
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.setGameState(GameState.LOADING);
			}
		});
	}

	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onAmbientSoundEffectCreated(AmbientSoundEffectCreated ambientSoundEffectCreated)
	{
		WorldView worldView = ambientSoundEffectCreated.getWorldView();
		if (hasNoAmbientMutes() || !worldView.isTopLevel())
			return;

		net.runelite.api.AmbientSoundEffect created = ambientSoundEffectCreated.getAmbientSoundEffect();

		// some ambient sounds are created as -1 with no random sounds, just remove them imo.
		int soundId = created.getSoundEffectId();
		int[] randomSounds = getRandomSounds(created);
		boolean empty = randomSounds == null || randomSounds.length == 0;
		if ((soundId == -1 && empty) || ambientSoundsToMuteById.containsKey(soundId))
		{
			restoreSoundsToKeep();
		}
	}

	// In some areas the server sets a single ambient sound (client.getAmbientSound()) with its own random sounds
	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onAmbientSoundChanged(AmbientSoundChanged ambientSoundChanged)
	{
		muteAmbientSounds();
	}

	private boolean hasNoAmbientMutes()
	{
		return ambientSoundsToMute.isEmpty();
	}

	@Nullable
	private static int[] getRandomSounds(net.runelite.api.AmbientSoundEffect ambientSoundEffect)
	{
		RandomSound randomSound = ambientSoundEffect.getRandomSound();
		return randomSound == null ? null : randomSound.getSoundEffectIds();
	}

	@Subscribe(priority = -2) // priority -2 to run after music plugin
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		// if nothing to mute then return
		if (hasNoAmbientMutes())
		{
			return;
		}

		GameState gameState = gameStateChanged.getGameState();

		// on map load mute ambient sounds
		if (gameState == GameState.LOGGED_IN)
		{
			Player localPlayer = client.getLocalPlayer();
			if (localPlayer != null)
			{
				WorldPoint wp = localPlayer.getWorldLocation();
				currentPlayerRegionId = wp.getRegionID();
			}
			muteAmbientSounds();
		}
	}

	@Subscribe
	public void onGameTick(GameTick e)
	{
		// on first login and on region changes we can mute the ambient sounds that are region based
		// this function might not be necessary tbh
		Player localPlayer = client.getLocalPlayer();
		if (localPlayer != null)
		{
			WorldPoint wp = localPlayer.getWorldLocation();
			if (wp.getRegionID() != currentPlayerRegionId)
			{
				currentPlayerRegionId = wp.getRegionID();
				muteAmbientSounds();
			}
		}
	}

	// Check the ambient sounds currently being played and remove the ones that should be muted. That is both the
	// ambient sound effects and the ambient sound the server set, if there is one
	private void muteAmbientSounds()
	{
		if (hasNoAmbientMutes())
			return;

		soundsToKeep.clear();
		Deque<net.runelite.api.AmbientSoundEffect> ambientSoundEffects = client.getAmbientSoundEffects();

		for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : ambientSoundEffects)
		{
			List<AmbientSoundEffect> mutedAmbientsSameID = ambientSoundsToMuteById
				.getOrDefault(ambientSoundEffect.getSoundEffectId(), Collections.emptyList());
			boolean muteSound = false;
			for (int i = 0; i < mutedAmbientsSameID.size() && !muteSound; i++)
			{
				int[] randomSounds = getRandomSounds(ambientSoundEffect);
				int[] randomSoundsToMatch = mutedAmbientsSameID.get(i).getRandomSoundEffects();

				// mute if id is the same and there are no random sounds
				if (randomSoundsToMatch.length == 0)
				{
					muteSound = true;
				}

				// mute only if the random sounds are similar to a selection
				if (randomSounds != null && randomSoundsToMatch.length != 0 && arraysLikeEnough(randomSounds, randomSoundsToMatch))
				{
					muteSound = true;
				}

				// if ambient sound is configured with a regionid
				int regionId = mutedAmbientsSameID.get(i).getRegionId();
				if (muteSound && regionId != -1)
				{
					// if current player region is not the same location as the defined regionid, don't mute the sound
					if (currentPlayerRegionId != regionId)
					{
						muteSound = false;
						break;
					}
				}
			}
			if (!muteSound)
			{
				soundsToKeep.add(ambientSoundEffect);
			}
		}

		// ambient sounds with a -1 soundid with no other sounds with them can just be removed, it's probably a magic tree.
		if (config.muteMagicTrees())
		{
			soundsToKeep.removeIf(s -> s.getSoundEffectId() == -1
				&& (getRandomSounds(s) == null || getRandomSounds(s).length == 0));
		}

		restoreSoundsToKeep();
	}

	private boolean isAmbientSoundMuted(AmbientSoundComposition ambientSound)
	{
		if (isAnyAmbientSoundIdMuted(ambientSound.getSoundEffectIds()))
		{
			return true;
		}

		List<RandomSound> randomSounds = ambientSound.getRandomSounds();
		if (randomSounds != null)
		{
			for (RandomSound randomSound : randomSounds)
			{
				if (isAnyAmbientSoundIdMuted(randomSound.getSoundEffectIds()))
				{
					return true;
				}
			}
		}
		return false;
	}

	// Only the ids the user entered count, the built in toggles are meant for ambient sound effects and share ids
	// with sounds that are part of other areas
	private boolean isAnyAmbientSoundIdMuted(@Nullable int[] ids)
	{
		if (ids == null)
		{
			return false;
		}

		for (AmbientSoundEffect muted : customAmbientSoundsToMute)
		{
			if ((muted.getRegionId() == -1 || muted.getRegionId() == currentPlayerRegionId)
				&& Arrays.stream(ids).anyMatch(id -> id == muted.getId()))
			{
				return true;
			}
		}
		return false;
	}

	// Clear the game's ambient sounds (mutes all of them), then add back the ones that are not black listed
	private void restoreSoundsToKeep()
	{
		Deque<net.runelite.api.AmbientSoundEffect> ambientSoundEffects = client.getAmbientSoundEffects();
		ambientSoundEffects.clear();
		for (net.runelite.api.AmbientSoundEffect ambientSoundEffect : soundsToKeep)
		{
			ambientSoundEffects.addLast(ambientSoundEffect);
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
			addGeneric(SoundEffectID.ACB_REEEE);
		}
		if (config.muteCannon())
		{
			addGeneric(SoundEffectID.CANNON_SPIN, SoundEffectID.SHATTERED_CANNON_SPIN);
		}
		if (config.muteIceSpells())
		{
			addGeneric(SoundEffectID.ICE_BARRAGE_CAST, SoundEffectID.ICE_BLITZ_CAST, SoundEffectID.ICE_BURST_CAST,
				SoundEffectID.ICE_SPELL_LAND, SoundEffectID.ICE_RUSH_CAST);
		}
		if (config.muteThralls())
		{
			addGeneric(SoundEffectID.WATER_STRIKE_CAST, SoundEffectID.WATER_STRIKE_LAND, SoundEffectID.ZOMBIE_THRALL_ATTACK,
				SoundEffectID.IMP_RANGED_THRALL_ATTACK_1, SoundEffectID.IMP_RANGED_THRALL_ATTACK_2,
				SoundEffectID.IMP_MAGE_THRALL_ATTACK_1, SoundEffectID.IMP_MAGE_THRALL_ATTACK_2, SoundEffectID.IMP_MAGE_THRALL_ATTACK_3,
				SoundEffectID.IMP_MELEE_THRALL_ATTACK_1, SoundEffectID.IMP_MELEE_THRALL_ATTACK_2, SoundEffectID.IMP_MELEE_THRALL_ATTACK_3);
		}

		// ------- NPCs -------
		if (config.muteCaveHorrors())
		{
			addGeneric(SoundEffectID.CAVE_HORROR);
		}
		if (config.muteCows())
		{
			addGeneric(SoundEffectID.MOO_MOO);
		}
		if (config.muteDemons())
		{
			addGeneric(SoundEffectID.GREATER_DEMON_ATTACK, SoundEffectID.GREATER_DEMON_DEATH, SoundEffectID.GREATER_DEMON_PARRY,
				SoundEffectID.DEMON_ATTACK, SoundEffectID.DEMON_DEATH, SoundEffectID.DEMON_PARRY);
		}
		if (config.muteDustDevils())
		{
			addGeneric(SoundEffectID.DUST_DEVIL_ATTACK, SoundEffectID.DUST_DEVIL_DEATH, SoundEffectID.DUST_DEVIL_PARRY);
		}
		if (config.muteWyverns())
		{
			addGeneric(SoundEffectID.FOSSIL_ISLAND_WYVERN_69, SoundEffectID.FOSSIL_ISLAND_WYVERN_71, SoundEffectID.FOSSIL_ISLAND_WYVERN_73);
		}
		if (config.muteJellies())
		{
			addGeneric(SoundEffectID.JELLY_ATTACK, SoundEffectID.JELLY_DEATH, SoundEffectID.JELLY_PARRY);
		}
		if (config.muteNailBeasts())
		{
			addGeneric(SoundEffectID.NAIL_BEAST_ATTACK, SoundEffectID.NAIL_BEAST_DEATH, SoundEffectID.NAIL_BEAST_PARRY);
		}
		if (config.muteNechryael())
		{
			addGeneric(SoundEffectID.NECHRYAEL_ATTACK, SoundEffectID.NECHRYAE_DEATH, SoundEffectID.NECHRYAEL_PARRY);
		}
		if (config.muteNightmare())
		{
			addGeneric(SoundEffectID.NIGHTMARE_SOUND);
		}
		if (config.mutePetSounds())
		{
			addActorCombat(0, SoundEffectID.SNAKELING_METAMORPHOSIS, SoundEffectID.CLOCKWORK_CAT_CLICK_CLICK,
				SoundEffectID.PET_KREEARRA_WING_FLAP, SoundEffectID.ELECTRIC_HYDRA_IN, SoundEffectID.ELECTRIC_HYDRA_OUT,
				SoundEffectID.IKKLE_HYDRA_RIGHT_FOOT_LETS_STOMP, SoundEffectID.IKKLE_HYDRA_LEFT_FOOT_LETS_STOMP,
				SoundEffectID.PET_WALKING_THUMP, SoundEffectID.VETION_JR_RIGHT_FOOT_LETS_STOMP,
				SoundEffectID.VETION_JR_LEFT_FOOT_LETS_STOMP, SoundEffectID.NOON_FLAP_1, SoundEffectID.NOON_FLAP_2,
				SoundEffectID.BEEF_NOISE_1, SoundEffectID.BEEF_NOISE_2, SoundEffectID.GULLIVER_EMOTE_1, SoundEffectID.GULLIVER_EMOTE_2);
		}
		if (config.mutePetSounds() || config.muteRandoms())
		{
			addGeneric(SoundEffectID.CAT_HISS);
		}

		// Applicable to both pet sounds and random event sounds
		if (config.muteRandoms())
		{
			addGeneric(SoundEffectID.NPC_TELEPORT_WOOSH, SoundEffectID.DRUNKEN_DWARF, SoundEffectID.POSTIE_PETE, SoundEffectID.FROG_SPLASH);
		}
		if (config.muteScarabs())
		{
			addGeneric(SoundEffectID.SCARAB_ATTACK_SOUND, SoundEffectID.SCARAB_SPAWN_SOUND);
		}
		if (config.muteSire())
		{
			addGeneric(SoundEffectID.SIRE_SPAWNS, SoundEffectID.SIRE_SPAWNS_DEATH);
		}
		if (config.muteSpectres())
		{
			addGeneric(SoundEffectID.SPECTRE_ATTACK_SHOOT, SoundEffectID.SPECTRE_ATTACK_HIT,
				SoundEffectID.SPECTRE_DEATH, SoundEffectID.SPECTRE_PARRY);
		}
		if (config.muteTekton())
		{
			addGeneric(SoundEffectID.METEOR);
		}
		if (config.muteTownCrierSounds())
		{
			addGeneric(SoundEffectID.TOWN_CRIER_BELL_DING, SoundEffectID.TOWN_CRIER_BELL_DONG, SoundEffectID.TOWN_CRIER_SHOUT_SQUEAK);
		}

		// ------- Skilling -------
		if (config.muteAlchemy())
		{
			addGeneric(SoundEffectID.HIGH_ALCHEMY, SoundEffectID.LOW_ALCHEMY,
				SoundEffectID.TRAIL_BLAZERS_HIGH_ALCHEMY, SoundEffectID.TRAIL_BLAZERS_LOW_ALCHEMY);
		}
		if (config.muteChopChop())
		{
			addGeneric(SoundEffectID.CHOP_CHOP);
		}
		if (config.muteSmashing())
		{
			addGeneric(SoundEffectType.AREA_SOUND_EFFECT, SoundEffectID.SMASHING);
		}
		if (config.muteDenseEssence())
		{
			addGeneric(SoundEffectID.CHISEL);
		}
		if (config.muteFiremaking())
		{
			addGeneric(SoundEffectID.FIREMAKING_LOG_BURN, SoundEffectID.FIREMAKING_LOG_LIGHT);
		}
		if (config.muteFishing())
		{
			addGeneric(SoundEffectID.FISHING_SOUND);
		}
		if (config.muteFletching())
		{
			addGeneric(SoundEffectID.FLETCHING_CUT, SoundEffectID.AMETHYST_FLETCHING);
		}
		if (config.muteAOESounds())
		{
			addGeneric(SoundEffectID.HUMIDIFY_SOUND);
		}
		else if (config.muteOthersHumidify())
		{
			addAnimation(SoundEffectID.HUMIDIFY_SOUND, DREAM_PLAYER_HUMIDIFY_SPELL);
		}
		if (config.mutePickpocket())
		{
			addGeneric(SoundEffectID.PICKPOCKET_PLOP);
		}
		if (config.mutePickpocketStun())
		{
			addGeneric(SoundEffectID.PICKPOCKET_STUN);
		}
		if (config.muteMining())
		{
			addGeneric(SoundEffectID.MINING_PICK_SWING_1, SoundEffectID.MINING_PICK_SWING_2);
		}
		if (config.mutePlankMake())
		{
			addGeneric(SoundEffectID.PLANK_MAKE);
		}
		if (config.muteStringJewellery())
		{
			addGeneric(SoundEffectID.STRING_JEWELLERY);
		}
		if (config.muteWoodcutting())
		{
			addGeneric(SoundEffectID.WOODCUTTING_CHOP);
		}
		if (config.muteChargeOrb())
		{
			addGeneric(SoundEffectID.CHARGE_EARTH_ORB, SoundEffectID.CHARGE_AIR_ORB,
				SoundEffectID.CHARGE_FIRE_ORB, SoundEffectID.CHARGE_WATER_ORB);
		}

		// ------- Prayers -------
		if (config.muteThickSkin())
		{
			addGeneric(SoundEffectID.THICK_SKIN);
		}
		if (config.muteBurstofStrength())
		{
			addGeneric(SoundEffectID.BURST_OF_STRENGTH);
		}
		if (config.muteClarityOfThought())
		{
			addGeneric(SoundEffectID.CLARITY_OF_THOUGHT);
		}
		if (config.muteRockSkin())
		{
			addGeneric(SoundEffectID.ROCK_SKIN);
		}
		if (config.muteSuperhumanStrength())
		{
			addGeneric(SoundEffectID.SUPERHUMAN_STRENGTH);
		}
		if (config.muteImprovedReflexes())
		{
			addGeneric(SoundEffectID.IMPROVED_REFLEXES);
		}
		if (config.muteRapidHeal())
		{
			addGeneric(SoundEffectID.RAPID_HEAL);
		}
		if (config.muteProtectItem())
		{
			addGeneric(SoundEffectID.PROTECT_ITEM);
		}
		if (config.muteHawkEye())
		{
			addGeneric(SoundEffectID.HAWK_EYE);
		}
		if (config.muteMysticLore())
		{
			addGeneric(SoundEffectID.MYSTIC_LORE);
		}
		if (config.muteSteelSkin())
		{
			addGeneric(SoundEffectID.STEEL_SKIN);
		}
		if (config.muteUltimateStrength())
		{
			addGeneric(SoundEffectID.ULTIMATE_STRENGTH);
		}
		if (config.muteIncredibleReflexes())
		{
			addGeneric(SoundEffectID.INCREDIBLE_REFLEXES);
		}
		if (config.muteProtectFromMagic())
		{
			addGeneric(SoundEffectID.PROTECT_FROM_MAGIC);
		}
		if (config.muteProtectFromRange())
		{
			addGeneric(SoundEffectID.PROTECT_FROM_RANGE);
		}
		if (config.muteProtectFromMelee())
		{
			addGeneric(SoundEffectID.PROTECT_FROM_MELEE);
		}
		if (config.muteEagleEye())
		{
			addGeneric(SoundEffectID.EAGLE_EYE);
		}
		if (config.muteMysticMight())
		{
			addGeneric(SoundEffectID.MYSTIC_MIGHT);
		}
		if (config.muteRetribution())
		{
			addGeneric(SoundEffectID.RETRIBUTION);
		}
		if (config.muteRedemption())
		{
			addGeneric(SoundEffectID.REDEMPTION);
		}
		if (config.muteSmite())
		{
			addGeneric(SoundEffectID.SMITE);
		}
		if (config.mutePreserve())
		{
			addGeneric(SoundEffectID.PRESERVE);
		}
		if (config.muteChivalry())
		{
			addGeneric(SoundEffectID.CHIVALRY);
		}
		if (config.mutePiety())
		{
			addGeneric(SoundEffectID.PIETY);
		}
		if (config.muteRigour())
		{
			addGeneric(SoundEffectID.RIGOUR);
		}
		if (config.muteAugury())
		{
			addGeneric(SoundEffectID.AUGURY);
		}
		if (config.muteMysticVigour())
		{
			addGeneric(SoundEffectID.MYSTIC_VIGOUR);
		}
		if (config.muteDeadeye())
		{
			addGeneric(SoundEffectID.DEADEYE);
		}
		if (config.muteDeadeye())
		{
			addGeneric(SoundEffectID.ZEAL);
		}
		if (config.muteDeactivatePrayer())
		{
			addGeneric(SoundEffectID.DEACTIVATE_PRAYER);
		}

		// ------- Miscellaneous -------
		if (config.muteFishingExplosive())
		{
			addGeneric(SoundEffectID.FISHING_EXPLOSIVE);
		}
		if (config.muteHealOther())
		{
			addGeneric(SoundEffectID.HEAL_OTHER_2, SoundEffectID.HEAL_OTHER_3, SoundEffectID.HEAL_OTHER_4, SoundEffectID.HEAL_OTHER_5);
		}
		if (config.muteItemDrop())
		{
			addGeneric(SoundEffectID.ITEM_DROP);
		}
		if (config.muteLevelUp())
		{
			addGeneric(SoundEffectID.LEVEL_UP_1, SoundEffectID.LEVEL_UP_2);
		}
		if (config.muteNPCContact())
		{
			addGeneric(SoundEffectID.NPC_CONTACT);
		}
		if (config.muteSnowballSounds())
		{
			addGeneric(SoundEffectID.SNOWBALL_HIT, SoundEffectID.SNOWBALL_THROW);
		}
		if (config.muteTeleother())
		{
			addGeneric(SoundEffectID.TELEOTHER);
		}
		if (config.muteTeleport() && !config.muteTeleportOthers())
		{
			addGeneric(SoundEffectID.TELEPORT_VWOOP);
		}
		else if (config.muteTeleportOthers())
		{
			// when both are enabled, we only mute the sound when another player is doing it
			addAnimation(SoundEffectID.TELEPORT_VWOOP, TELEPORT_ANIMATIONS);
		}
		if (config.muteRubberChickenSounds())
		{
			addGeneric(SoundEffectID.WHACK);
		}
		if (config.muteObelisk())
		{
			addGeneric(SoundEffectID.WILDY_OBELISK);
		}

		// Ambient Sounds
		ambientSoundsToMute = new HashSet<>();

		if (config.muteMagicTrees())
		{
			addAmbient(SoundEffectID.MAGIC_TREE);
		}
		if (config.muteHousePortal())
		{
			addAmbient(SoundEffectID.HOUSE_PORTAL);
		}
		if (config.muteWhiteNoise())
		{
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_STATIC_1);

			addAmbient(SoundEffectID.STATIC_1, SoundEffectID.STATIC_2, SoundEffectID.STATIC_3, SoundEffectID.STATIC_4, SoundEffectID.STATIC_5);
		}
		if (config.muteChirps())
		{
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_1);
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_2);
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_3);
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_BIRD_4);

			addAmbient(SoundEffectID.CRICKET_1, SoundEffectID.CRICKET_2, SoundEffectID.CRICKET_3,
				SoundEffectID.CRICKET_4, SoundEffectID.CRICKET_5, SoundEffectID.CRICKET_6);
		}
		if (config.muteWater())
		{
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_WATER_1);
			addAmbient(SoundEffectID.COMMON_BACKGROUND_1, SoundEffectID.COMMON_BACKGROUND_2184_WATER_2);

			addAmbient(SoundEffectID.WATER_1, SoundEffectID.WATER_2, SoundEffectID.WATER_3, SoundEffectID.WATER_4,
				SoundEffectID.WATER_5, SoundEffectID.WATER_6, SoundEffectID.WATER_7, SoundEffectID.WATER_8,
				SoundEffectID.WATER_9, SoundEffectID.WATER_10, SoundEffectID.WATER_11, SoundEffectID.WATER_12);
		}
		if (config.muteRanges())
		{
			addAmbient(SoundEffectID.RANGE_1, SoundEffectID.RANGE_2, SoundEffectID.COOKING_POT);
		}
		if (config.muteFortisColosseum())
		{
			addAmbient(SoundEffectID.FORTIS_COLOSSEUM_AMBIENT_1, SoundEffectID.FORTIS_COLOSSEUM_AMBIENT_2, SoundEffectID.FORTIS_COLOSSEUM_FIRE);
		}

		if (config.muteStranglewoodHowls())
		{
			addAmbient(SoundEffectID.COMMON_BACKGROUND_2, SoundEffectID.VARDORVIS_AREA);
		}

		// add the user defined ambient sounds to mute to the list manually
		customAmbientSoundsToMute = new ArrayList<>();
		for (int i : getGenericAmbientSounds())
		{
			AmbientSoundEffect custom = new AmbientSoundEffect(i, SoundEffectType.AMBIENT);
			ambientSoundsToMute.add(custom);
			customAmbientSoundsToMute.add(custom);
		}

		getRegionalAmbientSounds().forEach(entry -> {
			String[] e = entry.split(":");
			int regionId = Integer.parseInt(e[0]);
			int soundId = Integer.parseInt(e[1]);

			AmbientSoundEffect custom = new AmbientSoundEffect(soundId, SoundEffectType.AMBIENT, regionId);
			ambientSoundsToMute.add(custom);
			customAmbientSoundsToMute.add(custom);
		});

		// index everything by sound id so the hot paths don't have to scan every effect
		selectedSounds = new HashSet<>(getSelectedSounds());

		soundEffectsById = new HashMap<>();
		for (SoundEffect soundEffect : soundEffects)
		{
			soundEffectsById.computeIfAbsent(soundEffect.getId(), k -> new ArrayList<>()).add(soundEffect);
		}

		ambientSoundsToMuteById = new HashMap<>();
		for (SoundEffect soundEffect : ambientSoundsToMute)
		{
			if (soundEffect instanceof AmbientSoundEffect)
			{
				ambientSoundsToMuteById.computeIfAbsent(soundEffect.getId(), k -> new ArrayList<>()).add((AmbientSoundEffect) soundEffect);
			}
		}
	}

	private void addGeneric(int... soundIds)
	{
		addGeneric(SoundEffectType.EITHER, soundIds);
	}

	private void addGeneric(SoundEffectType type, int... soundIds)
	{
		for (int soundId : soundIds)
		{
			soundEffects.add(new GenericSoundEffect(soundId, type));
		}
	}

	private void addAnimation(int soundId, int... animationIds)
	{
		for (int animationId : animationIds)
		{
			soundEffects.add(new AnimationSoundEffect(soundId, SoundEffectType.EITHER, animationId));
		}
	}

	private void addActorCombat(int combatLevel, int... soundIds)
	{
		for (int soundId : soundIds)
		{
			soundEffects.add(new ActorCombatSoundEffect(soundId, SoundEffectType.EITHER, combatLevel));
		}
	}

	private void addAmbient(int... soundIds)
	{
		for (int soundId : soundIds)
		{
			ambientSoundsToMute.add(new AmbientSoundEffect(soundId, SoundEffectType.AMBIENT));
		}
	}

	private void addAmbient(int soundId, int[] randomSoundIds)
	{
		ambientSoundsToMute.add(new AmbientSoundEffect(soundId, SoundEffectType.AMBIENT, randomSoundIds));
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
		if (selectedSounds.contains(Integer.toString(soundId)))
		{
			return true;
		}

		List<SoundEffect> candidates = soundEffectsById.get(soundId);
		if (candidates == null)
		{
			return false;
		}

		// sort the effects for this sound into generics, animations, and combat levels
		boolean hasGeneric = false;
		boolean hasActor = false;
		List<AnimationSoundEffect> animationSoundEffects = new ArrayList<>();
		for (SoundEffect s : candidates)
		{
			if (s.getSoundEffectType() != SoundEffectType.EITHER && s.getSoundEffectType() != type)
			{
				continue;
			}

			if (s instanceof GenericSoundEffect)
			{
				hasGeneric = true;
			}
			else if (s instanceof AnimationSoundEffect)
			{
				animationSoundEffects.add((AnimationSoundEffect) s);
			}
			else if (s instanceof ActorCombatSoundEffect
				&& source != null && ((ActorCombatSoundEffect) s).getActorCombatLevel() == source.getCombatLevel())
			{
				hasActor = true;
			}
		}

		if (!hasGeneric && !hasActor && animationSoundEffects.isEmpty())
		{
			return false;
		}
		return hasGeneric && hasActor && animationSoundEffects.stream().anyMatch(s -> s.getAnimationID() == -1)
			|| animationSoundEffects.stream().noneMatch(s -> s.getAnimationID() == client.getLocalPlayer().getAnimation());
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

	public List<Integer> getGenericAmbientSounds()
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

	public List<String> getRegionalAmbientSounds()
	{
		final String configSounds = config.ambientSoundsToMuteRegional().toLowerCase();

		List<String> configValue = Text.fromCSV(configSounds);

		ArrayList<String> returnValues = new ArrayList<>();
		for (String str : configValue)
		{
			String[] parts = str.split(":");
			if (parts.length > 1 && tryParseInt(parts[0]) && tryParseInt(parts[1]))
			{
				returnValues.add(Integer.parseInt(parts[0]) + ":" + Integer.parseInt(parts[1]));
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