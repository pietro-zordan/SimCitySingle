package GUI;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import controller.Controller;
import controller.GameObserver;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.ConstructionType;
import policies.PolicyType;
import progress.ProgressManager;

// La classe GameView rappresenta la schermata principale del gioco e implementa GameObserver per reagire ai cambiamenti del modello.
// Assembla tutte le sotto-viste (griglia, statistiche, grafici, barra costruzioni, animazioni) e gestisce la pulsantiera di controllo.
public final class GameView implements GameObserver
{
    private final Controller controller;
    private final GameNavigation navigation;
    private final ProgressManager progressManager;
    private final String saveFilePath;
    private final GridView gridView;
    private final ChartView chartView;
    private final GameStatusView statusView;
    private final ConstructionToolbarView constructionToolbarView;
    private final EventAnimationView eventAnimationView;

    private final Label toast = new Label();

    private final Button nextTurnButton =
            new Button("Next Turn");

    private final Button loanButton =
            new Button("Richiedi prestito");

    private final Label loanLimitLabel =
            new Label();

    private final TextField loanAmountField =
            new TextField();

    private final Button confirmLoanButton =
            new Button("Conferma");

    private final Button demolitionButton =
            new Button("Demolish");

    private final VBox loanRequestBox =
            new VBox(
                    5,
                    loanLimitLabel,
                    loanAmountField,
                    confirmLoanButton
            );

    private final Scene scene;
    private boolean observerRegistered;

    // Inizializza la vista di gioco istanziando le sotto-viste, configurando i componenti di controllo e registrandosi come observer.
    public GameView(
            Controller controller,
            GameNavigation navigation,
            ProgressManager progressManager,
            String saveFilePath)
    {
        if (controller == null
                || navigation == null
                || progressManager == null
                || saveFilePath == null)
        {
            throw new IllegalArgumentException(
                    "Game view dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.navigation = navigation;
        this.progressManager = progressManager;
        this.saveFilePath = saveFilePath;

        configureToast();
        configureNextTurnButton();
        configureLoanButton();

        gridView = new GridView(
                controller,
                new Consumer<String>()
                {
                    @Override
                    public void accept(String message)
                    {
                        showToast(message);
                    }
                }
        );

        configureDemolitionButton();

        chartView = new ChartView(controller);
        statusView = new GameStatusView(controller);

        constructionToolbarView =
                new ConstructionToolbarView(
                        controller,
                        new Consumer<ConstructionType>()
                        {
                            @Override
                            public void accept(
                                    ConstructionType type)
                            {
                                gridView.setSelectedType(type);
                            }
                        }
                );

        eventAnimationView =
                new EventAnimationView(
                        controller,
                        gridView,
                        nextTurnButton
                );

        scene = createScene();

        initializeDisplayedState();

        controller.addObserver(this);
        observerRegistered = true;
    }

    // Configura lo stile grafico e lo stato iniziale dell'etichetta toast usata per le notifiche temporanee.
    private void configureToast()
    {
        toast.setStyle(
                "-fx-background-color: rgba(0,0,0,0.75);"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 12 24 12 24;"
                        + "-fx-background-radius: 8;"
                        + "-fx-font-size: 14px;"
        );

        toast.setVisible(false);
        toast.setMouseTransparent(true);
    }

    // Configura le dimensioni e la gestione del click sul pulsante di avanzamento del turno.
    private void configureNextTurnButton()
    {
        nextTurnButton.setMaxWidth(
                Double.MAX_VALUE
        );

        nextTurnButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        controller.updateOfOneTick();
                    }
                }
        );
    }

    // Configura il pulsante e il piccolo pannello utilizzato per richiedere un prestito.
    private void configureLoanButton()
    {
        loanButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loanButton.managedProperty()
                .bind(
                        loanButton.visibleProperty()
                );

        loanButton.setVisible(false);

        loanRequestBox.setAlignment(
                Pos.CENTER
        );

        loanRequestBox.setVisible(false);

        loanRequestBox.managedProperty()
                .bind(
                        loanRequestBox.visibleProperty()
                );

        loanAmountField.setPromptText(
                "Importo prestito"
        );

        loanAmountField.setMaxWidth(
                Double.MAX_VALUE
        );

        confirmLoanButton.setMaxWidth(
                Double.MAX_VALUE
        );

        // Mostra il campo per inserire l'importo.
        loanButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        int maxAmount =
                                controller
                                        .getMaxLoanAmount();

                        loanLimitLabel.setText(
                                "Massimo: "
                                        + maxAmount
                                        + " €"
                        );

                        loanAmountField.setText(
                                String.valueOf(
                                        maxAmount
                                )
                        );

                        loanRequestBox.setVisible(
                                true
                        );
                    }
                }
        );

        // Conferma la richiesta del prestito.
        confirmLoanButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        requestLoan();
                    }
                }
        );
    }

    // Legge l'importo inserito dall'utente e prova a richiedere il prestito.
    private void requestLoan()
    {
        try
        {
            int amount =
                    Integer.parseInt(
                            loanAmountField
                                    .getText()
                                    .trim()
                    );

            int maxAmount =
                    controller.getMaxLoanAmount();

            if (amount <= 0
                    || amount > maxAmount)
            {
                showToast(
                        "Inserisci un importo tra 1 e "
                                + maxAmount
                                + " €."
                );

                return;
            }

            if (controller.requestLoan(amount))
            {
                showToast(
                        "Prestito ricevuto: "
                                + amount
                                + " €. Tra 3 tick dovrai restituire "
                                + (amount * 3)
                                + " €."
                );

                loanRequestBox.setVisible(false);
            }
            else
            {
                showToast(
                        "Non puoi richiedere un prestito."
                );
            }
        }
        catch (NumberFormatException exception)
        {
            showToast(
                    "Inserisci un numero valido."
            );
        }
    }

    // Crea e struttura la scena JavaFX organizzando i pannelli laterali, la griglia centrale e la barra inferiore in un BorderPane.
    private Scene createScene()
    {
        Button changePolicyButton =
                createPolicyButton();

        Button saveButton =
                createSaveButton();

        Button restartButton =
                createRestartButton();

        Button homeButton =
                createHomeButton();

        // ---------- PANNELLO LATERALE SINISTRO ----------
        VBox leftPanel = new VBox(
                5,
                statusView.getInfoPanel(),
                saveButton,
                restartButton,
                homeButton,
                chartView.getView(),
                chartView.getSwitchButton()
        );

        leftPanel.setAlignment(
                Pos.TOP_CENTER
        );

        leftPanel.setPadding(
                new Insets(
                        10,
                        10,
                        10,
                        20
                )
        );

        leftPanel.setPrefWidth(330);

        // ---------- PULSANTI AZIONI ----------
        VBox actionButtons = new VBox(
                10,
                nextTurnButton,
                changePolicyButton,
                loanButton,
                loanRequestBox
        );

        actionButtons.setAlignment(
                Pos.CENTER
        );

        // ---------- PANNELLO LATERALE DESTRO ----------
        VBox rightPanel = new VBox(
                20,
                statusView.getEventBanner(),
                actionButtons
        );

        rightPanel.setAlignment(
                Pos.CENTER
        );

        rightPanel.setPadding(
                new Insets(
                        10,
                        20,
                        10,
                        10
                )
        );

        rightPanel.setPrefWidth(220);

        // ---------- GRIGLIA CENTRATA ----------
        StackPane gridWithAnimation =
                new StackPane(
                        gridView.getView(),
                        eventAnimationView
                                .getHackerAttackPanel()
                );

        StackPane.setAlignment(
                eventAnimationView
                        .getHackerAttackPanel(),
                Pos.CENTER
        );

        HBox centralContent =
                new HBox(
                        20,
                        leftPanel,
                        gridWithAnimation,
                        rightPanel
                );

        centralContent.setAlignment(
                Pos.CENTER
        );

        VBox bottomPanel =
                new VBox(
                        5,
                        gridView.getInfoLabel(),
                        constructionToolbarView
                                .getView()
                );

        bottomPanel.setAlignment(
                Pos.CENTER
        );

        bottomPanel.setPadding(
                new Insets(
                        5,
                        0,
                        10,
                        0
                )
        );

        // ---------- ORDINE GRAFICO ----------
        BorderPane root =
                new BorderPane();

        root.setCenter(
                centralContent
        );

        root.setBottom(
                bottomPanel
        );

        StackPane rootWithToast =
                new StackPane(
                        root,
                        toast
                );

        StackPane.setAlignment(
                toast,
                Pos.CENTER
        );

        return new Scene(
                rootWithToast,
                1400,
                900
        );
    }

    // Crea e restituisce il pulsante che apre il menu a tendina per il cambio della politica di gioco.
    private Button createPolicyButton()
    {
        final Button button =
                new Button(
                        "Change policy"
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showPolicyMenu(
                                button
                        );
                    }
                }
        );

        return button;
    }

    // Verifica la disponibilita del cambio politica e mostra il ContextMenu con le opzioni selezionabili.
    private void showPolicyMenu(
            Button policyButton)
    {
        if (!controller.canChangePolicy())
        {
            showToast(
                    "Cannot change policy now! Please wait another "
                            + controller
                            .getTicksToPolicyChange()
                            + " tick."
            );

            return;
        }

        List<PolicyType> policyTypes =
                List.of(
                        PolicyType.STANDARD,
                        PolicyType.ENVIRONMENTAL,
                        PolicyType.INDUSTRIAL
                );

        ContextMenu policyMenu =
                new ContextMenu();

        for (final PolicyType policyType
                : policyTypes)
        {
            MenuItem item =
                    new MenuItem(
                            policyType
                                    .getDisplayName()
                    );

            item.setOnAction(
                    new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(
                                ActionEvent event)
                        {
                            changePolicy(
                                    policyType
                            );
                        }
                    }
            );

            policyMenu.getItems()
                    .add(item);
        }

        policyMenu.show(
                policyButton,
                Side.BOTTOM,
                30,
                0
        );
    }

    // Tenta di cambiare la politica attiva tramite il controller e notifica all'utente l'esito dell'operazione.
    private void changePolicy(
            PolicyType policyType)
    {
        if (controller.changePolicy(
                policyType))
        {
            showToast(
                    "Policy changed to: "
                            + policyType
                            .getDisplayName()
            );
        }
        else
        {
            showToast(
                    "Impossible to change the Policy."
            );
        }
    }

    private void configureDemolitionButton()
    {
        demolitionButton.setMaxWidth(
                Double.MAX_VALUE
        );

        demolitionButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        gridView.activateDemolition();
                    }
                }
        );
    }

    // Crea e restituisce il pulsante per avviare il salvataggio della partita su file.
    private Button createSaveButton()
    {
        Button button =
                new Button(
                        "Save Game"
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        saveGame();
                    }
                }
        );

        return button;
    }

    // Esegue il salvataggio dello stato di gioco tramite il progressManager mostrando un messaggio toast di successo o errore.
    private void saveGame()
    {
        try
        {
            progressManager.saveGame(
                    controller,
                    saveFilePath
            );

            showToast(
                    "Game saved successfully"
            );
        }
        catch (IOException exception)
        {
            showToast(
                    "Saving error: "
                            + exception.getMessage()
            );
        }
    }

    // Crea e restituisce il pulsante per ricominciare la partita senza salvare i progressi correnti.
    private Button createRestartButton()
    {
        Button button =
                new Button(
                        "Restart Game (without saving)"
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        navigation.restartGame();
                    }
                }
        );

        return button;
    }

    // Crea e restituisce il pulsante per abbandonare la partita corrente e tornare alla schermata principale.
    private Button createHomeButton()
    {
        Button button =
                new Button(
                        "Return to home"
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        navigation.returnToHome();
                    }
                }
        );

        return button;
    }

    // Esegue il primo aggiornamento dei valori grafici di tutte le sotto-viste all'avvio della schermata.
    private void initializeDisplayedState()
    {
        statusView.updateBudget();
        statusView.updateStatistics();
        statusView.updateTick();

        constructionToolbarView
                .refreshCosts();

        constructionToolbarView
                .refreshAvailability();

        refreshLoanButton();

        gridView.refresh();
    }

    // Aggiorna in modo asincrono nel JavaFX Application Thread tutti i componenti grafici in seguito alle notifiche del modello.
    @Override
    public void refreshGameView()
    {
        Platform.runLater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        statusView.updateBudget();

                        eventAnimationView.refresh();

                        if (!eventAnimationView
                                .isTsunamiAnimationRunning())
                        {
                            gridView.refresh();
                        }

                        statusView.updateStatistics();

                        constructionToolbarView
                                .refreshCosts();

                        constructionToolbarView
                                .refreshAvailability();

                        refreshLoanButton();

                        statusView.updateTick();
                        statusView.updateEventBanner();

                        chartView.refresh();
                    }
                }
        );
    }

    // Aggiorna la disponibilita del pulsante del prestito.
    private void refreshLoanButton()
    {
        boolean canRequestLoan =
                controller.canRequestLoan();

        loanButton.setVisible(
                canRequestLoan
        );

        if (!canRequestLoan)
        {
            loanRequestBox.setVisible(
                    false
            );
        }
    }

    // Mostra a schermo un messaggio informativo temporaneo della durata di 2.5 secondi.
    private void showToast(
            String message)
    {
        toast.setText(message);
        toast.setVisible(true);

        PauseTransition pause =
                new PauseTransition(
                        Duration.seconds(2.5)
                );

        pause.setOnFinished(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        toast.setVisible(false);
                    }
                }
        );

        pause.play();
    }

    // Restituisce l'oggetto Scene principale contenente l'intera interfaccia di gioco.
    public Scene getScene()
    {
        return scene;
    }

    // Rimuove la vista dal ruolo di observer e arresta le animazioni attive per rilasciare correttamente le risorse.
    public void close()
    {
        if (observerRegistered)
        {
            controller.removeObserver(this);
            observerRegistered = false;
        }

        eventAnimationView.stop();
    }
}