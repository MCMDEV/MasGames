package me.itsmas.game.game.games.test;

import me.itsmas.game.GameManager;
import me.itsmas.game.game.Game;
import me.itsmas.game.game.GameType;

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
