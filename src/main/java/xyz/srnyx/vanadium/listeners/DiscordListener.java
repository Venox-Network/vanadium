package xyz.srnyx.vanadium.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageHistory;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.CodeManager;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.List;
import java.util.UUID;


public class DiscordListener {
    public static final String linkChannel = Main.config.getString("link-channel");

    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        if (linkChannel != null) {
            TextChannel channel = DiscordUtil.getTextChannelById(linkChannel);
            if (channel != null) {
                List<Message> history = new MessageHistory(channel).retrievePast(100).complete();
                if (!history.isEmpty()) {
                    channel.deleteMessages(history).queue();
                }
            }
        }
    }

    @Subscribe
    public void onGuildMessageReceive(DiscordGuildMessageReceivedEvent event) {
        if (linkChannel != null && event.getChannel().getId().equals(linkChannel)) {
            String message = event.getMessage().getContentStripped();
            if (CodeManager.linkingCodes.containsValue(message)) {
                AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
                User author = event.getAuthor();
                String discord = author.getId();
                UUID minecraft = new CodeManager().getUUIDFromCode(message);
                Player player = Bukkit.getPlayer(minecraft);

                manager.unlink(discord);
                manager.unlink(minecraft);
                manager.link(discord, minecraft);
                new CodeManager(minecraft).removeCode();

                // Send success message to player on Minecraft
                //noinspection ConstantConditions
                new MessageManager("linking.success.minecraft")
                        .replace("%discord%", author.getAsTag())
                        .send(player);

                // Send success message to user on Discord
                new MessageManager("linking.success.discord")
                        .replace("%minecraft%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .send(author);
            }
            event.getMessage().delete().queue();
        }
    }
}
