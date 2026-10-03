package com.hcimsafetycheck;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class HCIMSafetyCheckPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(HCIMSafetyCheckPlugin.class);
		RuneLite.main(args);
	}
}