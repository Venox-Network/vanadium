package network.venox.vanadium.managers;

import com.olliez4.interface4.util.JSON;

import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;

import network.venox.vanadium.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class MessageManager {
    private String message;
    private final String splitter;

    /**
     * Initializes a new {@link MessageManager}
     *
     * @param   key The location of the message in the messages file
     */
    public MessageManager(String key) {
        // %prefix%
        String prefix = Main.messages.getString("plugin.prefix");
        if (prefix == null) prefix = "plugin.prefix";

        // Message
        String string = Main.messages.getString(key);
        if (string == null) string = key;

        // Set message variable
        this.message = string
                .replace("%prefix%", prefix);

        // Set splitter variable
        String split = Main.messages.getString("plugin.splitter");
        if (split == null) split = "@@";
        this.splitter = split;
    }

    /**
     * Initializes a new {@link MessageManager}
     *
     * @param   key     The location of the message in the messages file
     * @param   cmd     The command the player executed
     * @param   args    The arguments the player provided
     */
    public MessageManager(String key, Command cmd, String[] args) {
        // %prefix%
        String prefix = Main.messages.getString("plugin.prefix");
        if (prefix == null) prefix = "plugin.prefix";

        // Message
        String string = Main.messages.getString(key);
        if (string == null) string = key;

        // %command%
        final StringBuilder builder = new StringBuilder();
        builder.append(cmd.getName());
        for (final String arg : args) builder.append(" ").append(arg);
        final String command = builder.toString();

        // Set message variable
        this.message = string
                .replace("%prefix%", prefix)
                .replace("%command%", command);

        // Set splitter variable
        String split = Main.messages.getString("plugin.splitter");
        if (split == null) split = "@@";
        this.splitter = split;
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
     * @return  The message as a string
     */
    public String string() {
        return ChatColor.translateAlternateColorCodes('&', message);
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
     * Sends the message to the {@code sender}
     *
     * @param   sender  The person/thing that should be sent the message
     */
    public void send(CommandSender sender) {
        JSON.sendPrompt(sender, getMessage(), getHover(), getCommand());
    }

    public String getMessage() {
        final String[] split = message.split(splitter);
        if (split.length < 1) return message;

        return split[0];
    }

    public String getHover() {
        final String[] split = message.split(splitter);
        if (split.length < 2) return null;

        return split[1];
    }

    public String getCommand() {
        final String[] split = message.split(splitter);
        if (split.length < 3) return null;

        return split[2];
    }
}
