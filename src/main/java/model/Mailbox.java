package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Conserva le lettere in ordine di arrivo evitando identificatori duplicati. */
public class Mailbox
{
    private final List<MailMessage> messages;

    /** Inizializza una casella di posta vuota. */
    public Mailbox()
    {
        messages = new ArrayList<>();
    }

    /** Aggiunge la lettera; restituisce false se l'id è già presente. */
    public boolean addMessage(MailMessage message)
    {
        if (message == null)
        {
            throw new IllegalArgumentException("Message cannot be null");
        }

        for (MailMessage existing : messages)
        {
            if (existing.getId().equals(message.getId()))
            {
                return false;
            }
        }

        messages.add(message);
        return true;
    }

    /** Restituisce una copia dell'elenco che non può essere modificata. */
    public List<MailMessage> getMessages()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(messages)
        );
    }

    /** Indica se almeno una lettera è stata consegnata. */
    public boolean hasMessages()
    {
        return !messages.isEmpty();
    }

    /** Conta le lettere che non sono ancora state aperte. */
    public int getUnreadCount()
    {
        int count = 0;

        for (MailMessage message : messages)
        {
            if (!message.isRead())
            {
                count++;
            }
        }

        return count;
    }

    /** Segna come letta la lettera con questo id, se esiste. */
    public boolean markAsRead(String id)
    {
        if (id == null || id.isBlank())
        {
            throw new IllegalArgumentException("Id cannot be empty");
        }

        for (MailMessage message : messages)
        {
            if (message.getId().equals(id))
            {
                message.markRead();
                return true;
            }
        }

        return false;
    }
}
