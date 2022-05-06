package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.SlotManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@SuppressWarnings("NullableProblems")
public class CommandSlot implements TabExecutor {
    public static final List<UUID> stopLocks = new ArrayList<>();
    public static final List<UUID> stopTrusts = new ArrayList<>();

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!new PlayerManager(sender).hasPermission("vanadium.slot")) return true;

        //<player> <get|cooldown|start|stop|add|remove|set> <locks|trusts|all> [amount]
        if (args.length >= 3) {
            Player player = Bukkit.getServer().getPlayer(args[0]);
            if (player != null) {
                String action = args[1];
                String type = args[2];
                double slots = Main.slots.getInt(player.getUniqueId() + "." + type);

                //<player> <get|cooldown|start|stop> <locks|trusts|all>
                boolean arg3action = action.equalsIgnoreCase("get") ||
                        action.equalsIgnoreCase("cooldown") ||
                        action.equalsIgnoreCase("start") ||
                        action.equalsIgnoreCase("stop");
                boolean arg3type = type.equalsIgnoreCase("all") || type.equalsIgnoreCase("locks") || type.equalsIgnoreCase("trusts");
                if (args.length == 3 && arg3action && arg3type) {
                    //<player> <get> <locks|trusts>
                    if (action.equalsIgnoreCase("get")) {
                        // %slot%
                        String slot;
                        if (slots != 1) {
                            slot = "slots";
                        } else slot = "slot";

                        new MessageManager("slots.command.get")
                                .replace("%target%", player.getName())
                                .replace("%total%", String.valueOf(slots).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                                .replace("%type%", type.substring(0, type.length() - 1))
                                .replace("%slot%", slot)
                                .send(sender);
                        return true;
                    }

                    //<player> <cooldown> <locks|trusts>
                    String timeLeft = TimeUnit.MILLISECONDS.toSeconds(new SlotManager(type, player).timeLeft(false)) + " seconds";
                    if (new SlotManager(type, player).timeLeft(true) == null) timeLeft = "N/A (stopped)";
                    if (action.equalsIgnoreCase("cooldown")) {
                        new MessageManager("slots.command.cooldown")
                                .replace("%target%", player.getName())
                                .replace("%type%", type.substring(0, type.length() - 1))
                                .replace("%next%", timeLeft)
                                .send(sender);
                        return true;
                    }

                    //<player> <start|stop> <locks|trusts|all>
                    if (action.equalsIgnoreCase("start") || action.equalsIgnoreCase("stop")) {
                        if (action.equalsIgnoreCase("start")) {
                            if (type.equalsIgnoreCase("all")) {
                                new SlotManager("locks", player).start();
                                new SlotManager("trusts", player).start();
                                stopLocks.remove(player.getUniqueId());
                                stopTrusts.remove(player.getUniqueId());
                            } else if (type.equalsIgnoreCase("locks")) {
                                new SlotManager(type, player).start();
                                stopLocks.remove(player.getUniqueId());
                            } else if (type.equalsIgnoreCase("trusts")) {
                                new SlotManager(type, player).start();
                                stopTrusts.remove(player.getUniqueId());
                            }
                        }
                        if (action.equalsIgnoreCase("stop")) {
                            if (type.equalsIgnoreCase("all")) {
                                new SlotManager("locks", player).stop();
                                new SlotManager("trusts", player).stop();
                                stopLocks.add(player.getUniqueId());
                                stopTrusts.add(player.getUniqueId());
                            } else if (type.equalsIgnoreCase("locks")) {
                                new SlotManager(type, player).stop();
                                stopLocks.add(player.getUniqueId());
                            } else if (type.equalsIgnoreCase("trusts")) {
                                new SlotManager(type, player).stop();
                                stopTrusts.add(player.getUniqueId());
                            }
                        }

                        // %type%
                        String typePlaceholder = type.substring(0, type.length() - 1);
                        if (type.equalsIgnoreCase("all")) typePlaceholder = "all";

                        new MessageManager("slots.command." + action)
                                .replace("%target%", player.getName())
                                .replace("%type%", typePlaceholder)
                                .send(sender);
                        return true;
                    }
                }

                //<player> <add|remove|set> <locks|trusts> <amount>
                boolean arg4 = action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("set");
                if (args.length == 4 && arg4) {
                    double amount = Double.parseDouble(args[3]);

                    if (action.equalsIgnoreCase("add")) slots += amount;
                    if (action.equalsIgnoreCase("remove")) slots -= amount;
                    if (action.equalsIgnoreCase("set")) slots = amount;

                    Main.slots.set(player.getUniqueId() + "." + type, slots);
                    new SlotManager().save();

                    // %slot%
                    String slot;
                    if (amount != 1) {
                        slot = "slots";
                    } else slot = "slot";

                    new MessageManager("slots.command." + action)
                            .replace("%target%", player.getName())
                            .replace("%count%", String.valueOf(amount).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                            .replace("%type%", type.substring(0, type.length() - 1))
                            .replace("%slot%", slot)
                            .replace("%total%", String.valueOf(slots).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                            .send(sender);
                    return true;
                }
            }
            new MessageManager("errors.invalid-player")
                    .replace("%player%", args[0])
                    .send(sender);
            return true;
        }
        new MessageManager("errors.invalid-arguments").send(sender);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();

        if (args.length == 1) {
            for (Player online : Bukkit.getOnlinePlayers()) {
                suggestions.add(online.getName());
            }
        }
        if (args.length == 2) {
            suggestions.add("get");
            suggestions.add("cooldown");
            suggestions.add("add");
            suggestions.add("remove");
            suggestions.add("set");
            suggestions.add("start");
            suggestions.add("stop");
        }
        if (args.length == 3) {
            suggestions.add("locks");
            suggestions.add("trusts");
            if (Objects.equals(args[1], "start") || Objects.equals(args[1], "stop")) suggestions.add("all");
        }

        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) {
                results.add(suggestion);
            }
        }
        return results;
    }
}
