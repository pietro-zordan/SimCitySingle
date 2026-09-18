package policies;

/* Crea la policy richiesta in base al tipo ricevuto. */
public class PolicyFactory
{
    // Impedisce la creazione di oggetti PolicyFactory.
    private PolicyFactory()
    {
    }

    // Crea e restituisce la policy corrispondente al tipo indicato.
    public static Policy create(PolicyType type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException(
                    "policies.Policy type cannot be null"
            );
        }

        if (type == PolicyType.STANDARD)
        {
            return new StandardPolicy();
        }

        if (type == PolicyType.ENVIRONMENTAL)
        {
            return new EnvironmentalPolicy();
        }

        if (type == PolicyType.INDUSTRIAL)
        {
            return new IndustrialPolicy();
        }

        throw new IllegalArgumentException(
                "Unknown policy type: " + type
        );
    }
}