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

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.AmbientSoundEffect;
import net.runelite.api.Client;
import net.runelite.api.Deque;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.Varbits;
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

	private HashSet<Integer> ambientSoundsToMute = new HashSet<>();

	@Provides
	AnnoyanceMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnnoyanceMuteConfig.class);
	}

	@Override
	public void startUp()
	{
		setUpMutes();
	}

	@Override
	public void shutDown()
	{
		soundEffects.clear();

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
				case "muteWhiteNoise":
				case "muteChirps":
				case "muteWater":
				case "muteRanges":
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
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		GameState gameState = gameStateChanged.getGameState();

		if (gameState == GameState.LOGGED_IN)
		{
			// if nothing to mute then return
			if (ambientSoundsToMute.isEmpty())
			{
				return;
			}

			Deque<AmbientSoundEffect> ambientSoundEffects = client.getAmbientSoundEffects();
			ArrayList<AmbientSoundEffect> soundsToKeep = new ArrayList<>();

			for (AmbientSoundEffect ambientSoundEffect : ambientSoundEffects)
			{
				if (!ambientSoundsToMute.contains(ambientSoundEffect.getSoundEffectId()))
				{
					soundsToKeep.add(ambientSoundEffect);
				}
			}

			// clear the deque (mutes all sounds)
			client.getAmbientSoundEffects().clear();

			// add the sounds not black listed back in
			for (AmbientSoundEffect ambientSoundEffect: soundsToKeep)
			{
				client.getAmbientSoundEffects().addLast(ambientSoundEffect);
			}
		}
	}

	private void setUpMutes()
	{
		soundEffects = new HashSet<>();

		if (config.muteREEEE())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.ACB_REEEE, SoundEffectType.Either));
		}
		if (config.muteCannon())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CANNON_SPIN, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SHATTERED_CANNON_SPIN, SoundEffectType.Either));
		}
		if (config.muteIceSpells())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.ICE_BARRAGE_CAST, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ICE_BLITZ_CAST, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ICE_BURST_CAST, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ICE_SPELL_LAND, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ICE_RUSH_CAST, SoundEffectType.Either));
		}
		if (config.muteThralls())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.WATER_STRIKE_CAST, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.WATER_STRIKE_LAND, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ZOMBIE_THRALL_ATTACK, SoundEffectType.Either));
		}

		// ------- NPCs -------
		if (config.muteCaveHorrors())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CAVE_HORROR, SoundEffectType.Either));
		}
		if (config.muteCows())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.MOO_MOO, SoundEffectType.Either));
		}
		if (config.muteDemons())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.GREATER_DEMON_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.GREATER_DEMON_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.GREATER_DEMON_PARRY, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DEMON_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DEMON_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DEMON_PARRY, SoundEffectType.Either));
		}
		if (config.muteDustDevils())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.DUST_DEVIL_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DUST_DEVIL_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DUST_DEVIL_PARRY, SoundEffectType.Either));
		}
		if (config.muteWyverns())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_69, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_71, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.FOSSIL_ISLAND_WYVERN_73, SoundEffectType.Either));
		}
		if (config.muteJellies())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.JELLY_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.JELLY_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.JELLY_PARRY, SoundEffectType.Either));
		}
		if (config.muteNailBeasts())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.NAIL_BEAST_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.NAIL_BEAST_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.NAIL_BEAST_PARRY, SoundEffectType.Either));
		}
		if (config.muteNechryael())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.NECHRYAEL_ATTACK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.NECHRYAE_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.NECHRYAEL_PARRY, SoundEffectType.Either));
		}
		if (config.muteNightmare())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.NIGHTMARE_SOUND, SoundEffectType.Either));
		}
		if (config.mutePetSounds())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SNAKELING_METAMORPHOSIS, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.CLOCKWORK_CAT_CLICK_CLICK, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.PET_KREEARRA_WING_FLAP, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ELECTRIC_HYDRA_IN, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.ELECTRIC_HYDRA_OUT, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.IKKLE_HYDRA_RIGHT_FOOT_LETS_STOMP, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.IKKLE_HYDRA_LEFT_FOOT_LETS_STOMP, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.PET_WALKING_THUMP, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.VETION_JR_RIGHT_FOOT_LETS_STOMP, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.VETION_JR_LEFT_FOOT_LETS_STOMP, SoundEffectType.Either));
		}
		if (config.mutePetSounds() || config.muteRandoms())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CAT_HISS, SoundEffectType.Either));
		}

		// Applicable to both pet sounds and random event sounds
		if (config.muteRandoms())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.NPC_TELEPORT_WOOSH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.DRUNKEN_DWARF, SoundEffectType.Either));
		}
		if (config.muteScarabs())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SCARAB_ATTACK_SOUND, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SCARAB_SPAWN_SOUND, SoundEffectType.Either));
		}
		if (config.muteSire())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SIRE_SPAWNS, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SIRE_SPAWNS_DEATH, SoundEffectType.Either));
		}
		if (config.muteSpectres())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SPECTRE_ATTACK_SHOOT, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SPECTRE_ATTACK_HIT, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SPECTRE_DEATH, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SPECTRE_PARRY, SoundEffectType.Either));
		}
		if (config.muteTekton())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.METEOR, SoundEffectType.Either));
		}
		if (config.muteTownCrierSounds())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.TOWN_CRIER_BELL_DING, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.TOWN_CRIER_BELL_DONG, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.TOWN_CRIER_SHOUT_SQUEAK, SoundEffectType.Either));
		}

		// ------- Skilling -------
		if (config.muteAlchemy())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.HIGH_ALCHEMY, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.LOW_ALCHEMY, SoundEffectType.Either));
		}
		if (config.muteChopChop())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CHOP_CHOP, SoundEffectType.Either));
		}
		if (config.muteSmashing())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SMASHING, SoundEffectType.AREA_SOUND_EFFECT));
		}
		if (config.muteDenseEssence())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CHISEL, SoundEffectType.Either));
		}
		if (config.muteFiremaking())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.FIREMAKING_LOG_BURN, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.FIREMAKING_LOG_LIGHT, SoundEffectType.Either));
		}
		if (config.muteFishing())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.FISHING_SOUND, SoundEffectType.Either));
		}
		if (config.muteFletching())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.FLETCHING_CUT, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.AMETHYST_FLETCHING, SoundEffectType.Either));
		}
		if (config.muteAOESounds())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.HUMIDIFY_SOUND, SoundEffectType.Either));
		}
		if (config.mutePickpocket())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PICKPOCKET_PLOP, SoundEffectType.Either));
		}
		if (config.mutePickpocketStun())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PICKPOCKET_STUN, SoundEffectType.Either));
		}
		if (config.muteMining())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.MINING_PICK_SWING_1, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.MINING_PICK_SWING_2, SoundEffectType.Either));
		}
		if (config.mutePlankMake())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PLANK_MAKE, SoundEffectType.Either));
		}
		if (config.muteStringJewellery())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.STRING_JEWELLERY, SoundEffectType.Either));
		}
		if (config.muteWoodcutting())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.WOODCUTTING_CHOP, SoundEffectType.Either));
		}
		if (config.muteChargeOrb())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CHARGE_EARTH_ORB, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.CHARGE_AIR_ORB, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.CHARGE_FIRE_ORB, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.CHARGE_WATER_ORB, SoundEffectType.Either));
		}
		// ------- Prayers -------
		if (config.muteThickSkin())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.THICK_SKIN, SoundEffectType.Either));
		}
		if (config.muteBurstofStrength())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.BURST_OF_STRENGTH, SoundEffectType.Either));
		}
		if (config.muteClarityOfThought())
		{

			soundEffects.add(new SoundEffect(SoundEffectID.CLARITY_OF_THOUGHT, SoundEffectType.Either));
		}
		if (config.muteRockSkin())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.ROCK_SKIN, SoundEffectType.Either));
		}
		if (config.muteSuperhumanStrength())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SUPERHUMAN_STRENGTH, SoundEffectType.Either));
		}
		if (config.muteImprovedReflexes())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.IMPROVED_REFLEXES, SoundEffectType.Either));
		}
		if (config.muteRapidHeal())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.RAPID_HEAL, SoundEffectType.Either));
		}
		if (config.muteProtectItem())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PROTECT_ITEM, SoundEffectType.Either));
		}
		if (config.muteHawkEye())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.HAWK_EYE, SoundEffectType.Either));
		}
		if (config.muteMysticLore())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.MYSTIC_LORE, SoundEffectType.Either));
		}
		if (config.muteSteelSkin())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.STEEL_SKIN, SoundEffectType.Either));
		}
		if (config.muteUltimateStrength())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.ULTIMATE_STRENGTH, SoundEffectType.Either));
		}
		if (config.muteIncredibleReflexes())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.INCREDIBLE_REFLEXES, SoundEffectType.Either));
		}
		if (config.muteProtectFromMagic())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PROTECT_FROM_MAGIC, SoundEffectType.Either));
		}
		if (config.muteProtectFromRange())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PROTECT_FROM_RANGE, SoundEffectType.Either));
		}
		if (config.muteProtectFromMelee())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PROTECT_FROM_MELEE, SoundEffectType.Either));
		}
		if (config.muteEagleEye())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.EAGLE_EYE, SoundEffectType.Either));
		}
		if (config.muteMysticMight())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.MYSTIC_MIGHT, SoundEffectType.Either));
		}
		if (config.muteRetribution())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.RETRIBUTION, SoundEffectType.Either));
		}
		if (config.muteRedemption())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.REDEMPTION, SoundEffectType.Either));
		}
		if (config.muteSmite())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SMITE, SoundEffectType.Either));
		}
		if (config.mutePreserve())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PRESERVE, SoundEffectType.Either));
		}
		if (config.muteChivalry())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.CHIVALRY, SoundEffectType.Either));
		}
		if (config.mutePiety())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.PIETY, SoundEffectType.Either));
		}
		if (config.muteRigour())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.RIGOUR, SoundEffectType.Either));
		}
		if (config.muteAugury())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.AUGURY, SoundEffectType.Either));
		}
		if (config.muteDeactivatePrayer())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.DEACTIVATE_PRAYER, SoundEffectType.Either));
		}

		// ------- Miscellaneous -------
		if (config.muteFishingExplosive())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.FISHING_EXPLOSIVE, SoundEffectType.Either));
		}
		if (config.muteHealOther())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.HEAL_OTHER_2, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.HEAL_OTHER_3, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.HEAL_OTHER_4, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.HEAL_OTHER_5, SoundEffectType.Either));
		}
		if (config.muteItemDrop())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.ITEM_DROP, SoundEffectType.Either));
		}
		if (config.muteLevelUp())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.LEVEL_UP_1, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.LEVEL_UP_2, SoundEffectType.Either));
		}
		if (config.muteNPCContact())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.NPC_CONTACT, SoundEffectType.Either));
		}
		if (config.muteSnowballSounds())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.SNOWBALL_HIT, SoundEffectType.Either));
			soundEffects.add(new SoundEffect(SoundEffectID.SNOWBALL_THROW, SoundEffectType.Either));
		}
		if (config.muteTeleother())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.TELEOTHER, SoundEffectType.Either));
		}
		if (config.muteTeleport())
		{
			if (config.muteTeleportOthers())
			{
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 714)); // Normal
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 1816)); // Lunar
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3864)); // Scroll
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3865)); // Xeric
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3867)); // Wilderness
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3869)); // Cabbage
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3872)); // Ardougne
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3874)); // Burgh
			}
			else
			{
				soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either));
			}
		}
		else if (config.muteTeleportOthers())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 714)); // Normal
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 1816)); // Lunar
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3864)); // Scroll
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3865)); // Xeric
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3867)); // Wilderness
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3869)); // Cabbage
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3872)); // Ardougne
			soundEffects.add(new SoundEffect(SoundEffectID.TELEPORT_VWOOP, SoundEffectType.Either, 3874)); // Burgh
		}
		if (config.muteRubberChickenSounds())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.WHACK, SoundEffectType.Either));
		}
		if (config.muteObelisk())
		{
			soundEffects.add(new SoundEffect(SoundEffectID.WILDY_OBELISK, SoundEffectType.Either));
		}

		// Ambient Sounds
		ambientSoundsToMute = new HashSet<>();

		if (config.muteMagicTrees())
		{
			ambientSoundsToMute.add(SoundEffectID.MAGIC_TREE);
		}

		if (config.muteWhiteNoise())
		{
			ambientSoundsToMute.add(SoundEffectID.STATIC_1);
			ambientSoundsToMute.add(SoundEffectID.STATIC_2);
			ambientSoundsToMute.add(SoundEffectID.STATIC_3);

			// TODO confirm STATIC_4 is water + static or just static
//			ambientSoundsToMute.add(SoundEffectID.STATIC_4);
			ambientSoundsToMute.add(SoundEffectID.STATIC_5);
		}

		if (config.muteChirps())
		{
			ambientSoundsToMute.add(SoundEffectID.CRICKET_1);
			ambientSoundsToMute.add(SoundEffectID.CRICKET_2);
			ambientSoundsToMute.add(SoundEffectID.CRICKET_3);
			ambientSoundsToMute.add(SoundEffectID.CRICKET_4);
			ambientSoundsToMute.add(SoundEffectID.CRICKET_5);
			ambientSoundsToMute.add(SoundEffectID.CRICKET_6);
		}

		if (config.muteWater())
		{
			ambientSoundsToMute.add(SoundEffectID.WATER_1);
			ambientSoundsToMute.add(SoundEffectID.WATER_2);
			ambientSoundsToMute.add(SoundEffectID.WATER_3);
			ambientSoundsToMute.add(SoundEffectID.WATER_4);
			ambientSoundsToMute.add(SoundEffectID.WATER_5);
			ambientSoundsToMute.add(SoundEffectID.WATER_6);
			ambientSoundsToMute.add(SoundEffectID.WATER_7);
			ambientSoundsToMute.add(SoundEffectID.WATER_8);
			ambientSoundsToMute.add(SoundEffectID.WATER_9);
			ambientSoundsToMute.add(SoundEffectID.WATER_10);
		}

		if (config.muteRanges())
		{
			ambientSoundsToMute.add(SoundEffectID.RANGE_1);
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
			else if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT))
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
			if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT))
			{
				areaSoundEffectPlayed.consume();
			}
		}
		else if (shouldMute(soundId, SoundEffectType.AREA_SOUND_EFFECT))
		{
			areaSoundEffectPlayed.consume();
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed)
	{
		int soundId = soundEffectPlayed.getSoundId();
		if (shouldMute(soundId, SoundEffectType.SOUND_EFFECT))
		{
			soundEffectPlayed.consume();
		}
	}

	@VisibleForTesting
	boolean shouldMute(int soundId, SoundEffectType type)
	{
		SoundEffect soundEffect = new SoundEffect(soundId, type, client.getLocalPlayer().getAnimation());
		if (getSelectedSounds().contains(Integer.toString(soundId)))
		{
			return true;
		}

		List<SoundEffect> filteredSoundEffects = soundEffects.stream().filter(
			s -> s.id == soundEffect.id
			&& (s.type == SoundEffectType.Either || s.type == soundEffect.type)
		).collect(Collectors.toCollection(ArrayList::new));

		if (filteredSoundEffects.size() == 0)
		{
			return false;
		}
		else
		{
			return filteredSoundEffects.stream().anyMatch(s -> s.animID == -1) || filteredSoundEffects.stream().noneMatch(s -> s.animID == soundEffect.animID);
		}
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
}