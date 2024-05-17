package de.crafttogether.common.localization;

import de.crafttogether.CTCommons;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface ILocalizationEnum extends ILocalizationDefault {

    default void message(org.bukkit.command.CommandSender sender, Placeholder... arguments) {
        CTCommons.Bukkit.audiences.sender(sender).sendMessage(deserialize(arguments));
    }

    default void message(net.md_5.bungee.api.CommandSender sender, Placeholder... arguments) {
        CTCommons.BungeeCord.audiences.sender(sender).sendMessage(deserialize(arguments));
    }

    default Component deserialize(List<Placeholder> resolvers) {
        String text = get();

        if (text.isEmpty())
            return Component.empty();

        if (resolvers == null)
            resolvers = new ArrayList<>();

        List<Placeholder> globalPlaceholders = getManager().getPlaceholders();
        if (globalPlaceholders != null && globalPlaceholders.size() > 0)
            resolvers.addAll(globalPlaceholders);

        for (Placeholder placeholder : resolvers)
            text = placeholder.resolve(text);

        return getManager().miniMessage().deserialize(text);
    }

    default Component deserialize(Placeholder... resolvers) {
        return deserialize(new ArrayList<>(Arrays.asList(resolvers)));
    }

    default String get() {
        return getManager().getLocale(getName());
    }

    LocalizationManager getManager();
}
