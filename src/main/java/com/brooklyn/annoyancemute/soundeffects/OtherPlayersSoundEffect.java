package com.brooklyn.annoyancemute.soundeffects;

import com.brooklyn.annoyancemute.SoundEffectType;

/**
 * A sound that is only muted when another player is the source of it, so the local player still hears their own
 */
public class OtherPlayersSoundEffect extends SoundEffect
{
	public OtherPlayersSoundEffect(int id, SoundEffectType soundEffectType)
	{
		super(id, soundEffectType);
	}
}
