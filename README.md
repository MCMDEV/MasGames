# MasGames
A phase-based minigame API for Bukkit/Spigot servers.\
See the examples folder for usage.

My idea to create this API was because of inspiration from[this spigot thread.](https://www.spigotmc.org/threads/organizing-your-minigame-code-using-fsmgasm.235786/)\
All credits for the idea goes to Minikloon.

## Phase-based? What's that?
Phase-based refers to a game system which instead of having a concrete system of a lobby, countdown, ingame and endgame, etc, is customisable to the point of being able to create your own phases for each game.\
This is extremely helpful when you may have multiple games with similar mechanics and want to add mechanics from an old game to a new one, or if you want one game to be completely different to the rest in the way it starts/ends without having to change a load of code.

Don't want your new game to have a countdown? Easily done in one line.\
Want to add a deathmatch period to one of your existing games? Go ahead, it's easy!

As an example I will use Hypixel's SkyWars minigame:\
In a Hypixel SkyWars game, you start in a cage above your island (cages are cosmetic items which can be collected), then you fight other players, then the game ends when there is a single player left and there is a victory effect for the winner. If the game goes on for too long, a deathmatch begins where dragons begin to spawn, and if no one wins after this stage the game ends in a draw.

If you wanted to recreate this style of game within this API, you would probably have the following phases:
- Cage phase
- Fighting phase
- Victory/EndGame phase
- Deathmatch phase

If you then wanted another, unrelated game in which players also start in cages before the game starts, you would just add the CagePhase to another game's phase list, or even have an abstract CagePhase with subclasses for each game depending on whether you want the cage system to differ per game.

This is demonstrated on Hypixel, where there are several different modes of SkyWars (6 in total) which all use cages before the fighting stage. It won't have taken them long to make each variation after initially creating the first one if they use(d) a system like this (which they may do, probably even something better!), as all they need to do is add the the phase for each game variation.