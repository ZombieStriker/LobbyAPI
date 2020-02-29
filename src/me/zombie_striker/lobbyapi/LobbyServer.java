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

public class LobbyServer extends LobbyIcon {

	int playerCount = 0;

	public LobbyServer(boolean loadedFC, String servername, int ID, int amount, short color) {
		super(loadedFC, servername, ID, amount, color);
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public void setPlayerCount(int a) {
		this.playerCount = a;
	}

	@Override
	public String getInventorySaveName() {
		return getName();
	}
}
