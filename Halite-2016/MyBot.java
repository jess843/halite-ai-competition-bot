import java.util.ArrayList;
import java.util.Random;

public class MyBot {
	private static int[][] strengths;
	private static int myID;
    public static void main(String[] args) throws java.io.IOException {
        InitPackage iPackage = Networking.getInit();
        myID = iPackage.myID;
        GameMap gameMap = iPackage.map;
		
        Networking.sendInit("MySeventhBot");

        Random rand = new Random();

        while(true) {
            ArrayList<Move> moves = new ArrayList<Move>();

            gameMap = Networking.getFrame();
			
			strengths = getStrengthArray(gameMap);

            for(int y = 0; y < gameMap.height; y++) {
                for(int x = 0; x < gameMap.width; x++) {
                    Site site = gameMap.getSite(new Location(x, y));
                    if(site.owner == myID) {

                        boolean movedPiece = false;
						
                        for(Direction d : Direction.CARDINALS) {
                            if(gameMap.getSite(new Location(x, y), d).owner != myID && gameMap.getSite(new Location(x, y), d).strength < gameMap.getSite(new Location(x, y)).strength) {
								if (d == Direction.NORTH) {
									if (moveStrengthNorth(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), d));
										movedPiece = true;
									}
								}
								else if (d == Direction.SOUTH) {
									if (moveStrengthSouth(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), d));
										movedPiece = true;
									}
								}
								else if (d == Direction.EAST) {
									if (moveStrengthEast(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), d));
										movedPiece = true;
									}
								}
								else if (d == Direction.WEST) {
									if (moveStrengthWest(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), d));
										movedPiece = true;
									}
								}
								if (!movedPiece) {
									if (moveStrengthNorth(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), Direction.NORTH));
										movedPiece = true;
									}
									else if (moveStrengthSouth(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), Direction.SOUTH));
										movedPiece = true;
									}
									else if (moveStrengthEast(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), Direction.EAST));
										movedPiece = true;
									}
									else if (moveStrengthWest(x, y, gameMap)) {
										moves.add(new Move(new Location(x, y), Direction.WEST));
										movedPiece = true;
									}
									if (!movedPiece) {
										moves.add(new Move(new Location(x, y), Direction.STILL));
									}
								}
                                break;
                            }
                        }

                        if(!movedPiece && gameMap.getSite(new Location(x, y)).strength < gameMap.getSite(new Location(x, y)).production * 7) {
                            moves.add(new Move(new Location(x, y), Direction.STILL));
                            movedPiece = true;
                        }

						boolean insideBlob = insideBlob(x, y, gameMap, myID);
						
						if(!movedPiece && insideBlob) {
							int[] closestOutsideBlob = determineClosestOutsideBlob(x, y, gameMap, myID);
							double angle = gameMap.getAngle(new Location(closestOutsideBlob[0], closestOutsideBlob[1]), new Location(x, y));
							Direction direction = determineDirection(Math.toDegrees(angle));
							if (direction == Direction.NORTH) {
								if (moveStrengthNorth(x, y, gameMap)) {
									moves.add(new Move(new Location(x, y), direction));
									movedPiece = true;
								}
							}
							else if (direction == Direction.SOUTH) {
								if (moveStrengthSouth(x, y, gameMap)) {
									moves.add(new Move(new Location(x, y), direction));
									movedPiece = true;
								}
							}
							else if (direction == Direction.EAST) {
								if (moveStrengthEast(x, y, gameMap)) {
									moves.add(new Move(new Location(x, y), direction));
									movedPiece = true;
								}
							}
							else if (direction == Direction.WEST) {
								if (moveStrengthWest(x, y, gameMap)) {
									moves.add(new Move(new Location(x, y), direction));
									movedPiece = true;
								}
							}
							if (!movedPiece) {
								moves.add(new Move(new Location(x, y), Direction.STILL));
							}
							movedPiece = true;
						}
						
						if(!movedPiece) {
							moves.add(new Move(new Location(x, y), Direction.STILL));
                            movedPiece = true;
						}
                    }
                }
            }
            Networking.sendFrame(moves);
        }
    }
	
	public static boolean insideBlob (int x, int y, GameMap gameMap, int myID) {
		for (Direction d : Direction.CARDINALS) {
			if(gameMap.getSite(new Location(x, y), d).owner != myID) {
				return false;
			}
		}		
		return true;
	}

	public static Direction determineDirection (double degrees) {
		if (-45 <= degrees && degrees <= 45) {
			return Direction.EAST;
		}
		else if (45 <= degrees && degrees <= 135) {
			return Direction.NORTH;
		}
		else if ((135 <= degrees && degrees <= 180) || (-180 <= degrees && degrees <= -135)) {
			return Direction.WEST;
		}
		else if (-135 <= degrees && degrees <= -45) {
			return Direction.SOUTH;
		}
		return Direction.EAST;
	}

	public static int[] determineClosestOutsideBlob (int locX, int locY, GameMap gameMap, int myID) {
		double min = Double.MAX_VALUE;
		int[] output = new int[2];
		for (int y = 0; y < gameMap.height; y++) {
			for (int x = 0; x < gameMap.width; x++) {
				if (gameMap.getSite(new Location(x, y)).owner == myID && !insideBlob(x, y, gameMap, myID)) {
					double distance = gameMap.getDistance(new Location(locX, locY), new Location(x, y));
					if (distance < min) {
						min = distance;
						output[0] = x;
						output[1] = y;
					}
				}
			}
		}
		return output;
	}
	
	public static int[][] getStrengthArray (GameMap gameMap) {
		int[][] strengthsArray = new int[gameMap.height][gameMap.width];
		for (int y = 0; y < gameMap.height; y++) {
			for (int x = 0; x < gameMap.width; x++) {
				Site site = gameMap.getSite(new Location(x,y));
				if(site.owner == myID) {
					strengthsArray[y][x] = site.strength;
				}
				else {
					strengthsArray[y][x] = 0;
				}
			}
		}
		return strengthsArray;
	}
	
	public static boolean moveStrengthWest (int x, int y, GameMap gameMap) {
		int strengthOne = strengths[y][x];
		int strengthTwo = strengths[y][(x-1+gameMap.width)%gameMap.width];
		if (strengthOne + strengthTwo >= 300) {
			return false;
		}
		strengths[y][x] = 0;
		strengths[y][(x-1+gameMap.width)%gameMap.width] = strengthOne + strengthTwo;
		return true;
	}
	
	public static boolean moveStrengthEast (int x, int y, GameMap gameMap) {
		int strengthOne = strengths[y][x];
		int strengthTwo = strengths[y][(x+1)%gameMap.width];
		if (strengthOne + strengthTwo >= 300) {
			return false;
		}
		strengths[y][x] = 0;
		strengths[y][(x+1)%gameMap.width] = strengthOne + strengthTwo;
		return true;
	}
	
	public static boolean moveStrengthNorth (int x, int y, GameMap gameMap) {
		int strengthOne = strengths[y][x];
		int strengthTwo = strengths[(y-1+gameMap.width)%gameMap.width][x];
		if (strengthOne + strengthTwo >= 300) {
			return false;
		}
		strengths[y][x] = 0;
		strengths[(y-1+gameMap.width)%gameMap.width][x] = strengthOne + strengthTwo;
		return true;
	}
	
	public static boolean moveStrengthSouth (int x, int y, GameMap gameMap) {
		int strengthOne = strengths[y][x];
		int strengthTwo = strengths[(y+1)%gameMap.width][x];
		if (strengthOne + strengthTwo >= 300) {
			return false;
		}
		strengths[y][x] = 0;
		strengths[(y+1)%gameMap.width][x] = strengthOne + strengthTwo;
		return true;
	}
}
