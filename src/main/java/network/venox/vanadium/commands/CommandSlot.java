package network.venox.vanadium.commands;

import network.venox.vanadium.managers.DataManager;
import network.venox.vanadium.managers.MessageManager;
import network.venox.vanadium.managers.PlayerManager;
import network.venox.vanadium.managers.slots.LockSlotManager;
import network.venox.vanadium.managers.slots.SlotManager;
import network.venox.vanadium.managers.slots.TrustSlotManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;


public class CommandSlot implements TabExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (PlayerManager.noPermission(sender, "vanadium.slot")) return true;
        if (args.length < 3) {
            new MessageManager("errors.invalid-arguments", cmd, args).send(sender);
            return true;
        }

        //<player> <get|cooldown|start|stop|add|remove|set> <locks|trusts|all> [amount]
        final OfflinePlayer player = PlayerManager.getOfflinePlayer(args[0]);
        final String action = args[1];
        final String type = args[2];

        if (player == null) {
            new MessageManager("errors.invalid-player", cmd, args)
                    .replace("%player%", args[0])
                    .send(sender);
            return true;
        }

        // Get slot count
        int slots = 0;
        if (Objects.equals(type, "locks")) slots = new LockSlotManager().getCount(player.getUniqueId());
        if (Objects.equals(type, "trusts")) slots = new TrustSlotManager().getCount(player.getUniqueId());

        // %slot%
        final String slot = slots == 1 ? "slot" : "slots";

        //<player> <get|cooldown|start|stop> <locks|trusts|all>
        final boolean arg3action = action.equalsIgnoreCase("get")
                || action.equalsIgnoreCase("cooldown")
                || action.equalsIgnoreCase("start")
                || action.equalsIgnoreCase("stop");
        final boolean arg3type = type.equalsIgnoreCase("all")
                || type.equalsIgnoreCase("locks")
                || type.equalsIgnoreCase("trusts");

        if (args.length == 3 && arg3action && arg3type) {
            final Player playerOnline = player.getPlayer();
            if (playerOnline == null) {
                new MessageManager("errors.invalid-player", cmd, args)
                        .replace("%player%", args[0])
                        .send(sender);
                return true;
            }

            //<player> <get> <locks|trusts>
            if (action.equalsIgnoreCase("get")) {
                new MessageManager("slots.command.get", cmd, args)
                        .replace("%target%", player.getName())
                        .replace("%total%", String.valueOf(slots).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%slot%", slot)
                        .send(sender);
                return true;
            }

            //<player> <cooldown> <locks|trusts>
            if (action.equalsIgnoreCase("cooldown")) {
                final Long timeLeft = new SlotManager().timeLeft(playerOnline, type);
                new MessageManager("slots.command.cooldown", cmd, args)
                        .replace("%target%", player.getName())
                        .replace("%type%", type.substring(0, type.length() - 1))
                        .replace("%next%", timeLeft != null ? TimeUnit.MILLISECONDS.toSeconds(timeLeft) + " seconds" : "N/A (stopped)")
                        .send(sender);
                return true;
            }

            //<player> <start|stop> <locks|trusts|all>
            if (action.equalsIgnoreCase("start") || action.equalsIgnoreCase("stop")) {
                new SlotManager().startStop(playerOnline, type, action);
                new MessageManager("slots.command." + action, cmd, args)
                        .replace("%target%", player.getName())
                        .replace("%type%", type.equalsIgnoreCase("all") ? "all" : type.substring(0, type.length() - 1))
                        .send(sender);
                return true;
            }
        }

        //<player> <add|remove|set> <locks|trusts> <amount>
        final boolean arg4 = action.equalsIgnoreCase("add") || action.equalsIgnoreCase("remove") || action.equalsIgnoreCase("set");
        if (args.length == 4 && arg4) {
            final int amount = Integer.parseInt(args[3]);

            if (action.equalsIgnoreCase("add")) slots += amount;
            if (action.equalsIgnoreCase("remove")) slots -= amount;
            if (action.equalsIgnoreCase("set")) slots = amount;

            if (Objects.equals(type, "locks")) DataManager.slots.put(player.getUniqueId(), new int[]{amount, new TrustSlotManager().getCount(player.getUniqueId())});
            if (Objects.equals(type, "trusts")) DataManager.slots.put(player.getUniqueId(), new int[]{new LockSlotManager().getCount(player.getUniqueId()), amount});

            new MessageManager("slots.command." + action, cmd, args)
                    .replace("%target%", player.getName())
                    .replace("%count%", String.valueOf(amount).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                    .replace("%type%", type.substring(0, type.length() - 1))
                    .replace("%slot%", slot)
                    .replace("%total%", String.valueOf(slots).replaceAll("\\.0*$|(\\.\\d*?)0+$", "$1"))
                    .send(sender);
            return true;
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        final List<String> suggestions = new ArrayList<>();
        final List<String> results = new ArrayList<>();

        if (args.length == 1) for (final Player online : Bukkit.getOnlinePlayers()) suggestions.add(online.getName());

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

        for (final String suggestion : suggestions) if (suggestion.toLowerCase().startsWith(args[args.length - 1].toLowerCase())) results.add(suggestion);
        return results;
    }
}
