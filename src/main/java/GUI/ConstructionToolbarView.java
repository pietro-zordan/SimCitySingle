// La classe ConstructionToolbarView gestisce la barra degli strumenti per la selezione delle costruzioni.
// Crea e organizza visivamente i pulsanti per ogni tipo di edificio, ne mostra i relativi colori
// e aggiorna i costi di piazzamento in tempo reale

package GUI;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.ConstructionType;

//Costruisce la barra degli strumenti inizializzando il layout e i pulsanti per ogni tipo di costruzione definita.
public final class ConstructionToolbarView
{
    //crea la lista e gli assegna i valori degli enum degli edifici
    private static final List<ConstructionType> TYPES = List.of(
            ConstructionType.INDUSTRIAL,
            ConstructionType.RESIDENTIAL,
            ConstructionType.PARK,
            ConstructionType.ROAD,
            ConstructionType.POWER_PLANT,
            ConstructionType.COMMERCIAL,
            ConstructionType.BANK,
            ConstructionType.CONSTRUCTION_COMPANY,
            ConstructionType.POLICE_STATION,
            ConstructionType.NUCLEAR_PLANT,
            ConstructionType.WAST_TREATMENT_PLANT
    );

    private final Controller controller;
    private final Consumer<ConstructionType> selectionHandler;

    private final Map<ConstructionType, Label> costLabels =
            new EnumMap<>(ConstructionType.class);

    private final Map<ConstructionType, Button> buttons =
            new EnumMap<>(ConstructionType.class);

    private final Map<ConstructionType, Label> lockLabels =
            new EnumMap<>(ConstructionType.class);

    private final HBox view;
    private VBox nuclearPlantBox;

    //Restituisce l'etichetta testuale descrittiva associata a uno specifico tipo di costruzione.
    public ConstructionToolbarView(
            Controller controller,
            Consumer<ConstructionType> selectionHandler)
    {
        if (controller == null || selectionHandler == null)
        {
            throw new IllegalArgumentException(
                    "controller.Controller and selection handler cannot be null"
            );
        }

        this.controller = controller;
        this.selectionHandler = selectionHandler;

        // ---------- PANNELLO BOTTONI COSTRUZIONI (IN BASSO) ----------
        view = new HBox(10);
        view.setAlignment(Pos.CENTER);
        view.setPadding(new Insets(0, 30, 20, 30));

        for (ConstructionType type : TYPES)
        {
            VBox constructionBox = createConstructionBox(type);

            if (type == ConstructionType.NUCLEAR_PLANT)
            {
                nuclearPlantBox = constructionBox;
            }

            view.getChildren().add(constructionBox);
        }

        refreshCosts();
    }
    //Crea il singolo blocco visuale (VBox) contenente il pulsante di selezione con la sua icona colorata e l'etichetta del costo.
    private VBox createConstructionBox(
            final ConstructionType type)
    {
        Button button = new Button(
                getButtonText(type)
        );

        // Salva il pulsante per poterlo abilitare/disabilitare in seguito
        buttons.put(type, button);

        // Crea il quadratino colorato
        Rectangle colorBox = new Rectangle(12, 12);
        colorBox.setFill(
                getConstructionColor(type)
        );
        colorBox.setStroke(Color.BLACK);
        colorBox.setStrokeWidth(0.5);

        button.setGraphic(colorBox);
        button.setContentDisplay(ContentDisplay.LEFT);

        // Quando il pulsante viene premuto,
        // comunica alla GridView quale costruzione è stata selezionata
        button.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        selectionHandler.accept(type);
                    }
                }
        );

        // Etichetta che mostra il costo
        Label costLabel = new Label();
        costLabels.put(type, costLabel);

        /*
         * Lucchetto.
         * Viene creato per tutti i pulsanti,
         * ma sarà visibile solo quando necessario.
         */
        Label lockLabel = new Label("🔒");
        lockLabel.setStyle(
                "-fx-font-size: 22px;"
        );

        /*
         * Il lucchetto è soltanto grafico:
         * non deve intercettare il mouse.
         */
        lockLabel.setMouseTransparent(true);
        lockLabel.setVisible(false);

        lockLabels.put(type, lockLabel);

        /*
         * StackPane sovrappone il lucchetto al pulsante.
         *
         *       🔒
         *   [  Bank  ]
         */
        StackPane buttonContainer =
                new StackPane(
                        button,
                        lockLabel
                );

        // Testo del costo sotto al pulsante
        VBox box = new VBox(
                2,
                buttonContainer,
                costLabel
        );

        box.setAlignment(Pos.CENTER);

        return box;
    }
//Restituisce l'etichetta testuale descrittiva associata a uno specifico tipo di costruzione.
    private String getButtonText(
            ConstructionType type)
    {
        return switch (type)
        {
            case INDUSTRIAL -> "Industrial area";
            case RESIDENTIAL -> "Residential area";
            case PARK -> "Park";
            case ROAD -> "Road";
            case POWER_PLANT -> "Power Plant";
            case COMMERCIAL -> "Commercial";
            case BANK -> "Bank";
            case CONSTRUCTION_COMPANY -> "Construction Company";
            case CRIMINAL_ACTIVITY -> "Criminal Activity";
            case POLICE_STATION -> "Police station";
            case GRASS -> "Grass";
            case TERRORISTIC_GROUP -> "Terroristic Group";
            case NUCLEAR_PLANT -> "Nuclear Plant";
            case WAST_TREATMENT_PLANT -> "Waste Treatment Plant ";
        };
    }
//Associa ed ottiene il colore identificativo per ciascun tipo di costruzione sulla griglia/interfaccia.
    private Color getConstructionColor(
            ConstructionType type)
    {
        return switch (type)
        {
            case INDUSTRIAL -> Color.DARKGRAY;
            case RESIDENTIAL -> Color.LIGHTGREEN;
            case PARK -> Color.FORESTGREEN;
            case ROAD -> Color.DIMGRAY;
            case POWER_PLANT -> Color.ORANGE;
            case COMMERCIAL -> Color.CORNFLOWERBLUE;
            case BANK -> Color.GOLD;
            case CONSTRUCTION_COMPANY -> Color.web("#7B5E3B");
            case CRIMINAL_ACTIVITY -> Color.BLACK;
            case POLICE_STATION -> Color.DARKBLUE;
            case GRASS -> Color.PALEGREEN;
            case TERRORISTIC_GROUP -> Color.DARKRED;
            case NUCLEAR_PLANT -> Color.PURPLE;
            case WAST_TREATMENT_PLANT -> Color.web("#A0522D");
        };
    }

    public void refreshAvailability()
    {
        boolean nuclearPlantVisible = controller.shouldShowNuclearPlant();

        nuclearPlantBox.setVisible(nuclearPlantVisible);
        nuclearPlantBox.setManaged(nuclearPlantVisible);

        for (ConstructionType type : TYPES)
        {
            boolean locked = !controller.isConstructionUnlocked(type);
            buttons.get(type).setDisable(locked);
            lockLabels.get(type).setVisible(locked);
        }
    }

//Aggiorna le etichette dei costi di tutti i pulsanti recuperando i valori aggiornati dal controller.
    public void refreshCosts()
    {
        for (ConstructionType type : TYPES)
        {
            costLabels.get(type).setText(
                    Math.abs(controller.getPlacementCost(type))
                            + " €"
            );
        }
    }
//Restituisce il nodo grafico HBox contenente l'intera barra degli strumenti da inserire nell'interfaccia.
    public HBox getView()
    {
        return view;
    }
}