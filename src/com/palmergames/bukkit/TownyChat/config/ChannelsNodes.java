package com.palmergames.bukkit.TownyChat.config;

/**
 * Dummy class used to make Channels.yml have update-able comments.
 * 
 * @author LlmDl
 *
 */
public enum ChannelsNodes {
	COMMENTS("channelscomments", "",
			"# This file contains all configuration data for channels.",
			"# The formating for them remains in the chatconfig.yml file, using the Channel's TYPE.",
			"#","#",
			"# COMMANDS",
			"# --------",
			"# In order to swap channels, you must use the desired channel's command ",
			"#   to enter the new chat and leave the old one.",
			"#          eg: /tc (would put you in town chat) and typing it again would not",
			"#          change your channel. To get back to general chat, you can do /g. ",
			"#","#",
			"# TYPE",
			"# ----",
			"# The channel type is either GLOBAL, TOWN, NATION or DEFAULT.",
			"#   These specify what chat formating section they will use and where the chat",
			"#   will go (town goes to all town residents, depending on the range setting)",
			"#","#",
			"# CHANNELTAG",
			"# ----------",
			"# channeltag is applied if the chat format for that channel has the {channelTag}",
			"#","#",
			"# PERMISSION",
			"# ----------",
			"# permission is an optional key in a channel, when it is set the given value",
			"#   will be the permission node required to listen/speak in the channel. It is used",
			"#   when either spearkpermission or listenpermission is not set.",
			"#","#",
			"# SPEAKPERMISSION",
			"# ---------------",
			"# speakpermission is an optional key in a channel, when it is set the given value",
			"#   will be the permission node required to speak in the channel.",
			"#","#",
			"# LISTENPERMISSION",
			"# ----------------",
			"# listenpermission is an optional key in a channel, when it is set the given value",
			"#   will be the permission node required to listen to the channel.",
			"#","#",
			"# LEAVEPERMISSION",
			"# ---------------",
			"# leavepermission is an optional key in a channel, when it is set the given value",
			"#   will be the permission node required to listen to the channel.",
			"#","#",
			"# MESSAGECOLOUR",
			"# -------------",
			"# messagecolour sets the colour of the message when sent.",
			"#","#",
			"# RANGE",
			"# -----",
			"# range is a setting which allows greater control over each channel.",
			"#   this will set the maximum distance between players who can hear the message.",
			"#   range is in blocks (if set to a limit).",
			"#",
			"#   -1 = no limits",
			"#	  0 = same world only",
			"#	  any positive value = limited range in the same world.",
			"#","#",
			"# FOCUSABLE",
			"# ---------",
			"# focusable is a setting which allows players to set their focused channel, ",
			"#   ie: using the /tc command with no message. Channels are focusable by default,",
			"#   so there is no point to adding focusable: true. You make a channel un-focusable",
			"#   by adding focusable: false.",
			"#","#",
			"# SOUND",
			"# ------------",
			"# Each channel can have a sound by adding sound: SOUND_TO_USE",
			"#   Sounds can be found here: https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Sound.html",
			"#","#",
			"# Text Colouring Hints",
			"# --------------",
			"# Black = &0, Navy = &1, Green = &2, Blue = &3, Red = &4",
			"# Purple = &5, Gold = &6, LightGray = &7, Gray = &8",
			"# DarkPurple = &9, LightGreen = &a, LightBlue = &b",
			"# Rose = &c, LightPurple = &d, Yellow = &e, White = &f"),
	CHANNELS_ROOT("channels", "", "", "");

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
