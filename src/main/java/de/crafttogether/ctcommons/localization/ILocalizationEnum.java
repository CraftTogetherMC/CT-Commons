package de.crafttogether.ctcommons.localization;

import de.crafttogether.ctcommons.CTCommons;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
public interface ILocalizationEnum extends ILocalizationDefault {

    default void message(CommandSender sender, Placeholder... arguments) {
        CTCommons.plugin.adventure().sender(sender).sendMessage(deserialize(arguments));
    }

    default Component deserialize(List<Placeholder> resolvers) {
        String text = get();

        if (text == null || text.isEmpty())
            return null;

        for (Placeholder resolver : resolvers)
            text = resolver.resolve(text);

        return CTCommons.plugin.getMiniMessageParser().deserialize(text);
    }

    default Component deserialize(Placeholder... resolvers) {
        return deserialize(Arrays.stream(resolvers).toList());
    }

    String get();
}
