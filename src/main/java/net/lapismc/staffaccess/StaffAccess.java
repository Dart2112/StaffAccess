package net.lapismc.staffaccess;

import org.bukkit.Bukkit;
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

public final class StaffAccess extends JavaPlugin implements Listener {

    private ArrayList<Permission> staffPerms = new ArrayList<>();

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
        Bukkit.getLogger().info("Permissions Loaded!");
        Bukkit.getLogger().info("Staff Access v." + this.getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        staffPerms.clear();
        Bukkit.getLogger().info("Disabled!");
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
                e.getPlayer().sendMessage(getConfig().getString("StaffJoin"));
            } else {
                e.getPlayer().kickPlayer("NonStaffJoin");
            }
        }
    }
}
