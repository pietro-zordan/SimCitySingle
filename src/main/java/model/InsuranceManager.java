package model;

import Events.Tsunami;

import java.util.ArrayDeque;
import java.util.List;

/*
 * Gestisce le assicurazioni della città.
 * Per ora supporta l'assicurazione contro lo tsunami.
 */
public class InsuranceManager
{
    private static final double BANK_DISCOUNT = 0.05;
    private static final double MAX_BANK_DISCOUNT = 0.50;

    private static final int RESIDENTIAL_COST = 80;
    private static final int COMMERCIAL_COST = 120;
    private static final int INDUSTRIAL_COST = 200;
    private static final int POWER_PLANT_COST = 240;
    private static final int BANK_COST = 160;
    private static final int CONSTRUCTION_COMPANY_COST = 200;
    private static final int POLICE_STATION_COST = 200;

    private final City city;
    private final Grid grid;

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
    }

    public boolean hasBanks()
    {
        return grid.getNumberOfBanks() > 0;
    }

    /*
     * Non esiste un cooldown temporale:
     * l'assicurazione può essere acquistata/estesa ogni volta
     * che esistono nuovi edifici a rischio non ancora coperti.
     */
    public boolean canBuyTsunamiInsurance()
    {
        return hasBanks()
                && getTsunamiInsuranceBuildingCount() > 0;
    }

    public boolean isTsunamiInsuranceActive()
    {
        return tsunamiInsuranceActive;
    }

    /*
     * Il costo comprende soltanto gli edifici a rischio
     * che non sono già assicurati.
     */
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

                if (needsTsunamiCoverage(
                        construction,
                        row,
                        column))
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

    /*
     * Restituisce quanti edifici verrebbero coperti
     * premendo Apply insurance in questo momento.
     */
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

                if (needsTsunamiCoverage(
                        construction,
                        row,
                        column))
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

    /*
     * La prima applicazione assicura gli edifici presenti.
     * Le applicazioni successive coprono soltanto quelli aggiunti dopo.
     */
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

        insureCurrentlyUncoveredBuildings();

        tsunamiInsuranceActive = true;

        return true;
    }

    public void registerTsunamiDamage(
            List<ReconstructionEntry> destroyedBuildings,
            String direction)
    {
        if (!tsunamiInsuranceActive
                || destroyedBuildings == null)
        {
            return;
        }

        boolean hasCoveredDamage = false;

        for (ReconstructionEntry entry
                : destroyedBuildings)
        {
            if (entry != null
                    && entry.construction()
                            .isTsunamiInsured())
            {
                reconstructionQueue.addLast(
                        entry
                );

                hasCoveredDamage = true;
            }
        }

        if (hasCoveredDamage)
        {
            reserveTsunamiArea(
                    direction
            );
        }
    }

    public void startTsunamiReconstruction()
    {
        reconstructionActive =
                !reconstructionQueue.isEmpty();
    }

    /*
     * Ricostruisce un edificio assicurato per tick usando
     * lo stesso oggetto e quindi gli stessi parametri precedenti.
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
            finishReconstruction();
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

        if (reconstructionQueue.isEmpty())
        {
            finishReconstruction();
        }
    }

    private void insureCurrentlyUncoveredBuildings()
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

                if (needsTsunamiCoverage(
                        construction,
                        row,
                        column))
                {
                    construction.setTsunamiInsured(
                            true
                    );
                }
            }
        }
    }

    private boolean needsTsunamiCoverage(
            Construction construction,
            int row,
            int column)
    {
        return construction != null
                && !construction.isTsunamiInsured()
                && isAtTsunamiRisk(
                        row,
                        column
                )
                && isInsurable(
                        construction.getType()
                );
    }

    private void finishReconstruction()
    {
        reconstructionActive = false;
        grid.releaseAllReconstructionReservations();
    }

    private void reserveTsunamiArea(
            String direction)
    {
        int depth =
                Tsunami.ADVANCEMENT_LENGTH;

        if ("UP".equals(direction))
        {
            for (int row = 0;
                 row < depth;
                 row++)
            {
                reserveRow(row);
            }
        }
        else if ("DOWN".equals(direction))
        {
            for (int row =
                 grid.getNumberOfRows() - depth;
                 row < grid.getNumberOfRows();
                 row++)
            {
                reserveRow(row);
            }
        }
        else if ("LEFT".equals(direction))
        {
            for (int column = 0;
                 column < depth;
                 column++)
            {
                reserveColumn(column);
            }
        }
        else if ("RIGHT".equals(direction))
        {
            for (int column =
                 grid.getNumberOfColumns() - depth;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                reserveColumn(column);
            }
        }
    }

    private void reserveRow(int row)
    {
        for (int column = 0;
             column < grid.getNumberOfColumns();
             column++)
        {
            grid.reserveCellForReconstruction(
                    row,
                    column
            );
        }
    }

    private void reserveColumn(int column)
    {
        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            grid.reserveCellForReconstruction(
                    row,
                    column
            );
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
