package xyz.srnyx.vanadium.commands;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.util.DiscordUtil;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.listeners.DiscordListener;
import xyz.srnyx.vanadium.managers.CodeManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.UUID;


public class CommandLink implements CommandExecutor {
    @SuppressWarnings("NullableProblems")
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!Main.isPlayer(sender)) return true;
        if (!Main.hasPermission(sender, "vanadium.link")) return true;
        Player player = (Player) sender;

        UUID minecraft = player.getUniqueId();
        String discord = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(minecraft);
        String code = new CodeManager(minecraft).generateCode();
        String channel = DiscordUtil.getTextChannelById(DiscordListener.linkChannel).getName();

        if (discord == null) {
            new MessageManager("linking.generate")
                    .replace("%code%", code)
                    .replace("%channel%", channel)
                    .send(player);
        } else {
            new MessageManager("linking.already-linked")
                    .replace("%discord%", DiscordUtil.getUserById(discord).getName())
                    .replace("%code%", code)
                    .replace("%channel%", channel)
                    .send(player);
        }
        return true;
    }
}
