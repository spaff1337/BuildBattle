package me.spaff.buildbattle.server.lobby;

import me.spaff.buildbattle.server.npc.PlayNPC;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.logger.SLevel;
import me.spaff.spflib.logger.SLogger;
import me.spaff.spflib.utils.FileUtils;
import org.bukkit.Location;

import java.util.Optional;

public class Lobby {
    private Location spawnLocation;
    private PlayNPC playNPC;

    public void loadData() {
        boolean wasLobbySetup = FileManager.isFileInDirectory(FileManager.getPluginDirectory(), "lobby");
        if (!wasLobbySetup) {
            SLogger.log(SLevel.WARNING, "Lobby configuration is not set up for this server yet!");
            return;
        }

        spawnLocation = FileUtils.getLocationFromFile("lobby", "spawn-location");

        /*Location npcLocation = FileUtils.getLocationFromFile("lobby", "play-npc-location");
        if (npcLocation != null)
            playNPC = new PlayNPC(npcLocation);*/
    }

    public Optional<Location> getSpawnLocation() {
        return Optional.ofNullable(spawnLocation);
    }

    public Optional<PlayNPC> getPlayNPC() {
        return Optional.ofNullable(playNPC);
    }
}
