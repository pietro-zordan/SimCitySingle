package GUI;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

public final class MapViewport
{
    private static final double MIN_ZOOM = 0.50;
    private static final double MAX_ZOOM = 1.60;
    private static final double ZOOM_STEP = 0.10;
    private static final double VIEWPORT_WIDTH = 660;
    private static final double VIEWPORT_HEIGHT = 660;
    private static final double EXPANSION_ANIMATION_DURATION = 850;
    private static final double FIT_MARGIN = 0;

    private final StackPane zoomPane;
    private final Group zoomGroup;
    private final StackPane mapHolder;
    private final ScrollPane scrollPane;
    private final Label zoomLabel = new Label();
    private final VBox view;
    private final HBox zoomControls;
    private double zoom = 1.0;
    private Timeline zoomAnimation;

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
        Button fitGridButton = new Button("Fit");
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

        fitGridButton.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                animateToFit();
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

        zoomControls = new HBox(6, zoomOutButton, zoomLabel, zoomInButton, fitGridButton);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setVisible(false);
        zoomControls.setManaged(false);

        view = new VBox(5, scrollPane, zoomControls);
        view.setAlignment(Pos.CENTER);

        updateZoomLabel();
        centerMap();
    }

    private void setZoom(double requestedZoom)
    {
        if (zoomAnimation != null)
        {
            zoomAnimation.stop();
            zoomAnimation = null;
        }

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

    // Dopo un'espansione riduce dolcemente lo zoom fino a mostrare l'intera nuova griglia e centra la visuale.
    public void animateToFit()
    {
        if (!zoomControls.isVisible())
        {
            zoomControls.setVisible(true);
            zoomControls.setManaged(true);
        }

        zoomPane.applyCss();
        zoomPane.layout();
        scrollPane.applyCss();
        scrollPane.layout();

        double contentWidth = zoomPane.getLayoutBounds().getWidth();
        double contentHeight = zoomPane.getLayoutBounds().getHeight();
        double viewportWidth = scrollPane.getViewportBounds().getWidth();
        double viewportHeight = scrollPane.getViewportBounds().getHeight();

        if (contentWidth <= 0 || contentHeight <= 0 || viewportWidth <= 0 || viewportHeight <= 0)
        {
            Platform.runLater(new Runnable()
            {
                @Override
                public void run()
                {
                    animateToFit();
                }
            });
            return;
        }

        double fitZoom = Math.min(
                (viewportWidth - FIT_MARGIN) / contentWidth,
                (viewportHeight - FIT_MARGIN) / contentHeight
        );
        fitZoom = Math.max(MIN_ZOOM, Math.min(1.0, fitZoom));

        if (zoomAnimation != null)
        {
            zoomAnimation.stop();
        }

        final double targetZoom = fitZoom;
        zoomAnimation = new Timeline(
                new KeyFrame(
                        Duration.millis(EXPANSION_ANIMATION_DURATION),
                        new KeyValue(zoomPane.scaleXProperty(), targetZoom, Interpolator.EASE_BOTH),
                        new KeyValue(zoomPane.scaleYProperty(), targetZoom, Interpolator.EASE_BOTH),
                        new KeyValue(scrollPane.hvalueProperty(), 0.5, Interpolator.EASE_BOTH),
                        new KeyValue(scrollPane.vvalueProperty(), 0.5, Interpolator.EASE_BOTH)
                )
        );

        zoomAnimation.setOnFinished(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                zoom = targetZoom;
                updateZoomLabel();
                zoomAnimation = null;
            }
        });

        zoomAnimation.play();
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
