package com.palmergames.bukkit.TownyChat.Command;

import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.TownyUniverse;
import com.palmergames.bukkit.util.Colors;

public class ChanListCommand implements CommandExecutor {

	Chat plugin = null;

	public ChanListCommand(Chat instance) {
		this.plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label,	String[] args) {
		// If not our command
		if ((!label.equalsIgnoreCase("chanlist")) && 
			(!label.equalsIgnoreCase("ch") || args.length < 1 || !args[0].equalsIgnoreCase("chanlist"))) {
			TownyMessaging.sendErrorMsg(sender, "[TownyChat] Error: Invalid command!");
			return false;
		}
			
		if (!(sender instanceof Player)) {
			return false; // Don't think it can happen but ...
		}

		Player player = ((Player) sender);
		Map<String, Channel> chanList = plugin.getChannelsHandler().getAllChannels();
		
		// Otherwise, list all the channels, and whether you're in it or not
		TownyMessaging.sendMsg(sender, Colors.Gold + ".oOo.______.[ " + Colors.Yellow + "Your channels" + Colors.Gold + " ].______.oOo");
		TownyMessaging.sendMsg(sender, Colors.Gold + "Channel" + Colors.Gray + " - " + Colors.LightBlue + "(Status)");
		for (Map.Entry<String, Channel> channel : chanList.entrySet()) {
			if (channel.getValue().isPresent(player.getName())) {
				TownyMessaging.sendMsg(sender, Colors.Gold + channel.getKey() + Colors.Gray + " - " + Colors.LightBlue + "In");
			} else {
				TownyMessaging.sendMsg(sender, Colors.Gold + channel.getKey() + Colors.Gray + " - " + Colors.LightBlue + "Out");
			}
		}
		
		return true;
	}
}