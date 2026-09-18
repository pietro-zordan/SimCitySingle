package model;

/* Crea una costruzione concreta
   in base al tipo ricevuto. */
public class ConstructionFactory
{
    // Crea e restituisce la costruzione corrispondente al tipo indicato.
    public static Construction create(ConstructionType type)
    {
        switch (type)
        {
            case ROAD:
                return new Road();

            case PARK:
                return new Park();

            case RESIDENTIAL:
                return new Residential();

            case COMMERCIAL:
                return new Commercial();

            case INDUSTRIAL:
                return new Industrial();

            case POWER_PLANT:
                return new PowerPlant();

            case BANK:
                return new Bank();

            default:
                throw new IllegalArgumentException(
                        "Unknown construction type: " + type
                );
        }
    }
}