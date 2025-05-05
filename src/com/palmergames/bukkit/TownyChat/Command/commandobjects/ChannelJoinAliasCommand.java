package com.palmergames.bukkit.TownyChat.Command.commandobjects;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.events.PlayerJoinChatChannelEvent;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.util.StringMgmt;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

public class ChannelJoinAliasCommand extends BukkitCommand {
	public Channel channel;
	public Chat plugin;

	public ChannelJoinAliasCommand(String name, Channel channel, Chat plugin) {
		super(name);
		this.channel = channel;
		this.plugin = plugin;
		this.description = name + " - Channel command join alias for " + channel.getName();
		this.usageMessage = "/" + name;
	}

	@Override
	public boolean execute(CommandSender commandSender, String label, String[] args) {
		if (!channel.getCommands().contains(this.getName()))
			return true;

		if (!(commandSender instanceof Player player)) {
			TownyMessaging.sendMsg("You may not use this command as the console!");
			return false;
		}

		// So a player has ran some /g command or something
		String message = args.length > 0 ? StringMgmt.join(args, " ") : "";

		if (message.isEmpty()) {
			// There was no message, just a channel command, they are trying to switch channels.
			Channel chan = plugin.getPlayerChannel(player); 
			if (chan != null && chan.getName().equalsIgnoreCase(channel.getName())) {
				// They're already in that channel.
				TownyMessaging.sendMessage(player, Translatable.of("tc_you_are_already_in_channel", channel.getName()));
				return true;
			}
			// You can join a channel if:
			// - Towny doesn't recognize your permissions plugin
			// - channel has no permission set [by default they don't] OR
			//   - channel has permission set AND:
			//     - player has channel permission
			// - the Channel is designated as being joinable (which they are by default.)
			if (!channel.hasSpeakPermission(player) || !channel.isFocusable()) {
				TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cannot_join_channel", channel.getName()));
				return true;
			}

			plugin.setPlayerChannel(player, channel);
			TownyMessaging.sendMessage(player, Translatable.of("tc_you_are_now_talking_in_channel", Colors.translateColorCodes(channel.getMessageColour()) + channel.getName()));
			Bukkit.getPluginManager().callEvent(new PlayerJoinChatChannelEvent(player, channel));
			return true;
		}

		// The player is sending a message into a channel using /g somemessage
		if (channel.isMuted(player.getName())) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_are_currently_muted_in_channel", channel.getName()));
			return true;
		}
		// You can speak in a channel if:
		// - Towny doesn't recognize your permissions plugin
		// - channel has no permission set [by default they don't] OR
		//   - channel has permission set AND:
		//     - player has channel permission
		if (!channel.hasSpeakPermission(player)) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cannot_join_channel", channel.getName()));
			return true;
		}

		plugin.getTownyPlayerListener().directedChat.put(player, channel.getName().toLowerCase());

		final String msg = message;

		// https://www.spigotmc.org/threads/plugins-triggering-commands-async.31815/
		TaskScheduler scheduler = plugin.getScheduler();
		if (!scheduler.isEntityThread(player)) {
			scheduler.run(player, () -> player.chat(msg));
		} else {
			player.chat(msg);
		}
		return true;
	}
}
