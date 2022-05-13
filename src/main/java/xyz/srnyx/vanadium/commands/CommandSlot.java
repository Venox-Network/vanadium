package xyz.srnyx.vanadium.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.managers.DataManager;
import xyz.srnyx.vanadium.managers.MessageManager;
import xyz.srnyx.vanadium.managers.PlayerManager;
import xyz.srnyx.vanadium.managers.SlotManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class CommandSlot implements TabExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.slot")) return true;

        //<player> <get|cooldown|start|stop|add|remove|set> <locks|trusts|all> [amount]
        if (args.length >= 3) {
            Player player = Bukkit.getPlayer(args[0]);
            if (player != null) {
                String action = args[1];
                String type = args[2];

                int slots = 0;
                if (Objects.equals(type, "locks")) slots = new SlotManager("locks", player).getCount();
                if (Objects.equals(type, "trusts")) slots = new SlotManager("trusts", player).getCount();

                // %slot%
                String slot = slots == 1 ? "slot" : "slots";

                //<player> <get|cooldown|start|stop> <locks|trusts|all>
                boolean arg3action = action.equalsIgnoreCase("get")
                        || action.equalsIgnoreCase("cooldown")
                        || action.equalsIgnoreCase("start")
                        || action.equalsIgnoreCase("stop");
                boolean arg3type = type.equalsIgnoreCase("all")
                        || type.equalsIgnoreCase("locks")
                        || type.equalsIgnoreCase("trusts");

                if (args.length == 3 && arg3action && arg3type) {
                    //<player> <get> <locks|trusts>
                    if (action.equalsIgnoreCase("get")) {
                        new MessageManager("slots.command.get")
                                .replace("%target%", player.getName())
                                .replace("%total%", String.valueOf(slots).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                                .replace("%type%", type.substring(0, type.length() - 1))
                                .replace("%slot%", slot)
                                .send(sender);
                        return true;
                    }

                    //<player> <cooldown> <locks|trusts>
                    Long timeLeft = new SlotManager(type, player).timeLeft();
                    if (action.equalsIgnoreCase("cooldown")) {
                        new MessageManager("slots.command.cooldown")
                                .replace("%target%", player.getName())
                                .replace("%type%", type.substring(0, type.length() - 1))
                                .replace("%next%", timeLeft != null ? TimeUnit.MILLISECONDS.toSeconds(timeLeft) + " seconds" : "N/A (stopped)")
                                .send(sender);
                        return true;
                    }

                    //<player> <start|stop> <locks|trusts|all>
                    if (action.equalsIgnoreCase("start") || action.equalsIgnoreCase("stop")) {
                        if (action.equalsIgnoreCase("start")) {
                            if (type.equalsIgnoreCase("all")) {
                                new SlotManager("locks", player).start();
                                new SlotManager("trusts", player).start();
                                player.removeScoreboardTag("stop-locks");
                                player.removeScoreboardTag("stop-trusts");
                            } else  {
                                new SlotManager(type, player).start();
                                player.removeScoreboardTag("stop-" + type);
                            }
                        }

                        if (action.equalsIgnoreCase("stop")) {
                            if (type.equalsIgnoreCase("all")) {
                                new SlotManager("locks", player).stop();
                                new SlotManager("trusts", player).stop();
                                player.addScoreboardTag("stop-locks");
                                player.addScoreboardTag("stop-trusts");
                            } else  {
                                new SlotManager(type, player).stop();
                                player.addScoreboardTag("stop-" + type);
                            }
                        }

                        new MessageManager("slots.command." + action)
                                .replace("%target%", player.getName())
                                .replace("%type%", type.equalsIgnoreCase("all") ? "all" : type.substring(0, type.length() - 1))
                                .send(sender);
                        return true;
                    }
                }

                //<player> <add|remove|set> <locks|trusts> <amount>
                boolean arg4 = action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("set");
                if (args.length == 4 && arg4) {
                    int amount = Integer.parseInt(args[3]);

                    if (action.equalsIgnoreCase("add")) slots += amount;
                    if (action.equalsIgnoreCase("remove")) slots -= amount;
                    if (action.equalsIgnoreCase("set")) slots = amount;

                    if (Objects.equals(type, "locks")) DataManager.slots.put(player.getUniqueId(), new int[]{amount, new SlotManager("trusts", player).getCount()});
                    if (Objects.equals(type, "trusts")) DataManager.slots.put(player.getUniqueId(), new int[]{new SlotManager("locks", player).getCount(), amount});

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

    @SuppressWarnings("NullableProblems")
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        List<String> results = new ArrayList<>();

        if (args.length == 1) for (Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());

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

        for (String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
