package com.hcimsafetycheck;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("hcimsafetycheck")
public interface HCIMSafetyCheckConfig extends Config
{
	@ConfigItem(
			keyName = "showInfoBox",
			name = "Show safe-area infobox",
			description = "Show a green helmet infobox while you're in a safe-death area",
			position = 0
	)
	default boolean showInfoBox()
	{
		return false;
	}
}