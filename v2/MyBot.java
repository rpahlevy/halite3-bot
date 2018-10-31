// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

package v2;

import hlt.*;

import java.util.ArrayList;
import java.util.Random;

public class MyBot {
    public static void main(final String[] args) {
        final long rngSeed;
        if (args.length > 1) {
            rngSeed = Integer.parseInt(args[1]);
        } else {
            rngSeed = System.nanoTime();
        }
        final Random rng = new Random(rngSeed);

        Game game = new Game();
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("rpahlevy v2");

        log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");
		
		int maxShip = game.players.size() > 2 ? 17 : 25;

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
				if (ship.halite >= Constants.MAX_HALITE*0.85) {
					Direction nextDirection = gameMap.naiveNavigate(ship, me.shipyard.position);
					log("Naive navigate: "+ nextDirection);
					commandQueue.add(ship.move(nextDirection));
				} else if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10) {
					int maxHalite = gameMap.at(ship).halite;
					Direction nextDirection = Direction.STILL;
					Position nextPosition = ship.position;
					for (Direction d: Direction.ALL_CARDINALS) {
						Position p = ship.position.directionalOffset(d);
						MapCell cell = gameMap.at(p);
						if (cell.isOccupied()) continue;
						
						if (cell.halite > maxHalite) {
							maxHalite = cell.halite;
							nextDirection = d;
							nextPosition = p;
						}
					}
					log("Next Direction: "+ nextDirection);
					if (nextDirection != Direction.STILL) {
						gameMap.at(nextPosition).markUnsafe(ship);
					}
					
                    //final Direction randomDirection = Direction.ALL_CARDINALS.get(rng.nextInt(4));
                    commandQueue.add(ship.move(nextDirection));
                } else {
                    commandQueue.add(ship.stayStill());
                }
            }

            if (
                game.turnNumber <= 200 &&
				me.ships.values().size() < maxShip &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                commandQueue.add(me.shipyard.spawn());
            }

            game.endTurn(commandQueue);
        }
    }
	
	public static void log(String msg) {
		Log.log(msg);
	}
}
