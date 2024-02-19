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
import java.util.Map;
import java.util.Set;

public class ChannelsSettings {

	private static final String CHANNELS_ROOT = "Channels";
	private static CommentedConfiguration channelConfig, newChannelConfig;
	private final static List<String> DEFAULT_CHANNELS = Arrays.asList("general","town","nation","alliance","admin","mod","local");

	private static final String CHANNEL_TAG = "channeltag";
	private static final String LEGACY_CHANNEL_TAG = "channelTag";
	private static final String SPAM_TIME = "spam_time";
	private static final String RANGE = "range";
	private static final String DEFAULT = "default";
	private static final String PERMISSION = "permission";
	private static final String MESSAGECOLOUR = "messagecolour";
	private static final String TYPE = "type";
	private static final String COMMANDS = "commands";
	private static final String FOCUSABLE = "focusable";
	private static final String HOOKED = "hooked";
	private static final String SOUND = "sound";
	private static final String SPEAKPERMISSION = "speakpermission";
	private static final String LISTENPERMISSION = "listenpermission";
	private static final String LEAVEPERMISSION = "leavepermission";
	
	/**
	 * 
	 * @return true if the channels.yml has loaded. 
	 * @throws IOException
	 */
	public static boolean loadCommentedChannelsConfig() {
		String filepath = Chat.getTownyChat().getChannelsConfigPath();
		if (FileMgmt.checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the channels.yml into memory
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
			DEFAULT_CHANNELS.forEach(channel -> newChannelConfig.createSection(CHANNELS_ROOT + "." + channel));

			setConfigSection("general", generalDefaults());
			setConfigSection("town", townDefaults());
			setConfigSection("nation", nationDefaults());
			setConfigSection("alliance", allianceDefaults());
			setConfigSection("admin", adminDefaults());
			setConfigSection("mod", modDefaults());
			setConfigSection("local", localDefaults());
		}
	}

	private static void setConfigSection(String section, Map<String,Object> settings) {
		ConfigurationSection configurationSection = newChannelConfig.getConfigurationSection(CHANNELS_ROOT + "." + section);
		settings.entrySet().stream().forEach(entry -> configurationSection.set(entry.getKey(), entry.getValue()));
	}

	private static Map<String, Object> generalDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "g");
		channelMap.put(TYPE, "GLOBAL");
		channelMap.put(CHANNEL_TAG, "&f[g]");
		channelMap.put(MESSAGECOLOUR, "&f");
		channelMap.put(PERMISSION, "towny.chat.general");
		channelMap.put(DEFAULT, "true");
		channelMap.put(RANGE, "-1");
		channelMap.put(SPAM_TIME, "0.5");
		return channelMap;
	}

	private static Map<String, Object> townDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "tc");
		channelMap.put(TYPE, "TOWN");
		channelMap.put(CHANNEL_TAG, "&f[&3TC&f]");
		channelMap.put(MESSAGECOLOUR, "&b");
		channelMap.put(PERMISSION, "towny.chat.town");
		channelMap.put(RANGE, "-1");
		channelMap.put(SPAM_TIME, "0.5");
		return channelMap;
	}

	private static Map<String, Object> nationDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "nc");
		channelMap.put(TYPE, "NATION");
		channelMap.put(CHANNEL_TAG, "&f[&6NC&f]");
		channelMap.put(MESSAGECOLOUR, "&e");
		channelMap.put(PERMISSION, "towny.chat.nation");
		channelMap.put(RANGE, "-1");
		channelMap.put(SPAM_TIME, "0.5");
		return channelMap;
	}

	private static Map<String, Object> allianceDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "ac");
		channelMap.put(TYPE, "ALLIANCE");
		channelMap.put(CHANNEL_TAG, "&f[&2AC&f]");
		channelMap.put(MESSAGECOLOUR, "&a");
		channelMap.put(PERMISSION, "towny.chat.alliance");
		channelMap.put(RANGE, "-1");
		channelMap.put(SPAM_TIME, "0.5");
		return channelMap;
	}

	private static Map<String, Object> adminDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "a,admin");
		channelMap.put(TYPE, "DEFAULT");
		channelMap.put(CHANNEL_TAG, "&f[&4ADMIN&f]");
		channelMap.put(MESSAGECOLOUR, "&c");
		channelMap.put(PERMISSION, "towny.chat.admin");
		channelMap.put(RANGE, "-1");
		return channelMap;
	}

	private static Map<String, Object> modDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "m,mod");
		channelMap.put(TYPE, "DEFAULT");
		channelMap.put(CHANNEL_TAG, "&f[&9MOD&f]");
		channelMap.put(MESSAGECOLOUR, "&5");
		channelMap.put(PERMISSION, "towny.chat.mod");
		channelMap.put(RANGE, "-1");
		return channelMap;
	}

	private static Map<String, Object> localDefaults() {
		Map<String, Object> channelMap = new LinkedHashMap<>();
		channelMap.put(COMMANDS, "l,lc");
		channelMap.put(TYPE, "GLOBAL");
		channelMap.put(CHANNEL_TAG, "&f[local]");
		channelMap.put(MESSAGECOLOUR, "&f");
		channelMap.put(PERMISSION, "towny.chat.local");
		channelMap.put(RANGE, "100");
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
			Channel channel = new StandardChannel(plugin, channelName);
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
			this.name = name;
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
			return (String) channelSettingsMap.getOrDefault(TYPE, "DEFAULT");
		}

		public boolean hasChannelTag() {
			return channelSettingsMap.containsKey(CHANNEL_TAG) || channelSettingsMap.containsKey(LEGACY_CHANNEL_TAG);
		}

		public String getChannelTag() {
			String slug = channelSettingsMap.containsKey(LEGACY_CHANNEL_TAG) ? LEGACY_CHANNEL_TAG : CHANNEL_TAG;
			return (String) channelSettingsMap.getOrDefault(slug, "");
		}

		public boolean hasMessageColour() {
			return channelSettingsMap.containsKey(MESSAGECOLOUR);
		}

		public String getMessageColour() {
			return (String) channelSettingsMap.getOrDefault(MESSAGECOLOUR, "");
		}

		public boolean hasPermission() {
			return channelSettingsMap.containsKey(PERMISSION);
		}

		public String getPermission() {
			return (String) channelSettingsMap.getOrDefault(PERMISSION, "");
		}

		public boolean hasLeavePermission() {
			return channelSettingsMap.containsKey(LEAVEPERMISSION);
		}

		public String getLeavePermission() {
			return (String) channelSettingsMap.getOrDefault(LEAVEPERMISSION, "towny.chat.leave." + name);
		}

		public boolean hasListenPermission() {
			return channelSettingsMap.containsKey(LISTENPERMISSION);
		}

		public String getListenPermission() {
			return (String) channelSettingsMap.getOrDefault(LISTENPERMISSION, "");
		}

		public boolean hasSpeakPermission() {
			return channelSettingsMap.containsKey(SPEAKPERMISSION);
		}

		public String getSpeakPermission() {
			return (String) channelSettingsMap.getOrDefault(SPEAKPERMISSION, "");
		}

		public boolean hasSound() {
			return channelSettingsMap.containsKey(SOUND);
		}

		public String getSound() {
			return (String) channelSettingsMap.getOrDefault(SOUND, "");
		}

		public boolean hasSpamTime() {
			return channelSettingsMap.containsKey(SPAM_TIME);
		}

		public double getSpamTime() {
			return Double.valueOf((String) channelSettingsMap.get(SPAM_TIME));
		}

		public double getRange() {
			return Double.valueOf((String) channelSettingsMap.getOrDefault(RANGE, -1));
		}

		public boolean isHooked() {
			return getBoolean(channelSettingsMap.getOrDefault(HOOKED, false));
		}

		public boolean isFocusable() {
			return getBoolean(channelSettingsMap.getOrDefault(FOCUSABLE, true));
		}

		public boolean isDefault() {
			return getBoolean(channelSettingsMap.getOrDefault(DEFAULT, false));
		}

		public List<String> getCommands() {
			Object object = channelSettingsMap.get(COMMANDS);
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