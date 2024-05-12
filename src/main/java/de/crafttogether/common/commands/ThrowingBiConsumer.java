package de.crafttogether.common.commands;

@FunctionalInterface
public interface ThrowingBiConsumer<T, U> {
    void accept(T t, U u) throws Throwable;
}