package com.brooklyn.notificationmessages;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("Notification Messages")
public interface NotificationMessagesConfig extends Config
{
	@ConfigSection(
		name = "Personal Best",
		description = "Settings for Personal Best notifications",
		position = 0
	)
	String personalBestSection = "Personal Best";

	@ConfigSection(
		name = "Pet Drop",
		description = "Settings for Pet Drop notifications",
		position = 1
	)
	String petSection = "Pet Drop";

	@ConfigItem(

		keyName = "notifyOnPersonalBest",
		name = "Notify on new PB",
		description = "Send a notification when you achieve a new personal best",
		position = 1,
		section = personalBestSection

	)
	default boolean notifyOnPersonalBest()
	{
		return true;
	}

	@ConfigItem(
		keyName = "messagePersonalBest",
		name = "New PB notification message",
		description = "The message with which to notify you of a new personal best",
		position = 2,
		section = personalBestSection
	)
	default String personalBestMessage()
	{
		return "New personal best!";
	}

	@ConfigItem(
		keyName = "notifyOnPet",
		name = "Notify on pet drop",
		description = "Send a notification when you receive a pet",
		position = 3,
		section = petSection
	)
	default boolean notifyOnPet()
	{
		return true;
	}

	@ConfigItem(
		keyName = "followPet",
		name = "Following pet notification message",
		description = "The message with which to notify you of a new follower <br> (You have a funny feeling like you're being followed)",
		position = 4,
		section = petSection

	)
	default String PetFollowMessage()
	{
		return "Congratulations! You just received a pet!";
	}

	@ConfigItem(
		keyName = "backpackPet",
		name = "Backpack pet notification message",
		description = "The message with which to notify you of a new pet in your inventory <br> (You feel something weird sneaking into your backpack)",
		position = 5,
		section = petSection

	)
	default String PetBackpackMessage()
	{
		return "Congratulations! You just received a pet! Check your inventory!";
	}

	@ConfigItem(
		keyName = "dupePet",
		name = "Duplicate pet notification message",
		description = "The message with which to notify you of a duplicate pet <br> (You have a funny feeling like you would have been followed...)",
		position = 6,
		section = petSection

	)
	default String PetDupeMessage()
	{
		return "Congratulations! You just received a pet, again...";
	}
}
