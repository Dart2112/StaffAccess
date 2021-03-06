/*
 * Copyright  2017 Benjamin Martin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.lapismc.staffaccess;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public final class StaffAccess extends JavaPlugin implements Listener {

    private ArrayList<Permission> staffPerms = new ArrayList<>();
    private Logger logger = getLogger();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        List<String> configPerms = getConfig().getStringList("Permissions");
        for (String s : configPerms) {
            Permission p;
            if (Bukkit.getPluginManager().getPermission(s) == null) {
                p = new Permission(s);
                Bukkit.getPluginManager().addPermission(p);
            } else {
                p = Bukkit.getPluginManager().getPermission(s);
            }
            staffPerms.add(p);
        }
        logger.info("Permissions Loaded!");
        logger.info("Staff Access v." + this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        staffPerms.clear();
        logger.info("Disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (cmd.getName().equalsIgnoreCase("StaffAccess")) {
            boolean permitted = false;
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (p.hasPermission("staffaccess.staff")) {
                    permitted = true;
                } else {
                    permitted = false;
                }
            } else {
                permitted = true;
            }
            if (permitted) {
                if (getConfig().getBoolean("Enabled")) {
                    getConfig().set("Enabled", false);
                    saveConfig();
                    sender.sendMessage("Staff Access mode disabled, non staff players can join again");
                } else {
                    getConfig().set("Enabled", true);
                    saveConfig();
                    sender.sendMessage("Staff Access mode enabled, only staff can join");
                }
            }
        }
        return false;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent e) {
        reloadConfig();
        if (getConfig().getBoolean("Enabled")) {
            boolean allowed = false;
            for (Permission p : staffPerms) {
                if (e.getPlayer().hasPermission(p)) {
                    allowed = true;
                    break;
                }
            }
            if (allowed) {
                e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("StaffJoin")));
            } else {
                e.getPlayer().kickPlayer(ChatColor.translateAlternateColorCodes('&', getConfig().getString("NonStaffJoin")));
            }
        }
    }
}
