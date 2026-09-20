package GUI;

import Events.EventType;
import controller.Controller;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

// La classe GameStatusView gestisce la visualizzazione dello stato generale della partita.
// Crea e aggiorna il pannello informativo laterale (budget, popolazione, felicità, tick) e il banner degli eventi attivi.
public final class GameStatusView
{
    private final Controller controller;
    private final Label budgetLabel = new Label();
    private final Label statsLabel = new Label();
    private final Label policyLabel = new Label();
    private final Label tickLabel = new Label();
    private final Label eventBanner = new Label();
    private final VBox infoPanel;

    // Costruisce la vista dello stato di gioco inizializzando i componenti grafici e il layout del pannello laterale.
    public GameStatusView(Controller controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException("controller.Controller cannot be null");
        }

        this.controller = controller;
        configureLabels();

        // ---------- BARRA STATISTICHE (VERTICALE A SINISTRA) ----------
        infoPanel = new VBox(
                10,
                tickLabel,
                budgetLabel,
                statsLabel,
                policyLabel
        );
        infoPanel.setPadding(new Insets(15));
        infoPanel.setStyle(
                "-fx-background-color: #f4f4f4;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #ddd;"
                        + "-fx-border-radius: 8;"
        );
        infoPanel.setMinHeight(Region.USE_PREF_SIZE);
        VBox.setVgrow(statsLabel, Priority.NEVER); // non farla comprimere
    }

    // Configura lo stile grafico, i font e le proprieta di visibilita delle etichette di testo e del banner.
    private void configureLabels()
    {
        budgetLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        statsLabel.setStyle("-fx-font-size: 13px;");
        statsLabel.setMinHeight(Region.USE_PREF_SIZE);
        statsLabel.setWrapText(true);
        policyLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic;");
        tickLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // ---------- BANNER EVENTI (STILE PULITO) ----------
        eventBanner.setStyle(
                "-fx-background-color: #f8d7da; -fx-text-fill: #721c24; "
                        + "-fx-font-size: 14px; -fx-font-weight: bold; "
                        + "-fx-padding: 10px 15px; -fx-border-color: #f5c6cb; "
                        + "-fx-border-width: 1px; -fx-border-radius: 5px; "
                        + "-fx-background-radius: 5px;"
        );
        eventBanner.setVisible(false);
        eventBanner.managedProperty().bind(eventBanner.visibleProperty()); // Collassa lo spazio se invisibile
        eventBanner.setWrapText(true);
        eventBanner.setAlignment(Pos.CENTER);
    }

    // Aggiorna la visualizzazione del budget corrente, evidenziandolo in rosso durante una crisi energetica.
    public void updateBudget()
    {
        budgetLabel.setText("Budget: " + controller.getBudget() + " €");

        // Colora il testo di rosso durante la crisi energetica
        if (controller.getActiveEventType() == EventType.ENERGY_CRISIS)
        {
            budgetLabel.setTextFill(Color.RED);
        }
        else
        {
            budgetLabel.setTextFill(Color.BLACK);
        }
    }

    // Aggiorna le statistiche della città (popolazione, inquinamento, economia, felicita, occupazione e politica attiva).
    public void updateStatistics()
    {
        int unemployed = controller.getUnemployed();
        String employmentLine = unemployed < 0
                ? "Available jobs: " + Math.abs(unemployed)
                : "Unemployed: " + unemployed;

        statsLabel.setText(
                "Population: " + controller.getPopulation() + "\n"
                        + "Pollution: " + controller.getPollution() + "\n"
                        + "Economy: " + controller.getEconomy() + "\n"
                        + "Happiness: " + controller.getHappiness() + "\n"
                        + "Energy: " + controller.getEnergyConsumed()
                        + " / " + controller.getEnergyAvailable() + "\n"
                        + employmentLine
        );
        policyLabel.setText("Policy: " + controller.getCurrentPolicyName());
    }

    // Aggiorna l'etichetta indicante il turno di gioco (tick) corrente.
    public void updateTick()
    {
        tickLabel.setText("Tick: " + controller.getCurrentTick());
    }

    // Controlla se c'è un evento in corso e aggiorna il banner mostrando il nome e l'eventuale direzione dello Tsunami.
    public void updateEventBanner()
    {
        EventType eventType = controller.getActiveEventType();

        if (eventType != null)
        {
            String bannerText = "Evento in corso:\n"
                    + eventType.getDisplayName().toUpperCase();

            if (eventType == EventType.TSUNAMI)
            {
                bannerText = bannerText + "\nProvenienza: "
                        + getTsunamiCardinalDirection(
                        controller.getActiveTsunamiDirection()
                );
            }

            eventBanner.setText(bannerText);
            eventBanner.setVisible(true);
        }
        else
        {
            eventBanner.setVisible(false);
        }
    }

    // Converte il codice di direzione dello Tsunami (UP, DOWN, LEFT, RIGHT) nei corrispondenti punti cardinali in italiano.
    private String getTsunamiCardinalDirection(String direction)
    {
        if ("UP".equals(direction)) return "Nord";
        if ("DOWN".equals(direction)) return "Sud";
        if ("LEFT".equals(direction)) return "Ovest";
        if ("RIGHT".equals(direction)) return "Est";
        return "Sconosciuta";
    }

    // Restituisce il pannello VBox contenente tutte le informazioni di stato della citta.
    public VBox getInfoPanel()
    {
        return infoPanel;
    }

    // Restituisce l'etichetta del banner eventi per la sua collocazione nell'interfaccia.
    public Label getEventBanner()
    {
        return eventBanner;
    }
}