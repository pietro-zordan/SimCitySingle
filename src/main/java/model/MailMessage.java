package model;

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
                    "Id, mittente, oggetto e testo non possono essere vuoti");
        }

        if (receivedTick < 0)
        {
            throw new IllegalArgumentException(
                    "Il tick di ricezione non può essere negativo");
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
        read = true;
    }

}
