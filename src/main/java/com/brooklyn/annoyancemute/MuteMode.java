package com.brooklyn.annoyancemute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Who a sound should be muted for.
 */
@Getter
@RequiredArgsConstructor
public enum MuteMode
{
	OFF("Off"),
	ONLY_OTHERS("Only Others"),
	ALL("All");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
