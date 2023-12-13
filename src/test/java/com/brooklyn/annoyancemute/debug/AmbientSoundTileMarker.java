package com.brooklyn.annoyancemute.debug;

import java.awt.Color;
import javax.annotation.Nullable;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
class AmbientSoundTileMarker
{
	private WorldPoint worldPoint;
	@Nullable
	private Color color;
	@Nullable
	private String label;
}
