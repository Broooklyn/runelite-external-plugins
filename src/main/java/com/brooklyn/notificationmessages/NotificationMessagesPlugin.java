package com.brooklyn.notificationmessages;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.RuneLiteConfig;

@Slf4j
@PluginDescriptor(
	name = "Notification Messages",
	description = "Notify yourself with custom messages",
	tags = "hub,chat,notify,pet,pb,personal best,follow,follower"
)
public class NotificationMessagesPlugin extends Plugin
{
	// Personal Best
	private static final String NEW_PB = "new personal best";
	private static final String NEW_TOB_PB = "Personal best!";

	// Pet Drops
	private static final String FOLLOW_PET = "You have a funny feeling like you're being followed";
	private static final String INVENTORY_PET = "You feel something weird sneaking into your backpack";
	private static final String DUPE_PET = "You have a funny feeling like you would have been followed";

	// Potions
	private static final String ANTIFIRE = "Your antifire potion has expired.";
	private static final String SUPER_ANTIFIRE = "Your super antifire potion has expired.";
	private static final String ANTIPOISON = "Your poison resistance has worn off.";
	private static final String DIVINE_POTION = "The effects of the divine potion have worn off";
	private static final String PRE_DIVINE_POTION = "Your divine potion effect is about to expire.";
	private static final String PRE_DIVINE_RANGE = "Your divine ranging potion is about to expire.";
	private static final String PRE_DIVINE_MAGE = "Your divine magic potion is about to expire.";
	private static final String OVERLOAD = "The effects of overload have worn off, and you feel normal again.";
	private static final String STAMINA = "Your stamina enhancement has expired.";
	private static final String IMBUED_HEART = "Your imbued heart has regained its magical power.";
	private static final String PRE_SUPER_ANTIFIRE = "Your super antifire potion is about to expire";
	private static final String PRE_ANTIFIRE = "Your antifire potion is about to expire";

	@Inject
	private NotificationMessagesConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private RuneLiteConfig runeLiteConfig;

	@Inject
	private NotificationMessagesNotifier notifier;

	@Provides
	NotificationMessagesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NotificationMessagesConfig.class);
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		switch (chatMessage.getType())
			{
			case GAMEMESSAGE:
				if (chatMessage.getMessage().contains(NEW_PB) || chatMessage.getMessage().contains(NEW_TOB_PB))
				{
					if (config.notifyOnPersonalBest())
					{
						notifier.notify(config.personalBestMessage());
					}
				}
				if (chatMessage.getMessage().contains(FOLLOW_PET))
				{
					if (config.notifyOnPet())
					{
						notifier.notify(config.PetFollowMessage());
					}
				}
				if (chatMessage.getMessage().contains(INVENTORY_PET))
				{
					if (config.notifyOnPet())
					{
						notifier.notify(config.PetBackpackMessage());
					}
				}
				if (chatMessage.getMessage().contains(DUPE_PET))
				{
					if (config.notifyOnPet())
					{
						notifier.notify(config.PetDupeMessage());
					}
				}
				if (chatMessage.getMessage().contains(ANTIFIRE) || chatMessage.getMessage().contains(SUPER_ANTIFIRE))
				{
					if (config.antifireNotification())
					{
						notifier.notify(config.antifireMessage());
					}
				}
				if (chatMessage.getMessage().contains(ANTIPOISON))
				{
					if (config.antipoisonNotification())
					{
						notifier.notify(config.antipoisonMessage());
					}
				}
				if (chatMessage.getMessage().contains(DIVINE_POTION))
				{
					if (config.divinePotionNotification())
					{
						notifier.notify(config.divinePotionMessage());
					}
				}
				if (chatMessage.getMessage().contains(PRE_DIVINE_POTION) || chatMessage.getMessage().contains(PRE_DIVINE_RANGE) || chatMessage.getMessage().contains(PRE_DIVINE_MAGE))
				{
					if (config.preDivinePotion())
					{
						notifier.notify(config.preDivinePotionMessage());
					}
				}
				if (chatMessage.getMessage().contains(OVERLOAD))
				{
					if (config.overloadNotification())
					{
						notifier.notify(config.overloadMessage());
					}
				}
				if (chatMessage.getMessage().contains(STAMINA))
				{
					if (config.staminaNotification())
					{
						notifier.notify(config.staminaMessage());
					}
				}
				if (chatMessage.getMessage().contains(IMBUED_HEART))
				{
					if (config.imbuedHeartNotification())
					{
						notifier.notify(config.imbuedHeartMessage());
					}
				}
				if (chatMessage.getMessage().contains(PRE_SUPER_ANTIFIRE) || chatMessage.getMessage().contains(PRE_ANTIFIRE))
				{
					if (config.preAntifire())
					{
						notifier.notify(config.preAntifireMessage());
					}
				}
					break;
			case FRIENDSCHATNOTIFICATION:
				if (chatMessage.getMessage().contains(NEW_PB))
				{
					if (config.notifyOnPersonalBest())
					{
						notifier.notify(config.personalBestMessage());
					}
				}
				break;
			}
	}
}
