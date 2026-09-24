// La classe ConstructionToolbarView gestisce la barra degli strumenti per la selezione delle costruzioni.
// Organizza gli edifici per categorie, mostra colori e costi e aggiorna la disponibilita in tempo reale.

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

public final class ConstructionToolbarView
{
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
            ConstructionType.WASTE_TREATMENT_PLANT,
            ConstructionType.MILITARY_BASE
    );

    private static final List<ConstructionType> ZONES = List.of(
            ConstructionType.RESIDENTIAL,
            ConstructionType.COMMERCIAL,
            ConstructionType.INDUSTRIAL
    );

    private static final List<ConstructionType> INFRASTRUCTURE = List.of(
            ConstructionType.ROAD,
            ConstructionType.POWER_PLANT,
            ConstructionType.NUCLEAR_PLANT,
            ConstructionType.WASTE_TREATMENT_PLANT
    );

    private static final List<ConstructionType> SERVICES = List.of(
            ConstructionType.PARK,
            ConstructionType.BANK,
            ConstructionType.CONSTRUCTION_COMPANY,
            ConstructionType.POLICE_STATION,
            ConstructionType.MILITARY_BASE
    );

    private final Controller controller;
    private final Consumer<ConstructionType> selectionHandler;

    private final Map<ConstructionType, Label> costLabels =
            new EnumMap<>(ConstructionType.class);

    private final Map<ConstructionType, Button> buttons =
            new EnumMap<>(ConstructionType.class);

    private final Map<ConstructionType, Label> lockLabels =
            new EnumMap<>(ConstructionType.class);

    private final Map<ConstructionType, VBox> constructionBoxes =
            new EnumMap<>(ConstructionType.class);

    private final VBox view;
    private final HBox categoryButtons;
    private final HBox constructionButtons;

    private final Button zonesButton =
            new Button("Zones");

    private final Button infrastructureButton =
            new Button("Infrastructure");

    private final Button servicesButton =
            new Button("Services");

    private VBox nuclearPlantBox;

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

        categoryButtons = new HBox(
                10,
                zonesButton,
                infrastructureButton,
                servicesButton
        );

        categoryButtons.setAlignment(Pos.CENTER);

        constructionButtons = new HBox(10);
        constructionButtons.setAlignment(Pos.CENTER);

        view = new VBox(
                8,
                categoryButtons,
                constructionButtons
        );

        view.setAlignment(Pos.CENTER);
        view.setPadding(
                new Insets(
                        0,
                        30,
                        20,
                        30
                )
        );

        createConstructionButtons();
        configureCategoryButtons();

        refreshCosts();
        showCategory(
                ZONES,
                zonesButton
        );
    }

    private void createConstructionButtons()
    {
        for (ConstructionType type : TYPES)
        {
            VBox constructionBox =
                    createConstructionBox(type);

            constructionBoxes.put(
                    type,
                    constructionBox
            );

            if (type == ConstructionType.NUCLEAR_PLANT)
            {
                nuclearPlantBox =
                        constructionBox;
            }
        }
    }

    private void configureCategoryButtons()
    {
        zonesButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showCategory(
                                ZONES,
                                zonesButton
                        );
                    }
                }
        );

        infrastructureButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showCategory(
                                INFRASTRUCTURE,
                                infrastructureButton
                        );
                    }
                }
        );

        servicesButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showCategory(
                                SERVICES,
                                servicesButton
                        );
                    }
                }
        );
    }

    private void showCategory(
            List<ConstructionType> category,
            Button selectedCategoryButton)
    {
        constructionButtons
                .getChildren()
                .clear();

        for (ConstructionType type : category)
        {
            constructionButtons
                    .getChildren()
                    .add(
                            constructionBoxes.get(type)
                    );
        }

        zonesButton.setStyle("");
        infrastructureButton.setStyle("");
        servicesButton.setStyle("");

        selectedCategoryButton.setStyle(
                "-fx-font-weight: bold;"
        );
    }

    private VBox createConstructionBox(
            final ConstructionType type)
    {
        Button button = new Button(
                getButtonText(type)
        );

        buttons.put(type, button);

        Rectangle colorBox =
                new Rectangle(
                        12,
                        12
                );

        colorBox.setFill(
                getConstructionColor(type)
        );
        colorBox.setStroke(Color.BLACK);
        colorBox.setStrokeWidth(0.5);

        button.setGraphic(colorBox);
        button.setContentDisplay(
                ContentDisplay.LEFT
        );

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

        Label costLabel = new Label();
        costLabels.put(type, costLabel);

        Label lockLabel = new Label("🔒");
        lockLabel.setStyle(
                "-fx-font-size: 22px;"
        );
        lockLabel.setMouseTransparent(true);
        lockLabel.setVisible(false);

        lockLabels.put(type, lockLabel);

        StackPane buttonContainer =
                new StackPane(
                        button,
                        lockLabel
                );

        VBox box = new VBox(
                2,
                buttonContainer,
                costLabel
        );

        box.setAlignment(Pos.CENTER);

        return box;
    }

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
            case WASTE_TREATMENT_PLANT -> "Waste Treatment Plant";
            case MILITARY_BASE -> "Military Base";
            case MASONIC_LODGE -> "Masonic Lodge";
        };
    }

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
            case WASTE_TREATMENT_PLANT -> Color.web("#A0522D");
            case MILITARY_BASE -> Color.DARKOLIVEGREEN;
            case MASONIC_LODGE -> Color.YELLOW;
        };
    }

    public void refreshAvailability()
    {
        boolean nuclearPlantVisible =
                controller.shouldShowNuclearPlant();

        nuclearPlantBox.setVisible(
                nuclearPlantVisible
        );
        nuclearPlantBox.setManaged(
                nuclearPlantVisible
        );

        for (ConstructionType type : TYPES)
        {
            boolean locked =
                    !controller
                            .isConstructionUnlocked(type);

            buttons.get(type)
                    .setDisable(locked);

            lockLabels.get(type)
                    .setVisible(locked);
        }
    }

    public void refreshCosts()
    {
        for (ConstructionType type : TYPES)
        {
            costLabels.get(type)
                    .setText(
                            Math.abs(
                                    controller
                                            .getPlacementCost(type)
                            )
                                    + " €"
                    );
        }
    }

    public VBox getView()
    {
        return view;
    }
}
