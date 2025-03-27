package me.spaff.buildbattle.cmd;

import me.spaff.buildbattle.Constants;
import me.spaff.buildbattle.Main;
import me.spaff.buildbattle.arena.Arena;
import me.spaff.buildbattle.server.game.Plot;
import me.spaff.buildbattle.server.game.Setup;
import me.spaff.buildbattle.player.BBPlayer;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.FileUtils;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildBattleCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("help")) {
            BukkitUtils.sendMessage(player, "");
            BukkitUtils.sendMessage(player, "");
            BukkitUtils.sendMessage(player, "&7&l                   BUILD BATTLE");
            BukkitUtils.sendMessage(player, "   &7-/bb bypass");
            BukkitUtils.sendMessage(player, "   &7-/bb lobby setspawn");
            BukkitUtils.sendMessage(player, "   &7-/bb game join");
            BukkitUtils.sendMessage(player, "   &7-/bb game leave");
            BukkitUtils.sendMessage(player, "   &7-/bb game setup start");
            BukkitUtils.sendMessage(player, "   &7-/bb game setup addplot");
            BukkitUtils.sendMessage(player, "   &7-/bb game setup end");
            BukkitUtils.sendMessage(player, "   &7-/bb game setup cancel");
            BukkitUtils.sendMessage(player, "");
            BukkitUtils.sendMessage(player, "");
        }

        if (args[0].equalsIgnoreCase("bypass")) {
            boolean isEnabled = BBPlayer.getInstance(player).hasRestrictionsBypass();
            BBPlayer.getInstance(player).setBypassRestrictions(isEnabled = !isEnabled);
            BukkitUtils.sendMessage(player, "&aBypass restrictions " + (isEnabled ? "enabled." : "disabled."));
        }

        if (args[0].equalsIgnoreCase("lobby")) {
            if (args.length < 2) {
                BukkitUtils.sendMessage(player, "&cUsage: /bb lobby");
                return false;
            }

            if (!Main.getServerManager().isHubServer()) {
                BukkitUtils.sendMessage(player, "&cYou can't set up a lobby in a game server!");
                return false;
            }

            new FileManager("lobby").save();

            if (args[1].equalsIgnoreCase("setspawn")) {
                FileUtils.saveLocationToFile(player.getLocation(), "lobby", "spawn-location");
            }
            /*if (args[1].equalsIgnoreCase("setplaynpc")) {
                SPFNPC npc = SPFLib.createNEWNPC(player.getLocation());
                npc.setSkin(Constants.PLAY_NPC_SKIN_VALUE, Constants.PLAY_NPC_SKIN_SIGNATURE);

                SPFTextDisplay textDisplay = SPFLib.createNEWTextDisplay(player.getLocation().clone().add(0, 1.2, 0));
                textDisplay.updateDisplayText("&a&lCLICK TO PLAY");

                FileUtils.saveLocationToFile(player.getLocation(), "lobby", "play-npc-location");
            }*/
        }

        if (args[0].equalsIgnoreCase("game")) {
            if (args.length < 2) {
                BukkitUtils.sendMessage(player, "&cUsage: /bb game");
                return false;
            }

            if (args[1].equalsIgnoreCase("join")) {
                if (!Main.getServerManager().isHubServer())
                    return false;

                Main.getServerManager().sendToRandomGame(player);
            }

            if (args[1].equalsIgnoreCase("leave")) {
                if (!Main.getServerManager().isGameServer())
                    return false;

                Arena.getInstance().leave(player);
                Main.getServerManager().sendToRandomHub(player);
            }

            if (args[1].equalsIgnoreCase("setup")) {
                if (args.length < 3) {
                    BukkitUtils.sendMessage(player, "&cUsage: /bb game setup");
                    return false;
                }

                if (!Main.getServerManager().isGameServer()) {
                    BukkitUtils.sendMessage(player, "&cYou cannot set up a game in a hub server!");
                    return false;
                }

                BBPlayer bbPlayer = BBPlayer.getInstance(player);

                if (args[2].equalsIgnoreCase("start")) {
                    if (bbPlayer.isSettingUpGame()) {
                        BukkitUtils.sendMessage(player, "&cYou are already setting up a game!");
                        return false;
                    }

                    bbPlayer.setSetup(new Setup(player));
                    bbPlayer.getSetup().start();
                }

                if (args[2].equalsIgnoreCase("addplot")) {
                    if (!bbPlayer.isSettingUpGame()) {
                        BukkitUtils.sendMessage(player, "&cStart the setup before you execute that!");
                        return false;
                    }

                    Location boundaryA = bbPlayer.getSetup().getPlotBoundaryA();
                    Location boundaryB = bbPlayer.getSetup().getPlotBoundaryB();

                    if (boundaryA == null) {
                        BukkitUtils.sendMessage(player, "&cBoundary A was not set!");
                        return false;
                    }
                    if (boundaryB == null) {
                        BukkitUtils.sendMessage(player, "&cBoundary B was not set!");
                        return false;
                    }

                    bbPlayer.getSetup().addPlot(new Plot(boundaryA, boundaryB));
                    BukkitUtils.sendMessage(player, "&aAdded new plot (" + bbPlayer.getSetup().getPlots().size() + "/" + Constants.MAX_PLAYERS + ").");
                }

                if (args[2].equalsIgnoreCase("end")) {
                    if (!bbPlayer.isSettingUpGame()) {
                        BukkitUtils.sendMessage(player, "&cStart the setup before you execute that!");
                        return false;
                    }

                    bbPlayer.getSetup().end();
                }

                if (args[2].equalsIgnoreCase("cancel")) {
                    if (!bbPlayer.isSettingUpGame()) {
                        BukkitUtils.sendMessage(player, "&cStart the setup before you execute that!");
                        return false;
                    }

                    bbPlayer.getSetup().cancel();
                }
            }
        }

        return true;
    }
}