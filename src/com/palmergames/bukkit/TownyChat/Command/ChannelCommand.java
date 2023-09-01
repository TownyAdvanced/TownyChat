package com.palmergames.bukkit.TownyChat.Command;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.events.PlayerJoinChatChannelEvent;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.towny.object.Translator;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.util.ChatTools;
import com.palmergames.bukkit.util.Colors;
import com.palmergames.util.StringMgmt;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ChannelCommand extends BaseCommand implements CommandExecutor {

	private static Chat plugin;
	private static final List<String> channel_help = new ArrayList<String>();

	static {

		channel_help.add(ChatTools.formatTitle("/channel"));
		//TODO: Add lang strings for description!
		channel_help.add(ChatTools.formatCommand("", "/channel", "join [Channel]", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "leave [Channel]", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "list ", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "mute [Channel] [Player]", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "unmute [Channel] [Player]", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "mutelist [Channel]", ""));
		channel_help.add(ChatTools.formatCommand("", "/channel", "soundtoggle [Channel]", ""));

	}

	public ChannelCommand(Chat instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (sender instanceof Player) {
			Player player = (Player) sender;
			if (command.getName().equalsIgnoreCase("channel")) {
				if (label.equalsIgnoreCase("join")) {
					parseChannelJoin(player, args);
					return true;
				}
				if (label.equalsIgnoreCase("leave")) {
					parseChannelLeave(player, args);
					return true;
				}
				if (label.equalsIgnoreCase("mutelist")) {
					parseChannelMuteList(player, args);
					return true;
				}
				if (label.equalsIgnoreCase("chmute")) {
					parseChannelMute(player, args, true);
					return true;
				}
				if (label.equalsIgnoreCase("chunmute")) {
					parseChannelMute(player, args, false);
					return true;
				}
				parseChannelCommand(player, args);
				return true;
			}
		} else {
			// Console
			for (String line : channel_help) {
				TownyMessaging.sendMessage(sender, Colors.strip(line));
				return true;
			}
		}
		return false;
	}

	private void parseChannelCommand(Player player, String[] split) {
		// So they just type /channel , We should probably send them to the help menu..
		if (split.length == 0 || split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("?")) { 
			for (String line : channel_help)
				TownyMessaging.sendMessage(player, line);
		} else if (split[0].equalsIgnoreCase("join")) { // /channel join [chn] (/label args[0] args[1] length = 2.)
			parseChannelJoin(player, StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("leave")) {
			parseChannelLeave(player, StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("mute")) {
			parseChannelMute(player, StringMgmt.remFirstArg(split), true);
		} else if (split[0].equalsIgnoreCase("unmute")) {
			parseChannelMute(player, StringMgmt.remFirstArg(split), false);
		} else if (split[0].equalsIgnoreCase("mutelist")) {
			parseChannelMuteList(player, StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("list")) {
			parseChannelList(player);
		} else if (split[0].equalsIgnoreCase("soundtoggle")) {
			parseChannelSoundToggle(player, StringMgmt.remFirstArg(split));
		} else {
			TownyMessaging.sendErrorMsg(player, Translatable.of("msg_err_invalid_sub"));
		}
	}

	public static void parseChannelList(Player player) {
		// If not our command
		Map<String, Channel> chanList = plugin.getChannelsHandler().getAllChannels();
		Translator translator = Translator.locale(Translation.getLocale(player));

		TownyMessaging.sendMessage(player, ChatTools.formatTitle("Channels"));
		TownyMessaging.sendMessage(player, Colors.Gold + "Channel" + Colors.Gray + " - " + Colors.LightBlue + translator.of("tc_channel_list_status"));
		for (Map.Entry<String, Channel> channel : chanList.entrySet()) {
			if (channel.getValue().hasListenPermission(player))
				if (channel.getValue().isPresent(player.getName()))
					TownyMessaging.sendMessage(player, Colors.Gold + channel.getKey() + Colors.Gray + " - " + Colors.LightBlue + translator.of("tc_channel_list_in"));
				else
					TownyMessaging.sendMessage(player, Colors.Gold + channel.getKey() + Colors.Gray + " - " + Colors.LightBlue + translator.of("tc_channel_list_out"));
		}
	}

	public static void parseChannelMuteList(Player player, String[] split) {
		if (split.length == 0) {
			for (String line : channel_help) {
				TownyMessaging.sendMessage(player, line);
			}
			return;
		}
		String mutePerm = plugin.getChannelsHandler().getMutePermission();
		String unmutePerm = plugin.getChannelsHandler().getUnmutePermission();
		if ((mutePerm == null && unmutePerm == null) ||
				(mutePerm != null && (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, mutePerm))) ||
				(unmutePerm != null && (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, unmutePerm)))) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_dont_have_perm_mute_list"));
			return;
		}

		Channel chan = plugin.getChannelsHandler().getChannel(split[0]);

		// If we can't find the channel by name, look up all the channel commands for an alias
		if (chan == null) {
			for (Channel chan2 : plugin.getChannelsHandler().getAllChannels().values()) {
				for (String command : chan2.getCommands()) {
					if (command.equalsIgnoreCase(split[0])) {
						chan = chan2;
						break;
					}
				}
				if (chan != null) {
					break;
				}
			}
		}

		if (chan == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_channel_called_channel", split[0]));
			return;
		}

		split[0] = chan.getName();

		// TODO Support paging through this list
		int count = 0;
		String players = "";

		if (chan.hasMuteList()) {
			Iterator<String> iter = chan.getMuteList().iterator();
			boolean first = true;
			while (iter.hasNext()) {
				if (!first) {
					players += Colors.Green + ", " + Colors.White;
				}
				players += iter.next();
				count++;
			}
		}

		if (count == 0) {
			TownyMessaging.sendMessage(player, Translatable.of("tc_err_no_muted_players_in_channel", chan.getName()));
			return;
		}

		TownyMessaging.sendMessage(player, Translatable.of("tc_players_muted_in_channel", count, chan.getName(), players));
	}

	public static void parseChannelMute(Player player, String[] split, boolean mute) {
		if (split.length < 2) {
			for (String line : channel_help) {
				TownyMessaging.sendMessage(player, line);
			}
			return;
		}
		// Split[0] = Channelname
		// Split[1} = Player to be muted
		Player muteePlayer = Bukkit.getPlayer(split[1]);
		if (muteePlayer == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_online_players_with_name", split[1]));
			return;
		}
		Channel chan = plugin.getChannelsHandler().getChannel(split[0]);

		// If we can't find the channel by name, look up all the channel commands for an alias
		if (chan == null) {
			for (Channel chan2 : plugin.getChannelsHandler().getAllChannels().values()) {
				for (String command : chan2.getCommands()) {
					if (command.equalsIgnoreCase(split[0])) {
						chan = chan2;
						break;
					}
				}
				if (chan != null) {
					break;
				}
			}
		}

		if (chan == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_channel_called_channel", split[0]));
			return;
		}

		if (mute) {
			String mutePerm = plugin.getChannelsHandler().getMutePermission();
			if ((mutePerm == null) || (mutePerm != null && (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, mutePerm)))) {
				TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_dont_have_mute_perms"));
				return;
			}

			split[1] = muteePlayer.getName();

			if (TownyUniverse.getInstance().getPermissionSource().isTownyAdmin(muteePlayer)) {
				TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cant_mute_admin"));
				return;
			}

			String unmutePerm = plugin.getChannelsHandler().getUnmutePermission();
			if ((mutePerm != null && (TownyUniverse.getInstance().getPermissionSource().testPermission(muteePlayer, mutePerm))) ||
					(unmutePerm != null && (TownyUniverse.getInstance().getPermissionSource().testPermission(muteePlayer, unmutePerm)))) {
				TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cant_mute_chat_mod"));
				return;
			}

			if (!chan.mute(split[1])) {
				TownyMessaging.sendMessage(player, Translatable.of("tc_player_already_muted_in_channel", chan.getName()));
				return;
			}

			TownyMessaging.sendMessage(player, Translatable.of("tc_player_is_now_muted_in_channel", split[1], chan.getName()));
		} else if (!mute) {
			String unmutePerm = plugin.getChannelsHandler().getUnmutePermission();
			if ((unmutePerm == null) || (unmutePerm != null && (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, unmutePerm)))) {
				TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_dont_have_unmute_perm"));
				return;
			}

			split[1] = muteePlayer.getName();


			if (!chan.unmute(split[1])) {
				TownyMessaging.sendMessage(player, Translatable.of("tc_player_is_not_muted_in_channel", chan.getName()));
				return;
			}

			TownyMessaging.sendMessage(player, Translatable.of("tc_player_is_now_unmuted_in_channel", split[1], chan.getName()));
			return;
		}
	}

	public static void parseChannelLeave(Player player, String[] split) {
		if (split.length == 0) {
			for (String line : channel_help) {
				TownyMessaging.sendMessage(player, line);
			}
			return;
		}
		Channel chan = plugin.getChannelsHandler().getChannel(split[0]);

		// If we can't find the channel by name, look up all the channel commands for an alias
		if (chan == null) {
			for (Channel chan2 : plugin.getChannelsHandler().getAllChannels().values()) {
				for (String command : chan2.getCommands()) {
					if (command.equalsIgnoreCase(split[0])) {
						chan = chan2;
						break;
					}
				}
				if (chan != null) {
					break;
				}
			}
		}

		if (chan == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_channel_called_channel", split[0]));
			return;
		}

		// You can leave if:
		// - Towny recognizes your permissions plugin AND
		// - channel has leaving permission set AND [by default they always do and you can provide your own permission name]
		// - player has leaving permission set      [by default they don't]
		String leavePerm = chan.getLeavePermission();
		if (leavePerm == null ||
				(leavePerm != null && (!TownyUniverse.getInstance().getPermissionSource().testPermission(player, leavePerm)))) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cannot_leave_channel", chan.getName()));
			return;
		}

		// If we fail you weren't in there to start with
		if (!chan.leave(player)) {
			TownyMessaging.sendMessage(player, Translatable.of("tc_you_already_left_channel", chan.getName()));
			return;
		}

		// Announce it
		TownyMessaging.sendMessage(player, Translatable.of("tc_you_left_channel", chan.getName()));

		// Find what the next channel is if any
		Channel nextChannel = null;
		if (plugin.getChannelsHandler().getDefaultChannel() != null && plugin.getChannelsHandler().getDefaultChannel().isPresent(player.getName()))
			nextChannel = plugin.getChannelsHandler().getDefaultChannel();

		if (nextChannel == null)
			nextChannel = plugin.getChannelsHandler().getActiveChannel(player, channelTypes.GLOBAL);

		// If the new channel is not us, announce it
		if (nextChannel != null && !chan.getName().equalsIgnoreCase(nextChannel.getName())) {
			TownyMessaging.sendMessage(player, Translatable.of("tc_you_are_now_talking_in_channel",nextChannel.getName()));
			plugin.setPlayerChannel(player, nextChannel);
		}
	}

	public static void parseChannelJoin(Player player, String[] split) {
		if (split.length == 0) {
			for (String line : channel_help) {
				TownyMessaging.sendMessage(player, line);
			}
			return;
		}
		// Removed first argument of the list so now args[0] is the channel he wants to join
		// If we call this from /join, we just won't remove the argument!
		Channel chan = plugin.getChannelsHandler().getChannel(split[0]);

		if (chan == null) { // From Old JoinCommand (If we don't find a channel with said name, check aliases)
			for (Channel chan2 : plugin.getChannelsHandler().getAllChannels().values()) {
				for (String command : chan2.getCommands()) {
					if (command.equalsIgnoreCase(split[0])) {
						chan = chan2;
						break;
					}
				}
				if (chan != null) {
					break;
				}
			}
		}

		if (chan == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_channel_called_channel", split[0]));
			return;
		}
		// You can join if:
		// - Towny doesn't recognize your permissions plugin
		// - channel has no permission set OR  [by default they don't]
		//   - channel has permission set AND:
		//     - player has channel permission
		if (!chan.hasListenPermission(player)) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_you_cannot_join_channel", chan.getName()));
			return;
		}

		if (!chan.join(player)) {
			TownyMessaging.sendMessage(player, Translatable.of("tc_you_are_already_in_channel", chan.getName()));
			return;
		}

		Bukkit.getPluginManager().callEvent(new PlayerJoinChatChannelEvent(player, chan));

		TownyMessaging.sendMessage(player, Translatable.of("tc_you_joined_channel", Colors.translateColorCodes(chan.getMessageColour()) + chan.getName()));
	}

	public static void parseChannelSoundToggle(Player player, String[] split) {
		if (split.length == 0) {
			for (String line : channel_help) {
				TownyMessaging.sendMessage(player, line);
			}
			return;
		}
		Channel chan = plugin.getChannelsHandler().getChannel(split[0]);

		if (chan == null) {
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_no_channel_called_channel", split[0]));
			return;
		}

		if (chan.isSoundMuted(player)) {
			chan.unmuteSound(player);
			TownyMessaging.sendMessage(player, Translatable.of("tc_player_channel_sound_unmuted"));
		} else {
			chan.muteSound(player);
			TownyMessaging.sendMessage(player, Translatable.of("tc_player_channel_sound_muted"));
		}
	}
}
