package model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/* Coordina la distribuzione dell'energia della città.
   Gestisce aree servite, collegamenti, scollegamenti e riconnessioni
   senza affidare queste responsabilità alle singole centrali. */
public class EnergyManager
{
    private static final int MAX_ENERGY_SERVED = 24000;

    private final Grid grid;

    private final Map<PowerPlant, List<Cell>> nearbyCells =
            new HashMap<>();

    private final Map<PowerPlant, Queue<Construction>> servedConstructions =
            new HashMap<>();

    private final Map<PowerPlant, PriorityQueue<Construction>>
            candidatesForReconnection =
            new HashMap<>();

    public EnergyManager(Grid grid)
    {
        if (grid == null)
        {
            throw new IllegalArgumentException(
                    "Grid cannot be null"
            );
        }

        this.grid = grid;
    }

    // Registra una nuova centrale e memorizza le celle comprese nella sua area di servizio.
    public void registerConstruction(
            Construction construction,
            int row,
            int column)
    {
        if (construction instanceof PowerPlant)
        {
            PowerPlant powerPlant =
                    (PowerPlant) construction;

            registerPowerPlant(
                    powerPlant,
                    row,
                    column
            );
        }
    }

    private void registerPowerPlant(
            PowerPlant powerPlant,
            int plantRow,
            int plantColumn)
    {
        List<Cell> cellsInRange =
                new ArrayList<>();

        for (int row = plantRow - 3;
             row <= plantRow + 3;
             row++)
        {
            for (int column = plantColumn - 3;
                 column <= plantColumn + 3;
                 column++)
            {
                if (grid.isInside(row, column)
                        && (row != plantRow
                        || column != plantColumn))
                {
                    cellsInRange.add(
                            grid.getCell(
                                    row,
                                    column
                            )
                    );
                }
            }
        }

        nearbyCells.put(
                powerPlant,
                cellsInRange
        );

        if (!servedConstructions.containsKey(
                powerPlant))
        {
            servedConstructions.put(
                    powerPlant,
                    new ArrayDeque<>()
            );
        }

        if (!candidatesForReconnection.containsKey(
                powerPlant))
        {
            candidatesForReconnection.put(
                    powerPlant,
                    new PriorityQueue<>(
                            new PowerComparator()
                    )
            );
        }
    }

    // Aggiorna la distribuzione dell'energia di tutte le centrali attive.
    public void updatePowerConnections()
    {
        for (PowerPlant powerPlant
                : grid.getAllPowerPlants())
        {
            if (powerPlant.isActive())
            {
                serveConstruction(
                        powerPlant
                );

                reconnect(
                        powerPlant
                );
            }
        }
    }

    // Collega alla centrale le costruzioni nella sua area che richiedono energia.
    public void serveConstruction(
            PowerPlant powerPlant)
    {
        List<Cell> cellsInRange =
                nearbyCells.get(powerPlant);

        if (cellsInRange == null)
        {
            return;
        }

        Queue<Construction> served =
                servedConstructions.get(
                        powerPlant
                );

        PriorityQueue<Construction> candidates =
                candidatesForReconnection.get(
                        powerPlant
                );

        for (Cell cell : cellsInRange)
        {
            if (!cell.isEmpty())
            {
                Construction construction =
                        cell.getConstruction();

                if (construction.requiresPower()
                        && !construction.isPowerPlantConnected()
                        && !served.contains(construction)
                        && !candidates.contains(construction))
                {
                    construction.connectToPowerPlant(
                            powerPlant
                    );

                    served.offer(
                            construction
                    );
                }
            }
        }

        while (calculateUsedPower(powerPlant)
                > powerPlant.getPowerCapacity()
                && !served.isEmpty())
        {
            unplug(
                    powerPlant
            );
        }
    }

    // Scollega la costruzione servita da più tempo quando la capacità viene superata.
    private void unplug(
            PowerPlant powerPlant)
    {
        Queue<Construction> served =
                servedConstructions.get(
                        powerPlant
                );

        PriorityQueue<Construction> candidates =
                candidatesForReconnection.get(
                        powerPlant
                );

        Construction unpluggedConstruction =
                served.poll();

        if (unpluggedConstruction != null)
        {
            if (unpluggedConstruction
                    .getConnectedPowerPlant()
                    == powerPlant)
            {
                unpluggedConstruction
                        .disconnectFromPowerPlant();
            }

            candidates.add(
                    unpluggedConstruction
            );
        }
    }

    // Ricollega prima le costruzioni che consumano meno energia.
    public void reconnect(
            PowerPlant powerPlant)
    {
        PriorityQueue<Construction> candidates =
                candidatesForReconnection.get(
                        powerPlant
                );

        Queue<Construction> served =
                servedConstructions.get(
                        powerPlant
                );

        if (candidates == null
                || served == null)
        {
            return;
        }

        int availablePower =
                powerPlant.getPowerCapacity()
                        - calculateUsedPower(
                        powerPlant
                );

        while (!candidates.isEmpty())
        {
            Construction candidate =
                    candidates.peek();

            if (!isStillNearby(
                    powerPlant,
                    candidate))
            {
                candidates.remove();
            }
            else if (candidate
                    .isPowerPlantConnected())
            {
                candidates.remove();
            }
            else if (candidate
                    .getPowerConsumption()
                    <= availablePower)
            {
                candidates.remove();

                candidate.connectToPowerPlant(
                        powerPlant
                );

                served.add(
                        candidate
                );

                availablePower =
                        availablePower
                                - candidate
                                .getPowerConsumption();
            }
            else
            {
                return;
            }
        }
    }

    private boolean isStillNearby(
            PowerPlant powerPlant,
            Construction construction)
    {
        List<Cell> cellsInRange =
                nearbyCells.get(powerPlant);

        if (cellsInRange == null)
        {
            return false;
        }

        for (Cell cell : cellsInRange)
        {
            if (!cell.isEmpty()
                    && cell.getConstruction()
                    == construction)
            {
                return true;
            }
        }

        return false;
    }

    private int calculateUsedPower(
            PowerPlant powerPlant)
    {
        Queue<Construction> served =
                servedConstructions.get(
                        powerPlant
                );

        if (served == null)
        {
            return 0;
        }

        int usedPower = 0;

        for (Construction construction
                : served)
        {
            usedPower =
                    usedPower
                            + construction
                            .getPowerConsumption();
        }

        return usedPower;
    }

    // Restituisce l'energia realmente utilizzata dalla singola centrale.
    public int getUsedPower(
            PowerPlant powerPlant)
    {
        if (powerPlant == null
                || !powerPlant.isActive())
        {
            return 0;
        }

        return calculateUsedPower(
                powerPlant
        );
    }

    // Rimuove una costruzione dalle strutture usate per la distribuzione dell'energia.
    public void removeConstruction(
            Construction construction)
    {
        if (construction == null)
        {
            return;
        }

        if (construction
                instanceof PowerPlant)
        {
            PowerPlant powerPlant =
                    (PowerPlant) construction;

            disconnectAll(
                    powerPlant
            );

            nearbyCells.remove(
                    powerPlant
            );

            servedConstructions.remove(
                    powerPlant
            );

            candidatesForReconnection.remove(
                    powerPlant
            );

            return;
        }

        for (Queue<Construction> served
                : servedConstructions.values())
        {
            served.remove(
                    construction
            );
        }

        for (PriorityQueue<Construction> candidates
                : candidatesForReconnection.values())
        {
            candidates.remove(
                    construction
            );
        }

        if (construction
                .isPowerPlantConnected())
        {
            construction
                    .disconnectFromPowerPlant();
        }
    }

    // Scollega tutte le costruzioni servite dalla centrale indicata.
    public void disconnectAll(
            PowerPlant powerPlant)
    {
        Queue<Construction> served =
                servedConstructions.get(
                        powerPlant
                );

        if (served != null)
        {
            for (Construction construction
                    : served)
            {
                if (construction
                        .getConnectedPowerPlant()
                        == powerPlant)
                {
                    construction
                            .disconnectFromPowerPlant();
                }
            }

            served.clear();
        }

        PriorityQueue<Construction> candidates =
                candidatesForReconnection.get(
                        powerPlant
                );

        if (candidates != null)
        {
            candidates.clear();
        }
    }

    // Ricostruisce da zero i collegamenti energetici dopo il caricamento di una partita.
    public void rebuildConnections()
    {
        for (Construction construction
                : grid.getConstructions())
        {
            if (construction
                    .isPowerPlantConnected())
            {
                construction
                        .disconnectFromPowerPlant();
            }
        }

        nearbyCells.clear();
        servedConstructions.clear();
        candidatesForReconnection.clear();

        for (int row = 0;
             row < grid.getNumberOfRows();
             row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Cell cell =
                        grid.getCell(
                                row,
                                column
                        );

                if (!cell.isEmpty()
                        && cell.getConstruction()
                        instanceof PowerPlant)
                {
                    PowerPlant powerPlant =
                            (PowerPlant)
                                    cell.getConstruction();

                    registerPowerPlant(
                            powerPlant,
                            row,
                            column
                    );
                }
            }
        }

        for (PowerPlant powerPlant
                : grid.getAllPowerPlants())
        {
            if (powerPlant.isActive())
            {
                serveConstruction(
                        powerPlant
                );
            }
        }
    }

    public int getEnergyConsumed()
    {
        int energyConsumed = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction.requiresPower()
                    && construction.isPowered())
            {
                energyConsumed =
                        energyConsumed
                                + construction
                                .getPowerConsumption();
            }
        }

        return energyConsumed;
    }

    public int getTotalEnergyDemand()
    {
        int totalEnergyDemand = 0;

        for (Construction construction
                : grid.getConstructions())
        {
            if (construction.requiresPower())
            {
                totalEnergyDemand =
                        totalEnergyDemand
                                + construction
                                .getPowerConsumption();
            }
        }

        return totalEnergyDemand;
    }

    public int getEnergyAvailable()
    {
        int energyAvailable = 0;

        for (PowerPlant powerPlant
                : grid.getAllPowerPlants())
        {
            energyAvailable =
                    energyAvailable
                            + powerPlant
                            .getPowerGenerated();
        }

        return energyAvailable;
    }

    public int getMaxEnergyServed()
    {
        return MAX_ENERGY_SERVED;
    }
}
