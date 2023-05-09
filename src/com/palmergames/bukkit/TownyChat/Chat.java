package com.palmergames.bukkit.TownyChat;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.TownyChat.Command.ChannelCommand;
import com.palmergames.bukkit.TownyChat.Command.TownyChatCommand;
import com.palmergames.bukkit.TownyChat.Command.commandobjects.ChannelJoinAliasCommand;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.ChannelsHolder;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.config.ChannelConfigurationHandler;
import com.palmergames.bukkit.TownyChat.listener.EssentialsDiscordHookListener;
import com.palmergames.bukkit.TownyChat.listener.TownyChatPlayerListener;
import com.palmergames.bukkit.TownyChat.tasks.onLoadedTask;
import com.palmergames.bukkit.TownyChat.util.EssentialsIntegration;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.scheduling.TaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.BukkitTaskScheduler;
import com.palmergames.bukkit.towny.scheduling.impl.FoliaTaskScheduler;
import com.palmergames.bukkit.util.Version;
import com.palmergames.util.FileMgmt;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
	private ChannelConfigurationHandler channelsConfig;
	
	protected PluginManager pm;
	private static Chat chat = null;
	private final TaskScheduler scheduler;
	private Towny towny = null;
	private DynmapAPI dynMap = null;
	private Essentials essentials = null;
	
	private static Version requiredTownyVersion = Version.fromString("0.99.0.6");
	public static boolean usingPlaceholderAPI = false;
	public static boolean usingEssentialsDiscord = false;
	boolean chatConfigError = false;
	boolean channelsConfigError = false;
	private static ConcurrentMap<UUID, Channel> playerChannelMap;

	public Chat() {
		chat = this;
		this.scheduler = isFoliaClassPresent() ? new FoliaTaskScheduler(this) : new BukkitTaskScheduler(this);
	}

	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
		channelsConfig = new ChannelConfigurationHandler(this);
		channels = new ChannelsHolder(this);
		playerChannelMap = new ConcurrentHashMap<>();
		
		checkPlugins();
		if (towny == null || !townyVersionCheck(towny.getDescription().getVersion())) {
			disableWithMessage("Towny version does not meet required minimum version: " + requiredTownyVersion.toString());
			return;
		} else {
			getLogger().info("Towny version " + towny.getDescription().getVersion() + " found.");
		}
		
		loadConfigs();
		if (channelsConfigError || chatConfigError) {
			disableWithMessage("The config could not be loaded.");
			return;
		}
		
		/*
		 * This executes the task with a 1 tick delay avoiding the bukkit depends bug.
		 * TODO: What bug is this referencing? This goes back to the first version of TownyChat.
		 */
		scheduler.run(new onLoadedTask(this));

		getCommand("townychat").setExecutor(new TownyChatCommand(this));
		getCommand("channel").setExecutor(new ChannelCommand(this));
		registerObjectCommands();
	}
	
	private void disableWithMessage(String message) {
		getLogger().severe(message);
		getLogger().severe("Disabling TownyChat...");
		pm.disablePlugin(this);
	}

	private boolean townyVersionCheck(String version) {
		return Version.fromString(version).compareTo(requiredTownyVersion) >= 0; 
	}

	private void loadConfigs() {
		FileMgmt.checkOrCreateFolders(new String[] { getRootPath(), getTownySettingsPath() });
		chatConfigError = !ChatSettings.loadCommentedChatConfig();
		channelsConfigError = !channelsConfig.loadChannels();
	}

	public static Chat getTownyChat() {
		return chat;
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
		playerChannelMap = null;
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
		if (test != null && pm.getPlugin("dynmap").isEnabled()) {
			dynMap = (DynmapAPI) test;
		}
		
		test = pm.getPlugin("PlaceholderAPI");
		if (test != null) {
		    usingPlaceholderAPI = true;
		}

		test = pm.getPlugin("EssentialsDiscord");
		if (test != null) {
			usingEssentialsDiscord = true;
		}

		test = pm.getPlugin("Essentials");
		if (test != null) {
			this.essentials = (Essentials) test;
		}

	}

	public void registerEvents() {
		
		if (TownyPlayerListener == null) {
			TownyPlayerListener = new TownyChatPlayerListener(this);
	
			if (TownyPlayerListener != null)
				pm.registerEvents(TownyPlayerListener, this);
		}

		if (usingEssentialsDiscord) {
			pm.registerEvents(new EssentialsDiscordHookListener(this), this);
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

	public String getTownySettingsPath() {
		return getRootPath() + File.separator + "settings";
	}

	public String getChatConfigPath() {
		return getTownySettingsPath() + File.separator + "ChatConfig.yml";
	}
	
	public String getChannelsConfigPath() {
		return getTownySettingsPath() + File.separator + "Channels.yml";
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
	public ChannelConfigurationHandler getConfigurationHandler() {
		return channelsConfig;
	}

	public Towny getTowny() {
		return towny;
	}

	public DynmapAPI getDynmap() {
		return dynMap;
	}

	public boolean isUsingEssentials() {
		return essentials != null;
	}

	public Essentials getEssentials() {
		return essentials;
	}

	public boolean isEssentialsMuted(Player player) {
		if (!isUsingEssentials())
			return false;
		return EssentialsIntegration.isMuted(player);
	}

	public boolean isIgnoredByEssentials(Player sender, Player player) {
		if (!isUsingEssentials())
			return false;
		return EssentialsIntegration.ignoredByEssentials(sender, player);
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
	
	public Channel getPlayerChannel(Player player) {
		return playerChannelMap.get(player.getUniqueId());
	}
	
	public void setPlayerChannel(Player player, Channel channel) {
		playerChannelMap.put(player.getUniqueId(), channel);
	}

	public TaskScheduler getScheduler() {
		return this.scheduler;
	}

	private static boolean isFoliaClassPresent() {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}