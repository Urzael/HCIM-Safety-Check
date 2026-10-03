package com.hcimsafetycheck;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.widgets.Widget;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.ImageUtil;
import net.runelite.client.util.Text;
import java.awt.Color;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
@Slf4j
@PluginDescriptor(
/*
 * The chat icon replacement (sprite loading, <img> name rewriting and chatbox
 * updating) is adapted from the Fake Iron Icon plugin by thatgamerblue:
 * https://github.com/thatgamerblue/runelite-plugins
 */
		name = "HCIM Safety Check"
)
public class HCIMSafetyCheckPlugin extends Plugin
{
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	@Inject
	private HCIMSafetyCheckConfig config;
	private int greenIconId = -1;
	private boolean inSafeArea = false;
	@Inject
	private InfoBoxManager infoBoxManager;
	private SafeAreaInfoBox infoBox;
	@Provides
	HCIMSafetyCheckConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(HCIMSafetyCheckConfig.class);
	}
	@Override
	protected void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			loadSprite();
		}
	}
	@Override
	protected void shutDown()
	{
		greenIconId = -1;
		inSafeArea = false;
		updateInfoBox();
		clientThread.invoke(() -> client.runScript(ScriptID.CHAT_PROMPT_INIT));
	}
	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN)
		{
			greenIconId = -1;
		}
		if (event.getGameState() == GameState.LOGGED_IN && greenIconId == -1)
		{
			loadSprite();
		}
	}
	private void loadSprite()
	{
		clientThread.invoke(() ->
		{
			IndexedSprite[] modIcons = client.getModIcons();
			if (modIcons == null)
			{
				return;
			}
			BufferedImage image = ImageUtil.loadImageResource(getClass(), "hcim_green.png");
			IndexedSprite sprite = ImageUtil.getImageIndexedSprite(image, client);

			IndexedSprite[] newAry = Arrays.copyOf(modIcons, modIcons.length + 1);
			newAry[modIcons.length] = sprite;
			greenIconId = modIcons.length;
			client.setModIcons(newAry);
		});
	}
	@Subscribe
	public void onGameTick(GameTick tick)
	{
		boolean safe = isSafeNow();
		if (safe != inSafeArea)
		{
			inSafeArea = safe;
			client.runScript(ScriptID.CHAT_PROMPT_INIT);
		}
		updateInfoBox();
	}
	private boolean isSafeNow()
	{
		if (client.getLocalPlayer() == null || client.getVarbitValue(Varbits.ACCOUNT_TYPE) != 3)
		{
			return false;
		}
		WorldPoint wp = WorldPoint.fromLocalInstance(client, client.getLocalPlayer().getLocalLocation());
		return SafeArea.isSafe(wp, client.isInInstancedRegion(), client);
	}
	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (!inSafeArea || greenIconId == -1 || event.getName() == null || client.getLocalPlayer() == null)
		{
			return;
		}
		String name = Text.standardize(event.getName());
		String me = Text.standardize(client.getLocalPlayer().getName());
		if (name.equalsIgnoreCase(me))
		{
			event.getMessageNode().setName("<img=" + greenIconId + ">" + Text.removeTags(event.getName()));
		}
	}
	@Subscribe
	public void onScriptCallbackEvent(ScriptCallbackEvent event)
	{
		if (event.getEventName().equals("setChatboxInput"))
		{
			updateChatbox();
		}
	}
	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		updateChatbox(); // prevents flicker while typing
	}

	private void updateChatbox()
	{
		if (!inSafeArea || greenIconId == -1 || client.getLocalPlayer() == null)
		{
			return;
		}
		Widget input = client.getWidget(InterfaceID.Chatbox.INPUT);
		if (input == null || input.isHidden())
		{
			return;
		}
		String[] parts = input.getText().split(":", 2);
		if (parts.length < 2)
		{
			return;
		}
		String rsn = Text.removeTags(client.getLocalPlayer().getName());
		input.setText("<img=" + greenIconId + ">" + rsn + ":" + parts[1]);
	}
	private static class SafeAreaInfoBox extends InfoBox
	{
		SafeAreaInfoBox(BufferedImage image, Plugin plugin)
		{
			super(image, plugin);
			setTooltip("Safe HCIM death area");
		}
		@Override
		public String getText()
		{
			return "";
		}

		@Override
		public Color getTextColor()
		{
			return Color.WHITE;
		}
	}
	private void updateInfoBox()
	{
		boolean show = inSafeArea && config.showInfoBox();

		if (show && infoBox == null)
		{
			BufferedImage image = ImageUtil.loadImageResource(getClass(), "hcim_green.png");
			infoBox = new SafeAreaInfoBox(image, this);
			infoBoxManager.addInfoBox(infoBox);
		}
		else if (!show && infoBox != null)
		{
			infoBoxManager.removeInfoBox(infoBox);
			infoBox = null;
		}
	}
	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (event.getGroup().equals("hcimsafetycheck"))
		{
			updateInfoBox();
		}
	}
}