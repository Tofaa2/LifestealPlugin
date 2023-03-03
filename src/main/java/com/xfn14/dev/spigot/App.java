package com.xfn14.dev.spigot;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class App extends JavaPlugin implements Listener {
    private File playerFile;
    private FileConfiguration playerYML;

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.saveDefaultConfig();
            this.createPlayerYML();

            super.getCommand("lifesteal").setExecutor(new LifeStealCmd(this));

            super.getServer().getPluginManager().registerEvents(this, this);

            new HealthPlaceholder(this).register();

            Bukkit.getOnlinePlayers().forEach(p -> this.updateHearts(p));
        } else {
            getLogger().warning("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    // @Override
    // public void onDisable() {
    //     this.savePlayersYML();
    // }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String path = player.getUniqueId() + ".health";
        String lastHealth = player.getUniqueId() + ".lastHealth";
        if (!this.playerYML.contains(path)) {
            this.playerYML.set(path, super.getConfig().getInt("default-health"));
            this.savePlayersYML();
        }
        this.updateHearts(player);
        if(this.playerYML.contains(lastHealth)) {
            double lastHealthAmount = this.playerYML.getDouble(lastHealth);
            if(lastHealthAmount > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            else
                player.setHealth(lastHealthAmount);
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String path = player.getUniqueId() + ".lastHealth";
        this.playerYML.set(path, player.getHealth());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        this.updateHearts(player);
        player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Player killer = player.getKiller();

        int loseAmount = super.getConfig().getInt("lose-amount");
        int gainAmount = super.getConfig().getInt("gain-amount");

        if(killer == null && super.getConfig().getBoolean("mobs-lose"))
            this.removeHearthPlayer(player, loseAmount);

        if (killer != null && killer instanceof Player && !killer.getPlayer().getName().equals(player.getName())) {
            this.addHearthPlayer(killer.getPlayer(), gainAmount);
            this.removeHearthPlayer(player, loseAmount);
        }
    }

    public void addHearthPlayer(Player player, int amount) {
        String path = player.getUniqueId() + ".health";

        int fullHearts = (int) this.playerYML.get(path) + amount;
        if (fullHearts > super.getConfig().getInt("max-health") * 2) {
            fullHearts = super.getConfig().getInt("max-health") * 2;
        }
        this.playerYML.set(path, fullHearts);
        this.savePlayersYML();
        this.updateHearts(player);
        double futureAmount = player.getHealth() + amount;
        if (futureAmount > player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        else
            player.setHealth(futureAmount);
    }

    public void removeHearthPlayer(Player player, int amount) {
        String path = player.getUniqueId() + ".health";
        this.playerYML.set(path, Math.max(0, this.playerYML.getInt(path) - amount));
        this.savePlayersYML();
    }

    public void setHearthPlayer(Player player, int amount) {
        String path = player.getUniqueId() + ".health";
        this.playerYML.set(path, Math.min(Math.max(0, amount), super.getConfig().getInt("max-health") * 2));
        this.savePlayersYML();
        this.updateHearts(player);
    }

    public void updateHearts(Player player) {
        String path = player.getUniqueId() + ".health";
        if(this.playerYML.getInt(path) <= 0) {
            this.executeCommand(super.getConfig().getString("death-command").replace("%playerName%", player.getName()));
            this.playerYML.set(path, super.getConfig().getInt("default-health"));
            return;
        }
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(this.playerYML.getInt(path));
    }

    public String getConfigMessage(String label) {
        String msg = super.getConfig().getString(label);
        return msg == null ? "§cCouldn't retrieve message from config. Please contact staff (" + label + ")" :
                ChatColor.translateAlternateColorCodes('&', msg);
    }

    private void executeCommand(String cmd) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    private void createPlayerYML() {
        playerFile = new File(super.getDataFolder(), "players.yml");
        if (!playerFile.exists()) {
            Bukkit.getConsoleSender().sendMessage("Created players.yml file.");
            playerFile.getParentFile().mkdirs();
            super.saveResource("players.yml", false);
        }

        playerYML = new YamlConfiguration();
        try {
            playerYML.load(playerFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void savePlayersYML() {
        try {
            this.playerYML.save(this.playerFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getPlayerYML() {
        return this.playerYML;
    }
}
