/*
 *  Copyright (C) 2017 Zombie_Striker
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms of the
 *  GNU General Public License as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program;
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA
 */
package me.zombie_striker.lobbyapi;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class LobbyIcon {

	private String name;
	private String saveName;
	private int ID;

	private String displayname;
	private List<String> lore = new ArrayList<>();

	private int amount = 1;
	private short color;
	private Material material = Material.GLASS;

	private boolean isHidden = false;

	private boolean loadedFromConfig;

	public LobbyIcon(boolean loadedFC, String iconName, int ID, int amount, short color) {
		this.loadedFromConfig = loadedFC;
		this.name = iconName;
		this.saveName=name;
		this.ID = ID;
		this.amount = amount;
		this.color = color;
	}

	public boolean isHidden() {
		return this.isHidden;
	}

	public void setHidden(boolean b) {
		this.isHidden = b;
	}

	public boolean loadedFromConfig() {
		return this.loadedFromConfig;
	}

	public int getSlot() {
		return ID;
	}

	public void setSlot(int slot) {
		this.ID = slot;
	}

	public String getName() {
		return this.name;
	}

	public short getColor() {
		return this.color;
	}

	public void setData(short color) {
		this.color = color;
	}

	public Material getMaterial() {
		return this.material;
	}

	public void setMaterial(Material m) {
		this.material = m;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public String getDisplayName() {
		return displayname;
	}

	public void setDisplayName(String displayname) {
		this.displayname = displayname;
	}


	public String getSaveName() {
		return saveName;
	}

	public void setSaveName(String savename) {
		this.saveName = savename;
	}


	public static final String CONFIGPREFIX_WORLD = "Worlds";
	public static final String CONFIGPREFIX_DECOR = "Decor";
	public static final String CONFIGPREFIX_SERVER = "Servers";
	public static final String CONFIGPREFIX_BUTTON = "Buttons";
	public static String getConfigPrefix(LobbyIcon type){
		if(type instanceof  LobbyWorld)
			return CONFIGPREFIX_WORLD;
		if(type instanceof  LobbyDecor)
			return CONFIGPREFIX_DECOR;
		if(type instanceof  LobbyServer)
			return CONFIGPREFIX_SERVER;
		if(type instanceof  LobbyButton)
			return CONFIGPREFIX_BUTTON;
		return "Null";
	}


}
