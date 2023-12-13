package com.brooklyn.annoyancemute.debug;

import java.awt.Color;
import javax.annotation.Nullable;
import lombok.Value;

@Value
class AmbientSoundPoint
{
	private int regionId;
	private int regionX;
	private int regionY;
	private int z;
	@Nullable
	private Color color;
	@Nullable
	private String label;
}
