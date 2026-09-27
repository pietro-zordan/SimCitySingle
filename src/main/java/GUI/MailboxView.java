package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import model.MailMessage;
import model.Mailbox;

import java.util.List;

/** Compact mailbox card for the right-hand game controls. */
public final class MailboxView
{
    private final Mailbox mailbox;
    private final VBox view;
    private final VBox entries;
    private final Label heading;
    private final ScrollPane scroll;
    private String expandedId;

    public MailboxView(Mailbox mailbox)
    {
        if (mailbox == null)
        {
            throw new IllegalArgumentException("Mailbox cannot be null");
        }

        this.mailbox = mailbox;

        heading = new Label();
        heading.setStyle(
                "-fx-text-fill: #334155; -fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        entries = new VBox(6);

        scroll = new ScrollPane(entries);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setMaxHeight(250);
        scroll.setStyle(
                "-fx-background: transparent;"
                        + "-fx-background-color: transparent;"
                        + "-fx-border-color: transparent;"
        );

        view = new VBox(8, heading, scroll);
        view.setPadding(new Insets(10));
        view.setPrefWidth(190);
        view.setMaxWidth(190);
        view.setStyle(
                "-fx-background-color: #f8fafc;"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: #dce3eb;"
                        + "-fx-border-radius: 9;"
        );

        refresh();
    }

    public VBox getView()
    {
        return view;
    }

    /** Rebuild previews and hide the card entirely when the mailbox is empty. */
    public void refresh()
    {
        List<MailMessage> messages = mailbox.getMessages();

        boolean hasMessages = !messages.isEmpty();
        view.setVisible(hasMessages);
        view.setManaged(hasMessages);

        if (!hasMessages)
        {
            expandedId = null;
            return;
        }

        int unreadCount = mailbox.getUnreadCount();
        heading.setText(
                unreadCount > 0
                        ? "MAIL  ·  " + unreadCount + " new"
                        : "MAIL"
        );

        entries.getChildren().clear();

        for (MailMessage message : messages)
        {
            entries.getChildren().add(createEntry(message));
        }

        // Keep the card scrollable when several messages arrive.
        int estimatedHeight = 8 + messages.size() * 68;
        if (expandedId != null)
        {
            estimatedHeight += 140;
        }
        scroll.setPrefViewportHeight(Math.min(230, estimatedHeight));
    }

    private VBox createEntry(final MailMessage message)
    {
        final String id = message.getId();
        boolean expanded = id.equals(expandedId);

        Button preview = new Button(
                (message.isRead() ? "" : "●  ")
                        + message.getSender()
                        + "\n"
                        + message.getSubject()
        );
        preview.setWrapText(true);
        preview.setMaxWidth(Double.MAX_VALUE);
        preview.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #263446;"
                        + "-fx-font-size: 11px;"
                        + "-fx-alignment: CENTER-LEFT;"
                        + "-fx-padding: 2;"
                        + "-fx-cursor: hand;"
                        + "-fx-focus-color: transparent;"
                        + "-fx-faint-focus-color: transparent;"
        );

        VBox entry = new VBox(6, preview);
        entry.setPadding(new Insets(7));
        entry.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 7;"
                        + "-fx-border-color: "
                        + (expanded ? "#c99748;" : "#e4e8ee;")
                        + "-fx-border-radius: 7;"
        );

        // Only the selected message displays its full text.
        if (expanded)
        {
            Label body = new Label(message.getBody());
            body.setWrapText(true);
            body.setMaxWidth(140);
            body.setStyle(
                    "-fx-text-fill: #475569;"
                            + "-fx-font-size: 11px;"
            );
            entry.getChildren().add(body);
        }

        preview.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if (id.equals(expandedId))
                {
                    expandedId = null;
                }
                else
                {
                    expandedId = id;
                    mailbox.markAsRead(id);
                }

                refresh();
            }
        });

        return entry;
    }
}
