package xyz.srnyx.vanadium.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageHistory;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import xyz.srnyx.vanadium.Main;
import xyz.srnyx.vanadium.managers.MessageManager;

import java.util.List;
import java.util.UUID;


public class DiscordListener {
    public static final String linkChannel = Main.config.getString("link-channel");

    /**
     * Called when the bot is ready
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        String name = DiscordUtil.getJda().getSelfUser().getName();
        String discrim = DiscordUtil.getJda().getSelfUser().getDiscriminator();
        Main.plugin.getLogger().info(ChatColor.GREEN + "Successfully connected to " + ChatColor.DARK_GREEN + name + "#" + discrim);

        if (linkChannel != null) {
            TextChannel channel = DiscordUtil.getTextChannelById(linkChannel);
            if (channel != null) {
                final List<Message> history = new MessageHistory(channel).retrievePast(100).complete();
                if (history.size() > 1) {
                    channel.deleteMessages(history).queue();
                } else if (history.size() == 1) {
                    channel.deleteMessageById(history.get(0).getId()).queue();
                }
            }
        }
    }

    /**
     * Called when a server message is received
     */
    @Subscribe
    public void onGuildMessageReceive(DiscordGuildMessageReceivedEvent event) {
        final AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        final String message = event.getMessage().getContentStripped();
        final String discord = event.getAuthor().getId();

        if (manager.getLinkingCodes().containsKey(message) && linkChannel != null && event.getChannel().getId().equals(linkChannel)) {
            final UUID minecraft = manager.getLinkingCodes().get(message);

            manager.unlink(discord);
            manager.unlink(minecraft);
            manager.link(discord, minecraft);

            if (manager.getUuid(discord) != null) {
                Player player = Bukkit.getPlayer(manager.getUuid(discord));
                if (player != null) {
                    // Send success message to player on Minecraft
                    new MessageManager("linking.success.minecraft")
                            .replace("%discord%", event.getAuthor().getAsTag())
                            .send(player);

                    // Send success message to user on Discord
                    new MessageManager("linking.success.discord")
                            .replace("%minecraft%", player.getName())
                            .replace("%uuid%", player.getUniqueId().toString())
                            .send(event.getAuthor());
                }
            }

            event.getMessage().delete().queue();
        }
    }
}
