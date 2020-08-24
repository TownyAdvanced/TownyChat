package com.palmergames.bukkit.TownyChat.tasks;

import com.palmergames.bukkit.TownyChat.Chat;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import com.palmergames.bukkit.TownyChat.channels.Channel;
import com.palmergames.bukkit.TownyChat.config.ChatSettings;
import com.palmergames.bukkit.towny.Towny;

/*
 * 
 * Created by RocketRidah
 * 
 */

public class onPlayerJoinTask implements Runnable {
	
	Chat plugin;
	Towny towny;
	Player player;
	String mode;
	
	public onPlayerJoinTask(Chat plugin, Player player, Channel defaultChannel) {
        super();
        this.plugin = plugin;
        this.towny = plugin.getTowny();
        this.player = player;
        this.mode = defaultChannel.getName();
	}
	
	@Override
	public void run() {
		if (this.towny.isEnabled()) {
			if (!this.towny.isError()) {
				if (player != null) {
					List<String> modes = new ArrayList<>();
					modes.add(mode);
					if (!towny.getPlayerMode(player).isEmpty())					 
						modes.addAll(towny.getPlayerMode(player));
					towny.setPlayerMode(player, modes.toArray(new String[0]), ChatSettings.getDisplayeModesSetOnJoin());
				}
				return;
			}
		}
	}
	
}