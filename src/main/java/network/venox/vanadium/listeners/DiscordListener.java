package network.venox.vanadium.listeners;

import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.Subscribe;
import github.scarsz.discordsrv.api.events.DiscordGuildMessageReceivedEvent;
import github.scarsz.discordsrv.api.events.DiscordReadyEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.MessageHistory;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import github.scarsz.discordsrv.util.DiscordUtil;

import network.venox.vanadium.Main;
import network.venox.vanadium.managers.MessageManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;


public class DiscordListener {
    private final String linkChannel = Main.config.getString("link-channel");

    /**
     * Called when the bot is ready
     */
    @Subscribe
    public void onDiscordReady(DiscordReadyEvent event) {
        final String name = DiscordUtil.getJda().getSelfUser().getName();
        final String discrim = DiscordUtil.getJda().getSelfUser().getDiscriminator();
        Main.plugin.getLogger().info(ChatColor.GREEN + "Successfully connected to " + ChatColor.DARK_GREEN + name + "#" + discrim);

        if (linkChannel == null) return;
        final TextChannel channel = DiscordUtil.getTextChannelById(linkChannel);
        if (channel == null) return;
        final List<Message> history = new MessageHistory(channel).retrievePast(100).complete();

        if (history.size() == 1) {
            channel.deleteMessageById(history.get(0).getId()).queue();
            return;
        }
        if (history.size() > 1) channel.deleteMessages(history).queue();
    }

    /**
     * Called when a server message is received
     */
    @Subscribe
    public void onGuildMessageReceive(DiscordGuildMessageReceivedEvent event) {
        final AccountLinkManager manager = DiscordSRV.getPlugin().getAccountLinkManager();
        final Map<String, UUID> codes = manager.getLinkingCodes();
        final String message = event.getMessage().getContentStripped();

        if (!codes.containsKey(message) || linkChannel == null || !event.getChannel().getId().equals(linkChannel)) return;

        final UUID minecraft = codes.get(message);
        final String discord = event.getAuthor().getId();

        manager.unlink(discord);
        manager.unlink(minecraft);
        manager.link(discord, minecraft);

        if (manager.getUuid(discord) != null) {
            final Player player = Bukkit.getPlayer(manager.getUuid(discord));
            if (player != null) {
                // Send success message to player on Minecraft
                new MessageManager("linking.minecraft")
                        .replace("%discord%", event.getAuthor().getAsTag())
                        .send(player);

                // Send success message to user on Discord
                new MessageManager("linking.discord")
                        .replace("%minecraft%", player.getName())
                        .replace("%uuid%", player.getUniqueId().toString())
                        .send(event.getAuthor());
            }
        }

        event.getMessage().delete().queue();
    }
}
