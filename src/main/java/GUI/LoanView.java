package GUI;

import controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;

/* Gestisce la parte grafica dei prestiti: pulsante, inserimento importo,
   richiesta al controller e aggiornamento della disponibilita. */
public final class LoanView
{
    private final Controller controller;
    private final Consumer<String> messageHandler;

    private final Button loanButton = new Button("Ask for a loan");
    private final Label loanLimitLabel = new Label();
    private final TextField loanAmountField = new TextField();
    private final Button confirmLoanButton = new Button("Conferma");
    private final VBox requestBox = new VBox(5, loanLimitLabel, loanAmountField, confirmLoanButton);

    public LoanView(Controller controller, Consumer<String> messageHandler)
    {
        if (controller == null || messageHandler == null)
        {
            throw new IllegalArgumentException("Loan view dependencies cannot be null");
        }

        this.controller = controller;
        this.messageHandler = messageHandler;

        configure();
    }

    private void configure()
    {
        loanButton.setMaxWidth(Double.MAX_VALUE);
        loanButton.managedProperty().bind(loanButton.visibleProperty());
        loanButton.setVisible(false);

        requestBox.setAlignment(Pos.CENTER);
        requestBox.setVisible(false);
        requestBox.managedProperty().bind(requestBox.visibleProperty());

        loanAmountField.setPromptText("Importo prestito");
        loanAmountField.setMaxWidth(Double.MAX_VALUE);
        confirmLoanButton.setMaxWidth(Double.MAX_VALUE);

        loanButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                int maxAmount = controller.getMaxLoanAmount();
                loanLimitLabel.setText("Massimo: " + maxAmount + " €");
                loanAmountField.setText(String.valueOf(maxAmount));
                requestBox.setVisible(true);
            }
        });

        confirmLoanButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                requestLoan();
            }
        });
    }

    private void requestLoan()
    {
        try
        {
            int amount = Integer.parseInt(loanAmountField.getText().trim());
            int maxAmount = controller.getMaxLoanAmount();

            if (amount <= 0 || amount > maxAmount)
            {
                messageHandler.accept("Inserisci un importo tra 1 e " + maxAmount + " €.");
                return;
            }

            if (controller.requestLoan(amount))
            {
                messageHandler.accept("Prestito ricevuto: " + amount
                        + " €. Tra 3 tick dovrai restituire "
                        + Math.round(amount * 1.5) + " €.");
                requestBox.setVisible(false);
            }
            else
            {
                messageHandler.accept("You can't ask for a loan.");
            }
        }
        catch (NumberFormatException exception)
        {
            messageHandler.accept("Inserisci un numero valido.");
        }
    }

    public void refresh()
    {
        boolean canRequestLoan = controller.canRequestLoan();
        loanButton.setVisible(canRequestLoan);

        if (!canRequestLoan)
        {
            requestBox.setVisible(false);
        }
    }

    public Button getActionButton()
    {
        return loanButton;
    }

    public VBox getRequestBox()
    {
        return requestBox;
    }

    public List<Button> getButtons()
    {
        return List.of(loanButton, confirmLoanButton);
    }
}
