package com.palmergames.bukkit.TownyChat.listener;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EssentialsDiscordHookListener implements Listener {
  private Chat plugin;

  public EssentialsDiscordHookListener(Chat plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true)
  public void onDiscordChat(DiscordChatMessageEvent event) {
    Channel channel = plugin.getChannelsHandler().getChannel(event.getPlayer(), plugin.getTownyPlayerListener().directedChat.get(event.getPlayer()));
    if (channel != null) {
      event.setCancelled(!channel.isHooked());
      plugin.getTownyPlayerListener().directedChat.remove(event.getPlayer());
      return;
    }

    for (Channel curChannel : plugin.getChannelsHandler().getAllChannels().values()) {
      if (plugin.getTowny().hasPlayerMode(event.getPlayer(), curChannel.getName())) {
        event.setCancelled(!curChannel.isHooked());
        return;
      }
    }

    channel = plugin.getChannelsHandler().getActiveChannel(event.getPlayer(), channelTypes.GLOBAL);
    if (channel != null) {
      event.setCancelled(!channel.isHooked());
    }
  }
}
