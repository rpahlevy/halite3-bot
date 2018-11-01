package hlt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GameMap {
    public final int width;
    public final int height;
    public final MapCell[][] cells;

    public GameMap(final int width, final int height) {
        this.width = width;
        this.height = height;

        cells = new MapCell[height][];
        for (int y = 0; y < height; ++y) {
            cells[y] = new MapCell[width];
        }
    }

    public MapCell at(final Position position) {
        final Position normalized = normalize(position);
        return cells[normalized.y][normalized.x];
    }

    public MapCell at(final Entity entity) {
        return at(entity.position);
    }

    public int calculateDistance(final Position source, final Position target) {
        final Position normalizedSource = normalize(source);
        final Position normalizedTarget = normalize(target);

        final int dx = Math.abs(normalizedSource.x - normalizedTarget.x);
        final int dy = Math.abs(normalizedSource.y - normalizedTarget.y);

        final int toroidal_dx = Math.min(dx, width - dx);
        final int toroidal_dy = Math.min(dy, height - dy);

        return toroidal_dx + toroidal_dy;
    }

    public Position normalize(final Position position) {
        final int x = ((position.x % width) + width) % width;
        final int y = ((position.y % height) + height) % height;
        return new Position(x, y);
    }

    public ArrayList<Direction> getUnsafeMoves(final Position source, final Position destination) {
        final ArrayList<Direction> possibleMoves = new ArrayList<>();

        final Position normalizedSource = normalize(source);
        final Position normalizedDestination = normalize(destination);

        final int dx = Math.abs(normalizedSource.x - normalizedDestination.x);
        final int dy = Math.abs(normalizedSource.y - normalizedDestination.y);
        final int wrapped_dx = width - dx;
        final int wrapped_dy = height - dy;

        if (normalizedSource.x < normalizedDestination.x) {
            possibleMoves.add(dx > wrapped_dx ? Direction.WEST : Direction.EAST);
        } else if (normalizedSource.x > normalizedDestination.x) {
            possibleMoves.add(dx < wrapped_dx ? Direction.WEST : Direction.EAST);
        }

        if (normalizedSource.y < normalizedDestination.y) {
            possibleMoves.add(dy > wrapped_dy ? Direction.NORTH : Direction.SOUTH);
        } else if (normalizedSource.y > normalizedDestination.y) {
            possibleMoves.add(dy < wrapped_dy ? Direction.NORTH : Direction.SOUTH);
        }

        return possibleMoves;
    }

    public Direction naiveNavigate(final Ship ship, final Position destination) {
        // getUnsafeMoves normalizes for us
        for (final Direction direction : getUnsafeMoves(ship.position, destination)) {
            final Position targetPos = normalize(ship.position.directionalOffset(direction));
            if (!at(targetPos).isOccupied()) {
                at(targetPos).markUnsafe(ship);
                return direction;
            }
        }

        return Direction.STILL;
    }
	
	public Direction getNextDirection(final Ship ship, final Position destination)
	{
		int dist = calculateDistance(ship.position, destination) * 2;
		Position nextPosition = ship.position;
		Direction nextDirection = Direction.STILL;
		
		for (Direction d: Direction.ALL_CARDINALS) {
			Position p = normalize(ship.position.directionalOffset(d));
			MapCell cell = at(p);
			if (cell.isOccupied()) continue;
			
			int distTest = calculateDistance(p, destination);
			if (distTest < dist) {
				dist = distTest;
				nextPosition = p;
				nextDirection = d;
			}
		}
		
		// if (!nextPosition.equals(ship.position)) {
		if (nextDirection != Direction.STILL) {
            int cost = (int) Math.ceil(at(ship.position).halite * 0.1);
            if (cost == 0 || ship.halite > cost) {
                // MapCell dCell = at(destination);
                // if (!dCell.hasStructure()) {
                    // dCell.markUnsafe(ship);
                // }
                
                // at(nextPosition).markUnsafe(ship);
                // at(ship.position).ship = null;
				
				return nextDirection;
            }
			// else {
                // nextDirection = Direction.STILL;
            // }
		}
		
		return Direction.STILL;
	}
	
	public Direction getNextHaliteDirection(final Ship ship) {
		return getNextHaliteDirection(ship, 51);
	}
	public Direction getNextHaliteDirection(final Ship ship, final int targetHalite){
        final ArrayList<Position> frontier = new ArrayList<>();
        final ArrayList<Position> visited = new ArrayList<>();

        frontier.add(ship.position);
        visited.add(ship.position);

        boolean nodeFound = false;

        int altHalite = 0;
        Position nextPosition = ship.position;
        Position altPosition = ship.position;
        while (frontier.size() > 0 && !nodeFound)
        {
            Position currentPosition = frontier.remove(0);
            for (Direction d: Direction.ALL_CARDINALS) {
                Position p = normalize(currentPosition.directionalOffset(d));
                if (!visited.contains(p)) {
                    frontier.add(p);
                    visited.add(p);
                }

                MapCell cell = at(p);
                if (cell.booked) continue;

                if (cell.halite >= targetHalite) {
                    nodeFound = true;
                    nextPosition = p;

					// find the most, so dont breakdance
                    // break;
                }

                if (cell.halite > altHalite) {
                    altPosition = p;
                }
            }
        }

        // check if really found the node
        if (nodeFound)
        {
			at(nextPosition).booked = true;
            return getNextDirection(ship, nextPosition);
        }
        // else check alternative
        else if (!altPosition.equals(ship.position))
        {
			at(altPosition).booked = true;
            return getNextDirection(ship, altPosition);
        }

		// should not reached here cause of altPosition
        return Direction.STILL;
	}

    void _update() {
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                cells[y][x].ship = null;
				cells[y][x].booked = false;
            }
        }

        final int updateCount = Input.readInput().getInt();

        for (int i = 0; i < updateCount; ++i) {
            final Input input = Input.readInput();
            final int x = input.getInt();
            final int y = input.getInt();

            cells[y][x].halite = input.getInt();
        }
    }

    static GameMap _generate() {
        final Input mapInput = Input.readInput();
        final int width = mapInput.getInt();
        final int height = mapInput.getInt();

        final GameMap map = new GameMap(width, height);

        for (int y = 0; y < height; ++y) {
            final Input rowInput = Input.readInput();

            for (int x = 0; x < width; ++x) {
                final int halite = rowInput.getInt();
                map.cells[y][x] = new MapCell(new Position(x, y), halite);
            }
        }

        return map;
    }
}
