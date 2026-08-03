package de.crafttogether.ctcommons.commands;

import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.messaging.MessagingService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.CommandDescription;

public class MessagingCommands {
    @Command("${plugin} serverlist")
    @CommandDescription("This command lists all connected servers in the network")
    public void ctcommons_serverlist(
            final CommandSender sender
    ) {
        Component output = MiniMessage.miniMessage().deserialize("<gold>Verbundene Server</gold><yellow>:</yellow>").append(Component.newline());
        for (String serverName : MessagingService.getConnectedServers()) {
            output = output.append(Component.text(" - ").color(NamedTextColor.YELLOW))
                    .append(Component.text(serverName).color(NamedTextColor.YELLOW))
                    .append(Component.newline());
        }

        sender.sendMessage(output);
    }
}
