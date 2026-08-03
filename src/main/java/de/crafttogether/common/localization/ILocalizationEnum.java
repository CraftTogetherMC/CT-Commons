package de.crafttogether.common.localization;

import de.crafttogether.CTCommons;
import de.crafttogether.common.commands.CommandSender;
import de.crafttogether.common.util.AudienceUtil;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public interface ILocalizationEnum extends ILocalizationDefault {

    default void message(CommandSender sender, Placeholder... arguments) {
        AudienceUtil.getPlayer(sender.getUniqueId()).sendMessage(deserialize(arguments));
    }

    default void message(UUID uuid, Placeholder... arguments) {
        AudienceUtil.getPlayer(uuid).sendMessage(deserialize(arguments));
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
