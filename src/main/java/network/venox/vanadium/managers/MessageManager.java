package network.venox.vanadium.managers;

import com.olliez4.interface4.util.json.JSON;
import com.olliez4.interface4.util.json.components.*;

import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;

import network.venox.vanadium.Main;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.ArrayList;


public class MessageManager {
    private final String key;
    private String prefix;
    private String splitter;
    private String message;
    private Command cmd;
    private String[] args;

    /**
     * Initializes a new {@link MessageManager}
     *
     * @param   key The location of the message in the messages file
     */
    public MessageManager(String key) {
        this.key = key;
        this.message = message();
    }

    /**
     * Initializes a new {@link MessageManager}
     *
     * @param   key     The location of the message in the messages file
     * @param   cmd     The command the player executed
     * @param   args    The arguments the player provided
     */
    public MessageManager(String key, Command cmd, String[] args) {
        this.key = key;
        this.cmd = cmd;
        this.args = args;
        this.message = message().replace("%command%", command());
    }

    /**
     * Sets the prefix, splitter, and message
     *
     * @return  The message
     */
    private String message() {
        // %prefix%
        String pluginPrefix = Main.messages.getString("plugin.prefix");
        if (pluginPrefix == null) pluginPrefix = "plugin.prefix";
        this.prefix = pluginPrefix;

        // Set splitter variable
        String split = Main.messages.getString("plugin.splitter");
        if (split == null) split = "@@";
        this.splitter = split;

        // Message
        String string = Main.messages.getString(key);
        if (string == null) string = key;
        return string.replace("%prefix%", prefix);
    }

    /**
     * @return  The command the player executed
     */
    private String command() {
        if (cmd == null) return "";

        final StringBuilder builder = new StringBuilder();
        builder.append("/").append(cmd.getName());
        for (final String arg : args) builder.append(" ").append(arg);
        return builder.toString();
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
        final ConfigurationSection section = Main.messages.getConfigurationSection(key);
        if (section == null) {
            if (message == null) {
                JSON.send(sender, new JTextComponent(key, "Please check your messages.yml file"));
                return;
            }

            if (!message.contains(splitter)) {
                JSON.send(sender, new JTextComponent(message, null));
                return;
            }

            final String[] split = message.split(splitter);
            final String display = split[0];
            @Nullable final String hover = split[1];

            if (split.length == 3) {
                JSON.send(sender, new JPromptComponent(display, hover, split[2]));
                return;
            }

            JSON.send(sender, new JTextComponent(display, hover));
            return;
        }

        final ArrayList<JSONComponent> components = new ArrayList<>();
        for (final String subKey : section.getKeys(false)) {
            final String path = key + "." + subKey;
            final String subMessage = Main.messages.getString(path);
            if (subMessage == null) {
                components.add(new JTextComponent(path, "Please check your messages.yml file"));
                continue;
            }

            final String[] split = subMessage
                    .replace("%prefix%", prefix)
                    .replace("%command%", command())
                    .split(splitter);
            final String display = split[0];
            @Nullable final String hover = split[1];
            @Nullable final String function = split[2];

            // Prompt component
            if (subKey.startsWith("prompt")) {
                components.add(new JPromptComponent(display, hover, function));
                continue;
            }

            // Clipboard component
            if (subKey.startsWith("clipboard")) {
                components.add(new JClipboardComponent(display, hover, function));
                continue;
            }

            // Chat component
            if (subKey.startsWith("chat")) {
                components.add(new JChatComponent(display, hover, function));
                continue;
            }

            // Command component
            if (subKey.startsWith("command")) {
                components.add(new JCommandComponent(display, hover, function));
                continue;
            }

            // Web component
            if (subKey.startsWith("web")) {
                components.add(new JWebComponent(display, hover, function));
                continue;
            }

            // Text component
            components.add(new JTextComponent(display, hover));
        }

        // Send message
        JSON.send(sender, components);
    }
}
