package com.palmergames.bukkit.TownyChat.config;

public enum ChatConfigNodes {
	VERSION_HEADER("version", "", ""),
	VERSION(
			"version.version",
			"",
			"# This is the current version of Towny.  Please do not edit."),
	LAST_RUN_VERSION(
			"version.last_run_version",
			"",
			"# This is for showing the changelog on updates.  Please do not edit."),
	CHATCONFIGCOMMENTS(
			"chatconfigcomments",
			"",
			"",
			"############################################################",
			"# +------------------------------------------------------+ #",
			"# |                    ChatConfig.yml                    | #",
			"# +------------------------------------------------------+ #",
			"############################################################",
			"#",
			"# The formats below will specify the changes made to the player chat when talking.",
			"#",
			"# {worldname} - Displays the world the player is currently in.",
			"# {town} - Displays town name if a member of a town.",
			"# {townformatted} - Displays town name (if a member of a town) using tag_format.town.",
			"# {towntag} - Displays the formated town tag (if a member of a town) using modify_chat.tag_format.town.",
			"# {towntagoverride} - Displays the formated town tag (if a member of a town and present) or falls back to the full name (using modify_chat.tag_format.town).",
			"#",
			"# {nation} - Displays nation name if a member of a nation.",
			"# {nationformatted} - Displays nation name (if a member of a nation) using tag_format.town.",
			"# {nationtag} - Displays the formated nation tag (if a member of a nation) using modify_chat.tag_format.nation.",
			"# {nationtagoverride} - Displays the formated nation tag (if a member of a nation and present) or falls back to the full name (using modify_chat.tag_format.nation).",
			"#",
			"# {townytag} - Displays the formated town/nation tag as specified in modify_chat.tag_format.both.",
			"# {townyformatted} - Displays the formated full town/nation names as specified in modify_chat.tag_format.both.",
			"# {townytagoverride} - Displays the formated town/nation tag (if present) or falls back to the full names (using modify_chat.tag_format.both).",
			"#",
			"# {title} - Towny resident Title.",
			"# {surname} - Towny resident Surname.",
			"# {townynameprefix} - Towny name prefix taken from the townLevel/nationLevels.",
			"# {townynamepostfix} - Towny name postfix taken from the townLevel/nationLevels.",
			"# {townyprefix} - Towny resident title, or townynameprefix if no title exists.",
			"# {townypostfix} - Towny resident surname, or townynamepostfix if no surname exists",
			"#",
			"# {townycolor} - Towny name colour for king/mayor/resident.",
			"# {group} - Players group name pulled from your permissions plugin.",
			"# {permuserprefix} - Permission user prefix.",  
			"# {permusersuffix} - Permission user suffix.",
			"# {permgroupprefix} - Permission group prefix.",
			"# {permgroupsuffix} - Permission group suffix.",
			"# {permprefix} - Permission group and user prefix.",
			"# {permsuffix} - Permission group and user suffix.",
			"#",
			"# {playername} - Default player name.",
			"# {modplayername} - Modified player name (use if Towny is over writing some other plugins changes).",
			"# {msg} - The message sent.",
			"#",
			"# {channelTag} - Defined in the channels entry in Channels.yml.",
			"# {msgcolour} - Defined in the channels entry in Channels.yml.",
			"#",
			"# Text colouring",
			"# --------------",
			"# Black = &0, Navy = &1, Green = &2, Blue = &3, Red = &4",
			"# Purple = &5, Gold = &6, LightGray = &7, Gray = &8",
			"# DarkPurple = &9, LightGreen = &a, LightBlue = &b",
			"# Rose = &c, LightPurple = &d, Yellow = &e, White = &f",
			"#",
			"# Text altering",
			"# -------------",
			"# Bold = &l, Italics = &o, Underlined = &n,", 
			"# Magic = &k, Strike = &m, Reset = &r",
			"#",
			"# Hex Chat Coloring",
			"# -----------------",
			"# Valid formats: ",
			"# #RRGGBB", 
			"# &#RRGGBB", 
			"# {#RRGGBB}"),
			
	CHANNEL_FORMATS(
			"channel_formats",
			"",
			""),
	CHANNEL_FORMATS_GLOBAL(
			"channel_formats.global",
			"{channelTag} {worldname}{townytagoverride}{townycolor}{permprefix}{group} {townyprefix}{modplayername}{townypostfix}{permsuffix}&f:{msgcolour} {msg}",
		    "# This is the format which will be used for GLOBAL chat/channels.",
		    "# This is also the format used when you have modify_chat.enable: true, but use other plugins to handle chat."),
	CHANNEL_FORMATS_TOWN(
			"channel_formats.town",
			"{channelTag} {townycolor}{permprefix}{townyprefix}{playername}{townypostfix}{permsuffix}&f:{msgcolour} {msg}",
		    "# TOWN channel types."),
	CHANNEL_FORMATS_NATION(
			"channel_formats.nation",
			"{channelTag} {towntagoverride}{townycolor}{permprefix}{townyprefix}{playername}{townypostfix}{permsuffix}&f:{msgcolour} {msg}",
		    "# NATION channel types."),
	CHANNEL_FORMATS_ALLIANCE(
			"channel_formats.alliance",
			"{channelTag} {towntagoverride}{townycolor}{permprefix}{townyprefix}{playername}{townypostfix}{permsuffix}&f:{msgcolour} {msg}",
		    "# ALLIANCE channel types."),
	CHANNEL_FORMATS_DEFAULT(
			"channel_formats.default",
			"{channelTag} {permprefix}{playername}{permsuffix}&f:{msgcolour} {msg}",
		    "# DEFAULT channel types."),
	TAG_FORMATS(
			"tag_formats",
			"",
			""),
	TAG_FORMATS_WORLD(
			"tag_formats.world",
			"&f[&f%s&f] "),		
	TAG_FORMATS_TOWN(
			"tag_formats.town",
			"&f[&3%s&f] "),		
	TAG_FORMATS_NATION(
			"tag_formats.nation",
			"&f[&e%s&f] "),		
	TAG_FORMATS_BOTH(
			"tag_formats.both",
			"&f[&6%s&f|&3%s&f] ",
			"# First %s is the nation tag, second is the town tag.",
			"# You may also use %t for the town tag and %n for the nation tag!"),
	COLOUR(
			"colour",
			"",
			""),
	COLOUR_KING(
			"colour.king",
			"&6"),
	COLOUR_MAYOR(
			"colour.mayor",
			"&b"),		
	COLOUR_RESIDENT(
			"colour.resident",
			"&f"),
	COLOUR_NOMAD(
			"colour.nomad",
			"&f"),
	MODIFY_CHAT(
			"modify_chat",
			"",
			""),
    MODIFY_CHAT_ENABLE(
    		"modify_chat.enable",
    		"true",
    	    "# When true Towny will format all ChannelTypes,",
    	    "# When false Towny will only format TOWN, NATION, ALLIANCE, DEFAULT types.",
    	    "# When false Towny will not format GLOBAL types, leaving other chat plugins to do the work."),
    MODIFY_CHAT_PER_WORLD(
    		"modify_chat.per_world",
    		"false",
    	    "# If true the chat formats will be read from below to allow per world formatting.",
    	    "# These can then be altered individually."),
    MODIFY_CHAT_ALONE_MESSAGE(
    		"modify_chat.alone_message",
    		"false",
    	    "# If true any player who speaks in a channel in which he cannot be heard,",
    	    "# either by being along in the channel or out-of-range of another player in his chat channel,",
    	    "# that player will see a message saying they cannot be heard."),
    MODIFY_CHAT_ALONE_MESSAGE_STRING(
    		"modify_chat.alone_message_string",
    		"No one in range can hear you or you are alone in this channel.",
    	    "# This allows you to set your alone message."),
    DISPLAY_MODES_SET_ON_JOIN(
    		"display_modes_set_on_join",
    		"true",
    		"",
    		"# If true players will see [Towny] Modes set: general when they log in."),    
    WORLDS("worlds","","");
	


	private final String Root;
	private final String Default;
	private String[] comments;

	private ChatConfigNodes(String root, String def, String... comments) {

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
