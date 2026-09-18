package policies;

/* Elenca le policy disponibili
   e associa a ciascuna il nome mostrato all'utente. */
public enum PolicyType
{
    STANDARD("Standard policy"),
    ENVIRONMENTAL("Environmental policy"),
    INDUSTRIAL("Industrial policy");

    private final String displayName;

    // Associa al tipo della policy il suo nome visualizzato.
    PolicyType(String displayName)
    {
        this.displayName = displayName;
    }

    // Restituisce il nome della policy mostrato all'utente.
    public String getDisplayName()
    {
        return displayName;
    }
}