package me.itsmas.game.util;

@FunctionalInterface
public interface Action<T>
{
    void run(T data);
}
