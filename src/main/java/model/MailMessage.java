package model;

/** One message delivered to the player's in-game mailbox. */
public class MailMessage {

    private final String id;
    private final String sender;
    private final String subject;
    private final String body;
    private final int receivedTick;
    private boolean read;

    public MailMessage(String id, String sender, String subject,
                       String body, int receivedTick)
    {
        if (id == null || id.isBlank()
                || sender == null || sender.isBlank()
                || subject == null || subject.isBlank()
                || body == null || body.isBlank())
        {
            throw new IllegalArgumentException(
                    "Id, sender, subject and body cannot be empty");
        }

        if (receivedTick < 0)
        {
            throw new IllegalArgumentException(
                    "Received tick cannot be negative");
        }

        this.id = id;
        this.sender = sender;
        this.subject = subject;
        this.body = body;
        this.receivedTick = receivedTick;
        this.read = false;
    }

    public String getId()
    {
        return id;
    }

    public String getSender()
    {
        return sender;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getBody()
    {
        return body;
    }

    public int getReceivedTick()
    {
        return receivedTick;
    }

    public boolean isRead()
    {
        return read;
    }

    public void markRead()
    {
        // The text and delivery tick stay the same after opening the message.
        read = true;
    }

}
