package GUI;

import java.io.IOException;

import controller.Controller;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import model.AchievementManager;
import progress.AchievementProgressManager;
import progress.ProgressManager;

/* Avvia l'applicazione e gestisce il passaggio
   tra la schermata iniziale e quella di gioco. */
public class Main
        extends Application
        implements GameNavigation
{
    private static final String SAVE_FILE_PATH =
            "progress.json";

    private static final String ACHIEVEMENTS_FILE_PATH =
            "achievements.json";

    private final ProgressManager progressManager =
            new ProgressManager();

    private final AchievementProgressManager
            achievementProgressManager =
            new AchievementProgressManager();

    private AchievementManager achievementManager;

    private Stage stage;
    private Controller controller;
    private GameView gameView;

    // Avvia l'applicazione JavaFX.
    public static void main(String[] args)
    {
        launch(args);
    }

    // Inizializza la finestra e mostra la schermata iniziale.
    @Override
    public void start(Stage stage)
    {
        this.stage = stage;
        stage.setTitle("SimCity");

        loadAchievements();

        showHomeScreen();
        stage.show();
    }

    /* Carica gli achievement globali. Se il file non esiste,
       AchievementProgressManager restituisce un manager vuoto. */
    private void loadAchievements()
    {
        try
        {
            achievementManager =
                    achievementProgressManager.load(
                            ACHIEVEMENTS_FILE_PATH
                    );
        }
        catch (IOException exception)
        {
            achievementManager =
                    new AchievementManager();

            System.err.println(
                    "Unable to load achievements: "
                            + exception.getMessage()
            );
        }
    }

    /* Mostra la schermata iniziale con i comandi per iniziare,
       caricare o riprendere una partita. */
    private void showHomeScreen()
    {
        closeGameView();

        Button startButton = new Button(
                controller == null
                        ? "Inizia simulazione"
                        : "Nuova partita"
        );

        Button loadButton = new Button(
                "Carica simulazione"
        );

        Label labelInfo = new Label(
                "Created by: Borsetto Daniele, "
                        + "Jiang Jiaxin Luisa, "
                        + "Rahman Fahmid, "
                        + "Zordan Pietro"
        );

        startButton.setPrefWidth(200);
        loadButton.setPrefWidth(200);

        // Avvia una nuova partita.
        startButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        controller = new Controller(achievementManager);
                        showGameScreen();
                    }
                }
        );

        // Carica una partita salvata.
        loadButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        loadGame();
                    }
                }
        );

        VBox layout = new VBox(
                15,
                startButton,
                loadButton
        );

        // Aggiunge il pulsante per riprendere una partita già iniziata.
        if (controller != null)
        {
            Button resumeButton = new Button(
                    "Riprendi partita"
            );

            resumeButton.setPrefWidth(200);
            resumeButton.setOnAction(
                    new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            showGameScreen();
                        }
                    }
            );

            layout.getChildren().add(
                    0,
                    resumeButton
            );
        }

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setCenter(layout);

        BorderPane.setAlignment(
                labelInfo,
                Pos.BOTTOM_RIGHT
        );

        BorderPane.setMargin(
                labelInfo,
                new Insets(10)
        );

        root.setBottom(labelInfo);

        Scene scene = new Scene(
                root,
                800,
                600
        );

        // Comando nascosto per gli sviluppatori: Command + Shift + A.
        scene.setOnKeyPressed(
                new EventHandler<KeyEvent>()
                {
                    @Override
                    public void handle(KeyEvent event)
                    {
                        if (event.isMetaDown()
                                && event.isShiftDown()
                                && event.getCode()
                                == KeyCode.A)
                        {
                            resetAchievements();
                            event.consume();
                        }
                    }
                }
        );

        stage.setScene(scene);
    }

    /* Carica la partita dal file e mostra un messaggio
       di errore se il caricamento non riesce. */
    private void loadGame()
    {
        try
        {
            controller = progressManager.loadGame(
                    SAVE_FILE_PATH,
                    achievementManager
            );

            showGameScreen();
        }
        catch (IOException
               | IllegalArgumentException
               | IllegalStateException exception)
        {
            Alert alert =
                    new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Loading error");
            alert.setHeaderText(
                    "Impossible to load the game"
            );
            alert.setContentText(
                    exception.getMessage()
            );
            alert.showAndWait();
        }
    }

    /* Reset nascosto degli achievement sia in memoria
       sia nel relativo file JSON. */
    private void resetAchievements()
    {
        try
        {
            achievementProgressManager.reset(
                    achievementManager,
                    ACHIEVEMENTS_FILE_PATH
            );

            System.out.println(
                    "Achievements reset successfully."
            );
        }
        catch (IOException exception)
        {
            System.err.println(
                    "Unable to reset achievements: "
                            + exception.getMessage()
            );
        }
    }

    // Crea e mostra la schermata principale del gioco.
    private void showGameScreen()
    {
        closeGameView();

        gameView = new GameView(
                controller,
                this,
                progressManager,
                SAVE_FILE_PATH
        );

        stage.setScene(gameView.getScene());
    }

    // Chiude la schermata di gioco e torna alla schermata iniziale.
    @Override
    public void returnToHome()
    {
        showHomeScreen();
    }

    // Inizia una nuova partita con un nuovo controller.
    @Override
    public void restartGame()
    {
        controller = new Controller(achievementManager);
        showGameScreen();
    }

    // Chiude la schermata di gioco attualmente aperta.
    private void closeGameView()
    {
        if (gameView != null)
        {
            gameView.close();
            gameView = null;
        }
    }

    // Chiude correttamente la schermata di gioco insieme all'applicazione.
    @Override
    public void stop()
    {
        if (achievementManager != null)
        {
            try
            {
                achievementProgressManager.save(
                        achievementManager,
                        ACHIEVEMENTS_FILE_PATH
                );
            }
            catch (IOException exception)
            {
                System.err.println(
                        "Unable to save achievements: "
                                + exception.getMessage()
                );
            }
        }

        closeGameView();
    }
}