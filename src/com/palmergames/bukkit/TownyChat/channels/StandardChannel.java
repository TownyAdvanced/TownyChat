package com.palmergames.bukkit.TownyChat.channels;

import com.earth2me.essentials.User;
import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.HexFormatter;
import com.palmergames.bukkit.TownyChat.TownyChatFormatter;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.palmergames.bukkit.TownyChat.events.PlayerJoinChatChannelEvent;
import com.palmergames.bukkit.TownyChat.listener.LocalTownyChatEvent;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.util.Colors;

import me.clip.placeholderapi.PlaceholderAPI;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.dynmap.DynmapAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StandardChannel extends Channel {

	private Chat plugin;
	
	public StandardChannel(Chat instance, String name) {
		super(name);
		this.plugin = instance;
	}

	@Override
	public void chatProcess(AsyncPlayerChatEvent event) {
		
		channelTypes channelType = channelTypes.valueOf(getType().name());
		Player player = event.getPlayer();
		Set<Player> recipients = null;
		String format = "";
		boolean notifyjoin = false;

		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null)
			return;
		Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);
		Nation nation = TownyAPI.getInstance().getResidentNationOrNull(resident);
		
		// If player sends a message to a channel it had left
		// tell the channel to add the player back
		if (isAbsent(player.getName())) {
			join(player);
			notifyjoin = true;
			Bukkit.getPluginManager().callEvent(new PlayerJoinChatChannelEvent(player, this));
		}

		/*
		 *  Retrieve the channel specific format
		 *  and compile a set of recipients
		 */
		switch (channelType) {
		
		case TOWN:
			if (town == null) {
				event.setCancelled(true);
				return;
			}
			format = ChatSettings.getRelevantFormatGroup(player).getTOWN();
			recipients = new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayers(town)));
			break;
		
		case NATION:
			if (nation == null) {
				event.setCancelled(true);
				return;
			}
			format = ChatSettings.getRelevantFormatGroup(player).getNATION();
			recipients = new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayers(nation)));
			break;
			
		case ALLIANCE:
			if (nation == null) {
				event.setCancelled(true);
				return;
			}
			format = ChatSettings.getRelevantFormatGroup(player).getALLIANCE();
			recipients = new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayersAlliance(nation)));
			break;
			
		case DEFAULT:
			format = ChatSettings.getRelevantFormatGroup(player).getDEFAULT();
			recipients = new HashSet<>(findRecipients(player, new ArrayList<>(event.getRecipients())));
			break;
			
		case GLOBAL:
			format = ChatSettings.getRelevantFormatGroup(player).getGLOBAL();
			recipients = new HashSet<>(findRecipients(player, new ArrayList<>(event.getRecipients())));
			break;
			
		case PRIVATE:
			format = ChatSettings.getRelevantFormatGroup(player).getGLOBAL();
			recipients = new HashSet<>(findRecipients(player, new ArrayList<>(event.getRecipients())));
			break;
		}

		/*
		 * Perform all replace functions on this format
		 */
		if (Chat.usingPlaceholderAPI) {	    	
            format = PlaceholderAPI.setPlaceholders(player, format);
	    }		

		/*
		 * Only modify GLOBAL channelType chat (general and local chat channels) if isModifyChat() is true.
		 */
		if (!(channelType.equals(channelTypes.GLOBAL) && !ChatSettings.isModify_chat()))  {
			event.setFormat(getFormat(format));
			LocalTownyChatEvent chatEvent = new LocalTownyChatEvent(event, resident);
			event.setFormat(TownyChatFormatter.getChatFormat(chatEvent));
		}
		
		/*
		 *  Set all the listeners for Bukkit to send this message to.
		 */
        event.getRecipients().clear();
        event.getRecipients().addAll(recipients);
        
        if (isHooked()) {
        	AsyncChatHookEvent hookEvent = new AsyncChatHookEvent(event, this, !Bukkit.getServer().isPrimaryThread());
            Bukkit.getServer().getPluginManager().callEvent(hookEvent);
            if (hookEvent.isCancelled()) {
            	event.setCancelled(true);
            	return;
            }
            /*
             * Send spy message before another plugin changes any of the recipients,
             * so we know which people can see it.
             */
            sendSpyMessage(event, channelType);
            
            if (hookEvent.isChanged()) {
            	event.setMessage(hookEvent.getMessage());
            	event.setFormat(hookEvent.getFormat());
                event.getRecipients().clear();
                event.getRecipients().addAll(hookEvent.getRecipients());
            }
        } else {
        	/*
        	 * Send spy message.
        	 */
        	sendSpyMessage(event, channelType);
        }

		if (getChannelSound() != null) {
			for (Player recipient : event.getRecipients()) {
				if (!isSoundMuted(player)) {
					try {
						recipient.playSound(player.getLocation(), Sound.valueOf(getChannelSound()), 1.0f, 1.0f);
					} catch (IllegalArgumentException ex) {
						plugin.getLogger().warning("Channel " + this.getName() + " has an invalid sound configured.");
						setChannelSound(null);
						break;
					}
				}
			}
		}


        if (notifyjoin) {
			TownyMessaging.sendMessage(player, "You join " + Colors.White + getName());
        }

        /*
         * Perform any last channel specific functions
         * like logging this chat and relaying to Dynmap.
         */
        switch (channelType) {
		
		case TOWN:
			break;
		
		case NATION:
			break;
		
		case ALLIANCE:
			break;
			
		case DEFAULT:
			break;
			
		case PRIVATE:
		case GLOBAL:
			
			// Don't broadcast local/ranged chat into dynmap.
			if (super.getRange() > 0)
				return;
			
			DynmapAPI dynMap = plugin.getDynmap();
			
			if (dynMap != null)
				dynMap.postPlayerMessageToWeb(player, event.getMessage());
			break;
		}

	}

	private String getFormat(String format) {
		return format
			.replace("{channelTag}", Colors.translateColorCodes(getChannelTag() != null ? getChannelTag() : ""))
			.replace("{msgcolour}", Colors.translateColorCodes(getMessageColour() != null ? getMessageColour() : ""));
	}

	/**
	 * Check the distance between players and return a result based upon the range setting
	 * -1 = no limit
	 * 0 = same world
	 * any positive value = distance in blocks
	 *
	 * @param player1
	 * @param player2
	 * @param range
	 * @return true if in range
	 */
	private boolean testDistance(Player player1, Player player2, double range) {
		
		// unlimited range (all worlds)
		if (range == -1)
			return true;
		
		// Same world only
		if (range == 0)
			return player1.getWorld().equals(player2.getWorld());
		
		// Range check (same world)
		if (player1.getWorld().equals(player2.getWorld()))
			return (player1.getLocation().distance(player2.getLocation()) < range);
		else
			return false;
	}
	
	/**
	 * Compile a list of valid recipients for this message.
	 *
	 * @param sender
	 * @param playerList
	 * @return Set containing a list of players for this message.
	 */
	@SuppressWarnings("deprecation")
	private Set<Player> findRecipients(Player sender, List<Player> playerList) {
		
		Set<Player> recipients = new HashSet<>();
		boolean bEssentials = plugin.getTowny().isEssentials();
		String sendersName = sender.getName();
		Channel channel = plugin.getChannelsHandler().getChannel(getName());
		// Compile the list of recipients
        for (Player player : playerList) {
        	
        	/*
        	 * Refresh the potential channels a player can see, if they are not currently in the channel.
        	 */
        	if (!channel.isPresent(player.getName()))
        		channel.forgetPlayer(player);
        	
        	/*
        	 * If the player has the correct permission node.
        	 */
        	if (TownyUniverse.getInstance().getPermissionSource().has(player, getPermission())) {
        		
        		/*
        		 * If the player is within range for this channel.
        		 */
	        	if (testDistance(sender, player, getRange())){
	        		
	        		if (bEssentials) {
						try {
							User targetUser = plugin.getTowny().getEssentials().getUser(player);
							/*
							 *  Don't send this message if the user is ignored
							 */
							if (targetUser.isIgnoredPlayer(sendersName))
								continue;
						} catch (TownyException e) {
							// Failed to fetch user so ignore.
						}
					}

	        		// Spy's can leave channels and we'll respect that
	        		if (absentPlayers != null) {
	        			// Ignore players who have left this channel
	        			if (absentPlayers.containsKey(player.getName())) {
	        				continue;
	        			}
	        		}
	        		recipients.add(player);
	        	}
        	}
        }
        
        if (recipients.size() <= 1 && ChatSettings.isUsingAloneMessage()) {
        	
			String aloneMsg = ChatSettings.getUsingAloneMessageString(); 
			if (Towny.is116Plus())
				aloneMsg = HexFormatter.translateHexColors(aloneMsg);
        	
        	sender.sendMessage(ChatColor.translateAlternateColorCodes('&', aloneMsg));
        }

        return recipients;
	}
	
	/**
	 * Sends messages to spies who have not already seen the message naturally.
	 * 
	 * @param event - Chat Event.
	 * @param type - Channel Type
	 */
	private void sendSpyMessage(AsyncPlayerChatEvent event, channelTypes type) {		
		Set<Player> recipients = new HashSet<>();				
		Set<Player> spies = new HashSet<>();
		
		recipients.addAll(event.getRecipients());
		spies = checkSpying(spies);
		String format = formatSpyMessage(type, event.getPlayer());
		if (format == null) return;
		
		// Remove spies who've already seen the message naturally.
		for (Player spy : spies)
			if (recipients.contains(spy))
				continue;
			else
				spy.sendMessage(ChatColor.GOLD + "[SPY] " + ChatColor.WHITE + format + ": " + event.getMessage());
	}

	/**
	 * Formats look of message for spies
	 * 
	 * @param type - Channel Type.
	 * @param player - Player who chatted.
	 * @return format - Message format.
	 */
	@Nullable
	private String formatSpyMessage(channelTypes type, Player player) {
		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null)
			return null;
		Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);
		Nation nation = TownyAPI.getInstance().getResidentNationOrNull(resident);
		
		String format = ChatColor.translateAlternateColorCodes('&', getChannelTag() != null ? getChannelTag() : getName());
		if (Towny.is116Plus())
			format = HexFormatter.translateHexColors(format);
		
		switch (type) {
			case TOWN:
				format = format + " [" + town.getName() + "] " + resident.getName();
				break;
			case NATION:
			case ALLIANCE:
				format = format + " [" + nation.getName() + "] " + resident.getName();
				break;
			case GLOBAL:
			case PRIVATE:
			case DEFAULT:
				format = format + " " + resident.getName();
				break;
		}
		return format;
	}
	
	/**
	 * Add all spying players to the recipients list.
	 *
	 * @param recipients
	 * @return new recipients including spying.
	 */
	private Set<Player> checkSpying(Set<Player> recipients) {

		
		// Compile the list of recipients with spy perms
		for (Player test : plugin.getServer().getOnlinePlayers()) {
        	
        	if (plugin.getTowny().hasPlayerMode(test, "spy") && !(recipients.contains(test))) {
        		recipients.add(test);
        	}

        }
		
		return recipients;
	}

}
