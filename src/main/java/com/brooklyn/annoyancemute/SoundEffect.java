package com.brooklyn.annoyancemute;

public class SoundEffect
{
	int id;
	SoundEffectType type;
	int animID;

	public SoundEffect(int id, SoundEffectType type)
	{
		this.id = id;
		this.type = type;
		this.animID = -1;
	}

	public SoundEffect(int id, SoundEffectType type, int animID)
	{
		this.id = id;
		this.type = type;
		this.animID = animID;
	}
}
