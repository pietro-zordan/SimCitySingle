package model;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

/* Coordina la distribuzione dell'energia della città.
   Gestisce aree servite, collegamenti, scollegamenti e riconnessioni
   senza affidare queste responsabilità alle singole centrali. */
public class EnergyManager {
    private static final int BASE_MAX_ENERGY_SERVED = 24000;
    private int maxEnergyServed = BASE_MAX_ENERGY_SERVED;

    private final Grid grid;
    private final Map<PowerPlant, Cell[]> nearbyCells = new HashMap<>();
    private final Map<PowerPlant, Queue<Construction>> servedConstructions = new HashMap<>();
    private final Map<PowerPlant, PriorityQueue<Construction>> candidatesForReconnection = new HashMap<>();

    // Crea il gestore dell'energia associato alla griglia della città.
    public EnergyManager(Grid grid) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid cannot be null");
        }

        this.grid = grid;
    }

    // Se la costruzione appena inserita è una centrale, la registra nel sistema energetico.
    public void registerConstruction(Construction construction, int row, int column) {
        if (construction instanceof PowerPlant) {
            PowerPlant powerPlant = (PowerPlant) construction;
            registerPowerPlant(powerPlant, row, column);
        }
    }

    // Memorizza le 48 celle attorno alla centrale e prepara le sue code di gestione.
    private void registerPowerPlant(PowerPlant powerPlant, int plantRow, int plantColumn) {
        Cell[] cellsInRange = new Cell[48];
        int numberOfNearbyCells = 0;

        for (int row = plantRow - 3; row <= plantRow + 3; row++) {
            for (int column = plantColumn - 3; column <= plantColumn + 3; column++) {
                if (grid.isInside(row, column) && (row != plantRow || column != plantColumn)) {
                    cellsInRange[numberOfNearbyCells] = grid.getCell(row, column);
                    numberOfNearbyCells++;
                }
            }
        }

        nearbyCells.put(powerPlant, cellsInRange);

        if (!servedConstructions.containsKey(powerPlant)) {
            servedConstructions.put(powerPlant, new ArrayDeque<>());
        }

        if (!candidatesForReconnection.containsKey(powerPlant)) {
            candidatesForReconnection.put(powerPlant, new PriorityQueue<>(new PowerComparator()));
        }
    }

    // Aggiorna tutte le centrali attive: prima collega nuovi edifici, poi prova a ricollegare quelli rimasti senza energia.
    public void updatePowerConnections() {
        for (PowerPlant powerPlant : grid.getAllPowerPlants()) {
            if (powerPlant.isActive()) {
                serveConstruction(powerPlant);
                reconnect(powerPlant);
            }
        }
    }

    // Collega alla centrale le costruzioni nella sua area finché la capacità massima non viene superata.
    public void serveConstruction(PowerPlant powerPlant) {
        Cell[] cellsInRange = nearbyCells.get(powerPlant);

        if (cellsInRange == null) {
            return;
        }

        Queue<Construction> served = servedConstructions.get(powerPlant);
        PriorityQueue<Construction> candidates = candidatesForReconnection.get(powerPlant);

        for (int i = 0; i < cellsInRange.length; i++) {
            Cell cell = cellsInRange[i];

            if (cell != null && !cell.isEmpty()) {
                Construction construction = cell.getConstruction();

                if (construction.requiresPower()
                        && !construction.isPowerPlantConnected()
                        && !served.contains(construction)
                        && !candidates.contains(construction)) {
                    construction.connectToPowerPlant(powerPlant);
                    served.offer(construction);
                }
            }
        }

        while (calculateUsedPower(powerPlant) > powerPlant.getPowerCapacity() && !served.isEmpty()) {
            unplug(powerPlant);
        }
    }

    // Quando una centrale è sovraccarica, scollega la costruzione servita da più tempo e la mette tra i candidati al riaggancio.
    private void unplug(PowerPlant powerPlant) {
        Queue<Construction> served = servedConstructions.get(powerPlant);
        PriorityQueue<Construction> candidates = candidatesForReconnection.get(powerPlant);
        Construction unpluggedConstruction = served.poll();

        if (unpluggedConstruction != null) {
            if (unpluggedConstruction.getConnectedPowerPlant() == powerPlant) {
                unpluggedConstruction.disconnectFromPowerPlant();
            }

            candidates.add(unpluggedConstruction);
        }
    }

    // Usa l'energia rimasta per ricollegare gli edifici, dando priorità a quelli che consumano meno.
    public void reconnect(PowerPlant powerPlant) {
        PriorityQueue<Construction> candidates = candidatesForReconnection.get(powerPlant);
        Queue<Construction> served = servedConstructions.get(powerPlant);

        if (candidates == null || served == null) {
            return;
        }

        int availablePower = powerPlant.getPowerCapacity() - calculateUsedPower(powerPlant);

        while (!candidates.isEmpty()) {
            Construction candidate = candidates.peek();

            if (!isStillNearby(powerPlant, candidate)) {
                candidates.remove();
            }

            else if (candidate.isPowerPlantConnected()) {
                candidates.remove();
            }

            else if (candidate.getPowerConsumption() <= availablePower) {
                candidates.remove();
                candidate.connectToPowerPlant(powerPlant);
                served.add(candidate);
                availablePower = availablePower - candidate.getPowerConsumption();
            }

            else {
                return;
            }
        }
    }

    // Controlla che una costruzione candidata al riaggancio sia ancora presente nell'area della stessa centrale.
    private boolean isStillNearby(PowerPlant powerPlant, Construction construction) {
        Cell[] cellsInRange = nearbyCells.get(powerPlant);

        if (cellsInRange == null) {
            return false;
        }

        for (int i = 0; i < cellsInRange.length; i++) {
            Cell cell = cellsInRange[i];

            if (cell != null && !cell.isEmpty() && cell.getConstruction() == construction) {
                return true;
            }
        }

        return false;
    }

    // Somma il consumo delle costruzioni attualmente collegate alla centrale indicata.
    private int calculateUsedPower(PowerPlant powerPlant) {
        Queue<Construction> served = servedConstructions.get(powerPlant);

        if (served == null) {
            return 0;
        }

        int usedPower = 0;

        for (Construction construction : served) {
            usedPower = usedPower + construction.getPowerConsumption();
        }

        return usedPower;
    }

    // Restituisce quanta energia sta realmente usando una centrale attiva.
    public int getUsedPower(PowerPlant powerPlant) {
        if (powerPlant == null || !powerPlant.isActive()) {
            return 0;
        }

        return calculateUsedPower(powerPlant);
    }

    // Elimina una costruzione dalle code energetiche e la scollega dalla centrale a cui apparteneva.
    public void removeConstruction(Construction construction) {
        if (construction == null) {
            return;
        }

        if (construction instanceof PowerPlant) {

            PowerPlant powerPlant = (PowerPlant) construction;
            disconnectAll(powerPlant);
            nearbyCells.remove(powerPlant);
            servedConstructions.remove(powerPlant);
            candidatesForReconnection.remove(powerPlant);
            return;

        }

        for (Queue<Construction> served : servedConstructions.values()) {
            served.remove(construction);
        }

        for (PriorityQueue<Construction> candidates : candidatesForReconnection.values()) {
            candidates.remove(construction);
        }

        if (construction.isPowerPlantConnected()) {
            construction.disconnectFromPowerPlant();
        }
    }

    // Scollega tutte le costruzioni servite dalla centrale e svuota le sue code.
    public void disconnectAll(PowerPlant powerPlant) {
        Queue<Construction> served = servedConstructions.get(powerPlant);

        if (served != null) {
            for (Construction construction : served) {
                if (construction.getConnectedPowerPlant() == powerPlant) {
                    construction.disconnectFromPowerPlant();
                }
            }

            served.clear();
        }

        PriorityQueue<Construction> candidates = candidatesForReconnection.get(powerPlant);

        if (candidates != null) {
            candidates.clear();
        }
    }

    // Dopo un caricamento ricrea le aree delle centrali e ricostruisce tutti i collegamenti energetici.
    public void rebuildConnections() {
        for (Construction construction : grid.getConstructions()) {
            if (construction.isPowerPlantConnected()) {
                construction.disconnectFromPowerPlant();
            }
        }

        nearbyCells.clear();
        servedConstructions.clear();
        candidatesForReconnection.clear();

        for (int row = 0; row < grid.getNumberOfRows(); row++) {
            for (int column = 0; column < grid.getNumberOfColumns(); column++) {
                Cell cell = grid.getCell(row, column);

                if (!cell.isEmpty() && cell.getConstruction() instanceof PowerPlant) {
                    PowerPlant powerPlant = (PowerPlant) cell.getConstruction();
                    registerPowerPlant(powerPlant, row, column);
                }
            }
        }

        for (PowerPlant powerPlant : grid.getAllPowerPlants()) {
            if (powerPlant.isActive()) {
                serveConstruction(powerPlant);
            }
        }
    }

    // Restituisce il consumo delle sole costruzioni che in questo momento stanno ricevendo energia.
    public int getEnergyConsumed() {
        int energyConsumed = 0;

        for (Construction construction : grid.getConstructions()) {
            if (construction.requiresPower() && construction.isPowered()) {
                energyConsumed = energyConsumed + construction.getPowerConsumption();
            }
        }

        return energyConsumed;
    }

    // Restituisce il consumo richiesto da tutte le costruzioni, comprese quelle attualmente senza energia.
    public int getTotalEnergyDemand() {
        int totalEnergyDemand = 0;

        for (Construction construction : grid.getConstructions()) {
            if (construction.requiresPower()) {
                totalEnergyDemand = totalEnergyDemand + construction.getPowerConsumption();
            }
        }

        return totalEnergyDemand;
    }

    // Restituisce la somma dell'energia prodotta in questo momento da tutte le centrali attive.
    public int getEnergyAvailable() {
        int energyAvailable = 0;

        for (PowerPlant powerPlant : grid.getAllPowerPlants()) {
            energyAvailable = energyAvailable + powerPlant.getPowerGenerated();
        }

        return energyAvailable;
    }

    // Ricalcola la capacità massima della rete: 24000 di base più 10000 per ogni centrale nucleare presente.
    public void setMaxEnergyServed() {
        int numOfNuclearPlants = grid.getNumOfNuclearPlants();
        maxEnergyServed = BASE_MAX_ENERGY_SERVED + numOfNuclearPlants * NuclearPlant.getNetworkCapacityIncrease();
    }

    // Controlla se la rete può sostenere anche il consumo della nuova costruzione.
    // La centrale nucleare è sempre ammessa perché serve proprio ad aumentare il limite della rete.
    public boolean canSupportConstruction(Construction construction) {
        if (construction == null) {
            return false;
        }

        if (construction instanceof NuclearPlant) {
            return true;
        }

        int futureEnergyDemand = getTotalEnergyDemand() + construction.getPowerConsumption();
        return futureEnergyDemand <= getMaxEnergyServed();
    }

    // Mostra la centrale nucleare quando aggiungere l'edificio che consuma di più manderebbe la rete oltre il limite.
    public boolean shouldShowNuclearPlant() {
        int highestPowerConsumption = new Industrial().getPowerConsumption();
        return getTotalEnergyDemand() + highestPowerConsumption > getMaxEnergyServed();
    }

    // Restituisce il limite massimo attuale della rete, aggiornandolo in base alle centrali nucleari presenti.
    public int getMaxEnergyServed() {
        setMaxEnergyServed();
        return maxEnergyServed;
    }


}
