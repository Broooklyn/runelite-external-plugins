package com.brooklyn.annoyancemute;

public class SoundEffect
{
	int id;
	SoundEffectType type;
	int animID;

	int[] backGroundSoundEffect;

	public SoundEffect(int id, SoundEffectType type)
	{
		this.id = id;
		this.type = type;
		this.animID = -1;
		backGroundSoundEffect = new int[]{};
	}

	public SoundEffect(int id, SoundEffectType type, int animID)
	{
		this.id = id;
		this.type = type;
		this.animID = animID;
		backGroundSoundEffect = new int[]{};
	}

	public SoundEffect(int id, SoundEffectType type, int[] backGroundSoundEffect)
	{
		this.id = id;
		this.type = type;
		this.animID = -1;
		this.backGroundSoundEffect = backGroundSoundEffect;
	}
}
