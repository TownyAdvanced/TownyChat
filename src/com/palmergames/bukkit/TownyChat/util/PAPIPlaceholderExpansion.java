package com.palmergames.bukkit.TownyChat.util;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.md_5.bungee.api.ChatColor;

public class PAPIPlaceholderExpansion extends PlaceholderExpansion {

	private final Chat plugin;

	public PAPIPlaceholderExpansion(Chat plugin) {
		this.plugin = plugin;
	}

	@Override
	public @NotNull String getAuthor() {
		return "TownyAdvanced Team and Contributors";
	}

	@Override
	public @NotNull String getIdentifier() {
		return "townychat";
	}

	@Override
	public @NotNull String getVersion() {
		return plugin.getPluginMeta().getVersion();
	}

	@Override
	public boolean persist() {
		return true;
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public String onRequest(OfflinePlayer player, String identifier) {
		return ChatColor.translateAlternateColorCodes('&', getOfflinePlayerPlaceholder(player, identifier));
	}

	private String getOfflinePlayerPlaceholder(OfflinePlayer offlinePlayer, String identifier) {
		if (offlinePlayer == null || !offlinePlayer.isOnline())
			return "";

		Channel playerChannel = plugin.getPlayerChannel(offlinePlayer.getPlayer());
		if (playerChannel == null)
			return "";

		switch (identifier) {
		case "current_channel": // %townychat_current_channel%
		case "current_channel_name": // %townychat_current_channel_name%
			return playerChannel.getName();
		case "current_channel_tag": // %townychat_current_channel_tag%
			return playerChannel.getChannelTag();
		case "current_channel_message_colour": // %townychat_current_channel_message_colour%
			return playerChannel.getMessageColour();
		default:
			return "";
		}
	}
}
