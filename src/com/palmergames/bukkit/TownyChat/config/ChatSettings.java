package com.palmergames.bukkit.TownyChat.config;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.config.CommentedConfiguration;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;
import com.palmergames.util.FileMgmt;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class ChatSettings {

	private static Map<String, WorldFormat> worldFormatGroups = new HashMap<String, WorldFormat>();
	private static CommentedConfiguration chatConfig, newChatConfig;
	
	/**
	 * 
	 * @return true if the chatconfig has loaded. 
	 * @throws IOException
	 */
	public static boolean loadCommentedChatConfig() {
		String filepath = Chat.getTownyChat().getChatConfigPath();
		if (FileMgmt.checkOrCreateFile(filepath)) {
			File file = new File(filepath);

			// read the config.yml into memory
			ChatSettings.chatConfig = new CommentedConfiguration(file.toPath());
			if (!chatConfig.load()) {
				Bukkit.getLogger().severe("[TownyChat] Failed to load ChatConfig!");
				Bukkit.getLogger().severe("[TownyChat] Please check that the file passes a YAML Parser test:");
				Bukkit.getLogger().severe("[TownyChat] Online YAML Parser: https://yaml-online-parser.appspot.com/");
				return false;
			}
			setDefaults(file);
			chatConfig.save();
		}
		return true;
	}
	
	/**
	 * Builds a new chatconfig reading old chatconfig data,
	 * and setting new nodes to default values.
	 */
	private static void setDefaults(File file) {

		String version = Chat.getTownyChat().getDescription().getVersion();
		newChatConfig = new CommentedConfiguration(file.toPath());
		newChatConfig.load();

		for (ChatConfigNodes root : ChatConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());

			if (root.getRoot() == ChatConfigNodes.WORLDS.getRoot())
				setWorldDefaults(); // Per-worlds section.
			else if (root.getRoot() == ChatConfigNodes.VERSION.getRoot())
				setNewProperty(root.getRoot(), version);
			else if (root.getRoot() == ChatConfigNodes.LAST_RUN_VERSION.getRoot())
				setNewProperty(root.getRoot(), getLastRunVersion(version));
			else
				// A regular config node.
				setNewProperty(root.getRoot(), (chatConfig.get(root.getRoot().toLowerCase()) != null) ? chatConfig.get(root.getRoot().toLowerCase()) : root.getDefault());
		}
		chatConfig = newChatConfig;
		newChatConfig = null;
	}
	
	private static void setWorldDefaults() {
		if (isPer_world()) {
			for (TownyWorld world : TownyUniverse.getInstance().getTownyWorlds()) {
				String path = "worlds." + world;
				if (!chatConfig.contains(path )
						|| !chatConfig.contains(path + ".global") 
						|| !chatConfig.contains(path + ".town") 
						|| !chatConfig.contains(path + ".nation")
						|| !chatConfig.contains(path + ".alliance")
						|| !chatConfig.contains(path + ".default")) {
					newChatConfig.createSection(path);
					ConfigurationSection worldsection = newChatConfig.getConfigurationSection(path);
					worldsection.set("global", getOrDefault(path + ".global", ChatConfigNodes.CHANNEL_FORMATS_GLOBAL));
					worldsection.set("town", getOrDefault(path + ".town", ChatConfigNodes.CHANNEL_FORMATS_TOWN));
					worldsection.set("nation", getOrDefault(path + ".nation", ChatConfigNodes.CHANNEL_FORMATS_NATION));
					worldsection.set("alliance", getOrDefault(path + ".alliance", ChatConfigNodes.CHANNEL_FORMATS_ALLIANCE));
					worldsection.set("default", getOrDefault(path + ".default", ChatConfigNodes.CHANNEL_FORMATS_DEFAULT));
				}
			}
		} else {
			if (chatConfig.contains(ChatConfigNodes.WORLDS.getRoot()))
				newChatConfig.set(ChatConfigNodes.WORLDS.getRoot(), chatConfig.get(ChatConfigNodes.WORLDS.getRoot()));
		}
	}

	private static void addComment(String root, String... comments) {
		newChatConfig.addComment(root.toLowerCase(), comments);
	}
	
	private static void setNewProperty(String root, Object value) {
		if (value == null) {
			value = "";
		}
		newChatConfig.set(root.toLowerCase(), value.toString());
	}
	
	private static String getLastRunVersion(String currentVersion) {
		return getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
	}
	
	private static String getString(String root, String def) {
		String data = chatConfig.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from ChatConfig.yml");
			return "";
		}
		return data;
	}
	
	private static String getString(ChatConfigNodes node) {
		return chatConfig.getString(node.getRoot().toLowerCase(), node.getDefault());
	}
	
	private static void sendError(String msg) {
		Chat.getTownyChat().getLogger().warning("[TownyChat] Error could not read " + msg);
	}

	private static boolean getBoolean(ChatConfigNodes node) {
		
		return Boolean.parseBoolean(chatConfig.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	/*
	 * Get ChatConfig variables.
	 */

	/**
	 * @return the modify_chat
	 */
	public static boolean isModify_chat() {
		return getBoolean(ChatConfigNodes.MODIFY_CHAT_ENABLE);
	}

	/**
	 * @return the alone_message
	 */
	public static boolean isUsingAloneMessage() {
		return getBoolean(ChatConfigNodes.MODIFY_CHAT_ALONE_MESSAGE);
	}

	/**
	 * @return the alone_message_string
	 */
	public static String getUsingAloneMessageString() {
		return getString(ChatConfigNodes.MODIFY_CHAT_ALONE_MESSAGE_STRING);
	}

	/**
	 * @return the per_world
	 */
	public static boolean isPer_world() {
		return getBoolean(ChatConfigNodes.MODIFY_CHAT_PER_WORLD);
	}

	/**
	 * @return the display_modes_set_on_join setting
	 */
	public static boolean getShowChannelMessageOnServerJoin() {
		return getBoolean(ChatConfigNodes.DISPLAY_CHANNEL_JOIN_MESSAGE_ON_JOIN);
	}

	/**
	 * @return true if preceding exclamation points shouts into global channel with unlimited range.
	 */
	public static boolean isExclamationPoint() {
		return getBoolean(ChatConfigNodes.ALLOW_EXCLAMATION_POINT_SHOUTS);
	}

	public static String getChatListenerPriority() {
		String priority = getString(ChatConfigNodes.MODIFY_CHAT_LISTENER_PRIORITY).toLowerCase(Locale.ROOT);
		return switch(priority) {
		case "lowest","low","normal","high","highest"-> priority;
		default -> "normal";
		};
	}

	/*
	 * Get Tags formats.
	 */

	public static String getWorldTag() {
		return getString(ChatConfigNodes.TAG_FORMATS_WORLD);
	}

	public static String getTownTag() {
		return getString(ChatConfigNodes.TAG_FORMATS_TOWN);
	}

	public static String getNationTag() {
		return getString(ChatConfigNodes.TAG_FORMATS_NATION);
	}

	public static String getBothTag() {
		return getString(ChatConfigNodes.TAG_FORMATS_BOTH);
	}

	/*
	 * Get Colour formats.
	 */

	public static String getKingColour() {
		return getString(ChatConfigNodes.COLOUR_KING);
	}

	public static String getMayorColour() {
		return getString(ChatConfigNodes.COLOUR_MAYOR);
	}

	public static String getResidentColour() {
		return getString(ChatConfigNodes.COLOUR_RESIDENT);
	}

	public static String getNomadColour() {
		return getString(ChatConfigNodes.COLOUR_NOMAD);
	}

	/*
	 * Get ChannelType Formats
	 */

	public static String getChannelFormat(Player player, channelTypes type) {
		if (isPer_world()) {
			String worldName = player.getWorld().getName().toLowerCase(Locale.ROOT);
			if (worldFormatGroups.containsKey(worldName)) {
				String format = worldFormatGroups.get(worldName).getFormat(type);
				if (format != null && !format.isEmpty())
					return format;
			}
		}
		return switch(type) {
		case GLOBAL, PRIVATE -> getString(ChatConfigNodes.CHANNEL_FORMATS_GLOBAL);
		case NATION -> getString(ChatConfigNodes.CHANNEL_FORMATS_NATION);
		case TOWN -> getString(ChatConfigNodes.CHANNEL_FORMATS_TOWN);
		case ALLIANCE -> getString(ChatConfigNodes.CHANNEL_FORMATS_ALLIANCE);
		case DEFAULT -> getString(ChatConfigNodes.CHANNEL_FORMATS_DEFAULT);
		};
	}

	public static void loadWorldFormats() {
		if (!isPer_world())
			return;
		for (TownyWorld world : TownyUniverse.getInstance().getTownyWorlds()) {
			String worldName = world.getName();
			String path = "worlds." + worldName;
			if (chatConfig.contains(path)) {
				ChatSettings.WorldFormat worldFormat = new ChatSettings().new WorldFormat(worldName);
				worldFormat.setGLOBAL(getOrDefault(path + ".global", ChatConfigNodes.CHANNEL_FORMATS_GLOBAL));
				worldFormat.setDEFAULT(getOrDefault(path + ".default", ChatConfigNodes.CHANNEL_FORMATS_DEFAULT));
				worldFormat.setTOWN(getOrDefault(path + ".town", ChatConfigNodes.CHANNEL_FORMATS_TOWN));
				worldFormat.setNATION(getOrDefault(path + ".nation", ChatConfigNodes.CHANNEL_FORMATS_NATION));
				worldFormat.setALLIANCE(getOrDefault(path + ".alliance", ChatConfigNodes.CHANNEL_FORMATS_ALLIANCE));
				addWorldFormat(worldFormat);
			}
		}
	}
	
	private static String getOrDefault(String configPath, ChatConfigNodes channelNode) {
		return String.valueOf(chatConfig.get(configPath, channelNode.getDefault()));
	}

	private static void addWorldFormat(WorldFormat format) {
		worldFormatGroups.put(format.getName().toLowerCase(Locale.ROOT), format);
	}

	public class WorldFormat {

		/**
		 * Constructor
		 * 
		 * @param name
		 */
		public WorldFormat(String name) {
			super();
			this.name = name.toLowerCase();
		}

		public String getFormat(channelTypes type) {
			return switch(type) {
			case GLOBAL, PRIVATE -> GLOBAL;
			case NATION -> NATION;
			case TOWN -> TOWN;
			case ALLIANCE -> ALLIANCE;
			case DEFAULT -> DEFAULT;
			};
		}

		private String name, GLOBAL, TOWN, NATION, ALLIANCE, DEFAULT;
		
		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}
	
		/**
		 * @param GLOBAL the GLOBAL format to set
		 */
		public void setGLOBAL(String GLOBAL) {
			this.GLOBAL = GLOBAL;
		}

		/**
		 * @param TOWN the TOWN format to set
		 */
		public void setTOWN(String TOWN) {
			this.TOWN = TOWN;
		}

		/**
		 * @param NATION the NATION format to set
		 */
		public void setNATION(String NATION) {
			this.NATION = NATION;
		}

		/**
		 * @param ALLIANCE the ALLIANCE format to set
		 */
		public void setALLIANCE(String ALLIANCE) {
			this.ALLIANCE = ALLIANCE;
		}

		/**
		 * @param DEFAULT the DEFAULT format to set
		 */
		public void setDEFAULT(String DEFAULT) {
			this.DEFAULT = DEFAULT;
		}
	}
}