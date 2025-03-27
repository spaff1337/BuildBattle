package me.spaff.buildbattle.server.npc;

import me.spaff.api.display.SPFBillboardType;
import me.spaff.api.display.SPFTextDisplay;
import me.spaff.api.npc.SPFNPC;
import me.spaff.buildbattle.Constants;
import me.spaff.spflib.SPFLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PlayNPC {
    private final Location location;
    private final SPFNPC npc;
    private final List<SPFTextDisplay> textLines = new ArrayList<>();

    public PlayNPC(Location location) {
        this.location = location;

        npc = SPFLib.createNEWNPC(location);
        npc.setSkin(Constants.PLAY_NPC_SKIN_VALUE, Constants.PLAY_NPC_SKIN_SIGNATURE);

        double[] heightY = {2, 2.35};
        String[] lines = {"&7(Click to Play)", "&e&lBUILD BATTLE"};

        int i = 0;
        for (String line : lines) {
            SPFTextDisplay textDisplay = SPFLib.createNEWTextDisplay(location.clone().add(0, heightY[i], 0));
            textDisplay.updateBillboardType(SPFBillboardType.CENTER);
            textDisplay.updateDisplayText(line);

            textLines.add(textDisplay);
            i++;
        }
    }

    public Location getLocation() {
        return location;
    }

    public void show() {
        Bukkit.getOnlinePlayers().forEach(player -> {
            show(player);
        });
    }

    public void show(Player player) {
        npc.show(player);
        textLines.forEach(line -> {
            line.show(player);
            line.updateBillboardType(line.getBillboardType());
            line.updateDisplayText(line.getText());
        });
    }
}