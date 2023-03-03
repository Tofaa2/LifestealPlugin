package com.xfn14.dev.spigot;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LifeStealCmd implements CommandExecutor {
    private final App plugin;

    public LifeStealCmd(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {

        if ((sender instanceof Player player) && !player.isOp() && (plugin.getConfig().getString("cmd-permission") == null || !player.hasPermission(plugin.getConfig().getString("cmd-permission")))) {
            player.sendMessage(plugin.getConfigMessage("no-permission"));
            return false;
        }
        if (args.length != 3) {
            sender.sendMessage(plugin.getConfigMessage("cmd-usage"));
            return false;
        }
        final Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigMessage("invalid-player"));
            return false;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        }
        catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigMessage("cmd-invalid-amount"));
            return false;
        }
        if (amount <= 0) {
            sender.sendMessage(plugin.getConfigMessage("cmd-insert-number"));
            return false;
        }
        amount = amount * 2; // 1 heart = 2 hearts
        String path = target.getUniqueId()   + ".health";
        String firstArg = args[0].toLowerCase();
        switch (firstArg) {
            case "add" -> {
                int serverMaxHealth = plugin.getConfig().getInt("max-health") * 2;
                int playerCurrentHealth = plugin.getPlayerYML().getInt(path);
                if ((playerCurrentHealth + amount) > serverMaxHealth) {
                    amount = serverMaxHealth - playerCurrentHealth;
                }
                plugin.addHearthPlayer(target, amount);
                sender.sendMessage("§aAdded §6" + amount /2  + " §ahearts to §6" + target.getName());
                plugin.updateHearts(target);
                return true;
            }
            case "remove" -> {
                if (plugin.getPlayerYML().getInt(path) - amount < 0) {
                    sender.sendMessage("§cCan't remove that many hearts from this player");
                    return false;
                }
                sender.sendMessage("§aJust taken §6" + amount /2  + " §ahearts from §6" + target.getName());
                plugin.removeHearthPlayer(target, amount);
                plugin.updateHearts(target);
            }
            case "set" -> {
                if (amount < 0 || amount > plugin.getConfig().getInt("max-health") * 2) {
                    sender.sendMessage("§cCan't set that many hearts to player");
                    return false;
                }
                sender.sendMessage("§aJust set §6" + target.getName() + " §ahearts to §6" + amount / 2);
                plugin.setHearthPlayer(target, amount);
            }
            default -> {
                sender.sendMessage(plugin.getConfigMessage("cmd-usage"));
                return false;
            }
        }
        return true;
    }
}
