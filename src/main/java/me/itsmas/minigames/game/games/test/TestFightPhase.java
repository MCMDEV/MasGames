package me.itsmas.minigames.game.games.test;

import me.itsmas.minigames.game.Game;
import me.itsmas.minigames.game.phase.phases.game.LastStandingPhase;
import me.itsmas.minigames.util.C;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public List<String> getSidebar(Player player)
    {
        List<String> sidebar = new ArrayList<>();

        sidebar.add(C.GOLD + C.BOLD + "Alive");
        sidebar.add(String.valueOf(game.getPlayersSize()));

        return sidebar;
    }
}
