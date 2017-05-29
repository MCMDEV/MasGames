package me.itsmas.games.game.games.test;

import me.itsmas.games.GameManager;
import me.itsmas.games.game.Game;
import me.itsmas.games.game.GameType;

public class TestGame extends Game
{
    public TestGame(GameManager manager)
    {
        super(manager, GameType.TEST_GAME);

        itemDrop = false;
        deathDropItems = true;

        damage = true;
        pvp = true;

        addPhase(new TestFightPhase(this));
    }
}
