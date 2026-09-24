package GUI;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import audio.SoundManager;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Achievement;
import model.ConstructionType;
import model.FreemasonryChoice;
import policies.PolicyType;
import progress.ProgressManager;

// La classe GameView rappresenta la schermata principale del gioco e implementa GameObserver per reagire ai cambiamenti del modello.
// Assembla tutte le sotto-viste (griglia, statistiche, grafici, barra costruzioni, animazioni) e gestisce la pulsantiera di controllo.
public final class GameView implements GameObserver
{
    private final Controller controller;
    private final GameNavigation navigation;
    private final ProgressManager progressManager;
    private final SoundManager soundManager;
    private final String saveFilePath;
    private final GridView gridView;
    private final ChartView chartView;
    private final GameStatusView statusView;
    private final ConstructionToolbarView constructionToolbarView;
    private final LoanView loanView;
    private final EventAnimationView eventAnimationView;
    private final GameOverView gameOverView;
    private final FreemasonryInvitationView invitationView;
    private final RaEyeView raEyeView = new RaEyeView();
    private BorderPane gameRoot;

    private final Label toast = new Label();

    private final Button nextTurnButton =
            new Button("Next Turn");

    private final Button tsunamiInsuranceButton =
            new Button("Tsunami insurance");

    private final Label tsunamiInsuranceInfoLabel =
            new Label();

    private final Button confirmTsunamiInsuranceButton =
            new Button("Apply insurance");

    private final Button cancelTsunamiInsuranceButton =
            new Button("Cancel");

    private final HBox tsunamiInsuranceActions =
            new HBox(
                    5,
                    confirmTsunamiInsuranceButton,
                    cancelTsunamiInsuranceButton
            );

    private final VBox tsunamiInsuranceBox =
            new VBox(
                    5,
                    tsunamiInsuranceInfoLabel,
                    tsunamiInsuranceActions
            );

    private final Label nuclearFireProtectionInfoLabel = new Label();
    private final Button confirmNuclearFireProtectionButton = new Button("Apply protection");
    private final Button cancelNuclearFireProtectionButton = new Button("Not now");
    private final HBox nuclearFireProtectionActions = new HBox(
            5,
            confirmNuclearFireProtectionButton,
            cancelNuclearFireProtectionButton
    );
    private final VBox nuclearFireProtectionBox = new VBox(
            5,
            nuclearFireProtectionInfoLabel,
            nuclearFireProtectionActions
    );

    private int dismissedNuclearProtectionCount = -1;

    private final MissileDefenseView missileDefenseView;

    private final Button demolitionButton =
            new Button("Demolish");

    private final Scene scene;
    private boolean observerRegistered;
    private boolean closed;

    // Inizializza la vista di gioco istanziando le sotto-viste, configurando i componenti di controllo e registrandosi come observer.
    public GameView(
            Controller controller,
            GameNavigation navigation,
            ProgressManager progressManager,
            String saveFilePath,
            SoundManager soundManager)
    {
        if (controller == null
                || navigation == null
                || progressManager == null
                || saveFilePath == null
                || soundManager == null)
        {
            throw new IllegalArgumentException(
                    "Game view dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.navigation = navigation;
        gameOverView = new GameOverView(navigation);
        this.progressManager = progressManager;
        this.saveFilePath = saveFilePath;
        this.soundManager = soundManager;

        configureToast();
        configureNextTurnButton();

        loanView = new LoanView(controller, new Consumer<String>()
        {
            @Override
            public void accept(String message)
            {
                showToast(message);
            }
        });

        configureTsunamiInsuranceButton();
        configureNuclearFireProtection();
        missileDefenseView = new MissileDefenseView(
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

        gridView = new GridView(
                controller,
                new Consumer<String>()
                {
                    @Override
                    public void accept(String message)
                    {
                        showToast(message);
                    }
                },
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        soundManager.playDemolitionSound();
                    }
                },
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        soundManager.playPlacementSound();
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
                        nextTurnButton,
                        soundManager
                );

        invitationView = new FreemasonryInvitationView(
                new Consumer<FreemasonryChoice>()
                {
                    @Override
                    public void accept(FreemasonryChoice choice)
                    {
                        if (controller.chooseFreemasonry(choice))
                        {
                            raEyeView.refresh(
                                    controller.getFreemasonryChoice()
                            );
                        }
                    }
                },
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        gameRoot.setDisable(
                                controller.isGameOver()
                        );
                    }
                }
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
                        boolean criminalActivityCreated =
                                controller.updateOfOneTick();

                        showInvitationIfNeeded();

                        int militaryRemovals =
                                controller
                                        .getRemovedTerroristicGroupsByMilitary();

                        int policeTerroristRemovals =
                                controller.getRemovedTerroristicGroups()
                                        - militaryRemovals;

                        if (controller.isActiveMissileIntercepted())
                        {
                            showToast(
                                    "Missile intercepted! Missile Defense: "
                                            + controller
                                            .getMissileDefenseHitsRemaining()
                                            + "/3"
                            );
                        }
                        else if (militaryRemovals > 0)
                        {
                            String groupText =
                                    militaryRemovals == 1
                                            ? " terrorist group!"
                                            : " terrorist groups!";

                            showToast(
                                    "Military base eliminated "
                                            + militaryRemovals
                                            + groupText
                            );
                        }
                        else if (controller.wasTerroristicGroupCreated())
                        {
                            showToast(
                                    "Warning: a terrorist group has appeared!"
                            );
                        }
                        else if (policeTerroristRemovals > 0
                                && controller.getRemovedCriminalActivities() > 0)
                        {
                            showToast(
                                    "Criminal activity and terrorist group eliminated by the police!"
                            );
                        }
                        else if (policeTerroristRemovals > 0)
                        {
                            showToast(
                                    "Terrorist group eliminated by the police!"
                            );
                        }
                        else if (controller.getRemovedCriminalActivities() > 0)
                        {
                            showToast(
                                    "Criminal activity eliminated by the police!"
                            );
                        }
                        else if (criminalActivityCreated)
                        {
                            showToast(
                                    "Attenzione: è comparsa un'attività criminale!"
                            );
                        }
                    }
                }
        );
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

        cancelDemolitionWhenUsed(nextTurnButton);
        cancelDemolitionWhenUsed(changePolicyButton);
        for (Button button : loanView.getButtons())
        {
            cancelDemolitionWhenUsed(button);
        }
        cancelDemolitionWhenUsed(tsunamiInsuranceButton);
        cancelDemolitionWhenUsed(confirmTsunamiInsuranceButton);
        cancelDemolitionWhenUsed(cancelTsunamiInsuranceButton);
        cancelDemolitionWhenUsed(confirmNuclearFireProtectionButton);
        cancelDemolitionWhenUsed(cancelNuclearFireProtectionButton);
        for (Button button : missileDefenseView.getButtons())
        {
            cancelDemolitionWhenUsed(button);
        }
        cancelDemolitionWhenUsed(saveButton);
        cancelDemolitionWhenUsed(restartButton);
        cancelDemolitionWhenUsed(homeButton);
        cancelDemolitionWhenUsed(chartView.getSwitchButton());

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
                eventAnimationView
                        .getHackerAttackPanel(),
                nextTurnButton,
                changePolicyButton,
                loanView.getActionButton(),
                loanView.getRequestBox(),
                tsunamiInsuranceButton,
                tsunamiInsuranceBox,
                nuclearFireProtectionBox,
                missileDefenseView.getActionButton(),
                missileDefenseView.getView(),
                demolitionButton
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
                                .getMissileShieldNode(),
                        eventAnimationView
                                .getMissileNode()
                );

        eventAnimationView
                .getMissileShieldNode()
                .layoutXProperty()
                .bind(
                        gridWithAnimation
                                .widthProperty()
                                .divide(2.0)
                );

        eventAnimationView
                .getMissileShieldNode()
                .layoutYProperty()
                .bind(
                        gridWithAnimation
                                .heightProperty()
                                .divide(2.0)
                );

        StackPane.setAlignment(
                eventAnimationView
                        .getMissileNode(),
                Pos.CENTER
        );

        VBox gridColumn = new VBox(
                4,
                raEyeView.getView(),
                gridWithAnimation
        );
        gridColumn.setAlignment(Pos.CENTER);

        HBox centralContent =
                new HBox(
                        20,
                        leftPanel,
                        gridColumn,
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
        gameRoot = new BorderPane();

        gameRoot.setCenter(
                centralContent
        );

        gameRoot.setBottom(
                bottomPanel
        );

        StackPane rootWithToast =
                new StackPane(
                        gameRoot,
                        toast,
                        gameOverView.getView(),
                        invitationView.getView()
                );

        StackPane.setAlignment(
                toast,
                Pos.CENTER
        );

        StackPane.setAlignment(
                gameOverView.getView(),
                Pos.CENTER
        );

        return new Scene(
                rootWithToast,
                1400,
                900
        );
    }

    private void cancelDemolitionWhenUsed(Button button)
    {
        button.addEventFilter(
                ActionEvent.ACTION,
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        gridView.cancelDemolition();
                    }
                }
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

    private void configureTsunamiInsuranceButton()
    {
        tsunamiInsuranceButton.setMaxWidth(
                Double.MAX_VALUE
        );

        tsunamiInsuranceButton.managedProperty()
                .bind(
                        tsunamiInsuranceButton
                                .visibleProperty()
                );

        tsunamiInsuranceButton.setVisible(false);

        tsunamiInsuranceBox.setAlignment(
                Pos.CENTER
        );

        tsunamiInsuranceBox.managedProperty()
                .bind(
                        tsunamiInsuranceBox
                                .visibleProperty()
                );

        tsunamiInsuranceBox.setVisible(false);

        tsunamiInsuranceInfoLabel.setWrapText(
                true
        );

        tsunamiInsuranceActions.setAlignment(
                Pos.CENTER
        );

        confirmTsunamiInsuranceButton.setMaxWidth(
                Double.MAX_VALUE
        );

        cancelTsunamiInsuranceButton.setMaxWidth(
                Double.MAX_VALUE
        );

        tsunamiInsuranceButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showTsunamiInsuranceConfirmation();
                    }
                }
        );

        confirmTsunamiInsuranceButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        applyTsunamiInsurance();
                    }
                }
        );

        cancelTsunamiInsuranceButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        tsunamiInsuranceBox.setVisible(
                                false
                        );
                    }
                }
        );
    }

    /*
     * Mostra la conferma direttamente nel pannello del gioco,
     * come avviene per il prestito, evitando finestre JavaFX separate.
     */
    private void showTsunamiInsuranceConfirmation()
    {
        int cost =
                controller.getTsunamiInsuranceCost();

        int coveredBuildings =
                controller
                        .getTsunamiInsuranceBuildingCount();

        int discount =
                controller
                        .getTsunamiInsuranceDiscountPercentage();

        String action =
                controller.isTsunamiInsuranceActive()
                        ? "Extend tsunami insurance"
                        : "Activate tsunami insurance";

        tsunamiInsuranceInfoLabel.setText(
                action
                        + "\nBuildings to cover: "
                        + coveredBuildings
                        + "\nBank discount: "
                        + discount
                        + "%\nCost: "
                        + cost
                        + " €"
        );

        tsunamiInsuranceBox.setVisible(
                true
        );
    }

    private void applyTsunamiInsurance()
    {
        int cost =
                controller.getTsunamiInsuranceCost();

        boolean extending =
                controller.isTsunamiInsuranceActive();

        if (controller.buyTsunamiInsurance())
        {
            tsunamiInsuranceBox.setVisible(
                    false
            );

            if (extending)
            {
                showToast(
                        "Tsunami insurance extended for "
                                + cost
                                + " €."
                );
            }
            else
            {
                showToast(
                        "Tsunami insurance activated for "
                                + cost
                                + " €."
                );
            }
        }
        else
        {
            showToast(
                    "Unable to apply tsunami insurance. Check your budget."
            );
        }
    }

    // Configura il messaggio automatico per la protezione avanzata delle centrali nucleari dagli incendi.
    private void configureNuclearFireProtection()
    {
        nuclearFireProtectionBox.setAlignment(Pos.CENTER);
        nuclearFireProtectionBox.managedProperty().bind(nuclearFireProtectionBox.visibleProperty());
        nuclearFireProtectionBox.setVisible(false);

        nuclearFireProtectionInfoLabel.setWrapText(true);
        nuclearFireProtectionActions.setAlignment(Pos.CENTER);

        confirmNuclearFireProtectionButton.setMaxWidth(Double.MAX_VALUE);
        cancelNuclearFireProtectionButton.setMaxWidth(Double.MAX_VALUE);

        confirmNuclearFireProtectionButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        applyNuclearFireProtection();
                    }
                }
        );

        cancelNuclearFireProtectionButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        dismissedNuclearProtectionCount =
                                controller.getNuclearPlantsNeedingFireProtectionCount();
                        nuclearFireProtectionBox.setVisible(false);
                    }
                }
        );
    }

    // Applica la protezione e chiude subito il messaggio dopo la risposta dell'utente.
    private void applyNuclearFireProtection()
    {
        int cost = controller.getNuclearFireProtectionCost();
        int plantsToProtect = controller.getNuclearPlantsNeedingFireProtectionCount();

        nuclearFireProtectionBox.setVisible(false);

        if (controller.buyNuclearFireProtection())
        {
            dismissedNuclearProtectionCount = -1;

            String plantText = "nuclear plants";
            if (plantsToProtect == 1)
            {
                plantText = "nuclear plant";
            }

            showToast(
                    "Advanced fire protection applied to "
                            + plantsToProtect
                            + " "
                            + plantText
                            + " for "
                            + cost
                            + " €."
            );
        }
        else
        {
            dismissedNuclearProtectionCount = plantsToProtect;
            showToast("Unable to apply advanced fire protection. Check your budget.");
        }
    }

    // Mostra la richiesta solo quando esistono banca e centrali nucleari ancora non protette.
    private void refreshNuclearFireProtection()
    {
        int plantsToProtect = controller.getNuclearPlantsNeedingFireProtectionCount();

        if (plantsToProtect == 0)
        {
            dismissedNuclearProtectionCount = -1;
        }

        if (!controller.canBuyNuclearFireProtection()
                || plantsToProtect == dismissedNuclearProtectionCount)
        {
            nuclearFireProtectionBox.setVisible(false);
            return;
        }

        int cost = controller.getNuclearFireProtectionCost();
        int discount = controller.getNuclearFireProtectionDiscountPercentage();

        String plantText = "nuclear plants";
        if (plantsToProtect == 1)
        {
            plantText = "nuclear plant";
        }

        nuclearFireProtectionInfoLabel.setText(
                "Apply advanced fire protection to "
                        + plantsToProtect
                        + " "
                        + plantText
                        + "?\nBank discount: "
                        + discount
                        + "%\nCost: "
                        + cost
                        + " €"
        );

        nuclearFireProtectionBox.setVisible(true);
    }

    private void configureDemolitionButton()
    {
        demolitionButton.setMaxWidth(
                Double.MAX_VALUE
        );

        demolitionButton.managedProperty()
                .bind(
                        demolitionButton.visibleProperty()
                );

        demolitionButton.setVisible(false);

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

    // Mostra il pulsante di demolizione solo se esiste almeno un'impresa edile.
    private void refreshDemolitionButton()
    {
        boolean constructionCompanyPresent = false;

        for (int row = 0;
             row < controller.getNumberOfRows()
                     && !constructionCompanyPresent;
             row++)
        {
            for (int column = 0;
                 column < controller.getNumberOfColumns();
                 column++)
            {
                Controller.CellState state =
                        controller.getCellState(
                                row,
                                column
                        );

                if (!state.empty()
                        && state.type()
                        == ConstructionType.CONSTRUCTION_COMPANY)
                {
                    constructionCompanyPresent = true;
                    break;
                }
            }
        }

        demolitionButton.setVisible(
                constructionCompanyPresent
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
        catch (IllegalStateException exception)
        {
            showToast(
                    exception.getMessage()
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

        loanView.refresh();
        refreshTsunamiInsuranceButton();
        refreshNuclearFireProtection();
        missileDefenseView.refresh();
        refreshDemolitionButton();

        gridView.refresh();
        raEyeView.refresh(controller.getFreemasonryChoice());
        showGameOverIfNeeded();

        // Anche una partita salvata al turno 500 deve mostrare la scelta.
        Platform.runLater(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showInvitationIfNeeded();
                    }
                }
        );
    }

    private void showInvitationIfNeeded()
    {
        if (!closed
                && !controller.isGameOver()
                && controller.isFreemasonryInvitationPending()
                && !invitationView.isShowing())
        {
            gameRoot.setDisable(true);
            invitationView.show();
        }
    }

    private void showGameOverIfNeeded()
    {
        if (controller.isGameOver())
        {
            gameRoot.setDisable(true);
            gameOverView.show();
        }
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
                        if (closed)
                        {
                            return;
                        }

                        statusView.updateBudget();

                        eventAnimationView.refresh();

                        if (!eventAnimationView
                                .isTsunamiAnimationRunning()
                                && !eventAnimationView
                                .isMissileAnimationRunning()
                                && !eventAnimationView
                                .isExplosionAnimationRunning())
                        {
                            gridView.refresh();
                        }

                        statusView.updateStatistics();

                        constructionToolbarView
                                .refreshCosts();

                        constructionToolbarView
                                .refreshAvailability();

                        loanView.refresh();
                        refreshTsunamiInsuranceButton();
                        refreshNuclearFireProtection();
                        missileDefenseView.refresh();
                        refreshDemolitionButton();

                        statusView.updateTick();
                        statusView.updateEventBanner();
                        raEyeView.refresh(
                                controller.getFreemasonryChoice()
                        );

                        chartView.refresh();

                        checkNewAchievements();

                        showGameOverIfNeeded();

                        showInvitationIfNeeded();
                    }
                }
        );
    }

    private void refreshTsunamiInsuranceButton()
    {
        boolean hasBanks =
                controller.hasBanks();

        tsunamiInsuranceButton.setVisible(
                hasBanks
        );

        if (!hasBanks)
        {
            tsunamiInsuranceBox.setVisible(
                    false
            );
            return;
        }

        boolean insuranceActive =
                controller
                        .isTsunamiInsuranceActive();

        boolean canApplyInsurance =
                controller
                        .canBuyTsunamiInsurance();

        tsunamiInsuranceButton.setDisable(
                !canApplyInsurance
        );

        if (!canApplyInsurance)
        {
            tsunamiInsuranceBox.setVisible(
                    false
            );
        }

        if (!insuranceActive)
        {
            tsunamiInsuranceButton.setText(
                    "Tsunami insurance"
            );
        }
        else if (canApplyInsurance)
        {
            tsunamiInsuranceButton.setText(
                    "Extend tsunami insurance"
            );
        }
        else
        {
            tsunamiInsuranceButton.setText(
                    "Tsunami insurance up to date"
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
                        Duration.seconds(4)
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
        closed = true;

        if (observerRegistered)
        {
            controller.removeObserver(this);
            observerRegistered = false;
        }

        eventAnimationView.stop();
        gridView.stop();
        invitationView.stop();
    }

    private void checkNewAchievements()
    {
        Achievement achievement = controller
                .consumeNewlyUnlockedAchievement();

        while (achievement != null)
        {
            soundManager.playAchievementSound();

            showToast("Achievement unlocked: "
                            + achievement.getTitle());

            achievement = controller
                            .consumeNewlyUnlockedAchievement();
        }
    }
}
