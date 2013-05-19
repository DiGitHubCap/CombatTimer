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

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CombatTimer extends JavaPlugin implements Listener
  {
    private static CombatTimer instance = null;
    private final ExecutorService notifierPool = Executors
        .newCachedThreadPool();
    private final Map<Player, Long> timeouts = new WeakHashMap<Player, Long>();
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit (PlayerQuitEvent event)
      {
        if (this.isOnTimeout(event.getPlayer()))
          {
            event.getPlayer().setHealth(0);
          }
      }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity (EntityDamageByEntityEvent event)
      {
        if (event.getEntity() instanceof Player)
          {
            final Player player = (Player) event.getEntity();
            if ( !this.isOnTimeout(player))
              {
                player.sendMessage(ChatColor.RED
                    + "You are in combat! You cannot logout for 10 seconds");
              }
            timeouts.put(player, System.currentTimeMillis() + 10000L);
            notifierPool.submit(new Runnable()
              {
                public void run ()
                  {
                    try
                      {
                        Thread.sleep(10010L);
                      }
                    catch (InterruptedException e)
                      {
                        e.printStackTrace();
                      }
                    if (player.isOnline() && !instance.isOnTimeout(player))
                      {
                        player.sendMessage(ChatColor.GREEN
                            + "You can now logout");
                      }
                  }
              });
          }
      }
    
    @Override
    public void onDisable ()
      {
        instance = null;
        
        this.getLogger().info("Is now disabled");
      }
    
    @Override
    public void onEnable ()
      {
        instance = this;
        
        this.getServer().getPluginManager().registerEvents(this, this);
        
        this.getLogger().info("Is now enabled");
      }
    
    private boolean isOnTimeout (Player player)
      {
        if ( !timeouts.containsKey(player))
          return false;
        return timeouts.get(player) > System.currentTimeMillis();
      }
  }
