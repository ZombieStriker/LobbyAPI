package me.zombie_striker.lobbyapi;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

public class BungeeMessager implements PluginMessageListener {

	@Override
	public void onPluginMessageReceived(String arg0, Player arg1, byte[] message) {
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("PlayerCount")) {
			String server = in.readUTF(); // Name of server, as given in the arguments
			int playercount = in.readInt();
			LobbyAPI.getServer(server).setPlayerCount(playercount);
		}

	}

}
