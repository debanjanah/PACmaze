package application;

// Import necessary JavaFX classes for UI components and layouts
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Popup;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Import Java utility classes
import java.util.List;
import java.util.ArrayList;

//For the music
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

// The View class is responsible for creating and managing the game's user interface
public class View {
    // UI components
    private GridPane gameGrid; // Grid to hold the game board buttons
    private Button[][] gridButtons; // 2D array of buttons representing the game board
    private Button backToMenuButton; // Button to return to the main menu
    private Label livesLabel; // Label to display lives left
    private Label scoreLabel; // Label to display the current score
    private BorderPane mainLayout; // Main layout container for the game UI
    private VBox topPanel; // Top panel to hold score and lives
    private HBox livesPanel; // Panel to display player lives as images
    
    // Image resources for game elements
    private Image playerImage; 
    private Image enemyImage;
    private Image chestImage; 
    private Image clockImage;
    private Image finishImage;
    private List<ImageView> lifeImages; // List of ImageViews to display player lives
    
    private MediaPlayer mediaPlayer;
    
    // Constructor for the View class
    public View() {
        // Load all necessary images for the game
        playerImage = new Image("img/player.png");
        enemyImage = new Image("img/enemy.png");
        chestImage = new Image("img/coin.png");
        clockImage = new Image("img/clock.png");
        finishImage = new Image("img/exit.png");
        
        // Create the main layout container
        mainLayout = new BorderPane();
        
        // Create and set up the game grid
        gameGrid = new GridPane();
        gameGrid.setAlignment(Pos.CENTER);
        
        // Initialize the 2D array of grid buttons
        gridButtons = new Button[Model.SIZE][Model.SIZE];
        
        // Create buttons for each cell in the game grid
        for (int i = 0; i < Model.SIZE; i++) {
            for (int j = 0; j < Model.SIZE; j++) {
                Button btn = new Button();
                btn.setPrefSize(25, 25);
                btn.setMinSize(25, 25);
                btn.setMaxSize(25, 25);
                btn.setStyle("-fx-background-radius: 0;");  // Make buttons square
                gridButtons[i][j] = btn;
                gameGrid.add(btn, j, i);
            }
        }
        
        // Set up the top panel
        topPanel = new VBox(10);
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(30));
        
        // Create a status panel to hold score and lives
        HBox statusPanel = new HBox(20);
        statusPanel.setAlignment(Pos.CENTER);
        
        // Create labels for lives and score
        livesLabel = new Label("LIVES:");
        livesLabel.setStyle("-fx-text-fill: #5A5A5A;");
        scoreLabel = new Label("SCORE: 00");
        scoreLabel.getStyleClass().add("status");
        
        // Set up the lives panel with player images
        livesPanel = new HBox(5);
        livesPanel.setAlignment(Pos.CENTER_LEFT);
        lifeImages = new ArrayList<>();
        
        // Create 3 life images (player has 3 lives at start)
        for (int i = 0; i < 3; i++) {
            ImageView lifeImage = new ImageView(playerImage);
            lifeImage.setFitWidth(24);
            lifeImage.setFitHeight(24);
            lifeImages.add(lifeImage);
        }
        
        // Create a container for the lives label and images
        HBox livesContainer = new HBox(10);
        livesContainer.setAlignment(Pos.CENTER_LEFT);
        livesContainer.getChildren().addAll(livesLabel, livesPanel);
        livesPanel.getChildren().addAll(lifeImages);
        livesContainer.getStyleClass().add("status");
        
        // Create the "Back to Main Menu" button
        backToMenuButton = new Button("Back to Main Menu");
        backToMenuButton.getStyleClass().add("blue-button");
        
        // Add score label and lives container to the status panel
        statusPanel.getChildren().addAll(scoreLabel, livesContainer);
        topPanel.getChildren().add(statusPanel);

        // Set up the bottom control panel
        HBox controlPanel = new HBox(10);
        controlPanel.setAlignment(Pos.CENTER);
        controlPanel.setPadding(new Insets(50));
        controlPanel.getChildren().add(backToMenuButton);

        // Add all components to the main layout
        mainLayout.setTop(topPanel);
        mainLayout.setCenter(gameGrid);
        mainLayout.setBottom(controlPanel);
        
        // Apply CSS styles to the main layout
        mainLayout.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
    }
      
    // Method to update the view based on the current game state
    public void update(Model model) {
        Model.Cell[][] grid = model.getGrid();
        Model.Player player = model.getPlayer();
        Model.Enemy enemy = model.getEnemy();
        
        // Load game over and win images
        Image imgOver = new Image("img/over.png");
    	Image imgWin = new Image("img/win.png");

        // Update each cell in the grid
        for (int i = 0; i < Model.SIZE; i++) {
            for (int j = 0; j < Model.SIZE; j++) {
                Button btn = gridButtons[i][j];
                // Remove any existing custom styles
                btn.getStyleClass().removeAll("path-cell", "hover-cell");
                btn.setGraphic(null);
                
                // Set default style for empty cells
                btn.getStyleClass().add("empty-cell");
                                
                // Set wall style for wall cells
                if (grid[i][j] == Model.Cell.WALL) {
                    btn.getStyleClass().add("wall-cell");
                    continue;
                }
                
                ImageView imageView = null;
                
                // Set appropriate image for each cell type
                if (i == player.x && j == player.y) {
                    imageView = new ImageView(playerImage);
                } else if (i == enemy.x && j == enemy.y) {
                    imageView = new ImageView(enemyImage);
                } else {
                    switch (grid[i][j]) {
                        case CHEST:
                            imageView = new ImageView(chestImage);
                            break;
                        case CLOCK:
                            imageView = new ImageView(clockImage);
                            break;
                        case FINISH:
                            imageView = new ImageView(finishImage);
                            break;
                    }
                }
                
                // Set the image for the button if one was selected
                if (imageView != null) {
                    imageView.setFitWidth(23);
                    imageView.setFitHeight(23);
                    imageView.setPreserveRatio(true);
                    btn.setGraphic(imageView);
                }
            }
        }
        
        // Update the score display
        scoreLabel.setText("SCORE: " + model.getScore());
        
        // Update life images based on remaining lives
        int lives = model.getPlayerLives();
        for (int i = 0; i < 3; i++) {
            lifeImages.get(i).setOpacity(i < lives ? 1.0 : 0.3);
        }
        
        // Show life lost popup if a life was just lost
        if (model.wasLifeLost() && lives > 0) {
            showLifeLostPopup();
        }

        // Show game over or victory popup if the game has ended
        if (model.isGameOver()) {
            showGameEndPopup("GAME OVER!", "#950606", imgOver);
        } else if (model.isGameWon()) {
            showGameEndPopup("VICTORY!", "#2E6F40", imgWin);
        }
    }
    
    // Method to highlight the path found by the pathfinding algorithm
    public void highlightPath(List<Model.Position> path) {
        clearPathHighlight();  // Clear any existing highlights
        for (Model.Position pos : path) {
            gridButtons[pos.x][pos.y].getStyleClass().add("path-cell");
        }
    }

    // Method to clear the path highlight
    public void clearPathHighlight() {
        for (int i = 0; i < Model.SIZE; i++) {
            for (int j = 0; j < Model.SIZE; j++) {
                gridButtons[i][j].getStyleClass().remove("path-cell");
            }
        }
    }
        
    // Method to show the game end popup (either game over or victory)
    private void showGameEndPopup(String message, String color, Image img) {
        Stage popupStage = new Stage();
        popupStage.initOwner(mainLayout.getScene().getWindow());
        popupStage.setWidth(400);
        popupStage.setHeight(400);
        
        VBox popupVBox = new VBox(20);
        popupVBox.setAlignment(Pos.CENTER);
        popupVBox.setPadding(new Insets(20));
        
        // Set up the image for the popup
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(200);
        imgView.setPreserveRatio(true);    
        
        // Create and style the message label
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        
        // Create the home button
        Button homeButton = new Button("Home");
        homeButton.getStyleClass().add("blue-button");
        homeButton.setOnAction(e -> {
            backToMenuButton.fire(); // Simulate clicking the "Back to Main Menu" button
            popupStage.close();
        });
        
        // Add all elements to the popup
        popupVBox.getChildren().addAll(imgView, messageLabel, homeButton);
        
        // Set up the scene for the popup
        Scene popupScene = new Scene(popupVBox);
        popupScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        popupStage.setScene(popupScene);
        popupStage.setTitle("Game End");
        popupStage.show();
    }
    
    // Method to show the life lost popup
    private void showLifeLostPopup() {
        Stage popupStage = new Stage();
        popupStage.initOwner(mainLayout.getScene().getWindow());
        popupStage.setWidth(400);
        popupStage.setHeight(400);
        
        VBox popupVBox = new VBox(20);
        popupVBox.setAlignment(Pos.CENTER);
        popupVBox.setPadding(new Insets(20));
        
        // Set up the image for the popup
        Image img = new Image("img/lost.png");
        ImageView imgView = new ImageView(img);
        imgView.setFitWidth(200);
        imgView.setPreserveRatio(true);
        
        // Create and style the message label
        Label messageLabel = new Label("You've Lost a Life!");
        messageLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #950606;");
        
        // Create the continue button
        Button continueButton = new Button("Continue");
        continueButton.getStyleClass().add("blue-button");
        continueButton.setOnAction(e -> popupStage.close());
        
        // Add all elements to the popup
        popupVBox.getChildren().addAll(imgView, messageLabel, continueButton);
        
        // Set up the scene for the popup
        Scene popupScene = new Scene(popupVBox);
        popupScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        
        popupStage.setScene(popupScene);
        popupStage.setTitle("Life Lost");
        popupStage.show();
    }

    // Getter methods for accessing private fields
    public BorderPane getMainLayout() { return mainLayout; }
    public Button[][] getGridButtons() { return gridButtons; }
    public Button getBackToMenuButton() { return backToMenuButton; }
    public MediaPlayer getGameMusicPlayer() { return mediaPlayer; }
}