package me.zombie_striker.lobbyapi;

import java.util.List;

import org.bukkit.Material;

public class LobbyDecor {
	private int ID;

	private int amount=1;
	private short color;
	private Material material = Material.WOOL;
	private short data = 0;

	private String displayname;
	private List<String> lore;

	private String name;

	public LobbyDecor(int slot, String name, String displayname) {
		this.ID = slot;
		this.displayname = displayname;
		this.name = name;
	}
	
	public short getData() {
		return data;
	}
	public void setData(short d) {
		this.data = d;
	}

	public int getSlot() {
		return ID;
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public Material getMaterial() {
		return material;
	}

	public void setMaterial(Material material) {
		this.material = material;
	}

	public String getDisplayname() {
		return displayname;
	}

	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	public List<String> getLore() {
		return lore;
	}

	public void setLore(List<String> lore) {
		this.lore = lore;
	}

	public String getSaveName() {
		return name;
	}

}
