package me.spaff.buildbattle.manager;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.ServerType;
import me.spaff.buildbattle.config.Config;
import me.spaff.buildbattle.server.game.GameData;
import me.spaff.buildbattle.server.lobby.Lobby;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class ServerManager {
    private final ServerType serverType;
    private final Lobby lobby;
    private final GameData gameData;

    private Set<String> hubServers = new HashSet<>();
    private Set<String> gameServers = new HashSet<>();

    public ServerManager(ServerType serverType) {
        this.serverType = serverType;
        this.gameData = new GameData();
        this.lobby = new Lobby();

        // Loads data for lobby
        if (isHubServer())
            lobby.loadData();

        // Loads data for game
        else if (isGameServer())
            gameData.loadData();
    }

    public Lobby getLobby() {
        return lobby;
    }

    public GameData getGameData() {
        return gameData;
    }

    public void sendToServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
    }

    public void sendToRandomHub(Player player) {
        List<String> hubs = Config.readList("servers.hubs");
        int randomHubIndex = hubs.size() > 1 ? new Random().nextInt(hubs.size() - 1) : 0;
        sendToServer(player, hubs.get(randomHubIndex));
    }

    public void sendToRandomGame(Player player) {
        List<String> games = Config.readList("servers.games");
        int randomGameIndex = games.size() > 1 ? new Random().nextInt(games.size() - 1) : 0;
        sendToServer(player, games.get(randomGameIndex));
    }

    public void addGameServer(String server) {
        this.gameServers.add(server);
    }

    public void addGameServers(List<String> servers) {
        this.gameServers.addAll(servers);
    }

    public Set<String> getGameServers() {
        return gameServers;
    }

    public void addHubServer(String server) {
        this.hubServers.add(server);
    }

    public void addHubServers(List<String> servers) {
        this.hubServers.addAll(servers);
    }

    public Set<String> getHubServers() {
        return hubServers;
    }

    public boolean isHubServer() {
        return getServerType().equals(ServerType.LOBBY);
    }

    public boolean isGameServer() {
        return getServerType().equals(ServerType.GAME);
    }

    public ServerType getServerType() {
        return serverType;
    }
}