package de.crafttogether.ctcommons.localization;

public class Placeholder {
    private final String name;
    private final String value;

    private Placeholder(String key, String value) {
        this.name = key;
        this.value = value;
    }

    public static Placeholder set(String key, String value) {
        return new Placeholder(key, value);
    }

    public static Placeholder set(String key, int value) {
        return new Placeholder(key, String.valueOf(value));
    }

    public static Placeholder set(String key, double value) {
        return new Placeholder(key, String.valueOf(value));
    }

    public String resolve(String text) {
        return text.replace("{" + this.name + "}", this.value);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
