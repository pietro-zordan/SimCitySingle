package model;

import Events.Tsunami;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/*
 * Gestisce le assicurazioni della città.
 * Per ora supporta l'assicurazione contro lo tsunami.
 */
public class InsuranceManager
{
    private static final double BANK_DISCOUNT = 0.05;
    private static final double MAX_BANK_DISCOUNT = 0.50;

    private static final int RESIDENTIAL_COST = 40;
    private static final int COMMERCIAL_COST = 60;
    private static final int INDUSTRIAL_COST = 100;
    private static final int POWER_PLANT_COST = 120;
    private static final int BANK_COST = 80;
    private static final int CONSTRUCTION_COMPANY_COST = 100;
    private static final int POLICE_STATION_COST = 100;

    private final City city;
    private final Grid grid;

    private final Set<Construction> tsunamiCoveredBuildings =
            Collections.newSetFromMap(
                    new IdentityHashMap<Construction, Boolean>()
            );

    private final ArrayDeque<ReconstructionEntry>
            reconstructionQueue =
            new ArrayDeque<>();

    private boolean tsunamiInsuranceActive;
    private boolean reconstructionActive;

    public InsuranceManager(
            City city,
            Grid grid)
    {
        this(city, grid, false);
    }

    public InsuranceManager(
            City city,
            Grid grid,
            boolean tsunamiInsuranceActive)
    {
        if (city == null)
        {
            throw new IllegalArgumentException(
                    "The city cannot be null"
            );
        }

        if (grid == null)
        {
            throw new IllegalArgumentException(
                    "The grid cannot be null"
            );
        }

        this.city = city;
        this.grid = grid;
        this.tsunamiInsuranceActive =
                tsunamiInsuranceActive;

        if (tsunamiInsuranceActive)
        {
            coverExistingRiskBuildings();
        }
    }

    public boolean hasBanks()
    {
        return grid.getNumberOfBanks() > 0;
    }

    public boolean canBuyTsunamiInsurance()
    {
        return hasBanks()
                && !tsunamiInsuranceActive;
    }

    public boolean isTsunamiInsuranceActive()
    {
        return tsunamiInsuranceActive;
    }

    public int getTsunamiInsuranceCost()
    {
        int baseCost = 0;

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Construction construction =
                        grid.getCell(
                                row,
                                column
                        ).getConstruction();

                if (construction != null
                        && isAtTsunamiRisk(
                                row,
                                column
                        )
                        && isInsurable(
                                construction.getType()
                        ))
                {
                    baseCost +=
                            getBuildingInsuranceCost(
                                    construction.getType()
                            );
                }
            }
        }

        return applyBankDiscount(
                baseCost,
                grid.getNumberOfBanks()
        );
    }

    public int getTsunamiInsuranceBuildingCount()
    {
        int count = 0;

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Construction construction =
                        grid.getCell(
                                row,
                                column
                        ).getConstruction();

                if (construction != null
                        && isAtTsunamiRisk(
                                row,
                                column
                        )
                        && isInsurable(
                                construction.getType()
                        ))
                {
                    count++;
                }
            }
        }

        return count;
    }

    public int getBankDiscountPercentage()
    {
        return Math.min(
                grid.getNumberOfBanks() * 5,
                (int) (MAX_BANK_DISCOUNT * 100)
        );
    }

    public boolean buyTsunamiInsurance()
    {
        if (!canBuyTsunamiInsurance())
        {
            return false;
        }

        int cost = getTsunamiInsuranceCost();

        if (city.getBudget() < cost)
        {
            return false;
        }

        city.updateBudget(-cost);

        tsunamiInsuranceActive = true;
        coverExistingRiskBuildings();

        return true;
    }

    public int getAdditionalCoverageCost(
            ConstructionType type,
            int row,
            int column)
    {
        if (!tsunamiInsuranceActive
                || !isAtTsunamiRisk(row, column)
                || !isInsurable(type))
        {
            return 0;
        }

        int numberOfBanks =
                grid.getNumberOfBanks();

        if (type == ConstructionType.BANK)
        {
            numberOfBanks++;
        }

        return applyBankDiscount(
                getBuildingInsuranceCost(type),
                numberOfBanks
        );
    }

    public void coverNewConstruction(
            Construction construction,
            int row,
            int column,
            int additionalCost)
    {
        if (!tsunamiInsuranceActive
                || construction == null
                || !isAtTsunamiRisk(row, column)
                || !isInsurable(
                        construction.getType()
                ))
        {
            return;
        }

        if (additionalCost > 0)
        {
            city.updateBudget(
                    -additionalCost
            );
        }

        tsunamiCoveredBuildings.add(
                construction
        );
    }

    public void registerTsunamiDamage(
            List<ReconstructionEntry> destroyedBuildings)
    {
        if (!tsunamiInsuranceActive
                || destroyedBuildings == null)
        {
            return;
        }

        for (ReconstructionEntry entry
                : destroyedBuildings)
        {
            if (entry != null
                    && tsunamiCoveredBuildings.contains(
                            entry.construction()
                    ))
            {
                reconstructionQueue.addLast(
                        entry
                );

                grid.reserveCellForReconstruction(
                        entry.row(),
                        entry.column()
                );
            }
        }
    }

    public void startTsunamiReconstruction()
    {
        reconstructionActive =
                !reconstructionQueue.isEmpty();
    }

    /*
     * Ricostruisce un edificio assicurato per tick.
     * Viene riutilizzato lo stesso oggetto distrutto, quindi conserva
     * popolazione, crescita economica e gli altri parametri raggiunti.
     */
    public void updateReconstruction()
    {
        if (!reconstructionActive)
        {
            return;
        }

        ReconstructionEntry entry =
                reconstructionQueue.pollFirst();

        if (entry == null)
        {
            reconstructionActive = false;
            return;
        }

        Cell cell =
                grid.getCell(
                        entry.row(),
                        entry.column()
                );

        if (cell.isEmpty())
        {
            grid.restoreConstruction(
                    entry.construction(),
                    entry.row(),
                    entry.column()
            );

            grid.rebuildConnectionsAfterLoad();
            city.refreshStatistics();
        }

        grid.releaseCellFromReconstruction(
                entry.row(),
                entry.column()
        );

        if (reconstructionQueue.isEmpty())
        {
            reconstructionActive = false;
        }
    }

    private void coverExistingRiskBuildings()
    {
        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Construction construction =
                        grid.getCell(
                                row,
                                column
                        ).getConstruction();

                if (construction != null
                        && isAtTsunamiRisk(
                                row,
                                column
                        )
                        && isInsurable(
                                construction.getType()
                        ))
                {
                    tsunamiCoveredBuildings.add(
                            construction
                    );
                }
            }
        }
    }

    private boolean isAtTsunamiRisk(
            int row,
            int column)
    {
        int riskDepth =
                Tsunami.ADVANCEMENT_LENGTH;

        return row < riskDepth
                || column < riskDepth
                || row >= grid.getNumberOfRows()
                - riskDepth
                || column >= grid.getNumberOfColumns()
                - riskDepth;
    }

    private boolean isInsurable(
            ConstructionType type)
    {
        return type == ConstructionType.RESIDENTIAL
                || type == ConstructionType.COMMERCIAL
                || type == ConstructionType.INDUSTRIAL
                || type == ConstructionType.POWER_PLANT
                || type == ConstructionType.BANK
                || type
                == ConstructionType.CONSTRUCTION_COMPANY
                || type == ConstructionType.POLICE_STATION;
    }

    private int getBuildingInsuranceCost(
            ConstructionType type)
    {
        return switch (type)
        {
            case RESIDENTIAL -> RESIDENTIAL_COST;
            case COMMERCIAL -> COMMERCIAL_COST;
            case INDUSTRIAL -> INDUSTRIAL_COST;
            case POWER_PLANT -> POWER_PLANT_COST;
            case BANK -> BANK_COST;
            case CONSTRUCTION_COMPANY ->
                    CONSTRUCTION_COMPANY_COST;
            case POLICE_STATION ->
                    POLICE_STATION_COST;
            default -> 0;
        };
    }

    private int applyBankDiscount(
            int baseCost,
            int numberOfBanks)
    {
        double discount =
                Math.min(
                        numberOfBanks
                                * BANK_DISCOUNT,
                        MAX_BANK_DISCOUNT
                );

        return (int) Math.round(
                baseCost * (1.0 - discount)
        );
    }
}
