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

	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) {
			return false;
		}

		if (obj.getClass() != this.getClass()) {
			return false;
		}

		final SoundEffect other = (SoundEffect) obj;

		if (other.id != this.id)
		{
			return false;
		}

		if (this.type.equals(SoundEffectType.Either))
		{
			// if the animID is -1 it means it should be muted because the sound is a default mute
			// if the animID is not -1 it means we're caring about local player animation to determine mute
			// this is only valid for muting Teleports (only others)
			if (other.animID == -1 || this.animID == -1)
			{
				return true;
			}
			if (other.animID == this.animID)
			{
				return false;
			}
		}

		return this.type.equals(other.type);
	}
}
