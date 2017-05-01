package me.itsmas.game.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Mathematics utility methods
 */
public class MathUtil
{
    /**
     * @see ThreadLocalRandom#nextInt(int)
     */
    public static int nextInt(int bound)
    {
        return ThreadLocalRandom.current().nextInt(bound);
    }
}
