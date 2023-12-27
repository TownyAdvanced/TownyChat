package com.palmergames.bukkit.TownyChat.config;

/**
 * Dummy class used to make Channels.yml have update-able comments.
 * 
 * @author LlmDl
 *
 */
public enum ChannelsNodes {
	COMMENTS("channelscomments", "",
			"# This file contains all configuration data for the TownyChat channels.",
			"# Each channel is a map of keys and values. You can add and remove channels, as you see fit.",
			"# ",
			"# Channels do not require all of the keys to be set. The keys which are required are:",
			"#    - type",
			"#    - commands",
			"# The other keys are not required.",
			"# ",
			"# The keys are as follows: commands, type, channeltag, messagecolour, permission, leavepermission, speakpermission, listenpermission, sound, range,",
			"# spam_time, focusable, default.",
			"# ",
			"# The TYPE key will determine what formatting is used in a channel. Types' formats are configured in the chatconfig.yml file in the channel_formats section.",
			"# ",
			"# *** Further Explanation of the available keys is below: ***",
			"#","#",
			"# COMMANDS",
			"# --------",
			"# In order to swap channels, you must use the desired channel's command to enter the new chat and leave the old one.",
			"#    eg: /tc (would put you in town chat) and typing it again would not change your channel. To get back to general chat, you can do /g.",
			"# ",
			"# Commands can also be used to speak into a channel, without leaving the channel you are speaking in.",
			"#    eg: '/tc somewords' will send 'somewords' to the townchat channel.",
			"#","#",
			"# TYPE",
			"# ----",
			"# The channel type is either GLOBAL, TOWN, NATION, ALLIANCE, or DEFAULT. These specify what chat formating is used from the chatconfig.yml's channel_formats",
			"# section.)",
			"#","#",
			"# CHANNELTAG",
			"# ----------",
			"# channeltag is what defines how the chatconfig.yml's {channelTag} chat tag will appear in the channel.",
			"#","#",
			"# PERMISSION",
			"# ----------",
			"# permission is an optional key in a channel, when it is set the given value will be the permission node required to listen/speak in the channel. It is used",
			"# when either spearkpermission or listenpermission is not set.",
			"#","#",
			"# SPEAKPERMISSION",
			"# ---------------",
			"# speakpermission is an optional key in a channel, when it is set the given value will be the permission node required to speak in the channel, overriding",
			"# the channel's permission key when players are speaking.",
			"#","#",
			"# LISTENPERMISSION",
			"# ----------------",
			"# listenpermission is an optional key in a channel, when it is set the given value will be the permission node required to listen to the channel, overriding",
			"# the channel's permission key when a player's ability to hear a channel is tested.",
			"#","#",
			"# LEAVEPERMISSION",
			"# ---------------",
			"# leavepermission is an optional key in a channel, when it is set the given value will be the permission node required to listen to the channel, when not",
			"# set this is automatically set to 'towny.chat.leave.{channelname}'.",
			"#","#",
			"# MESSAGECOLOUR",
			"# -------------",
			"# messagecolour is what defines how the chatconfig.yml's {msgcolour} chat tag will appear in the channel.",
			"# ",
			"# Tip: if you want to have message colour determined per-rank on your server this is possible:",
			"# in your chatconfig.yml's channel_formats section add {permsuffix} or {permgroupsuffix} after the {msgcolour} tag. Then in your permission plugin give the",
			"# ranks you want to have coloured chat a suffix consisting of a colour code. Ranks without a suffix set will have the normal channel message colour used.",
			"#","#",
			"# RANGE",
			"# -----",
			"# range is what defines whether a channel has distance-based or world-based listening logic. Someone outside of the configured range will not see messages",
			"# in chat and cannot be 'heard' by others outside of the range. Range is in blocks (when set above 0).",
			"# ",
			"# Options for range:",
			"#   -1 = no limits, everyone will hear messages if they have permission.",
			"#	  0 = same world only",
			"#	  any positive value = limited range in the same world.",
			"#","#",
			"# SPAM_TIME",
			"# ---------",
			"# spam_time sets how long between chatting a player must wait, to prevent players from flooding chat with spam.",
			"#","#",
			"# FOCUSABLE",
			"# ---------",
			"# focusable is a setting which allows players to set their focused channel, ie: using the /tc command with no message. Channels are focusable by default, so",
			"# there is no point to adding focusable: true. You make a channel un-focusable by adding the key/value 'focusable: false'.",
			"#","#",
			"# DEFAULT",
			"# -------",
			"# default is a setting which decides which channel is the default when a player logs in to the server.",
			"#","#",
			"# HOOKED",
			"# ------",
			"# hooked is a setting which decides if a channel can be view by other plugins which are using the TownyChat AsyncChatHookEvent. You may find that a plugin",
			"# you are using will ask for 'hooked: true' to be added to a channel, in order for that plugin to further modify chat being spoken in town/nation channels.",
			"#","#",
			"# SOUND",
			"# -----",
			"# Each channel can have a sound by adding the key/value 'sound: SOUND_TO_USE'",
			"# Sounds can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html",
			"#","#",
			"# Text Colouring Hints",
			"# --------------------",
			"# Black = &0, Navy = &1, Green = &2, Blue = &3, Red = &4, Purple = &5, Gold = &6, LightGray = &7, Gray = &8, DarkPurple = &9, LightGreen = &a,",
			"# LightBlue = &b, Rose = &c, LightPurple = &d, Yellow = &e, White = &f"),
	CHANNELS_ROOT("Channels", "", "", "");

	private final String Root;
	private final String Default;
	private String[] comments;

	private ChannelsNodes(String root, String def, String... comments) {

		this.Root = root;
		this.Default = def;
		this.comments = comments;
	}

	/**
	 * Retrieves the root for a config option
	 *
	 * @return The root for a config option
	 */
	public String getRoot() {

		return Root;
	}

	/**
	 * Retrieves the default value for a config path
	 *
	 * @return The default value for a config path
	 */
	public String getDefault() {

		return Default;
	}

	/**
	 * Retrieves the comment for a config path
	 *
	 * @return The comments for a config path
	 */
	public String[] getComments() {

		if (comments != null) {
			return comments;
		}

		String[] comments = new String[1];
		comments[0] = "";
		return comments;
	}
}
