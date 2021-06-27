package com.palmergames.bukkit.TownyChat.listener;

import com.palmergames.bukkit.TownyChat.Chat;
import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.channels.channelTypes;
import net.essentialsx.api.v2.events.discord.DiscordChatMessageEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener which stops non-global or limited-range 
 * chats from passing on to EssentialsDiscord.
 * 
 * @author JRoy
 * @since 0.90
 */
public class EssentialsDiscordHookListener implements Listener {
  private final Chat plugin;

  public EssentialsDiscordHookListener(Chat plugin) {
    this.plugin = plugin;
  }

  @EventHandler()
  public void onDiscordChat(DiscordChatMessageEvent event) {
    String directChat = plugin.getTownyPlayerListener().directedChat.get(event.getPlayer());
    if (directChat != null) {
      plugin.getTownyPlayerListener().directedChat.remove(event.getPlayer());
      if (event.isCancelled()) {
        return;
      }
      Channel channel = plugin.getChannelsHandler().getChannel(event.getPlayer(), directChat);
      if (channel != null) {
        event.setCancelled(channel.getType() != channelTypes.GLOBAL || channel.getRange() != -1);
        return;
      }
    }

    for (Channel curChannel : plugin.getChannelsHandler().getAllChannels().values()) {
      if (plugin.getTowny().hasPlayerMode(event.getPlayer(), curChannel.getName())) {
        event.setCancelled( curChannel.getType() != channelTypes.GLOBAL || curChannel.getRange() != -1);
        return;
      }
    }

    Channel channel = plugin.getChannelsHandler().getActiveChannel(event.getPlayer(), channelTypes.GLOBAL);
    if (channel != null) {
      event.setCancelled(channel.getType() != channelTypes.GLOBAL || channel.getRange() != -1);
    }
  }
}
