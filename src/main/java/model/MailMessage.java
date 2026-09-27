package model;

/** Rappresenta una lettera ricevuta durante la partita. */
public class MailMessage {

    private final String id;
    private final String sender;
    private final String subject;
    private final String body;
    private final int receivedTick;
    private boolean read;

    /** Crea la lettera e controlla che i suoi dati siano validi. */
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

    /** Restituisce l'identificatore che evita consegne duplicate. */
    public String getId()
    {
        return id;
    }

    /** Restituisce il mittente mostrato nell'anteprima. */
    public String getSender()
    {
        return sender;
    }

    /** Restituisce l'oggetto della lettera. */
    public String getSubject()
    {
        return subject;
    }

    /** Restituisce il testo completo della lettera. */
    public String getBody()
    {
        return body;
    }

    /** Restituisce il tick in cui la lettera è arrivata. */
    public int getReceivedTick()
    {
        return receivedTick;
    }

    /** Indica se il giocatore ha già aperto la lettera. */
    public boolean isRead()
    {
        return read;
    }

    /** Segna la lettera come letta senza modificarne il contenuto. */
    public void markRead()
    {
        read = true;
    }

}
