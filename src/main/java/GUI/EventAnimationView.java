// La classe EventAnimationView gestisce le animazioni visive degli eventi speciali di gioco (Tsunami e Hacker Attack).
// Controlla la propagazione dell'onda sulla griglia e l'effetto glitch con codice binario a schermo,
// bloccando l'interfaccia quando necessario

package GUI;

import Events.EventType;
import controller.Controller;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public final class EventAnimationView
{
    private static final int TSUNAMI_LINE_DELAY = 500;

    private final Controller controller;
    private final GridView gridView;
    private final Button nextTurnButton;
    private final VBox hackerAttackPanel;
    private final Label hackerBinaryCode;

    private boolean tsunamiAnimationStarted;
    private boolean tsunamiAnimationRunning;
    private boolean hackerAttackAnimationStarted;
    private Timeline tsunamiTimeline;
    private Timeline hackerCodeTimeline;

//Inizializza le dipendenze visive e costruisce il pannello grafico di avviso per l'attacco hacker (icona computer, testo e codice).
    public EventAnimationView(
            Controller controller,
            GridView gridView,
            Button nextTurnButton)
    {
        if (controller == null
                || gridView == null
                || nextTurnButton == null)
        {
            throw new IllegalArgumentException(
                    "Animation dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.gridView = gridView;
        this.nextTurnButton = nextTurnButton;

        Rectangle monitorScreen = new Rectangle(70, 42);
        monitorScreen.setArcWidth(8);
        monitorScreen.setArcHeight(8);
        monitorScreen.setFill(Color.web("#263238"));
        monitorScreen.setStroke(Color.BLACK);
        monitorScreen.setStrokeWidth(2);

        Label terminalSymbol = new Label(">_");
        terminalSymbol.setStyle(
                "-fx-text-fill: #00e676;"
                        + "-fx-font-family: monospace;"
                        + "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        StackPane monitor =
                new StackPane(monitorScreen, terminalSymbol);

        Rectangle monitorStand =
                new Rectangle(8, 8, Color.web("#263238"));

        Rectangle monitorBase =
                new Rectangle(30, 4, Color.web("#263238"));

        VBox hackerComputerIcon = new VBox(
                0,
                monitor,
                monitorStand,
                monitorBase
        );
        hackerComputerIcon.setAlignment(Pos.CENTER);

        Label hackerAttackText = new Label("Hacker Attack");
        hackerAttackText.setStyle(
                "-fx-text-fill: red;"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );

        hackerBinaryCode = new Label("01001101 10110100");
        hackerBinaryCode.setStyle(
                "-fx-text-fill: #008a36;"
                        + "-fx-font-family: monospace;"
                        + "-fx-font-size: 12px;"
        );

        hackerAttackPanel = new VBox(
                4,
                hackerComputerIcon,
                hackerAttackText,
                hackerBinaryCode
        );
        hackerAttackPanel.setAlignment(Pos.CENTER);
        hackerAttackPanel.setStyle(
                "-fx-background-color: #e9eef5;"
                        + "-fx-border-color: red;"
                        + "-fx-border-width: 2px;"
                        + "-fx-padding: 12px;"
                        + "-fx-background-radius: 6px;"
                        + "-fx-border-radius: 6px;"
        );
        hackerAttackPanel.setMaxSize(
                Region.USE_PREF_SIZE,
                Region.USE_PREF_SIZE
        );
        hackerAttackPanel.setVisible(false);
        hackerAttackPanel.setMouseTransparent(true);
    }
//Sincronizza lo stato delle animazioni con il tipo di evento attualmente attivo nel controller.
    public void refresh()
    {
        updateTsunamiAnimation();
        updateHackerAttackAnimation();
    }
//Verifica se l'evento Tsunami e attivo nel controller e ne avvia o resetta lo stato di animazione.
    private void updateTsunamiAnimation()
    {
        boolean tsunamiActive =
                controller.getActiveEventType()
                        == EventType.TSUNAMI;

        if (tsunamiActive && !tsunamiAnimationStarted)
        {
            tsunamiAnimationStarted = true;
            startTsunamiAnimation();
        }
        else if (!tsunamiActive)
        {
            tsunamiAnimationStarted = false;
        }
    }
//Crea e avvia la Timeline per l'avanzamento sequenziale dello Tsunami, disabilitando temporaneamente il pulsante del turno successivo.
    private void startTsunamiAnimation()
    {
        tsunamiAnimationRunning = true;
        nextTurnButton.setDisable(true);

        final String direction =
                controller.getActiveTsunamiDirection();

        int advancementLength =
                controller.getActiveTsunamiAdvancementLength();

        tsunamiTimeline = new Timeline();

        for (int step = 0;
             step < advancementLength;
             step++)
        {
            final int currentStep = step;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(
                            (step + 1) * TSUNAMI_LINE_DELAY
                    ),
                    new EventHandler<ActionEvent>()
                    {
                        @Override
                        public void handle(ActionEvent event)
                        {
                            showTsunamiLine(
                                    direction,
                                    currentStep
                            );
                        }
                    }
            );

            tsunamiTimeline.getKeyFrames().add(frame);
        }

        KeyFrame resetFrame = new KeyFrame(
                Duration.millis(
                        (advancementLength + 2)
                                * TSUNAMI_LINE_DELAY
                ),
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        clearTsunamiAnimation();
                    }
                }
        );

        tsunamiTimeline.getKeyFrames().add(resetFrame);
        tsunamiTimeline.play();
    }
//Evidenzia sulla griglia di gioco la riga o colonna colpita dallo Tsunami in base alla direzione e allo step di avanzamento.
    private void showTsunamiLine(
            String direction,
            int step)
    {
        if ("UP".equals(direction))
        {
            gridView.showTsunamiRow(step);
        }
        else if ("DOWN".equals(direction))
        {
            int row =
                    controller.getNumberOfRows() - 1 - step;
            gridView.showTsunamiRow(row);
        }
        else if ("LEFT".equals(direction))
        {
            gridView.showTsunamiColumn(step);
        }
        else if ("RIGHT".equals(direction))
        {
            int column =
                    controller.getNumberOfColumns() - 1 - step;
            gridView.showTsunamiColumn(column);
        }
    }
//Ripristina lo stato iniziale della griglia dopo il passaggio dello Tsunami e riabilita l'interazione con il pulsante del turno.
    private void clearTsunamiAnimation()
    {
        gridView.clearTsunami();
        tsunamiAnimationRunning = false;

        controller.startTsunamiReconstruction();

        gridView.refresh();
        nextTurnButton.setDisable(false);
    }
//Gestisce la visibilità e lo stato di avanzamento dell'animazione dell'attacco hacker in base all'evento attivo.
    private void updateHackerAttackAnimation()
    {
        boolean hackerAttackActive =
                controller.getActiveEventType()
                        == EventType.HACKER_ATTACK;

        if (hackerAttackActive
                && !hackerAttackAnimationStarted)
        {
            hackerAttackAnimationStarted = true;
            startHackerAttackAnimation();
        }
        else if (!hackerAttackActive)
        {
            stopHackerCodeAnimation();
            hackerAttackAnimationStarted = false;
            hackerAttackPanel.setVisible(false);
            hackerAttackPanel.setOpacity(1.0);
        }
    }
//Mostra il pannello dell'attacco hacker eseguendo un effetto visivo di sfarfallio (glitch) e avviando la generazione del codice binario.
    private void startHackerAttackAnimation()
    {
        hackerAttackPanel.setVisible(true);
        hackerAttackPanel.setOpacity(0.0);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.ZERO,
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(120),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(240),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.2
                        )
                ),
                new KeyFrame(
                        Duration.millis(360),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                ),
                new KeyFrame(
                        Duration.millis(500),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                0.4
                        )
                ),
                new KeyFrame(
                        Duration.millis(650),
                        new KeyValue(
                                hackerAttackPanel.opacityProperty(),
                                1.0
                        )
                )
        );

        timeline.play();
        startHackerCodeAnimation();
    }
//Avvia un ciclo indefinito (Timeline) che aggiorna periodicamente la stringa di codice binario mostrata nel pannello hacker.
    private void startHackerCodeAnimation()
    {
        hackerCodeTimeline = new Timeline(
                new KeyFrame(
                        Duration.millis(180),
                        new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                hackerBinaryCode.setText(
                                        generateBinaryCode()
                                );
                            }
                        }
                )
        );

        hackerCodeTimeline.setCycleCount(Timeline.INDEFINITE);
        hackerCodeTimeline.play();
    }
//Ferma e distrugge la Timeline responsabile dell'aggiornamento del codice binario.
    private void stopHackerCodeAnimation()
    {
        if (hackerCodeTimeline != null)
        {
            hackerCodeTimeline.stop();
            hackerCodeTimeline = null;
        }
    }
//Genera una stringa casuale di 16 cifre binarie (0 e 1) formattata in due blocchi separati da uno spazio.
    private String generateBinaryCode()
    {
        StringBuilder code = new StringBuilder();

        for (int index = 0; index < 16; index++)
        {
            if (index == 8)
            {
                code.append(" ");
            }

            if (Math.random() < 0.5)
            {
                code.append("0");
            }
            else
            {
                code.append("1");
            }
        }

        return code.toString();
    }
//Restituisce true se l'animazione dello Tsunami e attualmente in corso sulla griglia, false altrimenti.
    public boolean isTsunamiAnimationRunning()
    {
        return tsunamiAnimationRunning;
    }

//Restituisce il pannello VBox dell'attacco hacker per permetterne l'inserimento nell'interfaccia principale.
    public VBox getHackerAttackPanel()
    {
        return hackerAttackPanel;
    }

//Interrompe immediatamente tutte le animazioni in corso, ripristinando lo stato di default dei componenti e riabilitando i comandi.
    public void stop()
    {
        if (tsunamiTimeline != null)
        {
            tsunamiTimeline.stop();
            tsunamiTimeline = null;
        }

        stopHackerCodeAnimation();
        tsunamiAnimationRunning = false;
        tsunamiAnimationStarted = false;
        hackerAttackAnimationStarted = false;
        nextTurnButton.setDisable(false);
        hackerAttackPanel.setVisible(false);
    }
}
