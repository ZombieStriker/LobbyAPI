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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;

public class LobbyIcon {

	private String name;
	private int ID;

	private String displayname;
	private List<String> lore = new ArrayList<>();

	private int amount = 1;
	private short color;
	private Material material = Material.GLASS;

	private boolean isHidden = false;

	private boolean loadedFromConfig;

	public LobbyIcon(boolean loadedFC, String servername, int ID, int amount, short color) {
		this.loadedFromConfig = loadedFC;
		this.name = servername;
		this.ID = ID;
		this.amount = amount;
		this.color = color;
	}

	public void setHidden(boolean b) {
		this.isHidden = b;
	}

	public boolean isHidden() {
		return this.isHidden;
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
	public void setColor(short color) {
		this.color = color;
	}

	public Material getMaterial() {
		return this.material;
	}

	public int getAmount() {
		return this.amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setMaterial(Material m) {
		this.material = m;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setDisplayName(String displayname) {
		this.displayname = displayname;
	}

	public String getDisplayName() {
		return displayname;
	}

}
