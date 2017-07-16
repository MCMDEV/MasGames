package me.itsmas.minigames.game.games.test;

import me.itsmas.minigames.GameManager;
import me.itsmas.minigames.game.Game;
import me.itsmas.minigames.game.GameType;

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
