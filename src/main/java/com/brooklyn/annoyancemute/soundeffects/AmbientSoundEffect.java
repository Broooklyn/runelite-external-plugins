package com.brooklyn.annoyancemute.soundeffects;

import com.brooklyn.annoyancemute.SoundEffectType;
import lombok.Getter;

@Getter
public class AmbientSoundEffect extends SoundEffect
{
	int[] backgroundSoundEffects;
	int regionId;

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType)
	{
		super(id, soundEffectType);
		this.backgroundSoundEffects = new int[]{};
		this.regionId = -1;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int[] backgroundSoundEffects)
	{
		super(id, soundEffectType);
		this.backgroundSoundEffects = backgroundSoundEffects;
		this.regionId = -1;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int regionId)
	{
		super(id, soundEffectType);
		this.backgroundSoundEffects = new int[]{};
		this.regionId = regionId;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int[] backgroundSoundEffects, int regionId)
	{
		super(id, soundEffectType);
		this.backgroundSoundEffects = backgroundSoundEffects;
		this.regionId = regionId;
	}
}
