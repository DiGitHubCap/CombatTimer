/*
 * Combat Timer plugin for Bukkit.
 * Copyright (C) 2013 Andrew Stevanus (Hoot215) <hoot893@gmail.com>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.hoot215.combattimer;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatTimer extends JavaPlugin implements Listener
  {
    private final Map<Player, Long> timeouts = new WeakHashMap<Player, Long>();
    private final Map<Player, Integer> playerTimers =
        new HashMap<Player, Integer>();
    
    public void doTimeoutCheck (final Player player)
      {
        if ( !this.isOnTimeout(player))
          {
            player.sendMessage(ChatColor.RED
                + "You are in combat! You cannot logout for 10 seconds");
          }
        this.setTimeout(player, 10000);
        if ( !playerTimers.containsKey(player))
          {
            int id =
                this.getServer().getScheduler()
                    .scheduleSyncRepeatingTask(this, new Runnable()
                      {
                        @Override
                        public void run ()
                          {
                            if (player.isOnline() && !isOnTimeout(player))
                              {
                                player.sendMessage(ChatColor.GREEN
                                    + "You can now logout");
                                getServer().getScheduler().cancelTask(
                                    playerTimers.get(player));
                                playerTimers.remove(player);
                              }
                          }
                      }, 0, 20);
            playerTimers.put(player, id);
          }
      }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent event)
      {
        if (playerTimers.containsKey(event.getPlayer()))
          {
            this.getServer().getScheduler()
                .cancelTask(playerTimers.get(event.getPlayer()));
            playerTimers.remove(event.getPlayer());
          }
        if (this.isOnTimeout(event.getPlayer()))
          {
            event.getPlayer().setHealth(0);
          }
      }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath (PlayerDeathEvent event)
      {
        if (playerTimers.containsKey(event.getEntity()))
          {
            this.getServer().getScheduler()
                .cancelTask(playerTimers.get(event.getEntity()));
            playerTimers.remove(event.getEntity());
          }
        if (this.isOnTimeout(event.getEntity()))
          {
            this.setTimeout(event.getEntity(), 0);
          }
      }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity (EntityDamageByEntityEvent event)
      {
        if (event.getEntity() instanceof Player)
          {
            this.doTimeoutCheck((Player) event.getEntity());
          }
        if (event.getDamager() instanceof Player)
          {
            this.doTimeoutCheck((Player) event.getDamager());
          }
      }
    
    private boolean isOnTimeout (Player player)
      {
        if ( !timeouts.containsKey(player))
          return false;
        return timeouts.get(player) > System.currentTimeMillis();
      }
    
    private void setTimeout (Player player, long time)
      {
        timeouts.put(player, System.currentTimeMillis() + time);
      }
    
    @Override
    public void onDisable ()
      {
        this.getLogger().info("Is now disabled");
      }
    
    @Override
    public void onEnable ()
      {
        this.getServer().getPluginManager().registerEvents(this, this);
        
        this.getLogger().info("Is now enabled");
      }
  }
