package com.palmergames.bukkit.TownyChat.channels;

import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;

public abstract class Channel {
	
	private String name;
	private List<String> commands;
	private channelTypes type;
	private String channelTag, messageColour, permission, leavePermission, channelSound;
	private double range;
	private boolean hooked=false;
	private boolean autojoin=true;
	private boolean focusable = true;
	private double spamtime;
	private WeakHashMap<Player, Long> spammers = new WeakHashMap<>();
	protected ConcurrentMap<String, Integer> absentPlayers = null;  
	protected ConcurrentMap<String, Integer> mutedPlayers = null;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 */
	public Channel(String name) {
		this.name = name;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the commands
	 */
	public List<String> getCommands() {
		return commands;
	}
	/**
	 * @param commands the commands to set
	 */
	public void setCommands(List<String> commands) {
		this.commands = commands;
	}
	/**
	 * @return the type
	 */
	public channelTypes getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(channelTypes type) {
		this.type = type;
	}
	/**
	 * @return the channelTag
	 */
	public String getChannelTag() {
		return channelTag;
	}
	/**
	 * @param channelTag the channelTag to set
	 */
	public void setChannelTag(String channelTag) {
		this.channelTag = channelTag;
	}
	/**
	 * @return the messageColour
	 */
	public String getMessageColour() {
		return messageColour;
	}
	/**
	 * @param messageColour the messageColour to set
	 */
	public void setMessageColour(String messageColour) {
		this.messageColour = messageColour;
	}
	/**
	 * @return the permission
	 */
	public String getPermission() {
		return permission;
	}
	/**
	 * @param permission the permission to set
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}
	/**
	 * @return the range
	 */
	public double getRange() {
		return range;
	}
	/**
	 * @param range the range to set
	 */
	public void setRange(double range) {
		this.range = range;
	}
	/**
	 * @param event the event to process
	 */
	public abstract void chatProcess(AsyncPlayerChatEvent event);
	
	/*
	 * Used to reset channel settings for a given player
	 */
	public void forgetPlayer(Player player) {

		if (playerIgnoringThisChannel(player)
			|| !hasPermission(player)
			|| !autojoin)
			leave(player);
		else
			join(player);
	}

	/*
	 * Mark a player as having left chat
	 */
	public boolean leave(Player player) {
		if (absentPlayers == null) {
			absentPlayers = new ConcurrentHashMap<String, Integer> ();
		}
		Integer res = absentPlayers.put(player.getName(), 1);
		
		// If the player could see this channel if they wanted to, 
		// we know they are ignoring the channel by choice.
		if (hasPermission(player) && !playerIgnoringThisChannel(player))
			playerAddIgnoreMeta(player);
		
		return (res == null || res == 0);
	}
	
	/*
	 * Mark a player has having joined the chat
	 */
	public boolean join(Player player) {
		if (absentPlayers == null) return false;
		Integer res = absentPlayers.remove(player.getName());
		playerRemoveIgnoreMeta(player);
		return (res != null && res == 1);
	}
	
	/*
	 * Check if a player is present in a chat
	 */
	public boolean isPresent(String name) {
		if (absentPlayers == null) return true;
		if (absentPlayers.containsKey(name)) return false;
		return true;
	}

	/*
	 * Check if a player is not present in a chat
	 */
	public boolean isAbsent(String name) {
		return !isPresent(name);
	}

	/*
	 * Check if a player is muted in a channel
	 */
	public boolean isMuted(String name) {
		if (mutedPlayers == null) return false;
		if (!mutedPlayers.containsKey(name)) return false;
		return true;
	}

	/*
	 * Mute a player
	 */
	public boolean mute(String name) {
		if (mutedPlayers == null) {
			mutedPlayers = new ConcurrentHashMap<String, Integer> ();
		}
		Integer i = mutedPlayers.get(name);
		if (i != null) return false;
		mutedPlayers.put(name, 1);
		return true;
	}

	/*
	 * Unmute a player 
	 */
	public boolean unmute(String name) {
		if (mutedPlayers == null) return false;
		Integer i = mutedPlayers.get(name);
		if (i == null) return false;
		mutedPlayers.remove(name);
		return true;
	}

	public boolean isSoundMuted(Player player) {
		return playerIgnoringSoundMeta(player);
	}

	public void muteSound(Player player) {
		playerAddSoundIgnoreMeta(player);
	}

	public void unmuteSound(Player player) {
		playerRemoveSoundIgnoreMeta(player);
	}
	
	/*
	 * Get name of permissions node to leave a the channel 
	 */
	public String getLeavePermission() {
		return leavePermission;
	}

	/*
	 * Set name of permissions node to leave a the channel 
	 */
	public void setLeavePermission(String permission) {
		leavePermission = permission;
	}

	public boolean hasMuteList() {
		if (mutedPlayers == null || mutedPlayers.isEmpty()) return false;
		return true;
	}

	public Set<String> getMuteList() {
		return mutedPlayers.keySet();
	}
	
	public void setHooked(boolean hooked) {
		this.hooked = hooked;
	}

	public boolean isHooked() {
		return hooked;
	}
	
	public void setAutoJoin(boolean autojoin) {
		this.autojoin = autojoin;
	}
	
	public boolean isAutoJoin() {
		return autojoin;
	}

	public boolean isFocusable() {
		return focusable;
	}
	public void setFocusable(boolean focusable) {
		this.focusable = focusable;
	}
	public double getSpam_time() {
		return spamtime;
	}

	public void setSpam_time(double spamtime) {
		this.spamtime = spamtime;
	}

	public String getChannelSound() {
		return channelSound;
	}

	public void setChannelSound(String channelSound) {
		this.channelSound = channelSound;
	}
	
	/**
	 * Test if this player is spamming chat.
	 * One message every 2 seconds limit
	 * 
	 * @param player
	 * @return
	 */
	public boolean isSpam(Player player) {
		if (player.hasPermission("townychat.spam.bypass"))
			return false;
		long timeNow = System.currentTimeMillis();
		long spam = timeNow;
		
		if (spammers.containsKey(player)) {
			spam = spammers.get(player);
			spammers.remove(player);
		} else {
			// No record found so ensure we don't trigger for spam
			spam -= ((getSpam_time() + 1)*1000);
		}
		
		if (timeNow - spam < (getSpam_time()*1000)) {
			spammers.put(player, spam);
			TownyMessaging.sendErrorMsg(player, Translatable.of("tc_err_unable_to_talk_you_are_spamming"));
			return true;
		}
		spammers.put(player, timeNow);
		return false;
	}

	private void playerAddIgnoreMeta(Player player) {
		StringDataField icdf = new StringDataField("townychat_ignoredChannels", "", "Ignored TownyChat Channels");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId()); 
		if (resident == null)
			return;

		if (resident.hasMeta(icdf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(icdf.getKey());
			if (cdf instanceof StringDataField) {
				StringDataField sdf = (StringDataField) cdf;
				sdf.setValue(sdf.getValue().concat(", " + this.getName()));
				TownyUniverse.getInstance().getDataSource().saveResident(resident);
			}

		} else {
			resident.addMetaData(new StringDataField("townychat_ignoredChannels", this.getName(), "Ignored TownyChat Channels"));
		}
	}
	
	private void playerRemoveIgnoreMeta(Player player) {
		StringDataField icdf = new StringDataField("townychat_ignoredChannels", "", "Ignored TownyChat Channels");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId()); 
		if (resident == null || !resident.hasMeta(icdf.getKey()))
			return;

		CustomDataField<?> cdf = resident.getMetadata(icdf.getKey());
		if (cdf instanceof StringDataField) {
			StringDataField sdf = (StringDataField) cdf;
			String newValues = "";
			String[] values = sdf.getValue().split(", ");
			for (String chanName : values)
				if (!chanName.equalsIgnoreCase(this.getName()))
					if (newValues.isEmpty())
						newValues = chanName;
					else 
						newValues += ", " + chanName;

			if (!newValues.isEmpty()) {
				sdf.setValue(newValues);
				TownyUniverse.getInstance().getDataSource().saveResident(resident);
			} else {
				resident.removeMetaData(icdf);
			}
		}
		
	}

	private boolean playerIgnoringThisChannel(Player player) {
		StringDataField idf = new StringDataField("townychat_ignoredChannels", "", "Ignored TownyChat Channels");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId()); 
		if (resident != null && resident.hasMeta(idf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(idf.getKey());
			if (cdf instanceof StringDataField) {
				StringDataField sdf = (StringDataField) cdf;
				String[] split = sdf.getValue().split(", ");
				for (String string : split)
					if (string.equalsIgnoreCase(this.getName()))
						return true;
			}
		}
		return false;
	}

	private void playerAddSoundIgnoreMeta(Player player) {
		StringDataField icdf = new StringDataField("townychat_soundOffChannels", "", "TownyChat Channels with Sound Toggle Off");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null)
			return;

		if (resident.hasMeta(icdf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(icdf.getKey());
			if (cdf instanceof StringDataField) {
				StringDataField sdf = (StringDataField) cdf;
				sdf.setValue(sdf.getValue().concat(", " + this.getName()));
				TownyUniverse.getInstance().getDataSource().saveResident(resident);
			}
		} else {
			resident.addMetaData(new StringDataField("townychat_soundOffChannels", this.getName(), "TownyChat Channels with Sound Toggle Off"));
		}
	}

	private void playerRemoveSoundIgnoreMeta(Player player) {
		StringDataField icdf = new StringDataField("townychat_soundOffChannels", "", "TownyChat Channels with Sound Toggle Off");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null || !resident.hasMeta(icdf.getKey()))
			return;

		CustomDataField<?> cdf = resident.getMetadata(icdf.getKey());
		if (cdf instanceof StringDataField) {
			StringDataField sdf = (StringDataField) cdf;
			String newValues = "";
			String[] values = sdf.getValue().split(", ");
			for (String chanName : values)
				if (!chanName.equalsIgnoreCase(this.getName()))
					if (newValues.isEmpty())
						newValues = chanName;
					else
						newValues += ", " + chanName;

			if (!newValues.isEmpty()) {
				sdf.setValue(newValues);
				TownyUniverse.getInstance().getDataSource().saveResident(resident);
			} else {
				resident.removeMetaData(icdf);
			}
		}
	}

	private boolean playerIgnoringSoundMeta(Player player) {
		StringDataField idf = new StringDataField("townychat_soundOffChannels", "", "TownyChat Channels with Sound Toggle Off");
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident != null && resident.hasMeta(idf.getKey())) {
			CustomDataField<?> cdf = resident.getMetadata(idf.getKey());
			if (cdf instanceof StringDataField) {
				StringDataField sdf = (StringDataField) cdf;
				String[] split = sdf.getValue().split(", ");
				for (String string : split)
					if (string.equalsIgnoreCase(this.getName()))
						return true;
			}
		}
		return false;
	}
	
	public boolean isGovernmentChannel() {
		return type.equals(channelTypes.TOWN) || type.equals(channelTypes.NATION) || type.equals(channelTypes.ALLIANCE); 
	}
	public boolean hasPermission(Player player) {
		return getPermission() != null && TownyUniverse.getInstance().getPermissionSource().testPermission(player, getPermission());
	}
}
