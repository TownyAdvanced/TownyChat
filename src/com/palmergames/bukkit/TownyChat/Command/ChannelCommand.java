package com.palmergames.bukkit.TownyChat.Command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.palmergames.bukkit.TownyChat.Chat;

public class ChannelCommand implements CommandExecutor {

	Chat plugin = null;

	public ChannelCommand(Chat instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,	String[] args) {
		if (!label.equalsIgnoreCase("ch") && !label.equalsIgnoreCase("channel")) {
			return false;
		}
		
		if (args.length == 0) {
			return false;
		}

		if (args[0].equalsIgnoreCase("join")) {
			return plugin.getCommand("join").getExecutor().onCommand(sender, cmd, label, args); 
		}

		if (args[0].equalsIgnoreCase("leave")) {
			return plugin.getCommand("leave").getExecutor().onCommand(sender, cmd, label, args); 
		}

		if (args[0].equalsIgnoreCase("mute")) {
			return plugin.getCommand("chmute").getExecutor().onCommand(sender, cmd, label, args); 
		}

		if (args[0].equalsIgnoreCase("unmute")) {
			return plugin.getCommand("chunmute").getExecutor().onCommand(sender, cmd, label, args); 
		}

		if (args[0].equalsIgnoreCase("mutelist")) {
			return plugin.getCommand("mutelist").getExecutor().onCommand(sender, cmd, label, args); 
		}
		
		if (args[0].equalsIgnoreCase("chanlist")) {
			return plugin.getCommand("chanlist").getExecutor().onCommand(sender, cmd, label, args); 
		}

		return false;
	}
}
