package me.spaff.buildbattle;

import me.spaff.buildbattle.cmd.BuildBattleCommands;
import me.spaff.buildbattle.config.Config;
import me.spaff.buildbattle.listener.PlayerListener;
import me.spaff.buildbattle.listener.GameListener;
import me.spaff.buildbattle.manager.ScoreboardManager;
import me.spaff.buildbattle.manager.ServerManager;
import me.spaff.spflib.SPFLib;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

public final class Main extends JavaPlugin implements PluginMessageListener {
    private static JavaPlugin instance;
    private static ServerManager serverManager;

    @Override
    public void onEnable() {
        instance = this;
        SPFLib.init(instance);

        Config.load();

        serverManager = new ServerManager(ServerType.valueOf(Config.readString("server-type")));
        serverManager.addHubServers(Config.readList("servers.hubs"));
        serverManager.addGameServers(Config.readList("servers.games"));

        System.out.println("server type: " + serverManager.getServerType());
        System.out.println("hub servers: " + serverManager.getHubServers());
        System.out.println("game servers: " + serverManager.getGameServers());

        // Auto update if lobby server
        if (serverManager.getServerType().equals(ServerType.LOBBY))
            ScoreboardManager.startUpdating();

        registerBungeeListeners();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static ServerManager getServerManager() {
        return serverManager;
    }

    // Registers
    private void registerListeners() {
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getServer().getPluginManager().registerEvents(new GameListener(), this);
    }

    private void registerBungeeListeners() {
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    private void registerCommands() {
        this.getCommand("buildbattle").setExecutor(new BuildBattleCommands());
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) return;

        // ByteArrayDataInput in = ByteStreams.newDataInput(message);
        // String subchannel = in.readUTF();
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}