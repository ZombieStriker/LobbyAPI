package me.zombie_striker.lobbyapi;

import java.util.ArrayList;
import java.util.List;

public class LobbyButton extends LobbyIcon {

	private List<String> commands = new ArrayList<>();

	public LobbyButton(int slot, String name, String displayname) {
		super(true, name, slot, 1, (short) 0);
		super.setDisplayName(displayname);
	}

	public List<String> getCommands(){
		return commands;
	}
	public void addCommand(String command){
		this.commands.add(command);
	}
	public void removeCommand(int slot){
		this.commands.remove(slot);
	}
	public  void setCommands(List<String> commands){
		this.commands = commands;
	}

}
