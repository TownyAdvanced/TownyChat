package com.palmergames.bukkit.TownyChat;

import com.palmergames.bukkit.TownyChat.Command.ChannelCommand;
import com.palmergames.bukkit.TownyChat.Command.TownyChatCommand;
import com.palmergames.bukkit.TownyChat.Command.commandobjects.ChannelJoinAliasCommand;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.ChannelsHolder;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.config.ConfigurationHandler;
import com.palmergames.bukkit.TownyChat.listener.TownyChatPlayerListener;
import com.palmergames.bukkit.TownyChat.tasks.onLoadedTask;
import com.palmergames.bukkit.TownyChat.util.FileMgmt;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.util.Version;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Chat plugin to manage all Towny chat
 * 
 * Website: http://code.google.com/a/eclipselabs.org/p/towny/
 * 
 * @author ElgarL
 */

public class Chat extends JavaPlugin {

	private TownyChatPlayerListener TownyPlayerListener;
	private ChannelsHolder channels;
	private ConfigurationHandler channelsConfig;
	
	protected PluginManager pm;
	private Towny towny = null;
	private DynmapAPI dynMap = null;
	
	private static String requiredTownyVersion = "0.96.2.12";
	public static boolean usingPlaceholderAPI = false;
	boolean chatConfigError = false;
	boolean channelsConfigError = false;


	@Override
	public void onEnable() {
		
		pm = getServer().getPluginManager();
		channelsConfig = new ConfigurationHandler(this);
		channels = new ChannelsHolder(this);
		
		checkPlugins();
		loadConfigs();
		
		if (!townyVersionCheck(towny.getDescription().getVersion())) {
			getLogger().severe("Towny version does not meet required version: " + requiredTownyVersion);
			this.getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			getLogger().info("Towny version " + towny.getDescription().getVersion() + " found.");
		}
		
		/*
		 * This executes the task with a 1 tick delay avoiding the bukkit
		 * depends bug.
		 */
		if ((towny == null) || (getServer().getScheduler().scheduleSyncDelayedTask(this, new onLoadedTask(this), 1) == -1)
			|| (channelsConfigError) || (chatConfigError)) {
			/*
			 * We either failed to find Towny or the Scheduler failed to
			 * register the task.
			 */
			getLogger().severe("Could not schedule onLoadedTask.");
			getLogger().severe("Disabling TownyChat...");
			pm.disablePlugin(this);
			return;
		}

		getCommand("townychat").setExecutor(new TownyChatCommand(this));
		getCommand("channel").setExecutor(new ChannelCommand(this));
		registerObjectCommands();
	}
	
	private boolean townyVersionCheck(String version) {
		Version ver = new Version(version);
		Version required = new Version(requiredTownyVersion);
		
		return ver.compareTo(required) >= 0; 
	}

	private void loadConfigs() {
		FileMgmt.checkFolders(new String[] { getRootPath(), getChannelsPath() });
		if (!ChatSettings.loadCommentedConfig(getChannelsPath() + FileMgmt.fileSeparator() + "ChatConfig.yml", this.getDescription().getVersion()))
			chatConfigError = true;
		if (!channelsConfig.loadChannels(getChannelsPath(), "Channels.yml"))
			channelsConfigError = true;
	}
	@Override
	public void onDisable() {
		unregisterPermissions();
		// reset any handles

		dynMap = null;
		towny = null;
		pm = null;
		
		channelsConfig = null;
		channels = null;
	}
	
	/**
	 * Perform a reload of this plugin.
	 */
	public void reload() {
		onDisable();
		onEnable();
	}

	/**
	 * Attempt to hook any plugins we want to access.
	 */
	private void checkPlugins() {
		Plugin test;

		test = pm.getPlugin("Towny");
		if (test != null && test instanceof Towny)
			towny = (Towny) test;

		test = pm.getPlugin("dynmap");
		if (test != null) {
			dynMap = (DynmapAPI) test;
		}
		
		test = pm.getPlugin("PlaceholderAPI");
		if (test != null) {
		    usingPlaceholderAPI = true;
		}

	}

	public void registerEvents() {
		
		if (TownyPlayerListener == null) {
			TownyPlayerListener = new TownyChatPlayerListener(this);
	
			if (TownyPlayerListener != null)
				pm.registerEvents(TownyPlayerListener, this);
		}
		
	}
	
	public void registerPermissions() {
		// Register all Permissions.
		for (String perm : getChannelsHandler().getAllPermissions()) {
			try {
				pm.addPermission(new Permission(perm, new HashMap<String, Boolean>()));
			} catch (IllegalArgumentException e) {
				//permission already registered.
			}
		}
	}
	
	public void unregisterPermissions() {
		// Register all Permissions.
		for (String perm : getChannelsHandler().getAllPermissions()) {
			pm.removePermission(new Permission(perm, new HashMap<String, Boolean>()));
		}
	}
	
	
	public String getRootPath() {
		return getTowny().getDataFolder().getPath();
	}

	public String getChannelsPath() {
		return getRootPath() + FileMgmt.fileSeparator() + "settings";
	}
	
	/**
	 * @return the channels
	 */
	public ChannelsHolder getChannelsHandler() {
		return channels;
	}

	/**
	 * @return the data
	 */
	public ConfigurationHandler getConfigurationHandler() {
		return channelsConfig;
	}

	public Towny getTowny() {
		return towny;
	}
	

	public DynmapAPI getDynmap() {
		return dynMap;
	}

	private void registerObjectCommands() {
		List<Command> commands = new ArrayList<Command>();
		for (Channel channel : channels.getAllChannels().values()) { // All channels
			for (String cmd : channel.getCommands()) { // All Commands of All Channels
				commands.add(new ChannelJoinAliasCommand(cmd, channel, this));
			}
		}
		try {
			final Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");

			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap) bukkitCommandMap.get(Bukkit.getServer());

			commandMap.registerAll("towny", commands);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public TownyChatPlayerListener getTownyPlayerListener() {
		return TownyPlayerListener;
	}
}