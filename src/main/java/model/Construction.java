package model;

/* Rappresenta la base comune di tutte le costruzioni.
   Gestisce produzione, consumi e collegamenti a strada e centrale elettrica. */
public abstract class Construction {

    private boolean roadConnected;
    private PowerPlant connectedPowerPlant;
    protected int moneyProduction;
    private final int powerConsumption;
    private final int placementPrice;


    // Inizializza le caratteristiche comuni della costruzione.
    public Construction(int moneyProduction, int powerConsumption, int placementPrice)
    {
        this.moneyProduction=moneyProduction;
        this.powerConsumption=powerConsumption;
        this.placementPrice=placementPrice;

    }

    /*Verifica dello stato della connessione della centrale elettrica
    passata come parametro*/
    public boolean isPowerPlantConnected()
    {
        return connectedPowerPlant!=null;
    }


    //restituisce la centrale elettrica a cui è collegata la costruzione
    public PowerPlant getConnectedPowerPlant()
    {
        return connectedPowerPlant;
    }


    //effettua il collegamento alla centrale passata come parametro
    public void connectToPowerPlant(PowerPlant powerPlant)
    {
        if (powerPlant == null)
        {
            throw new IllegalArgumentException(
                    "Power plant cannot be null"
            );
        }

        connectedPowerPlant=powerPlant;
    }

    //scollega la costruzione dalla centrale elettrica a cui è collegato
    public void disconnectFromPowerPlant()
    {
        connectedPowerPlant = null;
    }

    // Restituisce la quantità di energia consumata dalla costruzione.
    public int getPowerConsumption()
    {
        return powerConsumption;
    }

    // Verifica se la costruzione necessita di energia elettrica.
    public boolean requiresPower()
    {
        return powerConsumption > 0;
    }

    /* Restituisce l'impatto della costruzione sull'inquinamento.
       Le sottoclassi possono ridefinire questo valore. */
    public int getPollutionImpact()
    {
        return 0;
    }

    /* Restituisce l'impatto della costruzione sulla felicità.
       Le sottoclassi possono ridefinire questo valore. */
    public int getHappinessImpact()
    {
        return 0;
    }

    // Restituisce il prezzo base necessario per piazzare la costruzione.
    public int getPlacementPrice()
    {
        return placementPrice;
    }

    // Restituisce il denaro prodotto dalla costruzione a ogni tick.
    public int getMoneyProduction()
    {
        return moneyProduction;
    }

    /* Restituisce la popolazione presente nella costruzione.
      Il valore viene ridefinito dalle costruzioni residenziali. */
    public int getPopulation()
    {
        return 0;
    }

    /* Aggiorna la costruzione di un tick.
      L'implementazione di base non esegue alcuna operazione. */
    public void updateOfOneTick()
    {
        return;
    }

    //comunica alla classe cell se bisogna rimuovere la costruzione
    public boolean mustBeRemoved()
    {
        return false;
    }

    // Esegue le operazioni necessarie prima di rimuovere la costruzione.
    public void prepareForRemoval()
    {
        if (isPowerPlantConnected())
        {
            connectedPowerPlant.removeConstruction(this);
        }
    }

    // Registra se la costruzione è collegata a una strada
    public void setRoadConnected(boolean connectedState) {
        this.roadConnected = connectedState; // "this." toglie ogni ambiguità
    }

    // Verifica se la costruzione è collegata a una strada.
    public boolean isRoadConnected() {
        return roadConnected;
    }

    //verifica se una costruzione sta ricevendo l'energia da lei richiesta
    public boolean isPowered()
    {
        return !requiresPower()
                || (connectedPowerPlant != null
                && connectedPowerPlant.isActive());
    }

    /* Restituisce il numero di posti di lavoro offerti.
      Le sottoclassi economiche ridefiniscono questo valore. */
    public int getNumberOfEmployee(){
        return 0;
    }

    /* Restituisce il tasso di crescita della popolazione.
     Il valore viene ridefinito dalle costruzioni residenziali. */
    public int getPopulationGrowthRate()
    {
        return 0;
    }


    /* Restituisce il tasso di diminuzione della popolazione.
      Il valore viene ridefinito dalle costruzioni residenziali. */
    public int getPopulationDecreaseRate()
    {
        return 0;
    }

    /* Restituisce il tasso di crescita economica.
      Il valore viene ridefinito dalle costruzioni economiche. */
    public double getEconomyGrowthRate()
    {
        return 0;
    }

    // Restituisce il tipo della costruzione
    public abstract ConstructionType getType();

    /* Ripristina gli attributi comuni della costruzione.
   Le sottoclassi ripristinano anche i propri attributi specifici. */
    public void restoreState(
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction)
    {
        this.moneyProduction = moneyProduction;
    }

    /* Esegue le eventuali inizializzazioni richieste dopo il piazzamento.
   L'implementazione di base non esegue alcuna operazione. */
    public void initializeAfterPlacement(
            Grid grid,
            int row,
            int column)
    {
    }

    //stabilisce se una costruzione può venir rimossa
    public boolean canBeRemoved()
    {
        return true;
    }

}
