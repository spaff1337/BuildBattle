package me.spaff.buildbattle;

import me.spaff.spflib.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class Constants {
    public static final ItemStack SETUP_WAITING_LOBBY_ITEM = new ItemBuilder.Builder(Material.BEACON)
            .name("&aWaiting Lobby")
            .lore(List.of("&7Right-click to set Waiting Lobby location."))
            .build()
            .getItem();

    public static final ItemStack SETUP_PLOT_ITEM = new ItemBuilder.Builder(Material.STICK)
            .name("&aPlot Setter")
            .lore(List.of("&7Left-click to set Boundary A.", "&7Right-click to set Boundary B."))
            .build()
            .getItem();

    public static final String WORLD_BACKUP_PATH = "world_backup";

    public static final int GAME_START_COUNTDOWN_LENGTH = 15;
    public static final int GAME_TIME = 300;
    public static final int TIME_TO_VOTE = 10;

    public static final int PLAYERS_REQUIRED_TO_START = 4; // 2
    public static final int MAX_PLAYERS = 8; // 8

    public static final String PLAY_NPC_SKIN_VALUE = "ewogICJ0aW1lc3RhbXAiIDogMTY1NDAwMzk3ODk0MSwKICAicHJvZmlsZUlkIiA6ICIzYTdhMDVjMDc0MTI0N2Q2YWVmMDMzMDNkOWNlMjMzNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcXJ0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzkxNTA1NWFmZDVkM2FhMGJjYjdmMDZmZjc2YzQzNTBiOTJkYmYxZTgxZWJjZDI2Y2FhY2IwNDQ2MmU1MTg1OWMiCiAgICB9CiAgfQp9";
    public static final String PLAY_NPC_SKIN_SIGNATURE = "eZTQnAgP1+RHwj34EkqGgzXCLX4mIz3wxoQrRxQubt7H/Itdj1IhzJtoT+9G+W0recI2sOTT0G+4fG4ZJBlqphHGT2mLsgxAgOloOYz21foE/t1wQCmBzA6vZwqFWglIuFM/OYPqTwpdWqqEu17bXTpfTLX3giErlcFj22/V7J1N4TXGrMUMtY9kVYoK7ti78NmrwnCJakrJBX9H092sEKbHK5C3aHTtzsR8i3E3umsgL4AEqZqc9cZDbiMID+0dmmZOwV3xkPTFLD7SC50e4xOW0xfNwpxMJKch4z1S7N6t3F1vY62aCdOEIP81FYqSWce9MPSfbX/5iljYJf6TMs9s9r44AuY2oxrNnqdEEi8dSm85cgDtcrcMgXH0Kcfu+MeS9kbmcBQAdbKQHf0eGW5p7itc7P8HRXdKEtIlGG7LdM5turXtpX8Pt7COIaN1aeaWUDJarM6Wihw+sg5skZEYCTeId24SUdsrunPt9qOPboF4HsJIrRVxjPJl5wf/VqXS/FuIYDp0KWq9IN/NUGwSPjCEyORaqynaEPm8SGLZGHeO/M9GKt1rC5QvTf59mQ+BN7N6I149MqqExKkOEEyJvuT0m/hGBw2GONSb+BuGjctlUA2f/D2sCxV8gqsu2yae+/D2VH9Ah0n7VJEzjA5APKA2AzYxkHtvO5jXFcY=";
}