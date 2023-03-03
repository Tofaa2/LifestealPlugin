package com.xfn14.dev.spigot;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class HealthPlaceholder extends PlaceholderExpansion {
    private final App plugin;

    public HealthPlaceholder(App plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getAuthor() {
        return "xfn14";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lifesteal";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if(player != null && player.isOnline())
            return onPlaceholderRequest(player.getPlayer(), params);
        return null;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(params.equalsIgnoreCase("hearts"))
            return String.valueOf(
                plugin.getPlayerYML().getInt(player.getUniqueId() + ".health") / 2 // 2 hearts = 1 heart
            );
        return null;
    }
}
