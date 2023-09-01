package com.palmergames.bukkit.TownyChat.channels;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.TownyChatFormatter;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.TownyChat.events.AsyncChatHookEvent;
import com.palmergames.bukkit.TownyChat.events.PlayerJoinChatChannelEvent;
import com.palmergames.bukkit.TownyChat.listener.LocalTownyChatEvent;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyMessaging;
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
import java.util.UnknownFormatConversionException;
import java.util.stream.Collectors;

public class StandardChannel extends Channel {

	private Chat plugin;
	
	public StandardChannel(Chat instance, String name) {
		super(name);
		this.plugin = instance;
	}

	@Override
	public void chatProcess(AsyncPlayerChatEvent event) {
		
		channelTypes channelType = this.getType();
		Player player = event.getPlayer();
		boolean notifyjoin = false;

		Resident resident = TownyUniverse.getInstance().getResident(player.getUniqueId());
		if (resident == null)
			return;
		Town town = TownyAPI.getInstance().getResidentTownOrNull(resident);
		Nation nation = TownyAPI.getInstance().getResidentNationOrNull(resident);

		// If the channel would require a town/nation which is null, cancel and fail early.
		if (town == null && channelType.equals(channelTypes.TOWN) ||
			nation == null && (channelType.equals(channelTypes.NATION) || channelType.equals(channelTypes.ALLIANCE))) {
			event.setCancelled(true);
			return;
		}

		// If player sends a message to a channel they have left
		// tell the channel to add the player back
		if (isAbsent(player.getName())) {
			join(player);
			notifyjoin = true;
			Bukkit.getPluginManager().callEvent(new PlayerJoinChatChannelEvent(player, this));
		}

		// Set the channel specific format
		String format = ChatSettings.getChannelFormat(player, channelType);

		// Get the list of message recipients.
		Set<Player> recipients = getRecipients(player, town, nation, channelType, event.getRecipients());

		// Try sending an alone message if it is called for.
		trySendingAloneMessage(player, recipients);

		// format is left to store the original non-PAPI-parsed chat format.
		String newFormat = format;
		// Parse any PAPI placeholders.
		if (Chat.usingPlaceholderAPI)
			newFormat = PlaceholderAPI.setPlaceholders(player, format);

		/*
		 * Only modify GLOBAL channelType chat (general and local chat channels) if isModifyChat() is true.
		 */
		if (!(channelType.equals(channelTypes.GLOBAL) && !ChatSettings.isModify_chat()))
			applyFormats(event, format, newFormat, resident);

		/*
		 *  Set recipients for Bukkit to send this message to.
		 */
		event.getRecipients().clear();
		event.getRecipients().addAll(recipients);

		// If the server has marked this Channel as hooked, fire the AsyncChatHookEvent.
		// If the event is cancelled, cancel the chat entirely.
		// Fires its own sendSpyMessage().
		if (isHooked())
			if (!sendOffHookedMessage(event, channelType)) {
				event.setCancelled(true);
				return;
			}

		// Send spy message if this was never hooked.
		if (!isHooked())
			sendSpyMessage(event, channelType);

		// Play the channel sound, if used.
		tryPlayChannelSound(event.getRecipients());

		if (notifyjoin)
			TownyMessaging.sendMessage(player, "You join " + Colors.translateColorCodes(getMessageColour()) + getName());

		/*
		 * Perform any last channel specific functions like logging this chat and
		 * relaying to Dynmap.
		 */
		switch (channelType) {
		case TOWN:
		case NATION:
		case ALLIANCE:
		case DEFAULT:
			break;
		case PRIVATE:
		case GLOBAL:
			tryPostToDynmap(player, event.getMessage());
			break;
		}
	}

	private Set<Player> getRecipients(Player player, Town town, Nation nation, channelTypes channelType, Set<Player> recipients) {
		return switch (channelType) {
		case TOWN -> new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayers(town)));
		case NATION -> new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayers(nation)));
		case ALLIANCE -> new HashSet<>(findRecipients(player, TownyAPI.getInstance().getOnlinePlayersAlliance(nation)));
		case DEFAULT -> new HashSet<>(findRecipients(player, new ArrayList<>(recipients)));
		case GLOBAL, PRIVATE -> new HashSet<>(findRecipients(player, new ArrayList<>(recipients)));
		};
	}

	/**
	 * Compile a list of valid recipients for this message.
	 *
	 * @param sender
	 * @param playerList
	 * @return Set containing a list of players for this message.
	 */
	private Set<Player> findRecipients(Player sender, List<Player> playerList) {
		// Refresh the potential channels a player can see, if they are not currently in the channel.
		playerList.stream().forEach(p -> refreshPlayer(this, p));
		return playerList.stream()
				.filter(p -> hasListenPermission(p)) // Check permission.
				.filter(p -> testDistance(sender, p, getRange())) // Within range.
				.filter(p -> !plugin.isIgnoredByEssentials(sender, p)) // Check essentials ignore.
				.filter(p -> !isAbsent(p.getName())) // Check if player is purposefully absent.
				.collect(Collectors.toSet());
	}

	private void refreshPlayer(Channel channel, Player player) {
		if (!channel.isPresent(player.getName()))
			channel.forgetPlayer(player);
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
		return player1.getWorld().equals(player2.getWorld()) && 
				player1.getLocation().distance(player2.getLocation()) < range;
	}

	private void trySendingAloneMessage(Player sender, Set<Player> recipients) {
		if (ChatSettings.isUsingAloneMessage() &&
				recipients.stream().filter(p -> sender.canSee(p)).count() < 2) // sender will usually be a recipient of their own message.
			TownyMessaging.sendMessage(sender, ChatSettings.getUsingAloneMessageString());
	}

	private void applyFormats(AsyncPlayerChatEvent event, String originalFormat, String workingFormat, Resident resident) {
		// Parse out our own channelTag and msgcolour tags.
		String newFormat = parseTagAndMsgColour(workingFormat);
		// Attempt to apply the new format.
		catchFormatConversionException(event, originalFormat, newFormat);
		
		// Fire the LocalTownyChatEvent.
		LocalTownyChatEvent chatEvent = new LocalTownyChatEvent(event, resident);
		// Format the chat line, replacing the TownyChat chat tags.
		newFormat = TownyChatFormatter.getChatFormat(chatEvent);
		// Attempt to apply the new format.
		catchFormatConversionException(event, originalFormat, newFormat);
	}

	private String parseTagAndMsgColour(String format) {
		return format
			.replace("{channelTag}", Colors.translateColorCodes(getChannelTag() != null ? getChannelTag() : ""))
			.replace("{msgcolour}", Colors.translateColorCodes(getMessageColour() != null ? getMessageColour() : ""));
	}

	private void catchFormatConversionException(AsyncPlayerChatEvent event, String format, String newFormat) {
		try {
			event.setFormat(newFormat);
		} catch (UnknownFormatConversionException e) {
			// This exception is usually thrown when a PAPI placeholder did not get parsed
			// and has left behind a % symbol followed by something that String#format
			// cannot handle.
			boolean percentSymbol = format.contains("%" + e.getConversion());
			String errmsg = "TownyChat tried to apply a chat format that is not allowed: '" +
					newFormat + "', because of the " + e.getConversion() + " symbol" +
					(percentSymbol ? ", found after a %. There is probably a PAPIPlaceholder that could not be parsed." : "." +
					" You should attempt to correct this in your towny\\settings\\chatconfig.yml file and use /townychat reload.");
			Chat.getTownyChat().getLogger().severe(errmsg);

			if (percentSymbol)
				// Attempt to remove the unparsed placeholder and send this right back.
				catchFormatConversionException(event, format, purgePAPI(newFormat, "%" + e.getConversion()));
			else
				// Just let the chat go, this results in an error in the log, and TownyChat not being able to format chat.
				event.setFormat(format);
		}
	}

	private String purgePAPI(String format, String startOfPlaceholder) {
		return format.replaceAll(startOfPlaceholder + ".*%", "");
	}

	/**
	 * Send off TownyChat's {@link AsyncChatHookEvent} which allows other plugins to
	 * cancel or modify TownyChat's messaging.
	 * 
	 * @param event {@link AsyncPlayerChatEvent} that has caused a message.
	 * @param channelType {@link channelTypes} which this message is being sent through.
	 * @return false if the AsyncChatHookEvent is cancelled.
	 */
	private boolean sendOffHookedMessage(AsyncPlayerChatEvent event, channelTypes channelType) {
		AsyncChatHookEvent hookEvent = new AsyncChatHookEvent(event, this, !Bukkit.getServer().isPrimaryThread());
		Bukkit.getServer().getPluginManager().callEvent(hookEvent);
		if (hookEvent.isCancelled())
			return false;
		/*
		 * Send spy message before another plugin changes any of the recipients, so we
		 * know which people can see it.
		 */
		sendSpyMessage(event, channelType);

		if (hookEvent.isChanged()) {
			event.setMessage(hookEvent.getMessage());
			event.setFormat(hookEvent.getFormat());
			event.getRecipients().clear();
			event.getRecipients().addAll(hookEvent.getRecipients());
		}
		return true;
	}

	/**
	 * Sends messages to spies who have not already seen the message naturally.
	 * 
	 * @param event - Chat Event.
	 * @param type - Channel Type
	 */
	private void sendSpyMessage(AsyncPlayerChatEvent event, channelTypes type) {
		Set<Player> recipients = event.getRecipients();
		Set<Player> spies = getSpies();
		String format = formatSpyMessage(type, event.getPlayer());
		if (format == null) return;
		
		String message = Colors.translateColorCodes(event.getMessage());
		// Remove spies who've already seen the message naturally.
		spies.stream()
			.filter(spy -> !recipients.contains(spy))
			.forEach(spy -> TownyMessaging.sendMessage(spy, format + message));
	}

	/**
	 * @return A Set of online players who are spying.
	 */
	private Set<Player> getSpies() {
		// Compile the list of recipients with spy perms
		return plugin.getServer().getOnlinePlayers().stream()
				.filter(p -> plugin.getTowny().hasPlayerMode(p, "spy"))
				.collect(Collectors.toSet());
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
		String channelPrefix = Colors.translateColorCodes(getChannelTag() != null ? getChannelTag() : getName()) + " ";
		if (isGovernmentChannel()) // Town, Nation, Alliance channels get an extra [Name] added after the channelPrefix.
			channelPrefix = getGovtChannelSpyingPrefix(resident, type, channelPrefix);
		return ChatColor.GOLD + "[SPY] " + ChatColor.WHITE + channelPrefix + resident.getName() + ": ";
	}

	private String getGovtChannelSpyingPrefix(Resident resident, channelTypes type, String channelPrefix) {
		String slug = type.equals(channelTypes.TOWN)
			? TownyAPI.getInstance().getResidentTownOrNull(resident).getName()    // Town chat.
			: TownyAPI.getInstance().getResidentNationOrNull(resident).getName(); // Nation or Alliance chat.
		return channelPrefix + "[" + slug + "] ";
	}

	/**
	 * Try to send a channel sound, if enabled.
	 * 
	 * @param recipients Set of Players that will receive the message and potentially the sound.
	 */
	private void tryPlayChannelSound(Set<Player> recipients) {
		if (getChannelSound() == null)
			return;
		for (Player recipient : recipients) {
			if (!isSoundMuted(recipient)) {
				try {
					recipient.playSound(recipient, Sound.valueOf(getChannelSound()), 1.0f, 1.0f);
				} catch (IllegalArgumentException ex) {
					plugin.getLogger().warning("Channel " + this.getName() + " has an invalid sound configured.");
					setChannelSound(null);
					break;
				}
			}
		}
	}

	/**
	 * Try to send a message to dynmap's web chat.
	 * @param player Player which has spoken.
	 * @param message Message being spoken.
	 */
	private void tryPostToDynmap(Player player, String message) {
		if (super.getRange() > 0)
			return;
		DynmapAPI dynMap = plugin.getDynmap();
		if (dynMap != null)
			dynMap.postPlayerMessageToWeb(player, message);
	}
}
