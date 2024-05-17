package de.crafttogether.common;

import net.kyori.adventure.text.Component;

public interface Consumer {
    void operation(Throwable err, Component feedback);
}
