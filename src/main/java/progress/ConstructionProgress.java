package progress;

import model.Construction;
import model.ConstructionFactory;
import model.ConstructionType;
import model.NuclearPlant;
import model.TerroristicGroup;

// Rappresenta lo stato salvato di una costruzione, compresi il tipo, la posizione e gli attributi modificabili
public class ConstructionProgress {
    private ConstructionType type;

    private int row;
    private int column;

    private int population;
    private int populationGrowthRate;
    private int populationDecreaseRate;

    private double economyGrowthRate;
    private int moneyProduction;
    private boolean tsunamiInsured;
    private boolean fireProtected;
    private int terroristicGroupLifetimeTicks;

    public ConstructionProgress(
            ConstructionType type,
            int row,
            int column,
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction)
    {
        this(
                type,
                row,
                column,
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction,
                false
        );
    }

    public ConstructionProgress(
            ConstructionType type,
            int row,
            int column,
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction,
            boolean tsunamiInsured)
    {
        this(
                type,
                row,
                column,
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction,
                tsunamiInsured,
                false
        );
    }

    public ConstructionProgress(
            ConstructionType type,
            int row,
            int column,
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction,
            boolean tsunamiInsured,
            boolean fireProtected)
    {
        this(
                type,
                row,
                column,
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction,
                tsunamiInsured,
                fireProtected,
                0
        );
    }

    public ConstructionProgress(
            ConstructionType type,
            int row,
            int column,
            int population,
            int populationGrowthRate,
            int populationDecreaseRate,
            double economyGrowthRate,
            int moneyProduction,
            boolean tsunamiInsured,
            boolean fireProtected,
            int terroristicGroupLifetimeTicks)
    {
        this.type = type;
        this.row = row;
        this.column = column;
        this.population = population;
        this.populationGrowthRate = populationGrowthRate;
        this.populationDecreaseRate = populationDecreaseRate;
        this.economyGrowthRate = economyGrowthRate;
        this.moneyProduction = moneyProduction;
        this.tsunamiInsured = tsunamiInsured;
        this.fireProtected = fireProtected;
        this.terroristicGroupLifetimeTicks = terroristicGroupLifetimeTicks;
    }

    /* Crea una copia dello stato corrente di una costruzione,
    salvando anche la posizione che occupa nella griglia. */

    public static ConstructionProgress fromConstruction(
            Construction construction,
            int row,
            int column)
    {
        boolean fireProtected = false;
        int terroristicGroupLifetimeTicks = 0;

        if (construction instanceof NuclearPlant)
        {
            NuclearPlant nuclearPlant = (NuclearPlant) construction;
            fireProtected = nuclearPlant.isFireProtected();
        }

        if (construction instanceof TerroristicGroup)
        {
            TerroristicGroup terroristicGroup = (TerroristicGroup) construction;
            terroristicGroupLifetimeTicks = terroristicGroup.getLifetimeTicks();
        }

        return new ConstructionProgress(
                construction.getType(),
                row,
                column,
                construction.getPopulation(),
                construction.getPopulationGrowthRate(),
                construction.getPopulationDecreaseRate(),
                construction.getEconomyGrowthRate(),
                construction.getMoneyProduction(),
                construction.isTsunamiInsured(),
                fireProtected,
                terroristicGroupLifetimeTicks
        );
    }

    public ConstructionType getType()
    {
        return type;
    }

    public int getRow()
    {
        return row;
    }

    public int getColumn()
    {
        return column;
    }

    public int getPopulation()
    {
        return population;
    }

    public int getPopulationGrowthRate()
    {
        return populationGrowthRate;
    }

    public int getPopulationDecreaseRate()
    {
        return populationDecreaseRate;
    }

    public double getEconomyGrowthRate()
    {
        return economyGrowthRate;
    }

    public int getMoneyProduction()
    {
        return moneyProduction;
    }

    public boolean isTsunamiInsured()
    {
        return tsunamiInsured;
    }

    public boolean isFireProtected()
    {
        return fireProtected;
    }

    public int getTerroristicGroupLifetimeTicks()
    {
        return terroristicGroupLifetimeTicks;
    }

    /* Ricrea la costruzione del tipo corretto
   e ripristina i valori presenti nel salvataggio. */

    public Construction toConstruction()
    {
        if (type == null)
        {
            throw new IllegalStateException(
                    "The saved construction type cannot be null"
            );
        }

        Construction construction =
                ConstructionFactory.create(type);

        construction.restoreState(
                population,
                populationGrowthRate,
                populationDecreaseRate,
                economyGrowthRate,
                moneyProduction
        );

        construction.setTsunamiInsured(
                tsunamiInsured
        );

        if (construction instanceof NuclearPlant)
        {
            NuclearPlant nuclearPlant = (NuclearPlant) construction;
            nuclearPlant.setFireProtected(fireProtected);
        }

        if (construction instanceof TerroristicGroup)
        {
            TerroristicGroup terroristicGroup = (TerroristicGroup) construction;
            terroristicGroup.setLifetimeTicks(terroristicGroupLifetimeTicks);
        }

        return construction;
    }

}
