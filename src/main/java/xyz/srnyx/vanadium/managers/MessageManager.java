package xyz.srnyx.vanadium.managers;

import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;

import xyz.srnyx.vanadium.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;


public class MessageManager {
    private String message;

    /**
     * Initializes a new {@link MessageManager}
     *
     * @param   key The location of the message in the messages file
     */
    public MessageManager(String key) {
        String string = Main.messages.getString(key);
        if (string != null) {
            String prefix = Main.messages.getString("plugin.prefix");
            if (prefix != null) {
                this.message = string.replace("%prefix%", prefix);
            } else this.message = string;
        } else this.message = key;
    }

    /**
     * Replaces {@code before} with {@code after}
     *
     * @param   before  The string to replace
     * @param   after   The replacement for the {@code before}
     *
     * @return              MessageManager
     */
    public MessageManager replace(String before, String after) {
        message = message.replace(before, after);
        return this;
    }

    /**
     * Sends the message to the {@code sender}
     *
     * @param   sender  The person/thing that should be sent the message
     */
    public void send(CommandSender sender) {
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    /**
     * Sends the message to the {@code user} in a DM (Discord)
     *
     * @param   user    The user that should be sent the message
     */
    public void send(User user) {
        DiscordUtil.privateMessage(user, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', message)));
    }

    /**
     * @return  The message as a string
     */
    public String string() {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
