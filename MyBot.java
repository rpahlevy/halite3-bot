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

			final HashMap<Ship,Direction> queuedShip = new HashMap<>();
			final HashMap<Ship,Direction> plannedShip = new HashMap<>();
            final ArrayList<Command> commandQueue = new ArrayList<>();

            for (final Ship ship : me.ships.values()) {
                ship.planned = false;
				String status = shipStatus.get(ship.id);
				if (status == null) {
					shipStatus.put(ship.id, STATUS_EXPLORE);
				}
				
				// log("Ship "+ ship.id +" ["+ shipStatus.get(ship.id) +"] has "+ ship.halite +" halite");
				Direction d = Direction.STILL;
				if (status == STATUS_RETURN)
				{
					if (ship.position.equals(me.shipyard.position)) {
						shipStatus.put(ship.id, STATUS_EXPLORE);
						d = gameMap.getNextHaliteDirection(ship);
					} else {
						d = gameMap.getNextDirection(ship, me.shipyard.position);
					}
				}
				else
				{
					if (ship.halite >= Constants.MAX_HALITE * 0.9) {
						shipStatus.put(ship.id, STATUS_RETURN);
						d = gameMap.getNextDirection(ship, me.shipyard.position);
					} else if (gameMap.at(ship).halite < Constants.MAX_HALITE / 20) {
						d = gameMap.getNextHaliteDirection(ship);
					}
				}
				
				// commandQueue.add(ship.move(d));
				Position targetPosition = gameMap.normalize(ship.position.directionalOffset(d));
				MapCell targetNode = gameMap.at(targetPosition);
				
				// check targetNode safe or not
				boolean planned = true;
				Ship partnerShip = null;
				Direction partnerDirection = null;
				Position partnerTargetPosition = null;
				do {
					// if not moving
					if (d == Direction.STILL) {
						break;
					}
					
					// if target node not occupied
					if (!targetNode.isOccupied()) {
						break;
					}
					
					// check if node occupied by other player
					Ship ocp = targetNode.ship;
					if (!ocp.owner.equals(ship.owner)) {
						d = Direction.STILL;
						break;
					}
					
					// check if ocp has been queued
					Direction ocpQueue = queuedShip.get(ocp);
					if (ocpQueue == null) {
						planned = false;
						break;
					}
					
					// check ocp target position is ship's position
					Position ocpTargetPosition = gameMap.normalize(ocp.position.directionalOffset(ocpQueue));
					if (ocpTargetPosition.equals(ship.position)) {
						// best match!
						queuedShip.remove(ocp);
						partnerShip = ocp;
						partnerDirection = ocpQueue;
						partnerTargetPosition = ocpTargetPosition;
					} else {
						// wait for ocp, ok?
						planned = false;
					}
				} while (false);
				
				// if planned add to command
				if (planned) {
                    ship.planned = true;
					commandQueue.add(ship.move(d));
					
					if (d == Direction.STILL) {
						gameMap.at(ship.position).markUnsafe(ship);
						log("[PLAN] Ship "+ ship.id +" ["+ d +"] at "+ ship.position.x +","+ ship.position.y);
					} else {
						gameMap.at(ship.position).ship = null;
						targetNode.markUnsafe(ship);
						log("[PLAN] Ship "+ ship.id +" ["+ d +"] to "+ targetPosition.x +","+ targetPosition.y);
					}
					
				} else {
                    queuedShip.put(ship, d);
					log("[QUED] Ship "+ ship.id +" ["+ d +"] to "+ targetPosition.x +","+ targetPosition.y);
				}
				
				// also help partner to move if any
				if (partnerShip != null && partnerDirection != null) {
                    partnerShip.planned = true;
					commandQueue.add(partnerShip.move(partnerDirection));
					gameMap.at(partnerTargetPosition).markUnsafe(partnerShip);
                    log("[PLAN-P] Ship "+ partnerShip.id +" ["+ d +"] to "+ partnerTargetPosition.x +","+ partnerTargetPosition.y);
				}
				
            }
			
            // log("##")
			
			for (final Ship ship: queuedShip.keySet()) {
                // log("\t[CP] Ship "+ ship.id +" ["+ d +"]");
				// if (ship == null) continue;
				
				Direction d = queuedShip.get(ship);
				Position targetPosition = gameMap.normalize(ship.position.directionalOffset(d));
				MapCell targetNode = gameMap.at(targetPosition);
				
				boolean planned = true;
				Ship partnerShip = null;
				Direction partnerDirection = null;
				Position partnerTargetPosition = null;
				do {
					// if target node not occupied
					if (!targetNode.isOccupied()) {
						break;
					}
					
					// check if node occupied by other player
					Ship ocp = targetNode.ship;
					if (!ocp.owner.equals(ship.owner)) {
						d = Direction.STILL;
						break;
					}
					
					// check if ocp has been queued
					Direction ocpQueue = queuedShip.get(ocp);
					if (ocpQueue == null) {
						planned = false;
						break;
					}
					
					// check ocp target position is ship's position
					Position ocpTargetPosition = gameMap.normalize(ocp.position.directionalOffset(ocpQueue));
					if (ocpTargetPosition.equals(ship.position)) {
						// best match!
						// queuedShip.remove(ocp);
						partnerShip = ocp;
						partnerDirection = ocpQueue;
						partnerTargetPosition = ocpTargetPosition;
					} else {
						// wait for ocp, ok?
						planned = false;
					}
				} while (false);
				
				// if planned add to command
                ship.planned = true;
				if (planned) {
					commandQueue.add(ship.move(d));
					
					if (d == Direction.STILL) {
						gameMap.at(ship.position).markUnsafe(ship);
                        log("[PLAN] Ship "+ ship.id +" ["+ d +"] at "+ ship.position.x +","+ ship.position.y);
					} else {
                        if (partnerShip == null || (partnerShip != null && !partnerShip.planned)) {
						    gameMap.at(ship.position).ship = null;
                        }

						targetNode.markUnsafe(ship);
                        log("[PLAN] Ship "+ ship.id +" ["+ d +"] to "+ targetPosition.x +","+ targetPosition.y);
					}
				} else {
					commandQueue.add(ship.stayStill());
					gameMap.at(ship.position).markUnsafe(ship);
                    log("[PLAN-Q] Ship "+ ship.id +" ["+ d +"] at "+ ship.position.x +","+ ship.position.y);
				}
				
				
				// also help partner to move if any
				if (partnerShip != null && partnerDirection != null) {
                    if (!partnerShip.planned) {
                        partnerShip.planned = true;
                        commandQueue.add(partnerShip.move(partnerDirection));
                        gameMap.at(partnerTargetPosition).markUnsafe(partnerShip);
                        log("[PLAN-P] Ship "+ partnerShip.id +" ["+ d +"] to "+ partnerTargetPosition.x +","+ partnerTargetPosition.y);
                    }
				}
			}
			

            if(
                game.turnNumber <= 200 &&
                me.halite >= Constants.SHIP_COST )
			{
				final Ship ocp = gameMap.at(me.shipyard).ship;
				if (ocp == null || !ocp.owner.equals(me.id))
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
            }

            game.endTurn(commandQueue);
        }
    }
	
	public static void log(String msg)
	{
		Log.log(msg);
	}
}
