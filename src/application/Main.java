package application;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class Main extends Application {
    // The main stage of the application
    private Stage primaryStage;
    // The game model
    private Model model;
    // The size of the game window
    private int WINDOW_SIZE = 800;
    
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        // Show the start screen when the application launches
        showStartScreen();
    }
    
    //Displays the start screen of the game
    private void showStartScreen() {
        // Create a vertical box layout for the start screen
        VBox startLayout = new VBox(20);
        startLayout.setAlignment(Pos.CENTER);
        
        // Load and display the game logo
        Image logo = new Image("img/logo.png");
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(250); 
        logoView.setPreserveRatio(true);
        
        // Create and style the "PLAY" button
        Button playButton = new Button("PLAY");
        playButton.setStyle("-fx-font-size: 20px; -fx-min-width: 150px; -fx-min-height: 40px;");
        playButton.setOnAction(e -> showGameScreen());
        playButton.getStyleClass().add("blue-button");
        
        // Create and style the "ABOUT" button
        Button aboutButton = new Button("ABOUT");
        aboutButton.setStyle("-fx-font-size: 20px; -fx-min-width: 150px; -fx-min-height: 40px;");
        aboutButton.setOnAction(e -> showAboutScreen());
        aboutButton.getStyleClass().add("blue-button");
        
        // Add all elements to the start layout
        startLayout.getChildren().addAll(logoView, playButton, aboutButton);
        
        // Create a new scene with the start layout
        Scene scene = new Scene(startLayout, WINDOW_SIZE, WINDOW_SIZE);
        // Apply CSS styles to the scene
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setTitle("PAC Maze");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    //Displays the main game screen
    private void showGameScreen() {
        // Initialize a new game model
        model = new Model();
        // Create a new view for the game
        View view = new View();
        
        // Create a new controller, passing a callback to return to the menu
        Controller controller = new Controller(model, view, unused -> showStartScreen());
        
        // Perform initial update of the view
        view.update(model);
        
        initializeMusic();
        
        // Create a new scene with the game layout
        Scene gameScene = new Scene(view.getMainLayout(), WINDOW_SIZE, WINDOW_SIZE);
        primaryStage.setScene(gameScene);
        primaryStage.setTitle("PAC Maze - Game");
    }
    
    //Displays the about screen of the game
    private void showAboutScreen() {
        // Create a vertical box layout for the about screen
        VBox aboutLayout = new VBox(20);
        aboutLayout.setAlignment(Pos.CENTER);
        
        Image logo = new Image("img/logo.png");
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(250); 
        logoView.setPreserveRatio(true);
        
        // Create a vertical box for the text content
        VBox textContent = new VBox(10);
        textContent.setAlignment(Pos.CENTER);
        Text content = new Text(
        		"• Click adjacent cells to move (3 moves per turn)\n\n" +
        		"• Collect chests for points\n\n" +
        		"• Collect clocks to slow down the enemy\n\n" +
        		"• Avoid the enemy or lose a life\n\n" +
        		"• Undo your moves by pressing Back Space key!\n\n" +
        		"• Reach the finish point to win!\n\n" +
        		"• You have 3 lives - Good luck!\n\n"
            );        
        textContent.getChildren().add(content);
        textContent.getStyleClass().add("text");
        
        // Create and style the "Back to Main Menu" button
        Button backButton = new Button("Back to Main Menu");
        backButton.getStyleClass().add("blue-button");
        backButton.setOnAction(e -> showStartScreen());
        
        // Add all elements to the about layout
        aboutLayout.getChildren().addAll(logoView, textContent, backButton);
        
        // Create a new scene with the about layout
        Scene aboutScene = new Scene(aboutLayout, WINDOW_SIZE, WINDOW_SIZE);
        aboutScene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        primaryStage.setScene(aboutScene);
        primaryStage.setTitle("PAC Maze - About");
    }
    
    // Launch the application
    public static void main(String[] args) {
        launch(args);
    }
    
    
    public void initializeMusic() {
        try {
            // Load media from the resources folder using getResource
            String mediaPath = getClass().getResource("/res/music_maze.mp3").toExternalForm();

            // Initialize Media and MediaPlayer
            Media media = new Media(mediaPath);
            MediaPlayer mediaPlayer = new MediaPlayer(media);

            // Optionally, set the music to auto-play
            mediaPlayer.setAutoPlay(true);

            // Play the media
            mediaPlayer.play();
        } catch (Exception e) {
            System.err.println("Error loading media file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}