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

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerSelectWorldEvent extends Event{

    private static final HandlerList handlers = new HandlerList();
    private Player clicker;
    private Location des;
    private boolean isCanceled = false;
 
    public PlayerSelectWorldEvent(Player clicker,LobbyWorld selectedWorld) {
       this.clicker = clicker;
       this.des = selectedWorld.getSpawn();
    }
 
    public Player getPlayer() {
        return clicker;
    }
    public Location getDestination(){
    	return des;
    }
    public void setDestination(Location location){
    	this.des = location;
    }   
    public boolean getIsCanceled(){
    	return this.isCanceled;
    }
    public void setCanceled(boolean canceled){
    	this.isCanceled = canceled;
    }
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
