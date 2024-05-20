package de.crafttogether.common.localization;

import de.crafttogether.common.commands.CommandSender;
import net.kyori.adventure.text.Component;

import java.util.UUID;

@SuppressWarnings("unused")
public abstract class LocalizationEnum implements ILocalizationEnum {
    private final String name;
    private final String defValue;

    private LocalizationManager manager;

    public LocalizationEnum(String name, String defValue) {
        this.name = name;
        this.defValue = defValue;
    }

    @Override
    public String getDefault() {
        return this.defValue;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void message(CommandSender sender, Placeholder... arguments) {
        ILocalizationEnum.super.message(sender, arguments);
    }

    @Override
    public void message(UUID uuid, Placeholder... arguments) {
        ILocalizationEnum.super.message(uuid, arguments);
    }

    @Override
    public Component deserialize(Placeholder... arguments) {
        return ILocalizationEnum.super.deserialize(arguments);
    }

    @Override
    public String get() {
        return ILocalizationEnum.super.get();
    }

    @Override
    public abstract LocalizationManager getManager();
}
