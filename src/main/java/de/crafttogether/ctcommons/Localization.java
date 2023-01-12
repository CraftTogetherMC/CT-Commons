package de.crafttogether.ctcommons;

import de.crafttogether.ctcommons.localization.LocalizationEnum;

public class Localization extends LocalizationEnum {
    public static final Localization PREFIX = new Localization("prefix", "<gold>CTLib </gold><dark_gray>» </dark_gray>");

    public static final Localization UPDATE_LASTBUILD = new Localization("update.lastBuild", "<prefix/><green>Your installed version is up to date</green>");
    public static final Localization UPDATE_RELEASE = new Localization("update.devBuild", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new full version was released!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");
    public static final Localization UPDATE_DEVBUILD = new Localization("update.release", """
            <hover:show_text:'<green>Click here to download this version'><click:open_url:'{url}'><prefix/><green>A new snapshot build is available!</green>
            <prefix/><green>Version: </green><yellow>{version} #{build}</yellow>
            <prefix/><green>FileName: </green><yellow>{fileName}</yellow>
            <prefix/><green>FileSize: </green><yellow>{fileSize}</yellow>
            <prefix/><red>You are on version: </red><yellow>{currentVersion} #{currentBuild}</yellow></click></hover>""");

    private Localization(String name, String defValue) {
        super(name, defValue);
    }

    @Override
    public String get() {
        return CTCommons.plugin.getLocalizationManager().getLocale(this.getName());
    }
}