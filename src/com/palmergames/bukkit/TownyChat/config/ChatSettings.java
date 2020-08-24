package com.palmergames.bukkit.TownyChat.config;

import com.palmergames.bukkit.TownyChat.channels.channelFormats;
import com.palmergames.bukkit.TownyChat.util.FileMgmt;
import com.palmergames.bukkit.config.ConfigNodes;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.TownyWorld;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ChatSettings extends tag_formats {

	private static boolean modify_chat;
	private static boolean per_world;
	private static boolean alone_message;
	private static boolean display_modes_set_on_join;
	private static String alone_message_string;

	private static Map<String, channelFormats> formatGroups = new HashMap<String, channelFormats>();
	private static CommentedConfiguration chatConfig, newChatConfig;
	
	/**
	 * 
	 * @param filepath - Location of chatconfig.yml.
	 * @param version - Chat version from plugin.yml.
	 * @return 
	 * @throws IOException
	 */
	public static boolean loadCommentedConfig(String filepath, String version) {

		File file = FileMgmt.CheckYMLExists(new File(filepath));
		if (file != null) {

			// read the config.yml into memory
			ChatSettings.chatConfig = new CommentedConfiguration(file);			
			if (!chatConfig.load()) {
				Bukkit.getLogger().severe("[TownyChat] Failed to load ChatConfig!");
				Bukkit.getLogger().severe("[TownyChat] Please check that the file passes a YAML Parser test:");
				Bukkit.getLogger().severe("[TownyChat] Online YAML Parser: https://yaml-online-parser.appspot.com/");
				return false;
			}
			setDefaults(version, file);
			chatConfig.save();			
		}
		return true;
	}
	
	/**
	 * Builds a new chatconfig reading old chatconfig data,
	 * and setting new nodes to default values.
	 */
	private static void setDefaults(String version, File file) {

		newChatConfig = new CommentedConfiguration(file);
		newChatConfig.load();

		for (ChatConfigNodes root : ChatConfigNodes.values()) {
			if (root.getComments().length > 0)
				addComment(root.getRoot(), root.getComments());
			if (root.getRoot() == ChatConfigNodes.WORLDS.getRoot()) {
				// Per-worlds section.
//				setNewProperty(root.getRoot(), root.getDefault());
				setWorldDefaults();
				
			} else if (root.getRoot() == ChatConfigNodes.VERSION.getRoot()) {
				setNewProperty(root.getRoot(), version);
			} else if (root.getRoot() == ChatConfigNodes.LAST_RUN_VERSION.getRoot())
				setNewProperty(root.getRoot(), getLastRunVersion(version));
			else {
				// A regular config node.
				setNewProperty(root.getRoot(), (chatConfig.get(root.getRoot().toLowerCase()) != null) ? chatConfig.get(root.getRoot().toLowerCase()) : root.getDefault());
			}		
		}
		chatConfig = newChatConfig;
		newChatConfig = null;
	}
	
	private static void setWorldDefaults() {
		if (perWorld()) {
			for (TownyWorld world : TownyUniverse.getInstance().getDataSource().getWorlds()) {
				if (!chatConfig.contains("worlds." + world )
						|| !chatConfig.contains("worlds." + world + ".global") 
						|| !chatConfig.contains("worlds." + world + ".town") 
						|| !chatConfig.contains("worlds." + world + ".nation")
						|| !chatConfig.contains("worlds." + world + ".alliance")
						|| !chatConfig.contains("worlds." + world + ".default")) {
					newChatConfig.createSection("worlds." + world);
					ConfigurationSection worldsection = newChatConfig.getConfigurationSection("worlds." + world);
					worldsection.set("global", (chatConfig.get("worlds." + world + ".global")!= null) ? chatConfig.get("worlds." + world + ".global") : ChatConfigNodes.CHANNEL_FORMATS_GLOBAL.getDefault());
					worldsection.set("town", (chatConfig.get("worlds." + world + ".town")!= null) ? chatConfig.get("worlds." + world + ".town") : ChatConfigNodes.CHANNEL_FORMATS_TOWN.getDefault());
					worldsection.set("nation", (chatConfig.get("worlds." + world + ".nation")!= null) ? chatConfig.get("worlds." + world + ".nation") : ChatConfigNodes.CHANNEL_FORMATS_NATION.getDefault());
					worldsection.set("alliance", (chatConfig.get("worlds." + world + ".alliance")!= null) ? chatConfig.get("worlds." + world + ".alliance") : ChatConfigNodes.CHANNEL_FORMATS_ALLIANCE.getDefault());
					worldsection.set("default", (chatConfig.get("worlds." + world + ".default")!= null) ? chatConfig.get("worlds." + world + ".default") : ChatConfigNodes.CHANNEL_FORMATS_DEFAULT.getDefault());
				}
			}
		} else {
			if (chatConfig.contains(ChatConfigNodes.WORLDS.getRoot()))
				newChatConfig.set(ChatConfigNodes.WORLDS.getRoot(), chatConfig.get(ChatConfigNodes.WORLDS.getRoot()));
		}
	}

	public static void addComment(String root, String... comments) {
		newChatConfig.addComment(root.toLowerCase(), comments);
	}
	
	private static void setNewProperty(String root, Object value) {
		if (value == null) {
			value = "";
		}
		newChatConfig.set(root.toLowerCase(), value.toString());
	}
	
	public static String getLastRunVersion(String currentVersion) {
		return getString(ConfigNodes.LAST_RUN_VERSION.getRoot(), currentVersion);
	}
	
	public static String getString(String root, String def) {
		String data = chatConfig.getString(root.toLowerCase(), def);
		if (data == null) {
			sendError(root.toLowerCase() + " from ChatConfig.yml");
			return "";
		}
		return data;
	}
	
	private static void sendError(String msg) {
		System.out.println("[TownyChat] Error could not read " + msg);
	}
	
	private static boolean perWorld() {

		return getBoolean(ChatConfigNodes.MODIFY_CHAT_PER_WORLD);
	}

	private static boolean getBoolean(ChatConfigNodes node) {
		
		return Boolean.parseBoolean(chatConfig.getString(node.getRoot().toLowerCase(), node.getDefault()));
	}

	/**
	 * @return the formatGroups
	 */
	public static Map<String, channelFormats> getFormatGroups() {
		return formatGroups;
	}

	/**
	 * @param formatGroups
	 *            the formatGroups to set
	 */
	public static void setFormatGroups(Map<String, channelFormats> formatGroups) {
		ChatSettings.formatGroups = formatGroups;
	}

	/**
	 * @return the formatGroup
	 */
	public static channelFormats getFormatGroup(String name) {
		return formatGroups.get(name.toLowerCase());
	}

	public static boolean hasFormatGroup(String groupName) {
		return formatGroups.containsKey(groupName.toLowerCase());
	}

	public static void addFormatGroup(channelFormats group) {
		if (hasFormatGroup(group.getName()))
			formatGroups.remove(group.getName());

		formatGroups.put(group.getName(), group);
	}

	public static void removeFormatGroup(String name) {
		if (hasFormatGroup(name.toLowerCase()))
			formatGroups.remove(name.toLowerCase());
	}
	
	public static channelFormats getRelevantFormatGroup (Player player) {
		
		if (isPer_world()) {
			String name = player.getWorld().getName();
			if (hasFormatGroup(name))
				return getFormatGroup(name);	
		}
		
		return getFormatGroup("channel_formats");

	}

	/**
	 * @return the modify_chat
	 */
	public static boolean isModify_chat() {
		return modify_chat;
	}

	/**
	 * @param modify_chat
	 *            the modify_chat to set
	 */
	public static void setModify_chat(boolean modify_chat) {
		ChatSettings.modify_chat = modify_chat;
	}

	/**
	 * @return the alone_message
	 */
	public static boolean isUsingAloneMessage() {
		return alone_message;
	}
	
	/**
	 * @param alone_message
	 */
	public static boolean setUsingAloneMessage(boolean alone_message) {
		return ChatSettings.alone_message = alone_message;
	}

	/**
	 * @param alone_message_string
	 * @return 
	 */
	public static String setUsingAloneMessageString(String alone_message_string) {
		return ChatSettings.alone_message_string = alone_message_string;
	}
	
	/**
	 * @return the alone_message_string
	 */
	public static String getUsingAloneMessageString() {
		return ChatSettings.alone_message_string;
	}
	
	/**
	 * @param display_modes_set_on_join
	 */
	public static boolean setDisplayModesSetOnJoin(boolean display_modes_set_on_join) {
		return ChatSettings.display_modes_set_on_join = display_modes_set_on_join;
	}
	
	/**
	 * @return the display_modes_set_on_join setting
	 */
	public static boolean getDisplayeModesSetOnJoin() {
		return display_modes_set_on_join;
	}

	/**
	 * @return the per_world
	 */
	public static boolean isPer_world() {
		return per_world;
	}

	/**
	 * @param per_world
	 *            the per_world to set
	 */
	public static void setPer_world(boolean per_world) {
		ChatSettings.per_world = per_world;
	}

	

	/**
	 * Adds customizable channel formats for each world.
	 * 
	 * @return true if new worlds we're found
	 */
	public static boolean populateWorldFormats() {

		boolean updated = false;

		for (TownyWorld world : TownyUniverse.getInstance().getDataSource().getWorlds()) {
			if (!hasFormatGroup(world.getName())) {
				addFormatGroup(getFormatGroup("channel_formats").clone(world.getName()));
				updated = true;
			}
		}

		return updated;

	}

}
//////////////////////////////////////////////////

class tag_formats extends chat_colours{

	private static String WorldTag, TownTag, NationTag, BothTags;

	/**
	 * @return the World tag
	 */
	public static String getWorldTag() {
		return WorldTag;
	}

	/**
	 * @param tag the World tag to set
	 */
	public static void setWorldTag(String tag) {
		tag_formats.WorldTag = tag;
	}

	/**
	 * @return the TownTag
	 */
	public static String getTownTag() {
		return TownTag;
	}

	/**
	 * @param tag the TOWN tag to set
	 */
	public static void setTownTag(String tag) {
		tag_formats.TownTag = tag;
	}

	/**
	 * @return NationTag
	 */
	public static String getNationTag() {
		return NationTag;
	}

	/**
	 * @param tag the NATION tag to set
	 */
	public static void setNationTag(String tag) {
		tag_formats.NationTag = tag;
	}

	/**
	 * @return BothTags the nation and town tags
	 */
	public static String getBothTags() {
		return BothTags;
	}

	/**
	 * @param tag the Nation and Town tags to set
	 */
	public static void setBothTags(String tag) {
		tag_formats.BothTags = tag;
	}

}

class chat_colours {

	private static String KING, MAYOR, RESIDENT, NOMAD;

	/**
	 * @return KING the KING colour
	 */
	public static String getKingColour() {
		return KING;
	}

	/**
	 * @param colour the colour to set
	 */
	public static void setKingColour(String colour) {
		chat_colours.KING = colour;
	}

	/**
	 * @return MAYOR the MAYOR colour
	 */
	public static String getMayorColour() {
		return MAYOR;
	}

	/**
	 * @param colour the colour to set
	 */
	public static void setMayorColour(String colour) {
		chat_colours.MAYOR = colour;
	}

	/**
	 * @return RESIDENT the RESIDENT colour
	 */
	public static String getResidentColour() {
		return RESIDENT;
	}

	/**
	 * @param colour the RESIDENT colour tag to set
	 */
	public static void setResidentColour(String colour) {
		chat_colours.RESIDENT = colour;
	}

	/**
	 * @return NOMAD the NOMAD colour
	 */
	public static String getNomadColour() {
		return NOMAD;
	}

	/**
	 * @param colour the NOMAD colour tag to set
	 */
	public static void setNomadColour(String colour) {
		chat_colours.NOMAD = colour;
	}}

