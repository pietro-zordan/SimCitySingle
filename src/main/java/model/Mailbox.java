package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Mailbox
{
    private final List<MailMessage> messages;

    public Mailbox()
    {
        messages = new ArrayList<>();
    }

    public boolean addMessage(MailMessage message)
    {
        if (message == null)
        {
            throw new IllegalArgumentException("Il messaggio non può essere null");
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

    public List<MailMessage> getMessages()
    {
        return Collections.unmodifiableList(
                new ArrayList<>(messages)
        );
    }

    public boolean hasMessages()
    {
        return !messages.isEmpty();
    }

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

    public boolean markAsRead(String id)
    {
        if (id == null || id.isBlank())
        {
            throw new IllegalArgumentException("L'id non può essere vuoto");
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