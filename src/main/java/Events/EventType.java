package Events;

/* Elenca i tipi di evento disponibili
   e associa a ciascuno il nome mostrato all'utente. */
public enum EventType
{
    HACKER_ATTACK("Hacker attack"),
    ENERGY_CRISIS("Energy crisis"),
    FIRE("Fire"),
    TSUNAMI("Tsunami"),
    ECONOMIC_BOOM("Economic boom");

    private final String displayName;

    // Associa al tipo di evento il suo nome visualizzato.
    EventType(String displayName)
    {
        this.displayName = displayName;
    }

    // Restituisce il nome dell'evento mostrato all'utente.
    public String getDisplayName()
    {
        return displayName;
    }
}