/*
 * Copyright (c) 2020, Brooklyn <https://github.com/Broooklyn>
 * Copyright (c) 2018, SomeoneWithAnInternetConnection
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

public final class SoundEffectID
{
	// Pet sounds
	protected final static int CAT_HISS = net.runelite.api.gameval.SoundEffectID.CAT_ATTACK;
	protected final static int SNAKELING_METAMORPHOSIS = net.runelite.api.gameval.SoundEffectID.SEASNAKE_ATTACK;
	protected final static int CLOCKWORK_CAT_CLICK_CLICK = net.runelite.api.gameval.SoundEffectID.POH_CLOCKWORK;
	protected final static int PET_KREEARRA_WING_FLAP = net.runelite.api.gameval.SoundEffectID.GODWARS_ARMADYL_WINGS;
	protected final static int ELECTRIC_HYDRA_IN = net.runelite.api.gameval.SoundEffectID.HYDRA_BOSS_STAGE_2_SPARK;
	protected final static int ELECTRIC_HYDRA_OUT = net.runelite.api.gameval.SoundEffectID.HYDRA_BOSS_STAGE_2_SPARK_PART_2;
	protected final static int IKKLE_HYDRA_RIGHT_FOOT_LETS_STOMP = net.runelite.api.gameval.SoundEffectID.HYDRA_BOSS_FOOTSTEP_1;
	protected final static int IKKLE_HYDRA_LEFT_FOOT_LETS_STOMP = net.runelite.api.gameval.SoundEffectID.HYDRA_BOSS_FOOTSTEP_2;
	protected final static int VETION_JR_RIGHT_FOOT_LETS_STOMP = net.runelite.api.gameval.SoundEffectID.WBR_VENTION_FOOTSTEP_02;
	protected final static int VETION_JR_LEFT_FOOT_LETS_STOMP = net.runelite.api.gameval.SoundEffectID.WBR_VENTION_FOOTSTEP_01;
	protected final static int NOON_FLAP_1 = net.runelite.api.gameval.SoundEffectID.L5_GG_DAWN_IDLE_FLY_01;
	protected final static int NOON_FLAP_2 = net.runelite.api.gameval.SoundEffectID.L5_GG_DAWN_IDLE_FLY_02;
	protected final static int BEEF_NOISE_1 = net.runelite.api.gameval.SoundEffectID.CB_NOSTRILS_BREATH_IN_01;
	protected final static int BEEF_NOISE_2 = net.runelite.api.gameval.SoundEffectID.CB_NOSTRILS_FLARE_01;
	protected final static int GULLIVER_EMOTE_1 = net.runelite.api.gameval.SoundEffectID.SAILING_BOSS_GRYPHON_MELEE_HIT_01;
	protected final static int GULLIVER_EMOTE_2 = net.runelite.api.gameval.SoundEffectID.SAILING_BOSS_GRYPHON_SCREAM_01;


	// Pet Thump
	protected final static int PET_WALKING_THUMP = net.runelite.api.gameval.SoundEffectID.GODWARS_AVATAR_FOOTSTEPS;

	// Town Crier
	protected final static int TOWN_CRIER_BELL_DING = net.runelite.api.gameval.SoundEffectID.TOWN_CRIER_RING_BELL_UP;
	protected final static int TOWN_CRIER_BELL_DONG = net.runelite.api.gameval.SoundEffectID.TOWN_CRIER_RING_BELL_DOWN;
	protected final static int TOWN_CRIER_SHOUT_SQUEAK = net.runelite.api.gameval.SoundEffectID.TOWN_CRIER_SCRATCH_HEAD;

	// AOE Spells
	protected final static int HUMIDIFY_SOUND = net.runelite.api.gameval.SoundEffectID.LUNAR_HUMIDIFY;

	// Chicken
	protected final static int WHACK = net.runelite.api.gameval.SoundEffectID.RUBBER_CHICKEN_ATTACK;

	// Snowball
	protected final static int SNOWBALL_HIT = net.runelite.api.gameval.SoundEffectID.GUBLINCH_HIT;
	protected final static int SNOWBALL_THROW = net.runelite.api.gameval.SoundEffectID.THROWN;

	// Cannon
	protected final static int CANNON_SPIN = net.runelite.api.gameval.SoundEffectID.MCANNON_TURN;
	protected final static int SHATTERED_CANNON_SPIN = net.runelite.api.gameval.SoundEffectID.LAVA_FLAME;

	// ACB
	protected final static int ACB_REEEE = net.runelite.api.gameval.SoundEffectID.GODWARS_ARMADYL_AVATAR_ATTACK;

	// Thralls
	protected final static int WATER_STRIKE_CAST = net.runelite.api.gameval.SoundEffectID.WATERSTRIKE_CAST_AND_FIRE; // Ghostly Thrall
	protected final static int WATER_STRIKE_LAND = net.runelite.api.gameval.SoundEffectID.WATERSTRIKE_HIT;
	protected final static int SKELETON_THRALL_ATTACK = net.runelite.api.gameval.SoundEffectID.LONGBOW;
	protected final static int ZOMBIE_THRALL_ATTACK = net.runelite.api.gameval.SoundEffectID.ZOMBIE_ATTACK;

	// Imp thralls
//	protected final static int IMP_RANGED_THRALL_SUMMON_1 = 5048;
//	protected final static int IMP_RANGED_THRALL_SUMMON_2 = 5054;
//	protected final static int IMP_RANGED_THRALL_SUMMON_3 = 11395;
	protected final static int IMP_RANGED_THRALL_ATTACK_1 = net.runelite.api.gameval.SoundEffectID.L6_IMP_RANGED_ATTACK_ARROW_THROW_01;
	protected final static int IMP_RANGED_THRALL_ATTACK_2 = net.runelite.api.gameval.SoundEffectID.L6_IMP_RANGED_ATTACK_JUMP_01;

//	protected final static int IMP_MAGE_THRALL_SUMMON_1 = 5063;
//	protected final static int IMP_MAGE_THRALL_SUMMON_2 = 11373;
//	protected final static int IMP_MAGE_THRALL_SUMMON_3 = 11372;
	protected final static int IMP_MAGE_THRALL_ATTACK_1 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MAGIC_ATTACK_JUMP_01;
	protected final static int IMP_MAGE_THRALL_ATTACK_2 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MAGIC_ATTACK_PROJECTILE_01;
	protected final static int IMP_MAGE_THRALL_ATTACK_3 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MAGIC_ATTACK_PROJECTILE_FLY_01;

//	protected final static int IMP_MELEE_THRALL_SUMMON_1 = 5058;
//	protected final static int IMP_MELEE_THRALL_SUMMON_2 = 5054;
//	protected final static int IMP_MELEE_THRALL_SUMMON_3 = 11387;
	protected final static int IMP_MELEE_THRALL_ATTACK_1 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MELEE_ATTACK_AXE_IMPACT_01;
	protected final static int IMP_MELEE_THRALL_ATTACK_2 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MELEE_ATTACK_AXE_WHOOSH_01;
	protected final static int IMP_MELEE_THRALL_ATTACK_3 = net.runelite.api.gameval.SoundEffectID.L6_IMP_MELEE_ATTACK_JUMP_01;

	// Ice Spells
	protected final static int ICE_BARRAGE_CAST = net.runelite.api.gameval.SoundEffectID.ICE_BARRAGE_IMPACT;
	protected final static int ICE_BLITZ_CAST = net.runelite.api.gameval.SoundEffectID.ICE_BLITZ_IMPACT;
	protected final static int ICE_BURST_CAST = net.runelite.api.gameval.SoundEffectID.ICE_BURST_IMPACT;
	protected final static int ICE_SPELL_LAND = net.runelite.api.gameval.SoundEffectID.ICE_CAST;
	protected final static int ICE_RUSH_CAST = net.runelite.api.gameval.SoundEffectID.ICE_RUSH_IMPACT;

	// Sire
	protected final static int SIRE_SPAWNS = net.runelite.api.gameval.SoundEffectID.FOURFACE_ATTACK;
	protected final static int SIRE_SPAWNS_DEATH = net.runelite.api.gameval.SoundEffectID.FOURFACE_DEATH;

	// Wilderness Obelisk
	protected final static int WILDY_OBELISK = net.runelite.api.gameval.SoundEffectID.WILDERNESS_TELEPORT;

	// Random Events
	protected final static int NPC_TELEPORT_WOOSH = net.runelite.api.gameval.SoundEffectID.SMOKEPUFF;
	protected final static int DRUNKEN_DWARF = net.runelite.api.gameval.SoundEffectID.DWARF_WHISTLE;
	protected final static int EVIL_BOB = net.runelite.api.gameval.SoundEffectID.CAT_ATTACK;
	protected final static int POSTIE_PETE = net.runelite.api.gameval.SoundEffectID.HW24_POSTY_PETE_BOUNCE_01;
	protected final static int FROG_SPLASH = net.runelite.api.gameval.SoundEffectID.GIANT_TOAD_ATTACK;

	// Tekton
	protected final static int METEOR = net.runelite.api.gameval.SoundEffectID.FIREBLAST_HIT;

	// Dense Essence Mine
	protected final static int CHISEL = net.runelite.api.gameval.SoundEffectID.ENAK_CHISEL;

	// Chop Chop
	protected final static int CHOP_CHOP = net.runelite.api.gameval.SoundEffectID.CLOBBER;

	// Smashing!
	protected final static int SMASHING = net.runelite.api.gameval.SoundEffectID.FOUND_GEM;

	// Plank Make
	protected final static int PLANK_MAKE = net.runelite.api.gameval.SoundEffectID.LUNAR_MAKE_PLANK;

	// Nightmare
	protected final static int NIGHTMARE_SOUND = net.runelite.api.gameval.SoundEffectID.NOA_HIT;

	// Pyramid Plunder Scarabs
	protected final static int SCARAB_SPAWN_SOUND = net.runelite.api.gameval.SoundEffectID.SCARABS_APPEAR;
	protected final static int SCARAB_ATTACK_SOUND = net.runelite.api.gameval.SoundEffectID.SCARABS_ATTACK;

	// Firemaking
	protected final static int FIREMAKING_LOG_BURN = net.runelite.api.gameval.SoundEffectID.FIRE_LIT;
	protected final static int FIREMAKING_LOG_LIGHT = net.runelite.api.gameval.SoundEffectID.FLINT;

	// Fletching
	protected final static int FLETCHING_CUT = net.runelite.api.gameval.SoundEffectID.FLETCH;
	protected final static int AMETHYST_FLETCHING = net.runelite.api.gameval.SoundEffectID.CHISEL;

	// Fishing
	protected final static int FISHING_SOUND = net.runelite.api.gameval.SoundEffectID.FISHING_CAST;

	// Alchemy
	protected final static int LOW_ALCHEMY = net.runelite.api.gameval.SoundEffectID.LOW_ALCHEMY;
	protected final static int HIGH_ALCHEMY = net.runelite.api.gameval.SoundEffectID.HIGH_ALCHEMY;
	// Trail Blazer's Alchemy
	protected final static int TRAIL_BLAZERS_LOW_ALCHEMY = net.runelite.api.gameval.SoundEffectID.LEAGUES4_LOWLVL_ALCHEMY_02;
	protected final static int TRAIL_BLAZERS_HIGH_ALCHEMY = net.runelite.api.gameval.SoundEffectID.LEAGUES4_HIGHLVL_ALCHEMY_01;

	// Prayers
	protected final static int THICK_SKIN = net.runelite.api.gameval.SoundEffectID.THICK_SKIN;
	protected final static int BURST_OF_STRENGTH = net.runelite.api.gameval.SoundEffectID.STRENGTH_BURST;
	protected final static int CLARITY_OF_THOUGHT = net.runelite.api.gameval.SoundEffectID.CLARITY;
	protected final static int ROCK_SKIN = net.runelite.api.gameval.SoundEffectID.ROCK_SKIN;
	protected final static int SUPERHUMAN_STRENGTH = net.runelite.api.gameval.SoundEffectID.SUPERHUMAN_STRENGTH;
	protected final static int IMPROVED_REFLEXES = net.runelite.api.gameval.SoundEffectID.IMPROVED_REFLEXES;
	protected final static int RAPID_HEAL = net.runelite.api.gameval.SoundEffectID.RAPID_HEAL;
	protected final static int PROTECT_ITEM = net.runelite.api.gameval.SoundEffectID.PROTECT_ITEMS;
	protected final static int HAWK_EYE = net.runelite.api.gameval.SoundEffectID.HAWK_EYE;
	protected final static int MYSTIC_LORE = net.runelite.api.gameval.SoundEffectID.MYSTIC_LORE;
	protected final static int STEEL_SKIN = net.runelite.api.gameval.SoundEffectID.STEEL_SKIN;
	protected final static int ULTIMATE_STRENGTH = net.runelite.api.gameval.SoundEffectID.ULTIMATE_STRENGTH;
	protected final static int INCREDIBLE_REFLEXES = net.runelite.api.gameval.SoundEffectID.INCREDIBLE_REFLEXES;
	protected final static int PROTECT_FROM_MAGIC = net.runelite.api.gameval.SoundEffectID.PROTECT_FROM_MAGIC;
	protected final static int PROTECT_FROM_RANGE = net.runelite.api.gameval.SoundEffectID.PROTECT_FROM_MISSILES;
	protected final static int PROTECT_FROM_MELEE = net.runelite.api.gameval.SoundEffectID.PROTECT_FROM_MELEE;
	protected final static int EAGLE_EYE = net.runelite.api.gameval.SoundEffectID.EAGLE_EYE;
	protected final static int MYSTIC_MIGHT = net.runelite.api.gameval.SoundEffectID.MYSTIC_MIGHT;
	protected final static int RETRIBUTION = net.runelite.api.gameval.SoundEffectID.RETRIBUTION;
	protected final static int REDEMPTION = net.runelite.api.gameval.SoundEffectID.REDEMPTION;
	protected final static int SMITE = net.runelite.api.gameval.SoundEffectID.SMITE;
	protected final static int PRESERVE = net.runelite.api.gameval.SoundEffectID.RAPID_RESTORE;
	protected final static int CHIVALRY = net.runelite.api.gameval.SoundEffectID.KR_CHIVALRY;
	protected final static int PIETY = net.runelite.api.gameval.SoundEffectID.KR_PIETY;
	protected final static int RIGOUR = net.runelite.api.gameval.SoundEffectID.SHARP_EYE;
	protected final static int AUGURY = net.runelite.api.gameval.SoundEffectID.MYSTIC_WILL;
	protected final static int DEADEYE = net.runelite.api.gameval.SoundEffectID.RT_DEADEYE_PRAYER_ACTIVATE_10 ;
	protected final static int MYSTIC_VIGOUR = net.runelite.api.gameval.SoundEffectID.RT_MYSTIC_VIGOUR_ACTIVATE;
	protected final static int ZEAL = net.runelite.api.gameval.SoundEffectID.TFA_ZEAL_PRAYER_ACTIVIATE_01;
	protected final static int DEACTIVATE_PRAYER = net.runelite.api.gameval.SoundEffectID.CANCEL_PRAYER;

	// Woodcutting
	protected final static int WOODCUTTING_CHOP = net.runelite.api.gameval.SoundEffectID.WOODCHOP;

	// Teleport
	protected static final int TELEPORT_VWOOP = net.runelite.api.gameval.SoundEffectID.TELEPORT_ALL;

	// Teleother
	protected static final int TELEOTHER = net.runelite.api.gameval.SoundEffectID.TELE_OTHER_CAST;

	// Pickpocket
	protected static final int PICKPOCKET_PLOP = net.runelite.api.gameval.SoundEffectID.PICK;
	protected static final int PICKPOCKET_STUN = net.runelite.api.gameval.SoundEffectID.STUNNED;

	// NPC Contact
	protected final static int NPC_CONTACT = net.runelite.api.gameval.SoundEffectID.LUNAR_NPC_CONTACT;

	// Cow
	protected final static int MOO_MOO = net.runelite.api.gameval.SoundEffectID.COW_ATMOSPHERIC;

	// String Jewelery
	protected final static int STRING_JEWELLERY = net.runelite.api.gameval.SoundEffectID.LUNAR_STRING_AMULET;

	// Cave Horror
	protected final static int CAVE_HORROR = net.runelite.api.gameval.SoundEffectID.CAVE_HORROR_HOWL;

	// Fossil Island Wyvern
	protected final static int FOSSIL_ISLAND_WYVERN_69 = net.runelite.api.gameval.SoundEffectID.SKWY_BITE;
	protected final static int FOSSIL_ISLAND_WYVERN_71 = net.runelite.api.gameval.SoundEffectID.SKWY_HIT;
	protected final static int FOSSIL_ISLAND_WYVERN_73 = net.runelite.api.gameval.SoundEffectID.SKWY_TAIL;

	// Fishing Explosive
	protected final static int FISHING_EXPLOSIVE = net.runelite.api.gameval.SoundEffectID.EXPLOSION;

	// Heal Other
	protected final static int HEAL_OTHER_2 = net.runelite.api.gameval.SoundEffectID.LUNAR_HEAL_OTHER_INDIVIDUAL;
	protected final static int HEAL_OTHER_3 = net.runelite.api.gameval.SoundEffectID.LUNAR_HEAL;
	protected final static int HEAL_OTHER_4 = net.runelite.api.gameval.SoundEffectID.LUNAR_HEAL_GROUP;
	protected final static int HEAL_OTHER_5 = net.runelite.api.gameval.SoundEffectID.LUNAR_HEAL_OTHER;

	// Item Dropping
	protected final static int ITEM_DROP = net.runelite.api.gameval.SoundEffectID.PUT_DOWN;

	// Level Up Fireworks
	protected final static int LEVEL_UP_1 = net.runelite.api.gameval.SoundEffectID.FIREWORK;
	protected final static int LEVEL_UP_2 = net.runelite.api.gameval.SoundEffectID.BURST;

	// Jellies
	protected final static int JELLY_ATTACK = net.runelite.api.gameval.SoundEffectID.JELLY_ATTACK;
	protected final static int JELLY_DEATH = net.runelite.api.gameval.SoundEffectID.JELLY_DEATH;
	protected final static int JELLY_PARRY = net.runelite.api.gameval.SoundEffectID.JELLY_HIT;

	// Nail Beasts
	protected final static int NAIL_BEAST_ATTACK = net.runelite.api.gameval.SoundEffectID.RAMBLE_NAILBEAST_ATTACK;
	protected final static int NAIL_BEAST_DEATH = net.runelite.api.gameval.SoundEffectID.RAMBLE_NAILBEAST_DEATH;
	protected final static int NAIL_BEAST_PARRY = net.runelite.api.gameval.SoundEffectID.RAMBLE_NAILBEAST_HIT;

	// Demons
	protected final static int GREATER_DEMON_ATTACK = net.runelite.api.gameval.SoundEffectID.BLACK_DEMON_ATTACK;
	protected final static int GREATER_DEMON_DEATH = net.runelite.api.gameval.SoundEffectID.BLACK_DEMON_DEATH;
	protected final static int GREATER_DEMON_PARRY = net.runelite.api.gameval.SoundEffectID.BLACK_DEMON_HIT;
	protected final static int DEMON_ATTACK = net.runelite.api.gameval.SoundEffectID.DEMON_ATTACK; // Lesser and Black Demons
	protected final static int DEMON_DEATH = net.runelite.api.gameval.SoundEffectID.DEMON_DEATH;
	protected final static int DEMON_PARRY = net.runelite.api.gameval.SoundEffectID.DEMON_HIT;

	// Nechryael
	protected static final int NECHRYAEL_ATTACK = net.runelite.api.gameval.SoundEffectID.NECHRAYEL_ATTACK;
	protected static final int NECHRYAE_DEATH = net.runelite.api.gameval.SoundEffectID.NECHRAYEL_DEATH;
	protected static final int NECHRYAEL_PARRY = net.runelite.api.gameval.SoundEffectID.NECHRAYEL_HIT;

	// Dust Devils
	protected static final int DUST_DEVIL_ATTACK = net.runelite.api.gameval.SoundEffectID.DUST_DEVIL_ATTACK;
	protected static final int DUST_DEVIL_DEATH = net.runelite.api.gameval.SoundEffectID.DUST_DEVIL_DEATH;
	protected static final int DUST_DEVIL_PARRY = net.runelite.api.gameval.SoundEffectID.DUST_DEVIL_HIT;

	// Spectres
	protected static final int SPECTRE_ATTACK_SHOOT = net.runelite.api.gameval.SoundEffectID.SPECTRE_ATTACK;
	protected static final int SPECTRE_ATTACK_HIT = net.runelite.api.gameval.SoundEffectID.GOO_HIT;
	protected static final int SPECTRE_DEATH = net.runelite.api.gameval.SoundEffectID.SPECTRE_DEATH;
	protected static final int SPECTRE_PARRY = net.runelite.api.gameval.SoundEffectID.SPECTRE_HIT;

	// Mining
	protected static final int MINING_PICK_SWING_1 = net.runelite.api.gameval.SoundEffectID.MINE1;
	protected static final int MINING_PICK_SWING_2 = net.runelite.api.gameval.SoundEffectID.MINE2;

	//Charge Orb spells
	protected static final int CHARGE_EARTH_ORB = net.runelite.api.gameval.SoundEffectID.CHARGE_EARTH_ORB;
	protected static final int CHARGE_AIR_ORB = net.runelite.api.gameval.SoundEffectID.CHARGE_AIR_ORB;
	protected static final int CHARGE_FIRE_ORB = net.runelite.api.gameval.SoundEffectID.CHARGE_FIRE_ORB;
	protected static final int CHARGE_WATER_ORB = net.runelite.api.gameval.SoundEffectID.CHARGE_WATER_ORB;

	// Magic Tree (Ambient Sound)
	protected final static int MAGIC_TREE = net.runelite.api.gameval.SoundEffectID.MAGIC_TREE_LOOP;

	// House Portal
	protected final static int HOUSE_PORTAL = net.runelite.api.gameval.SoundEffectID.PORTAL_LOOP_1;

	// Ambient Static sounds

	protected final static int STATIC_1 = net.runelite.api.gameval.SoundEffectID.BREEZE_LOOP_2;
	protected final static int STATIC_2 = net.runelite.api.gameval.SoundEffectID.JUNGLE_AMBIENCE_LOOP;
	protected final static int STATIC_3 = net.runelite.api.gameval.SoundEffectID.FIRE_CRACKLING_MED;
	protected final static int STATIC_4 = net.runelite.api.gameval.SoundEffectID.STREAM_LOOP_2;
	protected final static int STATIC_5 = net.runelite.api.gameval.SoundEffectID.JUNGLE_AMBIENCE_LOOP_1; // this is some noise in rimmington

	// Ambient Crickets/Birds
	protected final static int CRICKET_1 = net.runelite.api.gameval.SoundEffectID.CICADAS_1;
	protected final static int CRICKET_2 = net.runelite.api.gameval.SoundEffectID.CICADAS_2;
	protected final static int CRICKET_3 = net.runelite.api.gameval.SoundEffectID.CRICKETS_1;
	protected final static int CRICKET_4 = net.runelite.api.gameval.SoundEffectID.CRICKETS_2;
	protected final static int CRICKET_5 = net.runelite.api.gameval.SoundEffectID.CROAK_1;
	protected final static int CRICKET_6 = net.runelite.api.gameval.SoundEffectID.CROAK_2;
	// Water
	protected final static int WATER_1 = net.runelite.api.gameval.SoundEffectID.OCEAN_LOOP_1;
	protected final static int WATER_2 = net.runelite.api.gameval.SoundEffectID.COASTAL_WIND_LOOP;
	protected final static int WATER_3 = net.runelite.api.gameval.SoundEffectID.TANNING_VAT; // water 3 is a bubble noise but also white noise
	protected final static int WATER_4 = net.runelite.api.gameval.SoundEffectID.STREAM_LOOP_1; // water 4 is waves crashing into shore but also white noise
	protected final static int WATER_5 = net.runelite.api.gameval.SoundEffectID.OCEAN_GENTLE;
	protected final static int WATER_6 = net.runelite.api.gameval.SoundEffectID.FOUNTAIN_LOOP_1; // varrock armor shop fountain
	protected final static int WATER_7 = net.runelite.api.gameval.SoundEffectID.FOUNTAIN_LOOP_3; // varrock center fountains
	protected final static int WATER_8 = net.runelite.api.gameval.SoundEffectID.FOUNTAIN_LOOP_2; // lumbridge fountains
	protected final static int WATER_9 = net.runelite.api.gameval.SoundEffectID.BUBBLING_STREAM_1; // barbarian village bridge
	protected final static int WATER_10 = net.runelite.api.gameval.SoundEffectID.STREAM_LOOP_3; // barbarian village bridge
	protected final static int WATER_11 = net.runelite.api.gameval.SoundEffectID.ROCK_POOL_LOOP;
	protected final static int WATER_12 = net.runelite.api.gameval.SoundEffectID.CAVE_BUBBLING_LOOP_1; // lumbridge swamp bubbles


	// Ranges
	protected final static int RANGE_1 = net.runelite.api.gameval.SoundEffectID.COOKING_LOOP_2;
	protected final static int RANGE_2 = net.runelite.api.gameval.SoundEffectID.FIRE_CRACKLING_SMALL;
	protected final static int COOKING_POT = net.runelite.api.gameval.SoundEffectID.COOKING_LOOP_3;

	// Ambient Static Air 2184 base + modifiers

	protected final static int COMMON_BACKGROUND_1 = net.runelite.api.gameval.SoundEffectID.BREEZE_LOOP_1;
	protected final static int[] COMMON_BACKGROUND_2184_STATIC_1 = new int[]{net.runelite.api.gameval.SoundEffectID.BREEZE_LOOP_1, net.runelite.api.gameval.SoundEffectID.BREEZE_LOOP_2, net.runelite.api.gameval.SoundEffectID.TREE_RUSTLE_1, net.runelite.api.gameval.SoundEffectID.TREE_RUSTLE_2, net.runelite.api.gameval.SoundEffectID.TREE_WIND_1};

	protected final static int[] COMMON_BACKGROUND_2184_BIRD_1 = new int[]{net.runelite.api.gameval.SoundEffectID.BIRD_TWITTER_1, net.runelite.api.gameval.SoundEffectID.BIRD_TWITTER_2, net.runelite.api.gameval.SoundEffectID.BIRD_TWITTER_3, net.runelite.api.gameval.SoundEffectID.BIRD_TWITTER_4, net.runelite.api.gameval.SoundEffectID.BIRD_TWITTER_5};

	protected final static int[] COMMON_BACKGROUND_2184_BIRD_2 = new int[]{net.runelite.api.gameval.SoundEffectID.WARBLER_1, net.runelite.api.gameval.SoundEffectID.WARBLER_2, net.runelite.api.gameval.SoundEffectID.PEACOCK1, net.runelite.api.gameval.SoundEffectID.PEACOCK2, net.runelite.api.gameval.SoundEffectID.CROWS_1, net.runelite.api.gameval.SoundEffectID.WOODPECKER, net.runelite.api.gameval.SoundEffectID.OWL_PART_3};
	protected final static int[] COMMON_BACKGROUND_2184_BIRD_3 = new int[]{net.runelite.api.gameval.SoundEffectID.OWL_PART_1, net.runelite.api.gameval.SoundEffectID.OWL_PART_3, net.runelite.api.gameval.SoundEffectID.OWL_PART_2, net.runelite.api.gameval.SoundEffectID.TREE_RUSTLE_1}; // draynor village owls
	protected final static int[] COMMON_BACKGROUND_2184_BIRD_4 = new int[]{net.runelite.api.gameval.SoundEffectID.TREE_CREAK_1, net.runelite.api.gameval.SoundEffectID.TREE_CREAK_2, net.runelite.api.gameval.SoundEffectID.TREE_RUSTLE_1, net.runelite.api.gameval.SoundEffectID.TREE_RUSTLE_2, net.runelite.api.gameval.SoundEffectID.TREE_WIND_1, net.runelite.api.gameval.SoundEffectID.TREE_CREAK_3, net.runelite.api.gameval.SoundEffectID.BREEZE_LOOP_1}; // draynor village owls

	protected final static int[] COMMON_BACKGROUND_2184_WATER_1 = new int[]{net.runelite.api.gameval.SoundEffectID.OCEAN_GENTLE, net.runelite.api.gameval.SoundEffectID.OCEAN_GENTLE_1, net.runelite.api.gameval.SoundEffectID.OCEAN_GENTLE_2, net.runelite.api.gameval.SoundEffectID.OCEAN_GENTLE_3};
	protected final static int[] COMMON_BACKGROUND_2184_WATER_2 = new int[]{net.runelite.api.gameval.SoundEffectID.LAPPING_1, net.runelite.api.gameval.SoundEffectID.LAPPING_2, net.runelite.api.gameval.SoundEffectID.FISH_SPLASH, net.runelite.api.gameval.SoundEffectID.SEASHORE_1};

	// Fortis Colosseum lobby
	protected final static int FORTIS_COLOSSEUM_AMBIENT_1 = net.runelite.api.gameval.SoundEffectID.VARL_DWARVEN_CITY_RUMBLE_03;
	protected final static int FORTIS_COLOSSEUM_AMBIENT_2 = net.runelite.api.gameval.SoundEffectID.VARL_DWARVEN_CITY_RUMBLE_01;
	protected final static int FORTIS_COLOSSEUM_FIRE = net.runelite.api.gameval.SoundEffectID.FIRE_CRACKLING_SMALL;

	protected final static int COMMON_BACKGROUND_2 = -1;
	protected final static int[] VARDORVIS_AREA = new int[]{net.runelite.api.gameval.SoundEffectID.GRIM_WOLF, net.runelite.api.gameval.SoundEffectID.GRIM_WOLF_GROWL, net.runelite.api.gameval.SoundEffectID.GRIM_WOLF2, net.runelite.api.gameval.SoundEffectID.GRIM_WOLF3};
}
