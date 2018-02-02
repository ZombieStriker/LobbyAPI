package me.zombie_striker.lobbyapi.utils;

import me.zombie_striker.lobbyapi.Main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateAnouncer implements Listener{

	@SuppressWarnings("unused")
	private Main ml;
	
	public UpdateAnouncer(Main j) {
		Bukkit.getPluginManager().registerEvents(this,j);
		ml=j;
	}

	private boolean done=false;
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e){
		if(e.getPlayer().isOp()&&!done){
			done=true;
			Bukkit.broadcastMessage(Main.getPrefix()+" Version "+Main.getPlugin(Main.class).getDescription().getVersion()+" of LobbyAPI has been downloaded.");
			Bukkit.broadcastMessage("");
		}
		
	}
}
