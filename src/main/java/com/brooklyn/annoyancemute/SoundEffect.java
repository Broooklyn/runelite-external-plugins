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


	// this is more of an "equals enough" type of equals where we care if either side's type is EITHER
	// it also checks the animation id of the item set in the hashset of SoundEffects and checks if it's -1 or matches current player.
	@Override
	public boolean equals(Object obj)
	{
		// this refers to an item found in the sound effects hashset being compared to
		// obj refers to the SoundEffect created to based on the sound, type and current animation of the player
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
			if (this.animID == -1)
			{
				return true;
			}
			// if the animID is not -1 it means we're caring about local player animation to determine mute
			// this is only valid for muting Teleports (only others)
			if (other.animID != this.animID)
			{
				return true;
			}
		}

		return this.type.equals(other.type);
	}
}
