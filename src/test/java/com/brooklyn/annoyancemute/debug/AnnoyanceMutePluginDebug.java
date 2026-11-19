package com.brooklyn.annoyancemute.debug;

import com.brooklyn.annoyancemute.AnnoyanceMutePlugin;
import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Annoyance Mute Debug",
	description = "Annoyance Mute Debug mode",
	tags = {"sound", "volume", "mute", "hub", "brooklyn", "pet", "stomp"}
)
public class AnnoyanceMutePluginDebug extends Plugin
{
	@Inject
	private EventBus eventBus;

	@Inject
	private PluginManager pluginManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private AnnoyanceMuteAmbientSoundOverlay ambientSoundOverlay;

	@Inject
	private AnnoyanceMuteSoundEffectOverlay soundEffectOverlay;

	@Provides
	AnnoyanceMuteDebugConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AnnoyanceMuteDebugConfig.class);
	}

	@Override
	protected void startUp()
	{
		// RuneLite only allows @PluginDependency on plugins that expose a public module, so look the plugin up instead
		AnnoyanceMutePlugin mutePlugin = pluginManager.getPlugins().stream()
			.filter(AnnoyanceMutePlugin.class::isInstance)
			.map(AnnoyanceMutePlugin.class::cast)
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("Annoyance Mute is not loaded"));
		soundEffectOverlay.setMutePlugin(mutePlugin);
		ambientSoundOverlay.setMutePlugin(mutePlugin);
		overlayManager.add(ambientSoundOverlay);
		overlayManager.add(soundEffectOverlay);
		eventBus.register(ambientSoundOverlay);
		eventBus.register(soundEffectOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(ambientSoundOverlay);
		overlayManager.remove(soundEffectOverlay);
		eventBus.unregister(ambientSoundOverlay);
		eventBus.unregister(soundEffectOverlay);
	}
}
