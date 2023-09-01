package com.palmergames.bukkit.TownyChat.config;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.StandardChannel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class ChannelConfigurationHandler {
	
	private Chat plugin;
	
	/** Constructor
	 * 
	 * @param plugin
	 */
	public ChannelConfigurationHandler(Chat plugin) {
		super();
		this.plugin = plugin;
	}
	
	
	/**
	 * Load all Channels from the Channels.yml
	 * If it doesn't exist, create it from
	 * the resource in this jar.
	 * 
	 * @return true if the channels were loaded
	 */
	@SuppressWarnings("unchecked")
	public boolean loadChannels() {

		String filename = Chat.getTownyChat().getChannelsConfigPath();

		Map<String, Object> channelsMap;
		try {
			channelsMap = getMap(filename, "Channels.yml");
		} catch (Exception e) {
			Bukkit.getLogger().severe("[TownyChat] Failed to load Channels.yml!");
			Bukkit.getLogger().severe("[TownyChat] Please check that the file passes a YAML Parser test:");
			Bukkit.getLogger().severe("[TownyChat] Online YAML Parser: https://yaml-online-parser.appspot.com/");
			return false;
		}
		if (channelsMap == null || !channelsMap.containsKey("Channels")) 
			return false;

		// Parse the channels
		Map<String, Object> allChannelNodes = (Map<String, Object>) channelsMap.get("Channels");
		if (allChannelNodes == null)
			return false;
		
		// Load channels if the file is NOT empty
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

				if (key.equalsIgnoreCase("focusable")) {
					if (element instanceof Boolean) {
						channel.setFocusable((Boolean)element);
					} else if (element instanceof String) {
						channel.setFocusable(Boolean.parseBoolean(element.toString()));
					} else if (element instanceof Integer) {
						channel.setFocusable(Integer.parseInt(element.toString()) != 0);
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

				if (key.equalsIgnoreCase("listenpermission"))
					if (element instanceof String)
						channel.setListenPermission(element.toString());

				if (key.equalsIgnoreCase("speakpermission"))
					if (element instanceof String)
						channel.setSpeakPermission(element.toString());

				if (key.equalsIgnoreCase("range"))
					channel.setRange(Double.valueOf(element.toString()));

				if (key.equalsIgnoreCase("sound"))
					channel.setChannelSound(String.valueOf(element));
			}
			
			// If no leave permission is set, create a default permission name
			if (channel.getLeavePermission() == null) {
				channel.setLeavePermission("towny.chat.leave." + channel.getName().toLowerCase());
			}
			plugin.getChannelsHandler().addChannel(channel);
		}
		if (plugin.getChannelsHandler().getDefaultChannel() == null) {
			// If there is no default channel set it to the first one that was parsed (the top one in the config)
			// This is because not everyone knows that you need to add a default: true into the channels.yml to make it the default channel!
			plugin.getChannelsHandler().setDefaultChannel(plugin.getChannelsHandler().getAllChannels().entrySet().iterator().next().getValue());
		}
		return true;
		
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> getMap(String filepath, String resource) {

		File f = new File(filepath);
		if (!(f.exists() && f.isFile())) {
			// Populate a new file
			try {
				try (FileOutputStream fos = new FileOutputStream(f)) {
					fos.write(getResourceFileAsString("/" + resource).getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				// No resource file found
				e.printStackTrace();
				return null;
			}
		}

		f = new File(filepath);

		Yaml yamlChannels = new Yaml(new LoaderOptions());
		Object channelsRootDataNode;

		try (FileInputStream fileInputStream = new FileInputStream(f)) {
			channelsRootDataNode = yamlChannels.load(new UnicodeReader(fileInputStream));
			if (channelsRootDataNode == null) {
				throw new NullPointerException();
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException("The following file couldn't pass on Parser.\n" + f.getPath(), ex);
		}

		if (channelsRootDataNode instanceof Map)
			return (Map<String, Object>) channelsRootDataNode;

		return null;
	}

	/**
	 * Reads given resource file as a string.
	 *
	 * @param fileName path to the resource file
	 * @return the file's contents
	 * @throws IOException if read fails for any reason
	 */
	static String getResourceFileAsString(String fileName) throws IOException {
		if (fileName == null)
			return "";
		try (InputStream is = ChannelConfigurationHandler.class.getResourceAsStream(fileName);
			InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
			BufferedReader reader = new BufferedReader(isr)) {
				return reader.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}
}
