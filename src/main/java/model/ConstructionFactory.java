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

            case CONSTRUCTION_COMPANY:
                return new ConstructionCompany();

            case CRIMINAL_ACTIVITY:
                return new CriminalActivity();

            case POLICE_STATION:
                return new PoliceStation();

            case GRASS:
                return new Grass();

            case TERRORISTIC_GROUP:
                return new TerroristicGroup();

            case NUCLEAR_PLANT:
                return new NuclearPlant();

            case WAST_TREATMENT_PLANT:
                return new WasteTreatmentPlant();

            default:
                throw new IllegalArgumentException(
                        "Unknown construction type: " + type
                );
        }
    }
}