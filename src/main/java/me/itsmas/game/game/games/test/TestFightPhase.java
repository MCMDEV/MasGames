package me.itsmas.game.game.games.test;

import me.itsmas.game.game.Game;
import me.itsmas.game.game.phase.phases.game.LastStandingPhase;
import me.itsmas.game.util.C;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class TestFightPhase extends LastStandingPhase
{
    TestFightPhase(Game game)
    {
        super(game);
    }

    @Override
    public void onStart()
    {
        game.forEachPlayer(player -> player.getInventory().setItem(0, new ItemStack(Material.WOOD_SWORD)));
    }

    @Override
    public String getActionBar(Player player)
    {
        return C.GOLD + "Players left standing: " + C.BOLD + game.getPlayersSize();
    }
}
