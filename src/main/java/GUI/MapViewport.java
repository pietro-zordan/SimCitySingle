package GUI;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class MapViewport
{
    private static final double MIN_ZOOM = 0.50;
    private static final double MAX_ZOOM = 1.60;
    private static final double ZOOM_STEP = 0.10;
    private static final double VIEWPORT_WIDTH = 660;
    private static final double VIEWPORT_HEIGHT = 660;

    private final StackPane zoomPane;
    private final Group zoomGroup;
    private final StackPane mapHolder;
    private final ScrollPane scrollPane;
    private final Label zoomLabel = new Label();
    private final VBox view;
    private double zoom = 1.0;

    public MapViewport(Node mapContent)
    {
        if (mapContent == null)
        {
            throw new IllegalArgumentException("Map content cannot be null");
        }

        zoomPane = new StackPane(mapContent);
        zoomGroup = new Group(zoomPane);
        mapHolder = new StackPane(zoomGroup);
        mapHolder.setAlignment(Pos.CENTER);
        mapHolder.setMinSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);

        scrollPane = new ScrollPane(mapHolder);
        scrollPane.setPrefViewportWidth(VIEWPORT_WIDTH);
        scrollPane.setPrefViewportHeight(VIEWPORT_HEIGHT);
        scrollPane.setMaxSize(VIEWPORT_WIDTH + 18, VIEWPORT_HEIGHT + 18);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Button zoomOutButton = new Button("-");
        Button zoomInButton = new Button("+");
        Button resetZoomButton = new Button("Reset");
        zoomLabel.setMinWidth(48);
        zoomLabel.setAlignment(Pos.CENTER);

        zoomOutButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                setZoom(zoom - ZOOM_STEP);
            }
        });

        zoomInButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                setZoom(zoom + ZOOM_STEP);
            }
        });

        resetZoomButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                setZoom(1.0);
                centerMap();
            }
        });

        scrollPane.addEventFilter(ScrollEvent.SCROLL, new EventHandler<ScrollEvent>()
        {
            @Override
            public void handle(ScrollEvent event)
            {
                if (event.getDeltaY() == 0)
                {
                    return;
                }

                if (event.getDeltaY() > 0)
                {
                    setZoom(zoom + ZOOM_STEP);
                }
                else
                {
                    setZoom(zoom - ZOOM_STEP);
                }

                event.consume();
            }
        });

        HBox zoomControls = new HBox(6, zoomOutButton, zoomLabel, zoomInButton, resetZoomButton);
        zoomControls.setAlignment(Pos.CENTER);
        view = new VBox(5, scrollPane, zoomControls);
        view.setAlignment(Pos.CENTER);

        updateZoomLabel();
        centerMap();
    }

    private void setZoom(double requestedZoom)
    {
        double newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, requestedZoom));
        if (Math.abs(newZoom - zoom) < 0.0001)
        {
            return;
        }

        double horizontalPosition = scrollPane.getHvalue();
        double verticalPosition = scrollPane.getVvalue();

        zoom = newZoom;
        zoomPane.setScaleX(zoom);
        zoomPane.setScaleY(zoom);
        updateZoomLabel();

        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                scrollPane.setHvalue(horizontalPosition);
                scrollPane.setVvalue(verticalPosition);
            }
        });
    }

    private void updateZoomLabel()
    {
        zoomLabel.setText(Math.round(zoom * 100) + "%");
    }

    private void centerMap()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                scrollPane.setHvalue(0.5);
                scrollPane.setVvalue(0.5);
            }
        });
    }

    public VBox getView()
    {
        return view;
    }
}
