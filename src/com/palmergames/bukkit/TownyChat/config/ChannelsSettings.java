package com.palmergames.bukkit.TownyChat.config;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.StandardChannel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.util.FileMgmt;
import com.palmergames.util.StringMgmt;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ChannelsSettings {

	private static final String CHANNELS_ROOT = "Channels";
	private static CommentedConfiguration channelConfig, newChannelConfig;
	private final static List<String> DEFAULT_CHANNELS = Arrays.asList("general","town","nation","alliance","admin","mod","local");
	
	/**
	 * 
	 * @return true if the channels.yml has loaded. 
	 * @throws IOException
	 */
	public static boolean loadCommentedChannelsConfig() {
		String filepath = Chat.getTownyChat().getChannelsConfigPath();
		if (FileMgmt.checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the config.yml into memory
			channelConfig = new CommentedConfiguration(file.toPath());
			if (!channelConfig.load()) {
				Bukkit.getLogger().severe("[TownyChat] Failed to load Channels.yml!");
				Bukkit.getLogger().severe("[TownyChat] Please check that the file passes a YAML Parser test:");
				Bukkit.getLogger().severe("[TownyChat] Online YAML Parser: https://yaml-online-parser.appspot.com/");
				return false;
			}
			setDefaults(file);
			channelConfig.save();
			if (!loadChannels())
				return false;
		}
		return true;
	}

	/**
	 * Builds a new channels.yml reading old channels.yml data,
	 * and setting new nodes to default values.
	 */
	private static void setDefaults(File file) {
		newChannelConfig = new CommentedConfiguration(file.toPath());
		newChannelConfig.load();

		for (ChannelsNodes root : ChannelsNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());

			setNewProperty(root.getRoot(), getValue(root));
		}

		tryAndSetDefaultChannels();

		channelConfig = newChannelConfig;
		newChannelConfig = null;
	}

	private static Object getValue(ChannelsNodes root) {
		String key = root.getRoot();
		return channelConfig.get(key) != null ? channelConfig.get(key) : root.getDefault();
	}

	private static void addComment(String root, String... comments) {
		newChannelConfig.addComment(root, comments);
	}

	private static void setNewProperty(String root, Object value) {
		if (value == null) {
			value = "";
		}
		newChannelConfig.set(root, value.toString());
	}

	private static void tryAndSetDefaultChannels() {

		if (channelConfig.contains(CHANNELS_ROOT)) {
			// There is already a root channels present, not our first run.
			newChannelConfig.set(CHANNELS_ROOT, channelConfig.get(CHANNELS_ROOT));
		} else {
			// Populate a fresh channels.yml.
			Chat.getTownyChat().getLogger().info("TownyChat creating default channels.yml file.");

			newChannelConfig.createSection(CHANNELS_ROOT);
			for (String channel : DEFAULT_CHANNELS)
				newChannelConfig.createSection(CHANNELS_ROOT + "." + channel);
	
			ConfigurationSection configurationSection = newChannelConfig.getConfigurationSection(CHANNELS_ROOT);
			configurationSection.set("general", generalDefaults());
			configurationSection.set("town", townDefaults());
			configurationSection.set("nation", nationDefaults());
			configurationSection.set("alliance", allianceDefaults());
			configurationSection.set("admin", adminDefaults());
			configurationSection.set("mod", modDefaults());
			configurationSection.set("local", localDefaults());
		}
	}

	private static Map<String, Object> generalDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "g");
		channelMap.put("type", "GLOBAL");
		channelMap.put("channelTag", "&f[g]");
		channelMap.put("messagecolour", "&f");
		channelMap.put("permission", "towny.chat.general");
		channelMap.put("default", "true");
		channelMap.put("range", "-1");
		channelMap.put("spam_time", "0.5");
		return channelMap;
	}

	private static Map<String, Object> townDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "tc");
		channelMap.put("type", "TOWN");
		channelMap.put("channelTag", "&f[&3TC&f]");
		channelMap.put("messagecolour", "&b");
		channelMap.put("permission", "towny.chat.town");
		channelMap.put("range", "-1");
		channelMap.put("spam_time", "0.5");
		return channelMap;
	}

	private static Map<String, Object> nationDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "nc");
		channelMap.put("type", "NATION");
		channelMap.put("channelTag", "&f[&6NC&f]");
		channelMap.put("messagecolour", "&e");
		channelMap.put("permission", "towny.chat.nation");
		channelMap.put("range", "-1");
		channelMap.put("spam_time", "0.5");
		return channelMap;
	}

	private static Map<String, Object> allianceDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "ac");
		channelMap.put("type", "ALLIANCE");
		channelMap.put("channelTag", "&f[&2AC&f]");
		channelMap.put("messagecolour", "&a");
		channelMap.put("permission", "towny.chat.alliance");
		channelMap.put("range", "-1");
		channelMap.put("spam_time", "0.5");
		return channelMap;
	}

	private static Map<String, Object> adminDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "a,admin");
		channelMap.put("type", "DEFAULT");
		channelMap.put("channelTag", "&f[&4ADMIN&f]");
		channelMap.put("messagecolour", "&c");
		channelMap.put("permission", "towny.chat.admin");
		channelMap.put("range", "-1");
		return channelMap;
	}

	private static Map<String, Object> modDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "m,mod");
		channelMap.put("type", "DEFAULT");
		channelMap.put("channelTag", "&f[&9MOD&f]");
		channelMap.put("messagecolour", "&5");
		channelMap.put("permission", "towny.chat.mod");
		channelMap.put("range", "-1");
		return channelMap;
	}

	private static Map<String, Object> localDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put("commands", "l,lc");
		channelMap.put("type", "GLOBAL");
		channelMap.put("channelTag", "&f[local]");
		channelMap.put("messagecolour", "&f");
		channelMap.put("permission", "towny.chat.local");
		channelMap.put("default", "true");
		channelMap.put("range", "100");
		return channelMap;
	}

	private static boolean loadChannels() {
		ConfigurationSection configurationSection = channelConfig.getConfigurationSection(CHANNELS_ROOT);
		if (configurationSection == null) {
			Bukkit.getLogger().severe("[TownyChat] Failed to load Channels.yml!");
			Bukkit.getLogger().severe("[TownyChat] No channels root section was present!");
			return false;
		}

		Set<String> channelsKeys = configurationSection.getKeys(false);

		// This should never happen if the setDefaults(file) method has run.
		if (channelsKeys.isEmpty()) {
			Bukkit.getLogger().severe("[TownyChat] Failed to load Channels.yml!");
			Bukkit.getLogger().severe("[TownyChat] No channels keys were present!");
			Bukkit.getLogger().severe("[TownyChat] Deleting your towny\\settings\\channels.yml can solve this problem.");
			return false;
		}

		Chat plugin = Chat.getTownyChat();

		channelsKeys.forEach((channelName)-> {
			Channel channel = new StandardChannel(plugin, channelName.toLowerCase(Locale.ROOT));
			loadChannelSettings(channelName, channel);
			plugin.getChannelsHandler().addChannel(channel);
		});

		if (plugin.getChannelsHandler().getDefaultChannel() == null) {
			// If there is no default channel set it to the first one that was parsed (the top one in the config)
			// This is because not everyone knows that you need to add a default: true into the channels.yml to make it the default channel!
			plugin.getChannelsHandler().setDefaultChannel(plugin.getChannelsHandler().getAllChannels().entrySet().iterator().next().getValue());
		}

		return true;
	}

	private static void loadChannelSettings(String channelName, Channel channel) {
		ChannelDetails data = new ChannelsSettings.ChannelDetails(channelConfig, channelName);
		// The following will always be present in some manner
		channel.setCommands(data.getCommands());
		channel.setType(channelTypes.valueOf(data.getType()));
		channel.setRange(data.getRange());
		channel.setHooked(data.isHooked());
		channel.setFocusable(data.isFocusable());
		channel.setLeavePermission(data.getLeavePermission());
		// The following may not neccessarily be set.
		if (data.hasMessageColour())
			channel.setMessageColour(data.getMessageColour());
		if (data.hasChannelTag())
			channel.setChannelTag(data.getChannelTag());
		if (data.hasSound())
			channel.setChannelSound(data.getSound());
		if (data.hasPermission())
			channel.setPermission(data.getPermission());
		if (data.hasSpeakPermission())
			channel.setSpeakPermission(data.getSpeakPermission());
		if (data.hasListenPermission())
			channel.setListenPermission(data.getListenPermission());
		if (data.hasSpamTime())
			channel.setSpam_time(data.getSpamTime());
		if (data.isDefault()) {
			Chat.getTownyChat().getChannelsHandler().setDefaultChannel(channel);
			Chat.getTownyChat().getLogger().info("Default Channel set to " + channel.getName());
		}
	}

	static class ChannelDetails {
		private Map<String, Object> channelSettingsMap = new HashMap<>();
		private String name;

		/**
		 * Constructor
		 * 
		 * @param name Channel name
		 */
		public ChannelDetails(CommentedConfiguration channelsConfig, String name) {
			super();
			this.name = name.toLowerCase(Locale.ROOT);
			String path = CHANNELS_ROOT + "." + this.name;
			for (String key : channelsConfig.getConfigurationSection(path).getKeys(true)) {
				String innerPath = path + "." + key;
				if (channelsConfig.get(innerPath) != null)
					channelSettingsMap.put(key, parseObject(channelsConfig.get(innerPath)));
			}
		}

		private Object parseObject(Object object) {
			if (object instanceof @SuppressWarnings("rawtypes") List list)
				return StringMgmt.join(list, ",");
			if (object instanceof String string)
				return string;
			if (object instanceof Boolean b)
				return b;
			if (object instanceof Double d)
				return d;
			return "";
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		public String getType() {
			return (String) channelSettingsMap.getOrDefault("type", "DEFAULT");
		}

		public boolean hasChannelTag() {
			return channelSettingsMap.containsKey("channeltag");
		}

		public String getChannelTag() {
			return (String) channelSettingsMap.getOrDefault("channeltag", "");
		}

		public boolean hasMessageColour() {
			return channelSettingsMap.containsKey("messagecolour");
		}

		public String getMessageColour() {
			return (String) channelSettingsMap.getOrDefault("messagecolour", "");
		}

		public boolean hasPermission() {
			return channelSettingsMap.containsKey("permission");
		}

		public String getPermission() {
			return (String) channelSettingsMap.getOrDefault("permission", "");
		}

		public boolean hasLeavePermission() {
			return channelSettingsMap.containsKey("leavepermission");
		}

		public String getLeavePermission() {
			return (String) channelSettingsMap.getOrDefault("leavepermission", "towny.chat.leave." + name);
		}

		public boolean hasListenPermission() {
			return channelSettingsMap.containsKey("listenpermission");
		}

		public String getListenPermission() {
			return (String) channelSettingsMap.getOrDefault("listenpermission", "");
		}

		public boolean hasSpeakPermission() {
			return channelSettingsMap.containsKey("speakpermission");
		}

		public String getSpeakPermission() {
			return (String) channelSettingsMap.getOrDefault("speakpermission", "");
		}

		public boolean hasSound() {
			return channelSettingsMap.containsKey("sound");
		}

		public String getSound() {
			return (String) channelSettingsMap.getOrDefault("sound", "");
		}

		public boolean hasSpamTime() {
			return channelSettingsMap.containsKey("spam_time");
		}

		public double getSpamTime() {
			return Double.valueOf((String) channelSettingsMap.get("spam_time"));
		}

		public double getRange() {
			return Double.valueOf((String) channelSettingsMap.getOrDefault("range", -1));
		}

		public boolean isHooked() {
			return getBoolean(channelSettingsMap.getOrDefault("hooked", false));
		}

		public boolean isFocusable() {
			return getBoolean(channelSettingsMap.getOrDefault("focusable", true));
		}

		public boolean isDefault() {
			return getBoolean(channelSettingsMap.getOrDefault("default", false));
		}

		public List<String> getCommands() {
			Object object = channelSettingsMap.get("commands");
			if (object instanceof String string) {
				return Arrays.asList(string.split(","));
			}
			// As a fall-back we make a command from the channel name, this should not
			// happen often or at all.
			return Collections.singletonList(name + "chat");
		}

		private boolean getBoolean(Object value) {
			if (value instanceof Boolean) {
				return (Boolean) value;
			} else if (value instanceof String) {
				return Boolean.parseBoolean(value.toString());
			} else if (value instanceof Integer) {
				return Integer.parseInt(value.toString()) != 0;
			}
			return false;
		}
	}
}