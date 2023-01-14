package de.crafttogether.common.update;

@SuppressWarnings("unused")
public class Author {
    private final String absoluteUrl;
    private final String fullName;

    Author(String absoluteUrl, String fullName) {
        this.absoluteUrl = absoluteUrl;
        this.fullName = fullName;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public String getFullName() {
        return fullName;
    }
}
