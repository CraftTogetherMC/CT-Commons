package de.crafttogether.common.localization;

import de.crafttogether.common.util.PluginUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public interface ILocalizationEnum extends ILocalizationDefault {

    default void message(CommandSender sender, Placeholder... arguments) {
        PluginUtil.adventure().sender(sender).sendMessage(deserialize(arguments));
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
