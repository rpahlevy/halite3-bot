// This Java API uses camelCase instead of the snake_case as documented in the API docs.
//     Otherwise the names of methods are consistent.

import hlt.*;

import java.util.ArrayList;
import java.util.Random;
import java.util.HashMap;

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
		final HashMap<EntityId,String> shipStatus = new HashMap<>();
		final String STATUS_EXPLORE = "explore";
		final String STATUS_RETURN  = "return";
		
        // At this point "game" variable is populated with initial map data.
        // This is a good place to do computationally expensive start-up pre-processing.
        // As soon as you call "ready" function below, the 2 second per turn timer will start.
        game.ready("rpahlevy");

        log("Successfully created bot! My Player ID is " + game.myId + ". Bot rng seed is " + rngSeed + ".");

        for (;;) {
            game.updateFrame();
            final Player me = game.me;
            final GameMap gameMap = game.gameMap;

            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
				String status = shipStatus.get(ship.id);
				if (status == null) {
					shipStatus.put(ship.id, STATUS_EXPLORE);
				}
				
				log("Ship "+ ship.id +" ["+ shipStatus.get(ship.id) +"] has "+ ship.halite +" halite");
				Direction d = Direction.STILL;
				if (status == STATUS_RETURN)
				{
					if (ship.position.equals(me.shipyard.position)) {
						shipStatus.put(ship.id, STATUS_EXPLORE);
						d = gameMap.getNextHaliteNode(ship);
					} else {
						// log("\tShip: "+ ship.position +" | "+ me.shipyard.position +": "+ (ship.position == me.shipyard.position));
						d = gameMap.navigate(ship, me.shipyard.position);
					}
				}
				else
				{
					if (ship.halite >= Constants.MAX_HALITE * 0.75) {
						shipStatus.put(ship.id, STATUS_RETURN);
						d = gameMap.navigate(ship, me.shipyard.position);
					} else if (gameMap.at(ship).halite < Constants.MAX_HALITE / 10) {
						d = gameMap.getNextHaliteNode(ship);
					}
				}
				
				commandQueue.add(ship.move(d));
            }
			

            if (
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST &&
                !gameMap.at(me.shipyard).isOccupied())
            {
                // check if shipyard is surrounded
                boolean shipyardSurrounded = true;
                for (Direction d: Direction.ALL_CARDINALS) {
                    Position p = gameMap.normalize(me.shipyard.position.directionalOffset(d));
                    MapCell cell = gameMap.at(p);
                    if (!cell.isOccupied()) {
                        shipyardSurrounded = false;
                        break;
                    }
                }

                if (!shipyardSurrounded) {
                    commandQueue.add(me.shipyard.spawn());
                }
            }

            game.endTurn(commandQueue);
        }
    }
	
	public static void log(String msg)
	{
		Log.log(msg);
	}
}
