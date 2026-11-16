package com.brooklyn.annoyancemute.soundeffects;

import com.brooklyn.annoyancemute.SoundEffectType;
import lombok.Getter;

@Getter
public class AmbientSoundEffect extends SoundEffect
{
	int[] randomSoundEffects;
	int regionId;

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType)
	{
		super(id, soundEffectType);
		this.randomSoundEffects = new int[]{};
		this.regionId = -1;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int[] randomSoundEffects)
	{
		super(id, soundEffectType);
		this.randomSoundEffects = randomSoundEffects;
		this.regionId = -1;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int regionId)
	{
		super(id, soundEffectType);
		this.randomSoundEffects = new int[]{};
		this.regionId = regionId;
	}

	public AmbientSoundEffect(int id, SoundEffectType soundEffectType, int[] randomSoundEffects, int regionId)
	{
		super(id, soundEffectType);
		this.randomSoundEffects = randomSoundEffects;
		this.regionId = regionId;
	}
}
