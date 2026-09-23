package GUI;

import controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

// Gestisce il pannello di acquisto e riparazione della difesa missilistica.
public final class MissileDefensePanelView
{
    private final Controller controller;
    private final Consumer<String> toastHandler;

    private final Button missileDefenseButton =
            new Button("Missile Defense");

    private final Label missileDefenseInfoLabel =
            new Label();

    private final Button confirmMissileDefenseButton =
            new Button("Buy");

    private final Button refillMissileDefenseButton =
            new Button("Restore to 3");

    private final Button cancelMissileDefenseButton =
            new Button("Not now");

    private final HBox missileDefenseActions =
            new HBox(
                    5,
                    confirmMissileDefenseButton,
                    cancelMissileDefenseButton
            );

    private final VBox missileDefenseBox =
            new VBox(
                    5,
                    missileDefenseInfoLabel,
                    refillMissileDefenseButton,
                    missileDefenseActions
            );

    private boolean missileDefenseOfferShown;

    public MissileDefensePanelView(
            Controller controller,
            Consumer<String> toastHandler)
    {
        if (controller == null || toastHandler == null)
        {
            throw new IllegalArgumentException(
                    "Missile Defense panel dependencies cannot be null"
            );
        }

        this.controller = controller;
        this.toastHandler = toastHandler;
        configureMissileDefense();
    }

    private void configureMissileDefense()
    {
        missileDefenseButton.setMaxWidth(
                Double.MAX_VALUE
        );

        missileDefenseButton.managedProperty()
                .bind(
                        missileDefenseButton
                                .visibleProperty()
                );

        missileDefenseButton.setVisible(false);

        missileDefenseBox.setAlignment(Pos.CENTER);
        missileDefenseBox.managedProperty()
                .bind(
                        missileDefenseBox
                                .visibleProperty()
                );
        missileDefenseBox.setVisible(false);

        missileDefenseInfoLabel.setWrapText(true);
        missileDefenseActions.setAlignment(Pos.CENTER);

        refillMissileDefenseButton.setMaxWidth(Double.MAX_VALUE);
        refillMissileDefenseButton.managedProperty()
                .bind(refillMissileDefenseButton.visibleProperty());
        refillMissileDefenseButton.setVisible(false);

        confirmMissileDefenseButton.setMaxWidth(
                Double.MAX_VALUE
        );

        cancelMissileDefenseButton.setMaxWidth(
                Double.MAX_VALUE
        );

        missileDefenseButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showMissileDefensePanel();
                    }
                }
        );

        confirmMissileDefenseButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        applyMissileDefenseAction();
                    }
                }
        );

        refillMissileDefenseButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        applyFullMissileDefenseRepair();
                    }
                }
        );

        cancelMissileDefenseButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        missileDefenseBox.setVisible(false);
                    }
                }
        );
    }

    private void showMissileDefensePanel()
    {
        if (!controller.isMissileDefensePurchased())
        {
            missileDefenseInfoLabel.setText(
                    "Missile Defense System available."
                            + "\nRequires at least one Military Base."
                            + "\nCapacity: 3 missile impacts."
                            + "\nMilitary Base discount: "
                            + controller
                            .getMissileDefensePurchaseDiscountPercentage()
                            + "%"
                            + "\nCost: "
                            + controller.getMissileDefensePurchaseCost()
                            + " €"
                            + "\nBuy it?"
            );

            confirmMissileDefenseButton.setText("Buy");
            confirmMissileDefenseButton.setDisable(false);
            refillMissileDefenseButton.setVisible(false);
            cancelMissileDefenseButton.setText("Not now");
        }
        else
        {
            int hits =
                    controller.getMissileDefenseHitsRemaining();

            missileDefenseInfoLabel.setText(
                    "Missile Defense: "
                            + hits
                            + "/3 impacts remaining."
                            + (hits == 0
                            ? "\nThe shield is offline until repaired."
                            : "")
                            + (hits < 3
                            ? "\nRepair +1 impact for "
                            + controller
                            .getMissileDefenseRepairCost()
                            + " € or restore to 3 for "
                            + controller
                            .getMissileDefenseFullRepairCost()
                            + " € (no discount)."
                            : "\nThe shield is fully repaired.")
            );

            confirmMissileDefenseButton.setText(
                    hits < 3
                            ? "Repair +1"
                            : "Fully repaired"
            );

            confirmMissileDefenseButton.setDisable(
                    !controller.canRepairMissileDefense()
            );

            refillMissileDefenseButton.setVisible(hits < 3);
            refillMissileDefenseButton.setText(
                    "Restore to 3 ("
                            + controller.getMissileDefenseFullRepairCost()
                            + " €)"
            );

            cancelMissileDefenseButton.setText("Close");
        }

        missileDefenseBox.setVisible(true);
    }

    private void applyMissileDefenseAction()
    {
        if (!controller.isMissileDefensePurchased())
        {
            int cost =
                    controller.getMissileDefensePurchaseCost();

            if (controller.buyMissileDefense())
            {
                missileDefenseBox.setVisible(false);

                toastHandler.accept(
                        "Missile Defense activated for "
                                + cost
                                + " €. Capacity: 3 missiles."
                );
            }
            else
            {
                toastHandler.accept(
                        "Unable to buy Missile Defense. Check requirements and budget."
                );
            }

            return;
        }

        int repairCost =
                controller.getMissileDefenseRepairCost();

        if (controller.repairMissileDefense())
        {
            missileDefenseBox.setVisible(false);

            toastHandler.accept(
                    "Missile Defense repaired for "
                            + repairCost
                            + " €. Capacity: "
                            + controller
                            .getMissileDefenseHitsRemaining()
                            + "/3."
            );
        }
        else
        {
            toastHandler.accept(
                    "Unable to repair Missile Defense. Check your budget."
            );
        }
    }

    private void applyFullMissileDefenseRepair()
    {
        int cost = controller.getMissileDefenseFullRepairCost();

        if (controller.repairMissileDefenseFully())
        {
            missileDefenseBox.setVisible(false);
            toastHandler.accept(
                    "Missile Defense restored to 3/3 for "
                            + cost + " €."
            );
        }
        else
        {
            toastHandler.accept(
                    "Unable to restore Missile Defense. Check your budget."
            );
        }
    }

    public void refresh()
    {
        boolean purchased =
                controller.isMissileDefensePurchased();

        boolean available =
                controller.isMissileDefenseAvailable();

        missileDefenseButton.setVisible(
                purchased || available
        );

        if (!purchased && !available)
        {
            missileDefenseBox.setVisible(false);
            missileDefenseOfferShown = false;
            return;
        }

        if (purchased)
        {
            int hits =
                    controller.getMissileDefenseHitsRemaining();

            if (hits > 0)
            {
                missileDefenseButton.setText(
                        "Missile Defense ("
                                + hits
                                + "/3)"
                );
            }
            else
            {
                missileDefenseButton.setText(
                        "Missile Defense (OFF 0/3)"
                );
            }

            return;
        }

        missileDefenseButton.setText(
                "Buy Missile Defense"
        );

        if (controller.canBuyMissileDefense()
                && !missileDefenseOfferShown)
        {
            missileDefenseOfferShown = true;
            showMissileDefensePanel();
        }
    }

    public Button getActionButton()
    {
        return missileDefenseButton;
    }

    public VBox getView()
    {
        return missileDefenseBox;
    }

    public List<Button> getButtons()
    {
        return List.of(
                missileDefenseButton,
                confirmMissileDefenseButton,
                refillMissileDefenseButton,
                cancelMissileDefenseButton
        );
    }
}
