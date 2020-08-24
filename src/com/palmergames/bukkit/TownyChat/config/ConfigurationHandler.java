package com.palmergames.bukkit.TownyChat.config;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.StandardChannel;
import com.palmergames.bukkit.TownyChat.channels.channelFormats;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import com.palmergames.bukkit.TownyChat.util.FileMgmt;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;



public class ConfigurationHandler {
	
	private Chat plugin;
	
	/** Constructor
	 * 
	 * @param plugin
	 */
	public ConfigurationHandler(Chat plugin) {
		super();
		this.plugin = plugin;
	}
	
	
	/**
	 * Load all Channels from the Channels.yml
	 * If it doesn't exist, create it from
	 * the resource in this jar.
	 * 
	 * @param filepath
	 * @param defaultRes
	 * @return true if the channels were loaded
	 */
	@SuppressWarnings("unchecked")
	public boolean loadChannels(String filepath, String defaultRes) {

		String filename = filepath + FileMgmt.fileSeparator() + defaultRes;

		Map<String, Object> file;
		try {
			file = FileMgmt.getFile(filename, defaultRes, null);
		} catch (Exception e) {
			Bukkit.getLogger().severe("[TownyChat] Failed to load Channels.yml!");
			Bukkit.getLogger().severe("[TownyChat] Please check that the file passes a YAML Parser test:");
			Bukkit.getLogger().severe("[TownyChat] Online YAML Parser: https://yaml-online-parser.appspot.com/");
			return false;
		}
		if (file != null) {

			for (String rootNode : file.keySet()) {
				
				if (rootNode.equalsIgnoreCase("Channels")) {
					// Parse the channels
					Map<String, Object> allChannelNodes = (Map<String, Object>) file.get(rootNode);
		
					// Load channels if the file is NOT empty
					if (allChannelNodes != null) {
						for (String channelKey : allChannelNodes.keySet()) {
								
							Map<String, Object> thisChannelNode = (Map<String, Object>) allChannelNodes.get(channelKey);
							Channel channel = new StandardChannel(plugin, channelKey.toLowerCase());
		
							for (String key : thisChannelNode.keySet()) {
								Object element = thisChannelNode.get(key);
		
								if (key.equalsIgnoreCase("commands"))
									if (element instanceof ArrayList)
										channel.setCommands((List<String>) element);
									else if (element instanceof String)
										channel.setCommands(Arrays.asList(element.toString()));
		
								if (key.equalsIgnoreCase("type"))
									if (element instanceof String)
										channel.setType(channelTypes.valueOf(element.toString()));

								if (key.equalsIgnoreCase("hooked")) {
									if (element instanceof Boolean) {
										channel.setHooked((Boolean)element);
									} else if (element instanceof String) {
										channel.setHooked(Boolean.parseBoolean(element.toString()));
									} else if (element instanceof Integer) {
										channel.setHooked(Integer.parseInt(element.toString()) != 0);
									}
								}

								if (key.equalsIgnoreCase("autojoin")) {
									if (element instanceof Boolean) {
										channel.setAutoJoin((Boolean)element);
									} else if (element instanceof String) {
										channel.setAutoJoin(Boolean.parseBoolean(element.toString()));
									} else if (element instanceof Integer) {
										channel.setAutoJoin(Integer.parseInt(element.toString()) != 0);
									}
								}

								if (key.equalsIgnoreCase("default")) {
									boolean set = false;
									if (element instanceof Boolean) {
										set = (element != null && ((Boolean)element) == true);
									} else if (element instanceof String) {
										set = Boolean.parseBoolean((String)element);
									} else if (element instanceof Integer) {
										set = (element != null && ((Integer)element) != 0);
									}
									if (set) {
										plugin.getChannelsHandler().setDefaultChannel(channel);
										plugin.getLogger().info("Default Channel set to " + channel.getName());
									}
								}
								
								if (key.equalsIgnoreCase("spam_time")) {
									channel.setSpam_time(Double.valueOf(element.toString()));
								}

								if (key.equalsIgnoreCase("channeltag"))
									if (element instanceof String)
										channel.setChannelTag(element.toString());
		
								if (key.equalsIgnoreCase("messagecolour"))
									if (element instanceof String)
										channel.setMessageColour(element.toString());
		
								if (key.equalsIgnoreCase("permission"))
									if (element instanceof String)
										channel.setPermission(element.toString());
								
								if (key.equalsIgnoreCase("leavepermission"))
									if (element instanceof String)
										channel.setLeavePermission(element.toString());
		
								if (key.equalsIgnoreCase("range"))
									channel.setRange(Double.valueOf(element.toString()));
							}
							
							// If no leave permission is set, create a default permission name
							if (channel.getLeavePermission() == null) {
								channel.setLeavePermission("towny.chat.leave." + channel.getName().toLowerCase());
							}
							plugin.getChannelsHandler().addChannel(channel);
							
							//System.out.print("Channel: " + channel.getName() + " : Type : " + channel.getType().name());
						}
						if (plugin.getChannelsHandler().getDefaultChannel() == null) {
							// If there is no default channel set it to the first one that was parsed (the top one in the config)
							// This is because not everyone knows that you need to add a default: true into the channels.yml to make it the default channel!
							plugin.getChannelsHandler().setDefaultChannel(plugin.getChannelsHandler().getAllChannels().entrySet().iterator().next().getValue());
						}
						return true;
						
					}
				}
			}
		}
		return false;
	}
	
	/**
	 * Load the ChatConfig.yml
	 * 
	 * If it doesn't exist, create it from the resource in this jar.
	 * 
	 * @param filepath
	 * @param defaultRes
	 * @return true if the config was loaded
	 */
	@SuppressWarnings({ "unchecked" })
	public boolean loadConfig(String filepath, String defaultRes) {

		String filename = filepath + FileMgmt.fileSeparator() + defaultRes;

		// Pass the plugin reference so it can load the defaults if needed.
		Map<String, Object> file = FileMgmt.getFile(filename);
		
		if (file != null) {

			for (String Key : file.keySet()) {

				if (Key.equalsIgnoreCase("modify_chat")) {
					Map<String, Object> subNodes = (Map<String, Object>) file.get(Key);

					for (String element : subNodes.keySet()) {
						if (element.equalsIgnoreCase("enable"))
							ChatSettings.setModify_chat(Boolean.valueOf(subNodes.get(element).toString()));

						if (element.equalsIgnoreCase("per_world"))
							ChatSettings.setPer_world(Boolean.valueOf(subNodes.get(element).toString()));
						
						if (element.equalsIgnoreCase("alone_message"))
							ChatSettings.setUsingAloneMessage(Boolean.valueOf(subNodes.get(element).toString()));
						
						if (element.equalsIgnoreCase("alone_message_string"))
							ChatSettings.setUsingAloneMessageString(String.valueOf(subNodes.get(element).toString()));
					}
				}
				
				if (Key.equalsIgnoreCase("display_modes_set_on_join"))
					ChatSettings.setDisplayModesSetOnJoin(Boolean.valueOf(file.get(Key).toString()));

				if (Key.equalsIgnoreCase("colour")) {
					Map<String, Object> subNodes = (Map<String, Object>) file.get(Key);

					for (String element : subNodes.keySet()) {
						if (element.equalsIgnoreCase("king"))
							ChatSettings.setKingColour(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("mayor"))
							ChatSettings.setMayorColour(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("resident"))
							ChatSettings.setResidentColour(subNodes.get(element).toString());
						
						if (element.equalsIgnoreCase("nomad"))
							ChatSettings.setNomadColour(subNodes.get(element).toString());
					}

				}

				if (Key.equalsIgnoreCase("tag_formats")) {
					Map<String, Object> subNodes = (Map<String, Object>) file.get(Key);

					for (String element : subNodes.keySet()) {
						if (element.equalsIgnoreCase("world"))
							ChatSettings.setWorldTag(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("town"))
							ChatSettings.setTownTag(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("nation"))
							ChatSettings.setNationTag(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("both"))
							ChatSettings.setBothTags(subNodes.get(element).toString());
					}

				}

				if (Key.equalsIgnoreCase("channel_formats")) {
					Map<String, Object> subNodes = (Map<String, Object>) file.get(Key);

					channelFormats group = new channelFormats(Key);

					for (String element : subNodes.keySet()) {
						if (element.equalsIgnoreCase("global"))
							group.setGLOBAL(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("town"))
							group.setTOWN(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("nation"))
							group.setNATION(subNodes.get(element).toString());
						
						if (element.equalsIgnoreCase("alliance"))
							group.setALLIANCE(subNodes.get(element).toString());

						if (element.equalsIgnoreCase("default"))
							group.setDEFAULT(subNodes.get(element).toString());
					}
					ChatSettings.addFormatGroup(group);

				}

				if (Key.equalsIgnoreCase("worlds")) {
					Map<String, Object> allWorlds = (Map<String, Object>) file.get(Key);
					
					if (allWorlds != null) {
						for (String worlds : allWorlds.keySet()) {
							Map<String, Object> world = (Map<String, Object>) allWorlds.get(worlds);
	
							channelFormats group = new channelFormats(worlds);
	
							for (String element : world.keySet()) {
								if (element.equalsIgnoreCase("global"))
									group.setGLOBAL(world.get(element).toString());
	
								if (element.equalsIgnoreCase("town"))
									group.setTOWN(world.get(element).toString());
	
								if (element.equalsIgnoreCase("nation"))
									group.setNATION(world.get(element).toString());
								
								if (element.equalsIgnoreCase("alliance"))
									group.setALLIANCE(world.get(element).toString());
	
								if (element.equalsIgnoreCase("default"))
									group.setDEFAULT(world.get(element).toString());
							}
							ChatSettings.addFormatGroup(group);
						}
					}

				}

			}
			return true;
		}
		return false;
	}
}