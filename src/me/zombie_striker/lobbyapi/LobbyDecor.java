package me.zombie_striker.lobbyapi;

import org.bukkit.Material;

import java.util.List;

public class LobbyDecor extends LobbyIcon {
	public LobbyDecor(int slot, String name, String displayname) {
		super(true, name, slot, 1, (short) 0);
		super.setDisplayName(displayname);
	}
}
