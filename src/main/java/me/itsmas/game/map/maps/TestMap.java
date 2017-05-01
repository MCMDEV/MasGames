package me.itsmas.game.map.maps;

import me.itsmas.game.game.GameType;
import me.itsmas.game.map.GameMap;

public class TestMap extends GameMap
{
    public TestMap()
    {
        super("Testing Map", "TestGameWorld", GameType.TEST_GAME);

        setLobbyLocation(8, 20, 8, 90, 0);

        addSpawn("Players", 23, 4, -6, 45, 0);
        addSpawn("Players", 23, 4, 23, 135, 0);
        addSpawn("Players", -6, 4, 23, 225, 0);
        addSpawn("Players", -6, 4, -6, 315, 0);
    }
}
