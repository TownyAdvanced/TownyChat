package com.palmergames.bukkit.TownyChat.listener;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.HexFormatter;
import com.palmergames.bukkit.TownyChat.TownyChatFormatter;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.tasks.onPlayerJoinTask;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.TownyUniverse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.WeakHashMap;

public class TownyChatPlayerListener implements Listener  {
	private Chat plugin;
	
	public WeakHashMap<Player, String> directedChat = new WeakHashMap<>();

	public TownyChatPlayerListener(Chat instance) {
		this.plugin = instance;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		Player player = event.getPlayer();
		String name = player.getName();
		for (Channel channel : plugin.getChannelsHandler().getAllChannels().values()) {
			// If the channel is auto join, they will be added
			// If the channel is not auto join, they will marked as absent
			// TODO: Only do this for channels the user has permissions for
			channel.forgetPlayer(name);
		}
		Channel channel = plugin.getChannelsHandler().getDefaultChannel();
		if (channel != null) {
			// See if we have permissions for the default channel
			channel = plugin.getChannelsHandler().getChannel(player, channel.getCommands().get(0));
			if (channel != null) {
				// Schedule it as delayed task because Towny may not have processed this just yet
				// and would reset the mode otherwise
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new onPlayerJoinTask(plugin, player, channel), 5);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerQuit(final PlayerQuitEvent event) {
		String name = event.getPlayer().getName(); 
		for (Channel channel : plugin.getChannelsHandler().getAllChannels().values()) {
			// If the channel is auto join, they will be added
			// If the channel is not auto join, they will marked as absent
			channel.forgetPlayer(name);
		}
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		
		if (event.getMessage().contains("&L") || event.getMessage().contains("&l") ||
			event.getMessage().contains("&O") || event.getMessage().contains("&o") ||
			event.getMessage().contains("&N") || event.getMessage().contains("&n") ||
			event.getMessage().contains("&K") || event.getMessage().contains("&k") ||
			event.getMessage().contains("&M") || event.getMessage().contains("&m") ||
			event.getMessage().contains("&R") || event.getMessage().contains("&r") ) {			
			if (!player.hasPermission("townychat.chat.format.bold")) {			
				event.setMessage(event.getMessage().replaceAll("&L", ""));
				event.setMessage(event.getMessage().replaceAll("&l", ""));
			}
			if (!player.hasPermission("townychat.chat.format.italic")) {			
				event.setMessage(event.getMessage().replaceAll("&O", ""));
				event.setMessage(event.getMessage().replaceAll("&o", ""));
			}
			if (!player.hasPermission("townychat.chat.format.magic")) {			
				event.setMessage(event.getMessage().replaceAll("&K", ""));
				event.setMessage(event.getMessage().replaceAll("&k", ""));
			}
			if (!player.hasPermission("townychat.chat.format.underlined")) {			
				event.setMessage(event.getMessage().replaceAll("&N", ""));
				event.setMessage(event.getMessage().replaceAll("&n", ""));
			}
			if (!player.hasPermission("townychat.chat.format.strike")) {			
				event.setMessage(event.getMessage().replaceAll("&M", ""));
				event.setMessage(event.getMessage().replaceAll("&m", ""));
			}
			if (!player.hasPermission("townychat.chat.format.reset")) {			
				event.setMessage(event.getMessage().replaceAll("&R", ""));
				event.setMessage(event.getMessage().replaceAll("&r", ""));
			}
		}
		
		if(player.hasPermission("townychat.chat.color")) {
			event.setMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));

			if (Towny.is116Plus()) {
				event.setMessage(HexFormatter.translateHexColors(event.getMessage()));
			}
		}
		
		// Check if essentials has this player muted.
		if (!isMuted(player)) {

			/*
			 * If this was directed chat send it via the relevant channel
			 */
			if (directedChat.containsKey(player)) {
				Channel channel = plugin.getChannelsHandler().getChannel(player, directedChat.get(player));
				directedChat.remove(player);
				
				if (channel != null) {
					// Notify player he is muted
					if (channel.isMuted(player.getName())) {
						TownyMessaging.sendErrorMsg(player, String.format(Translation.of("tc_err_you_are_currently_muted_in_channel"), channel.getName()));
						event.setCancelled(true);
						return;
					}
					if (channel.isSpam(player)) {
						event.setCancelled(true);
						return;
					}
					channel.chatProcess(event);
					return;
				}
			}
			
			/*
			 * Check the player for any channel modes.
			 */
			for (Channel channel : plugin.getChannelsHandler().getAllChannels().values()) {
				if (plugin.getTowny().hasPlayerMode(player, channel.getName())) {
					// Notify player he is muted
					if (channel.isMuted(player.getName())) {
						TownyMessaging.sendErrorMsg(player, String.format(Translation.of("tc_err_you_are_currently_muted_in_channel"), channel.getName()));
						event.setCancelled(true);
						return;
					}
					if (channel.isSpam(player)) {
						event.setCancelled(true);
						return;
					}
					/*
					 *  Channel Chat mode set
					 *  Process the chat
					 */
					channel.chatProcess(event);
					return;
				}
			}
			
			// Find a global channel this player has permissions for.
			Channel channel = plugin.getChannelsHandler().getActiveChannel(player, channelTypes.GLOBAL);
					
			if (channel != null) {
				// Notify player he is muted
				if (channel.isMuted(player.getName())) {
					TownyMessaging.sendErrorMsg(player, String.format(Translation.of("tc_err_you_are_currently_muted_in_channel"), channel.getName()));
					event.setCancelled(true);
					return;
				}
				if (channel.isSpam(player)) {
					event.setCancelled(true);
					return;
				}
				channel.chatProcess(event);
				return;
			}
		}

		/*
		 * We found no channels available so modify the chat (if enabled) and exit.
		 */
		if (ChatSettings.isModify_chat()) {
			try {
				event.setFormat(ChatSettings.getRelevantFormatGroup(player).getGLOBAL().replace("{channelTag}", "").replace("{msgcolour}", ""));
				Resident resident = TownyUniverse.getInstance().getDataSource().getResident(player.getName());

				LocalTownyChatEvent chatEvent = new LocalTownyChatEvent(event, resident);

				if (Towny.is116Plus()) {
					event.setFormat(HexFormatter.translateHexColors(TownyChatFormatter.getChatFormat(chatEvent)));
				} else {
					event.setFormat(TownyChatFormatter.getChatFormat(chatEvent));
				}

			} catch (NotRegisteredException e) {
				// World or resident not registered with Towny
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Is this player Muted via Essentials?
	 * 
	 * @param player
	 * @return true if muted
	 */
	private boolean isMuted(Player player) {
		// Check if essentials has this player muted.
		if (plugin.getTowny().isEssentials()) {
			try {
				if (plugin.getTowny().getEssentials().getUser(player).isMuted()) {
					TownyMessaging.sendErrorMsg(player, Translation.of("tc_err_unable_to_talk_essentials_mute"));
					return true;
				}
			} catch (TownyException e) {
				// Get essentials failed
			}
			return false;
		}
		return false;
	}
}