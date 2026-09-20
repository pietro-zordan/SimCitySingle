package GUI;

import java.util.ArrayList;
import java.util.List;

import controller.Controller;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

/* Gestisce i grafici delle statistiche della città
   e permette di passare da un grafico al successivo. */
public final class ChartView
{
    private final Controller controller;
    private final StackPane view = new StackPane();
    private final Button switchButton =
            new Button("➞Next Graph➞");

    // Contiene i sei grafici mostrati a rotazione.
    private final List<LineChart<Number, Number>> charts =
            new ArrayList<>();

    // Serie contenenti i valori registrati a ogni tick.
    private final XYChart.Series<Number, Number>
            populationSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number>
            pollutionSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number>
            economySeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number>
            happinessSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number>
            unemployedSeries = new XYChart.Series<>();
    private final XYChart.Series<Number, Number>
            energyConsumptionSeries = new XYChart.Series<>();

    private int currentChartIndex;

    // Crea i grafici e configura il pulsante per cambiarli.
    public ChartView(Controller controller)
    {
        if (controller == null)
        {
            throw new IllegalArgumentException(
                    "controller.Controller cannot be null"
            );
        }

        this.controller = controller;

        createChart("Population", populationSeries);
        createChart("Pollution", pollutionSeries);
        createChart("Economy", economySeries);
        createChart("Happiness", happinessSeries);
        createChart("Unemployed", unemployedSeries);
        createChart("Energy Consumption", energyConsumptionSeries);

        showOnlyCurrentChart();

        switchButton.setPrefWidth(120);
        switchButton.setOnAction(
                new EventHandler<ActionEvent>()
                {
                    @Override
                    public void handle(ActionEvent event)
                    {
                        showNextChart();
                    }
                }
        );
    }

    // Crea un grafico e lo aggiunge alla vista.
    private void createChart(
            String statisticName,
            XYChart.Series<Number, Number> series)
    {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tick");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel(statisticName);

        LineChart<Number, Number> chart =
                new LineChart<>(xAxis, yAxis);

        chart.setTitle(statisticName + " Graph");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setPrefHeight(330);
        chart.setPrefWidth(330);
        chart.getData().add(series);

        charts.add(chart);
        view.getChildren().add(chart);
    }

    // Aggiunge ai grafici le statistiche del tick corrente.
    public void refresh()
    {
        int tick = controller.getCurrentTick();

        populationSeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getPopulation()
                )
        );

        pollutionSeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getPollution()
                )
        );

        economySeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getEconomy()
                )
        );

        happinessSeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getHappiness()
                )
        );

        unemployedSeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getUnemployed()
                )
        );

        energyConsumptionSeries.getData().add(
                new XYChart.Data<>(
                        tick,
                        controller.getEnergyConsumed()
                )
        );
    }

    // Passa al grafico successivo tornando al primo dopo l'ultimo.
    private void showNextChart()
    {
        currentChartIndex =
                (currentChartIndex + 1) % charts.size();

        showOnlyCurrentChart();
        switchButton.setText("➞Next Graph➞");
    }

    // Mostra solamente il grafico corrispondente all'indice corrente.
    private void showOnlyCurrentChart()
    {
        for (int index = 0;
             index < charts.size();
             index++)
        {
            boolean current =
                    index == currentChartIndex;

            charts.get(index).setVisible(current);
            charts.get(index).setManaged(current);
        }
    }

    // Restituisce il contenitore dei grafici.
    public StackPane getView()
    {
        return view;
    }

    // Restituisce il pulsante utilizzato per cambiare grafico.
    public Button getSwitchButton()
    {
        return switchButton;
    }
}