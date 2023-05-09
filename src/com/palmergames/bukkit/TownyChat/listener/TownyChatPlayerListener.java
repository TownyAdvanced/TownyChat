package com.palmergames.bukkit.TownyChat.listener;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.TownyChatFormatter;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import com.palmergames.bukkit.towny.utils.MetaDataUtil;
import com.palmergames.bukkit.util.Colors;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UnknownFormatConversionException;
import java.util.WeakHashMap;

public class TownyChatPlayerListener implements Listener  {
	private Chat plugin;
	
	public WeakHashMap<Player, String> directedChat = new WeakHashMap<>();

	public TownyChatPlayerListener(Chat instance) {
		this.plugin = instance;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		
		plugin.getScheduler().runLater(event.getPlayer(), () -> loginPlayer(event.getPlayer()), 2L);

	}

	private void loginPlayer(Player player) {
		checkPlayerForOldMeta(player);
		
		refreshPlayerChannels(player);

		Channel channel = plugin.getChannelsHandler().getDefaultChannel();
		if (channel != null &&  channel.hasPermission(player)) {
			plugin.setPlayerChannel(player, channel);
			if (ChatSettings.getShowChannelMessageOnServerJoin())
				TownyMessaging.sendMessage(player, Translatable.of("tc_you_are_now_talking_in_channel", channel.getName()));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		refreshPlayerChannels(event.getPlayer());
	}
	
	private void refreshPlayerChannels(Player player) {
		plugin.getChannelsHandler().getAllChannels().values().stream().forEach(channel -> channel.forgetPlayer(player));
	}
	
	@EventHandler(ignoreCancelled = true)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();

		// Check if the message contains colour codes we need to remove or parse.
		testColourCodes(event, player);

		// Check if essentials has this player muted.
		if (!isEssentialsMuted(player)) {
			
			boolean forceGlobal = ChatSettings.isExclamationPoint() ? event.getMessage().startsWith("!") : false;

			/*
			 * If this was directed chat send it via the relevant channel
			 */
			if (directedChat.containsKey(player)) {
				Channel channel = plugin.getChannelsHandler().getChannel(directedChat.get(player));
				
				if (channel != null) {
					if (isMutedOrSpam(event, channel, player)) {
						directedChat.remove(player);
						return;
					}
					channel.chatProcess(event);
					if (!Chat.usingEssentialsDiscord || event.isCancelled()) {
						directedChat.remove(player);
					}
					return;
				}
				directedChat.remove(player);
			}
			
			/*
			 * Check the player for any channel modes.
			 */
			Channel channel = plugin.getPlayerChannel(player);
			if (!forceGlobal && channel != null && channel.hasPermission(player)) {
				if (isMutedOrSpam(event, channel, player))
					return;
				channel.chatProcess(event);
				return;
			}
			
			/*
			 *  Find a global channel this player has permissions for.
			 */
			channel = plugin.getChannelsHandler().getActiveChannel(player, channelTypes.GLOBAL, forceGlobal);
			if (channel != null) {
				if (isMutedOrSpam(event, channel, player))
					return;
				if (forceGlobal)
					event.setMessage(event.getMessage().substring(1));
				channel.chatProcess(event);
				return;
			}
		}

		/*
		 * We found no channels available so modify the chat (if enabled) and exit.
		 */
		if (ChatSettings.isModify_chat()) {
			Resident resident = TownyAPI.getInstance().getResident(player);
			if (resident == null)
				return;
			// Nuke the channeltag and message colouring, but apply the remaining format.
			String format = ChatSettings.getChannelFormat(player, channelTypes.GLOBAL).replace("{channelTag}", "").replace("{msgcolour}", "");

			// format is left to store the original non-PAPI-parsed chat format.
			String newFormat = format;

			// Parse any PAPI placeholders.
			if (Chat.usingPlaceholderAPI)
				newFormat = PlaceholderAPI.setPlaceholders(player, format);

			// Attempt to apply the new format.
			catchFormatConversionException(event, format, newFormat);

			// Fire the LocalTownyChatEvent.
			LocalTownyChatEvent chatEvent = new LocalTownyChatEvent(event, resident);

			// Format the chat line, replacing the TownyChat chat tags.
			newFormat = TownyChatFormatter.getChatFormat(chatEvent);

			// Attempt to apply the new format.
			catchFormatConversionException(event, format, newFormat);

			// Set the format based on the global channelformat, with channeltag and msgcolour removed.
			event.setFormat(newFormat);
		}
	}

	/**
	 * Remove colour tags that a player doesn't not have permission for,
	 * and colour messages if the player is allowed.
	 * 
	 * @param event {@link AsyncPlayerChatEvent} which has been fired.
	 * @param player {@link Player} which has spoken.
	 */
	private void testColourCodes(AsyncPlayerChatEvent event, Player player) {
		if ((event.getMessage().contains("&L") || event.getMessage().contains("&l"))
				&& !player.hasPermission("townychat.chat.format.bold"))
			event.setMessage(event.getMessage().replaceAll("&L", "").replaceAll("&l", ""));

		if ((event.getMessage().contains("&O") || event.getMessage().contains("&o")) 
				&& !player.hasPermission("townychat.chat.format.italic"))
			event.setMessage(event.getMessage().replaceAll("&O", "").replaceAll("&o", ""));

		if ((event.getMessage().contains("&K") || event.getMessage().contains("&k")) 
				&& !player.hasPermission("townychat.chat.format.magic"))
			event.setMessage(event.getMessage().replaceAll("&K", "").replaceAll("&k", ""));

		if ((event.getMessage().contains("&N") || event.getMessage().contains("&n"))
			&& !player.hasPermission("townychat.chat.format.underlined"))
			event.setMessage(event.getMessage().replaceAll("&N", "").replaceAll("&n", ""));

		if ((event.getMessage().contains("&M") || event.getMessage().contains("&m"))
				&& !player.hasPermission("townychat.chat.format.strike"))
			event.setMessage(event.getMessage().replaceAll("&M", "").replaceAll("&m", ""));

		if ((event.getMessage().contains("&R") || event.getMessage().contains("&r"))
				&&!player.hasPermission("townychat.chat.format.reset"))
			event.setMessage(event.getMessage().replaceAll("&R", "").replaceAll("&r", ""));

		if (player.hasPermission("townychat.chat.color"))
			event.setMessage(Colors.translateColorCodes(event.getMessage()));
	}

	/**
	 * Is this player Muted via Essentials?
	 * 
	 * @param player {@link Player} speaking.
	 * @return true if muted by Essentials.
	 */
	private boolean isEssentialsMuted(Player player) {
		// Check if essentials has this player muted.
		if (plugin.isEssentialsMuted(player)) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_unable_to_talk_essentials_mute"));
			return true;
		}
		return false;
	}

	/**
	 * Check if the player is channel-muted or channel-spamming and cancel the
	 * {@link AsyncPlayerChatEvent} if this is the case.
	 * 
	 * @param event {@link AsyncPlayerChatEvent} which has fired.
	 * @param channel {@link Channel} being spoken in to.
	 * @param player {@link Player} speaking.
	 * @return true if the chat is muted or spammed.
	 */
	private boolean isMutedOrSpam(AsyncPlayerChatEvent event, Channel channel, Player player) {
		if (channel.isMuted(player.getName())) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_are_currently_muted_in_channel", channel.getName()));
			event.setCancelled(true);
			return true;
		}
		if (channel.isSpam(player)) {
			event.setCancelled(true);
			return true;
		}
		return false;
	}

	// From TownyChat 0.84-0.95 the symbol used to separate the channels in the meta
	// was not good for non-unicode-using mysql servers.
	private void checkPlayerForOldMeta(Player player) {
		Resident resident = TownyAPI.getInstance().getResident(player);
		if (resident != null && playerHasTCMeta(resident))
			checkIfMetaContainsOldSplitter(resident);
	}

	private boolean playerHasTCMeta(Resident resident) {
		StringDataField icsdf = new StringDataField("townychat_ignoredChannels", "", "Ignored TownyChat Channels");
		StringDataField socsdf = new StringDataField("townychat_soundOffChannels", "", "TownyChat Channels with Sound Toggle Off");
		return MetaDataUtil.hasMeta(resident, icsdf) || MetaDataUtil.hasMeta(resident, socsdf); 
	}

	private void checkIfMetaContainsOldSplitter(Resident resident) {
		StringDataField icsdf = new StringDataField("townychat_ignoredChannels", "", "Ignored TownyChat Channels");
		if (MetaDataUtil.hasMeta(resident, icsdf)) {
			String meta = MetaDataUtil.getString(resident, icsdf);
			if (meta.contains("\uFF0c ")) {
				meta = replaceSymbol(meta);
				MetaDataUtil.setString(resident, icsdf, meta, true);
			}
		}
		StringDataField socsdf = new StringDataField("townychat_soundOffChannels", "", "TownyChat Channels with Sound Toggle Off");
		if (MetaDataUtil.hasMeta(resident, socsdf)) {
			String meta = MetaDataUtil.getString(resident, socsdf);
			if (meta.contains("\uFF0c ")) {
				meta = replaceSymbol(meta);
				MetaDataUtil.setString(resident, socsdf, meta, true);
			}
		}
	}

	private String replaceSymbol(String meta) {
		char[] charray = meta.toCharArray();
		for (int i = 0; i < meta.length(); i++) {
			char n = meta.charAt(i);
			if (n == '\uFF0c')
				charray[i] = ',';
		}
		return new String(charray);
	}

	private void catchFormatConversionException(AsyncPlayerChatEvent event, String format, String newFormat) {
		try {
			event.setFormat(newFormat);
		} catch (UnknownFormatConversionException e) {
			// This exception is usually thrown when a PAPI placeholder did not get parsed
			// and has left behind a % symbol followed by something that String#format
			// cannot handle.
			boolean percentSymbol = format.contains("%" + e.getConversion());
			String errmsg = "TownyChat tried to apply a chat format that is not allowed: '" +
					newFormat + "', because of the " + e.getConversion() + " symbol" +
					(percentSymbol ? ", found after a %. There is probably a PAPIPlaceholder that could not be parsed." : "." +
					" You should attempt to correct this in your towny\\settings\\chatconfig.yml file and use /townychat reload.");
			Chat.getTownyChat().getLogger().severe(errmsg);

			if (percentSymbol)
				// Attempt to remove the unparsed placeholder and send this right back.
				catchFormatConversionException(event, format, purgePAPI(newFormat, "%" + e.getConversion()));
			else
				// Just let the chat go, this results in an error in the log, and TownyChat not being able to format chat.
				event.setFormat(format);
		}
	}

	private String purgePAPI(String format, String startOfPlaceholder) {
		return format.replaceAll(startOfPlaceholder + ".*%", "");
	}
}