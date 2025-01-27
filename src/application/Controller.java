package application;

import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.function.Consumer;

public class Controller {
    // Model instance to handle game logic
    private Model model;
    // View instance to handle UI updates
    private View view;
    // Callback function to return to the main menu
    private Consumer<Void> onBackToMenu;

    /*
     * Constructor for the Controller class.
     * @param model The Model instance handling game logic
     * @param view The View instance handling UI
     * @param onBackToMenu Callback function to return to main menu
     */
    public Controller(Model model, View view, Consumer<Void> onBackToMenu) {
        this.model = model;
        this.view = view;
        this.onBackToMenu = onBackToMenu;
        
        // Set up event handlers for user interactions
        setupEventHandlers();
    }

    /*
     * Sets up all event handlers for user interactions.
     */
    private void setupEventHandlers() {
        // Set up "Back to Menu" button action
        view.getBackToMenuButton().setOnAction(e -> onBackToMenu.accept(null));
        
        // Get the grid buttons from the view
        Button[][] gridButtons = view.getGridButtons();
        // Set up event handlers for each grid button
        for (int i = 0; i < Model.SIZE; i++) {
            for (int j = 0; j < Model.SIZE; j++) {
                final int row = i;
                final int col = j;
                // Set click event handler
                gridButtons[i][j].setOnAction(e -> handleGridClick(row, col));
                // Set mouse enter event handler for hover effect
                gridButtons[i][j].setOnMouseEntered(e -> handleMouseEnter(row, col));
                // Set mouse exit event handler to clear hover effect
                gridButtons[i][j].setOnMouseExited(e -> handleMouseExit());
            }
        }

        // Add key press event handler for backspace (to undo moves)
        view.getMainLayout().setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE) {
                undoPlayerMove();
            }
        });

        // Ensure the layout can receive focus for key events
        view.getMainLayout().setFocusTraversable(true);
    }

    /*
     * Handles the click event on a grid button.
     * @param row The row of the clicked button
     * @param col column of the clicked button
     */
    private void handleGridClick(int row, int col) {
        Model.Position clickedPosition = new Model.Position(row, col);
        // Attempt to move the player to the clicked position
        if (model.movePlayerToPosition(clickedPosition)) {
            // Update the view if the move was successful
            view.update(model);
            // End the player's turn
            endTurn();
        }
    }

    /*
     * Handles the mouse enter event on a grid button.
     * @param row The row of the hovered button
     * @param col The column of the hovered button
     */
    private void handleMouseEnter(int row, int col) {
        Model.Position hoverPosition = new Model.Position(row, col);
        // Check if the hovered position is a valid move target
        if (model.isValidMoveTarget(hoverPosition)) {
            // Calculate the path to the hovered position
            List<Model.Position> path = model.calculatePathTo(hoverPosition);
            // Highlight the calculated path
            view.highlightPath(path);
        }
    }

    /*
     * Handles the mouse exit event on a grid button.
     */
    private void handleMouseExit() {
        // Clear any highlighted path when the mouse exits a button
        view.clearPathHighlight();
    }

    /*
     * Ends the player's turn and processes the enemy's turn.
     */
    private void endTurn() {
        // Move the enemy
        model.moveEnemy();
        // Update the view after enemy movement
        view.update(model);
        // Clear any highlighted paths
        view.clearPathHighlight();
        
        // Check for game over or win conditions
//        if (model.isGameOver() || model.isGameWon()) {
//        }
    }

    /*
     * Undoes the player's last move.
     */
    public void undoPlayerMove() {
        // Attempt to undo the player's move
        if (model.undoPlayerMove()) {
            // Update the view if the undo was successful
            view.update(model);
        }
    }
    
}