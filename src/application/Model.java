package application;

import java.util.*;

public class Model {
	public static final int SIZE = 20; // Size of the game grid
	private Cell[][] grid; // 2D array representing the game grid
    private Player player; // Player object representing the player's position
    private Enemy enemy; // Enemy object representing the enemy's position
    private ArrayList<Position> chests; // List of chest positions
    private ArrayList<Position> clocks; // List of clock positions
    private Position finish; // Position of the finish point
    private int playerLives; // Number of lives the player has
    private int score; // Player's current score
    private boolean enemySlowed; // Flag indicating if the enemy is slowed
    private int slowedTurnsRemaining; // Number of turns the enemy remains slowed
    private boolean gameWon; // Flag indicating if the game is won
    private boolean lifeLost; // Flag indicating if a life was just lost
    private boolean gameEnded; // To track if the game has ended

    // Pathfinding variables
    private List<Position> currentPath; // Current calculated path
    private Stack<Position> playerMoveHistory; // Stack to store player's move history for undo

    /*
     * Constructor for the Model class.
     * Initializes the game state and sets up the initial maze.
     */
    public Model() {
        grid = new Cell[SIZE][SIZE];
        chests = new ArrayList<>();
        clocks = new ArrayList<>();
        playerLives = 3;
        score = 0;
        enemySlowed = false;
        slowedTurnsRemaining = 0;
        gameWon = false;
        currentPath = new ArrayList<>();        
        playerMoveHistory = new Stack<>();
        lifeLost = false;
        gameEnded = false;
        initializeHardcodedMaze();
        
    }
    
    /*
     * Calculates a path from the player's current position to the target position.
     * Uses a breadth-first search algorithm to find the shortest path.
     * 
     * @param target The target position to reach
     * @return A list of positions representing the path to the target
     */
    public List<Position> calculatePathTo(Position target) {
        currentPath.clear();
        // Check if the move is valid and within range
        if (!isValidMove(target.x, target.y) || manhattanDistance(player, target) > 3) {
            return currentPath;
        }

        Queue<List<Position>> queue = new LinkedList<>();
        Set<Position> visited = new HashSet<>();

        // Initialize the search with the player's current position
        List<Position> initialPath = new ArrayList<>();
        initialPath.add(new Position(player.x, player.y));
        queue.offer(initialPath);
        visited.add(new Position(player.x, player.y));

        while (!queue.isEmpty()) {
            List<Position> path = queue.poll();
            Position current = path.get(path.size() - 1);

            // If we've reached the target, return the path
            if (current.equals(target)) {
                currentPath = path.subList(1, path.size()); // Exclude the starting position
                return currentPath;
            }

            // If the path is within the allowed range, explore neighbors
            if (path.size() <= 3) {
                int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}}; // Right, Down, Left, Up
                for (int[] dir : directions) {
                    int newX = current.x + dir[0];
                    int newY = current.y + dir[1];
                    Position newPos = new Position(newX, newY);
                    if (isValidMove(newX, newY) && !visited.contains(newPos)) {
                        List<Position> newPath = new ArrayList<>(path);
                        newPath.add(newPos);
                        queue.offer(newPath);
                        visited.add(newPos);
                    }
                }
            }
        }

        return currentPath; // Return empty path if no valid path found
    }

    /*
     * Moves the player to the target position if a valid path exists and the game hasn't ended.
     * 
     * @param target The target position to move to
     * @return true if the move was successful, false otherwise
     */
    public boolean movePlayerToPosition(Position target) {
    	if (gameEnded) return false; // Prevent movement if the game has ended
    	
        List<Position> path = calculatePathTo(target);
        if (!path.isEmpty()) {
            for (Position pos : path) {
                playerMoveHistory.push(new Position(player.x, player.y));
                player.x = pos.x;
                player.y = pos.y;
                handleCellEffect(pos);
            }
            checkGameEnd(); // Check if the game has ended after the move
            return true;
        }
        return false;
    }

    /*
     * Undoes the player's last move if possible.
     * 
     * @return true if the move was undone, false if no moves to undo
     */
    public boolean undoPlayerMove() {
        if (!playerMoveHistory.isEmpty()) {
            Position previousPosition = playerMoveHistory.pop();
            player.x = previousPosition.x;
            player.y = previousPosition.y;
            return true;
        }
        return false;
    }

    /*
     * Checks if a given position is a valid move target for the player.
     * 
     * @param target The position to check
     * @return true if the position is a valid move target, false otherwise
     */
    public boolean isValidMoveTarget(Position target) {
        return isValidMove(target.x, target.y) && manhattanDistance(player, target) <= 3;
    }

    /*
     * Calculates the Manhattan distance between two positions.
     * 
     * @param a The first position
     * @param b The second position
     * @return The Manhattan distance between the two positions
     */
    private int manhattanDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    /*
     * Handles the effect of moving to a cell.
     * This includes collecting chests, activating clocks, and reaching the finish.
     * 
     * @param pos The position of the cell to handle
     */
    private void handleCellEffect(Position pos) {
        if (grid[pos.x][pos.y] == Cell.CHEST) {
            score += 10;
            chests.remove(pos);
            grid[pos.x][pos.y] = Cell.EMPTY;
        } else if (grid[pos.x][pos.y] == Cell.CLOCK) {
            enemySlowed = true;
            slowedTurnsRemaining = 3;
            clocks.remove(pos);
            grid[pos.x][pos.y] = Cell.EMPTY;
        } else if (grid[pos.x][pos.y] == Cell.FINISH) {
            gameWon = true;
        }
    }

    /*
     * Initializes the maze with a hardcoded layout.
     * This method sets up the walls, player, enemy, and finish positions.
     */
    private void initializeHardcodedMaze() {
        // Initialize all cells as walls
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                grid[i][j] = Cell.WALL;
            }
        }

        // Define the maze (hardcoded)
        String[] mazeRows = new String[] {
            "P#_#_###_###_##_##_#",
            "___#___#___#_#_____#",
            "#_##_#_#_#___#_###_#",
            "_____#___#_#_____#__",
            "##_####_##_#_###_#_#",
            "___#_______________#",
            "#_##_##_#_#_###_####",
            "#____#____#___#_#___",
            "###_##_####_#_#_##_#",
            "_______#____#______F",
            "##_###_#_##_#_####__",
            "_________________###",
            "#_##_###_###_###_#__",
            "#_#____#_#___#_____#",
            "#___##___#_#___#_###",
            "__###___##___###_#__",
            "#_____#____#___#___#",
            "___##_#__###_#_###_#",
            "#__#_____#___#_____#",
            "##___###___#_##_##_E"
        };

        // Verify all rows are correct length
        for (int i = 0; i < SIZE; i++) {
            if (mazeRows[i].length() != SIZE) {
                throw new IllegalStateException("Row " + i + " is not " + SIZE + " characters long");
            }
        }

        // Convert the string representation to the actual maze
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                char cell = mazeRows[i].charAt(j);
                switch (cell) {
                    case 'P':
                        grid[i][j] = Cell.EMPTY;
                        player = new Player(i, j);
                        break;
                    case 'E':
                        grid[i][j] = Cell.EMPTY;
                        enemy = new Enemy(i, j);
                        break;
                    case 'F':
                        grid[i][j] = Cell.FINISH;
                        finish = new Position(i, j);
                        break;
                    case '_':
                        grid[i][j] = Cell.EMPTY;
                        break;
                    case '#':
                        grid[i][j] = Cell.WALL;
                        break;
                }
            }
        }

        // Place items
        placeItems();
    }

    /*
     * Places chests and clocks randomly in empty cells of the maze.
     */
    private void placeItems() {
        Random rand = new Random();
        // Place 5 chests
        for (int i = 0; i < 5; i++) {
            placeItem(Cell.CHEST);
        }
        // Place 3 clocks
        for (int i = 0; i < 3; i++) {
            placeItem(Cell.CLOCK);
        }
    }

    /*
     * Places a single item (chest or clock) in a random empty cell.
     * 
     * @param item The type of item to place (CHEST or CLOCK)
     */
    private void placeItem(Cell item) {
        Random rand = new Random();
        int attempts = 0;
        while (attempts < 100) {
            int x = rand.nextInt(SIZE);
            int y = rand.nextInt(SIZE);
            if (grid[x][y] == Cell.EMPTY && 
                (x != player.x || y != player.y) && 
                (x != enemy.x || y != enemy.y) &&
                (finish == null || x != finish.x || y != finish.y)) {
                grid[x][y] = item;
                if (item == Cell.CHEST) {
                    chests.add(new Position(x, y));
                } else if (item == Cell.CLOCK) {
                    clocks.add(new Position(x, y));
                }
                break;
            }
            attempts++;
        }
    }

    /*
     * Moves the player in the specified direction if possible and game hasn't ended.
     * 
     * @param dx The change in x-coordinate
     * @param dy The change in y-coordinate
     * @return true if the move was successful, false otherwise
     */
    public boolean movePlayer(int dx, int dy) {
    	if (gameEnded) return false; // Prevent movement if the game has ended
    	
        int newX = player.x + dx;
        int newY = player.y + dy;
        
        if (isValidMove(newX, newY)) {
            if (grid[newX][newY] == Cell.CHEST) {
                score += 10;
                chests.remove(new Position(newX, newY));
                grid[newX][newY] = Cell.EMPTY;
            } else if (grid[newX][newY] == Cell.CLOCK) {
                enemySlowed = true;
                slowedTurnsRemaining = 3;
                clocks.remove(new Position(newX, newY));
                grid[newX][newY] = Cell.EMPTY;
            } else if (grid[newX][newY] == Cell.FINISH) {
                gameWon = true;
            }
            
            player.x = newX;
            player.y = newY;
            checkGameEnd(); // Check if the game has ended after the move
            return true;
        }
        return false;
    }

    /*
     * Moves the enemy towards the player using A* pathfinding.
     * The enemy moves twice as fast when not slowed.
     */
    public void moveEnemy() {
    	if (gameEnded) return; // Prevent enemy movement if the game has ended
    	
        if (player.x == enemy.x && player.y == enemy.y) return; // Don't move if already on player
        
        int moveDistance = enemySlowed ? 1 : 2;
        
        // Use A* pathfinding to move towards player
        for (int i = 0; i < moveDistance; i++) {
            Position nextMove = getNextMoveTowardsPlayer();
            if (nextMove != null) {
                enemy.x = nextMove.x;
                enemy.y = nextMove.y;
            }
        }
        
        if (enemySlowed) {
            slowedTurnsRemaining--;
            if (slowedTurnsRemaining <= 0) {
                enemySlowed = false;
            }
        }
        
        checkCollision();
        checkGameEnd(); // Check if the game has ended after the enemy move
    }

    /*
	 * Uses A* pathfinding to determine the next move for the enemy towards the player.
	 * 
	 * @return The next position the enemy should move to
	 */
	private Position getNextMoveTowardsPlayer() {
	    // Priority queue to store nodes to be evaluated, sorted by f-score
	    PriorityQueue<Node> openSet = new PriorityQueue<>();
	    // Set to store already evaluated positions
	    Set<Position> closedSet = new HashSet<>();
	    // Map to store the most efficient previous step for each position
	    Map<Position, Position> cameFrom = new HashMap<>();
	    // Map to store the cost of getting from start to each position
	    Map<Position, Integer> gScore = new HashMap<>();
	    
	    // Initialize start and goal positions
	    Position start = new Position(enemy.x, enemy.y);
	    Position goal = new Position(player.x, player.y);
	    
	    // Add start node to the open set with its f-score (g-score + heuristic)
	    openSet.offer(new Node(start, 0 + heuristic(start, goal)));
	    gScore.put(start, 0);
	    
	    // Define possible movement directions (right, down, left, up)
	    int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
	    
	    while (!openSet.isEmpty()) {
	        // Get the node with the lowest f-score from the open set
	        Node current = openSet.poll();
	        Position currentPos = current.position;
	        
	        // If we've reached the goal, backtrack to find the next move
	        if (currentPos.equals(goal)) {
	            Position next = currentPos;
	            while (cameFrom.get(next) != null && !cameFrom.get(next).equals(start)) {
	                next = cameFrom.get(next);
	            }
	            return next;
	        }
	        
	        // Add current position to closed set (already evaluated)
	        closedSet.add(currentPos);
	        
	        // Check all neighboring positions
	        for (int[] dir : directions) {
	            int nextX = currentPos.x + dir[0];
	            int nextY = currentPos.y + dir[1];
	            
	            // If the move is valid (not a wall and within grid)
	            if (isValidMove(nextX, nextY)) {
	                Position neighbor = new Position(nextX, nextY);
	                // Skip if we've already evaluated this position
	                if (closedSet.contains(neighbor)) continue;
	                
	                // Calculate tentative g-score for this neighbor
	                int tentativeGScore = gScore.get(currentPos) + 1;
	                
	                // If this path to neighbor is better than any previous one, record it
	                if (!gScore.containsKey(neighbor) || tentativeGScore < gScore.get(neighbor)) {
	                    cameFrom.put(neighbor, currentPos);
	                    gScore.put(neighbor, tentativeGScore);
	                    int fScore = tentativeGScore + heuristic(neighbor, goal);
	                    openSet.offer(new Node(neighbor, fScore));
	                }
	            }
	        }
	    }
	    
	    // If no path is found, return null
	    return null;
	}
	
	/*
	 * Calculates the heuristic (estimated distance) between two positions.
	 * Uses Manhattan distance as the heuristic.
	 *
	 * @param a The starting position
	 * @param b The target position
	 * @return The Manhattan distance between the two positions
	 */
	private int heuristic(Position a, Position b) {
	    return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
	}

	/*
	 * Checks if a move to the given coordinates is valid.
	 * A move is valid if it's within the grid bounds and not a wall.
	 *
	 * @param x The x-coordinate to check
	 * @param y The y-coordinate to check
	 * @return true if the move is valid, false otherwise
	 */
	private boolean isValidMove(int x, int y) {
	    return x >= 0 && x < SIZE && y >= 0 && y < SIZE && grid[x][y] != Cell.WALL;
	}

	/*
	 * Checks for a collision between the player and the enemy.
	 * If a collision occurs, decrements player lives, sets the lifeLost flag,
	 * and resets player and enemy positions.
	 */
	private void checkCollision() {
	    if (player.x == enemy.x && player.y == enemy.y) {
	        playerLives--;
	        lifeLost = true;
	        // Reset positions after collision
	        player.x = 0;
	        player.y = 0;
	        enemy.x = SIZE-1;
	        enemy.y = SIZE-1;
	    }
	}

	/*
	 * Checks if a life was lost in the most recent turn.
	 * Resets the lifeLost flag after checking.
	 *
	 * @return true if a life was lost, false otherwise
	 */
	public boolean wasLifeLost() {
	    boolean result = lifeLost;
	    lifeLost = false;  // Reset the flag after it's checked
	    return result;
	}
	
	// Checks for game-ending conditions (victory or game over) and updates the game state accordingly.
    private void checkGameEnd() {
        if (gameWon || playerLives == 0) {
            gameEnded = true;
            //endMusicGame();
        }
    }
    
    
    //Doesn't work ;-;
    //Make the game crash once the player lose or win, not worth having for the demo
    //    public void endMusicGame() {
    //        // Stop the game music
//        if (view.getGameMusicPlayer() != null) {
//            view.getGameMusicPlayer().stop();
//        }
//    }
    

    // Getter methods for accessing private fields
	public Cell[][] getGrid() { return grid; }
	public Player getPlayer() { return player; }
	public Enemy getEnemy() { return enemy; }
	public int getPlayerLives() { return playerLives; }
	public int getScore() { return score; }

	/*
	 * Checks if the game is over (player has no lives left).
	 *
	 * @return true if the game is over, false otherwise
	 */
	public boolean isGameOver() { return playerLives <= 0; }

	/*
	 * Checks if the game has been won (player reached the finish).
	 *
	 * @return true if the game is won, false otherwise
	 */
	public boolean isGameWon() { return gameWon; }

	/*
	 * Represents a position on the game grid.
	 */
	public static class Position {
	    int x, y;
	    
	    /*
	     * Constructor for Position.
	     *
	     * @param x The x-coordinate
	     * @param y The y-coordinate
	     */
	    public Position(int x, int y) {
	        this.x = x;
	        this.y = y;
	    }
	    
	    /*
	     * Checks if this Position is equal to another object.
	     *
	     * @param o The object to compare with
	     * @return true if the positions are equal, false otherwise
	     */
	    @Override
	    public boolean equals(Object o) {
	        if (this == o) return true;
	        if (!(o instanceof Position)) return false;
	        Position pos = (Position) o;
	        return x == pos.x && y == pos.y;
	    }
	    
	}

	/*
	 * Represents a node in the A* pathfinding algorithm.
	 */
	private static class Node implements Comparable<Node> {
	    Position position;
	    int fScore;
	    
	    /*
	     * Constructor for Node.
	     *
	     * @param position The position of this node
	     * @param fScore The f-score of this node
	     */
	    Node(Position position, int fScore) {
	        this.position = position;
	        this.fScore = fScore;
	    }
	    
	    /*
	     * Compares this Node with another Node based on their f-scores.
	     *
	     * @param other The other Node to compare with
	     * @return A negative integer, zero, or a positive integer as this Node's f-score
	     *         is less than, equal to, or greater than the other Node's f-score
	     */
	    @Override
	    public int compareTo(Node other) {
	        return Integer.compare(this.fScore, other.fScore);
	    }
	}

	/*
	 * Represents the player in the game.
	 */
	public static class Player extends Position {
	    /*
	     * Constructor for Player.
	     *
	     * @param x The initial x-coordinate of the player
	     * @param y The initial y-coordinate of the player
	     */
	    public Player(int x, int y) {
	        super(x, y);
	    }
	}

	/*
	 * Represents the enemy in the game.
	 */
	public static class Enemy extends Position {
	    /*
	     * Constructor for Enemy.
	     *
	     * @param x The initial x-coordinate of the enemy
	     * @param y The initial y-coordinate of the enemy
	     */
	    public Enemy(int x, int y) {
	        super(x, y);
	    }
	}

	/*
	 * Enumeration of possible cell types in the game grid.
	 */
	public enum Cell {
	    EMPTY, WALL, CHEST, CLOCK, FINISH
	}
}