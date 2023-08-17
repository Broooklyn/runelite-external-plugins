package com.brooklyn.annoyancemute;

public class SoundEffect
{
	int id;
	SoundEffectType type;

	public SoundEffect(int id, SoundEffectType type)
	{
		this.id = id;
		this.type = type;
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
			return true;
		}

		return this.type.equals(other.type);
	}
}
