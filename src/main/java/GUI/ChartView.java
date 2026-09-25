package GUI;

import java.util.ArrayList;
import java.util.List;

import controller.Controller;
import javafx.collections.ObservableList;
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
    private static final int MAX_CHART_POINTS = 300;

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

    // Mantiene un solo punto per tick e conserva i 300 tick piu recenti.
    public void refresh()
    {
        int tick = controller.getCurrentTick();

        record(populationSeries, tick, controller.getPopulation());
        record(pollutionSeries, tick, controller.getPollution());
        record(economySeries, tick, controller.getEconomy());
        record(happinessSeries, tick, controller.getHappiness());
        record(unemployedSeries, tick, controller.getUnemployed());
        record(energyConsumptionSeries, tick, controller.getEnergyConsumed());
    }

    private void record(
            XYChart.Series<Number, Number> series,
            int tick,
            int value)
    {
        ObservableList<XYChart.Data<Number, Number>> points =
                series.getData();

        if (!points.isEmpty())
        {
            XYChart.Data<Number, Number> latest =
                    points.get(points.size() - 1);

            if (latest.getXValue().intValue() == tick)
            {
                if (latest.getYValue().intValue() != value)
                {
                    latest.setYValue(value);
                }
                return;
            }
        }

        points.add(new XYChart.Data<>(tick, value));

        if (points.size() > MAX_CHART_POINTS)
        {
            points.remove(0);
        }
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
