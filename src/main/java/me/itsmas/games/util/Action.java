package me.itsmas.games.util;

@FunctionalInterface
public interface Action<T>
{
    void run(T data);
}
