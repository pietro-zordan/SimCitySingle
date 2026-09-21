package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CrimeManager {
    private static final double CRIME_ECONOMY_FACTOR = 0.00008;
    private static final double MAX_CRIME_PROBABILITY = 0.25;
    private static final int CRIME_UNLOCK_TICK = 20;
    private static final int HAPPINESS_DROP_THRESHOLD = 1200;

    private final Random random = new Random();
    private final City city;
    private final Grid grid;

    private int happinessThreeTicksAgo;
    private int happinessTwoTicksAgo;
    private int happinessOneTickAgo;
    private int recordedHappinessTicks;

    // Crea il gestore della criminalità usando la città e la griglia della simulazione.
    public CrimeManager(City city, Grid grid) {
        if (city == null) {
            throw new IllegalArgumentException("The city cannot be null");
        }

        if (grid == null) {
            throw new IllegalArgumentException("The grid cannot be null");
        }

        this.city = city;
        this.grid = grid;
    }

    // Dopo il tick 20 prova a generare criminalità per un brusco calo di felicità oppure con la probabilità basata sull'economia.
    public boolean tryToCreateCriminalActivity(int currentTick) {
        boolean suddenHappinessDrop = checkSuddenHappinessDrop();

        if (currentTick < CRIME_UNLOCK_TICK) {
            return false;
        }

        if (suddenHappinessDrop) {
            return placeCriminalActivity(currentTick);
        }

        double probability = city.getGlobalEconomy() * CRIME_ECONOMY_FACTOR;
        probability = Math.min(probability, MAX_CRIME_PROBABILITY);

        if (random.nextDouble() < probability) {
            return placeCriminalActivity(currentTick);
        }

        return false;
    }

    // Confronta la felicità attuale con quella di tre tick fa. Un calo di almeno 1000 genera una nuova causa di criminalità.
    private boolean checkSuddenHappinessDrop() {
        int currentHappiness = city.getGlobalHappiness();
        boolean suddenDrop = recordedHappinessTicks >= 3
                && happinessThreeTicksAgo - currentHappiness >= HAPPINESS_DROP_THRESHOLD;

        happinessThreeTicksAgo = happinessTwoTicksAgo;
        happinessTwoTicksAgo = happinessOneTickAgo;
        happinessOneTickAgo = currentHappiness;

        if (recordedHappinessTicks < 3) {
            recordedHappinessTicks++;
        }

        // Dopo aver rilevato il calo, il nuovo livello diventa il riferimento per evitare tre spawn consecutivi per lo stesso crollo.
        if (suddenDrop) {
            happinessThreeTicksAgo = currentHappiness;
            happinessTwoTicksAgo = currentHappiness;
            happinessOneTickAgo = currentHappiness;
        }

        return suddenDrop;
    }

    // Piazza una nuova attività criminale in una cella costruibile scelta casualmente, ma mai prima del tick 20.
    public boolean placeCriminalActivity(int currentTick) {
        if (currentTick < CRIME_UNLOCK_TICK) {
            return false;
        }

        List<Cell> buildableCells = grid.getBuildableCells();

        // L'ultima cella costruibile viene riservata alle strade da Grid.
        if (buildableCells.size() <= 1) {
            return false;
        }

        int randomIndex = random.nextInt(buildableCells.size());
        Cell randomCell = buildableCells.get(randomIndex);
        CriminalActivity criminalActivity = (CriminalActivity) ConstructionFactory.create(ConstructionType.CRIMINAL_ACTIVITY);

        criminalActivity.setCreationTick(currentTick);
        grid.placeConstruction(criminalActivity, randomCell.getRow(), randomCell.getColumn());
        city.refreshStatistics();

        return true;
    }

    // Conta quante stazioni di polizia alimentate sono presenti nella città.
    public int getNumOfPoliceStation() {
        int numOfPoliceStation = 0;

        for (Construction construction : grid.getConstructions()) {
            if (construction instanceof PoliceStation && construction.isPowered()) {
                numOfPoliceStation++;
            }
        }

        return numOfPoliceStation;
    }

    // Restituisce quante minacce possono essere rimosse nello stesso momento: una per ogni stazione alimentata.
    public int getMaxRemoval() {
        return getNumOfPoliceStation();
    }

    // Cerca le attività criminali che hanno già superato il tempo minimo prima della rimozione da parte della polizia.
    private List<Cell> getCriminalActivities(int currentTick) {
        List<Cell> criminalActivities = new ArrayList<>();

        for (int row = 0; row < grid.getNumberOfRows(); row++) {
            for (int column = 0; column < grid.getNumberOfColumns(); column++) {
                Cell cell = grid.getCell(row, column);

                if (!cell.isEmpty() && cell.getConstruction() instanceof CriminalActivity) {
                    CriminalActivity criminalActivity = (CriminalActivity) cell.getConstruction();

                    if (criminalActivity.canBeRemovedByPolice(currentTick)) {
                        criminalActivities.add(cell);
                    }
                }
            }
        }

        return criminalActivities;
    }

    // Crea l'elenco delle minacce eliminabili: attività criminali valide e gruppi terroristici con almeno 5 tick di vita.
    private List<Cell> getPoliceTargets(int currentTick) {
        List<Cell> policeTargets = new ArrayList<>(getCriminalActivities(currentTick));

        for (int row = 0; row < grid.getNumberOfRows(); row++) {
            for (int column = 0; column < grid.getNumberOfColumns(); column++) {
                Cell cell = grid.getCell(row, column);

                if (!cell.isEmpty() && cell.getConstruction() instanceof TerroristicGroup) {
                    TerroristicGroup terroristicGroup = (TerroristicGroup) cell.getConstruction();

                    if (terroristicGroup.canBeRemovedByPolice()) {
                        policeTargets.add(cell);
                    }
                }
            }
        }

        return policeTargets;
    }

    // Elimina casualmente una sola attività criminale tra quelle che possono già essere rimosse.
    public boolean destroyCriminalActivity(int currentTick) {
        List<Cell> criminalActivities = getCriminalActivities(currentTick);

        if (criminalActivities.isEmpty()) {
            return false;
        }

        int randomIndex = random.nextInt(criminalActivities.size());
        Cell criminalCell = criminalActivities.get(randomIndex);
        grid.removeConstruction(criminalCell.getRow(), criminalCell.getColumn());

        return true;
    }

    // Sceglie casualmente una minaccia tra criminalità e terrorismo e ne rimuove una sola.
    private boolean destroyPoliceTarget(int currentTick) {
        List<Cell> policeTargets = getPoliceTargets(currentTick);

        if (policeTargets.isEmpty()) {
            return false;
        }

        int randomIndex = random.nextInt(policeTargets.size());
        Cell targetCell = policeTargets.get(randomIndex);
        grid.removeConstruction(targetCell.getRow(), targetCell.getColumn());

        return true;
    }

    // Fa agire ogni stazione disponibile: ciascuna può rimuovere al massimo una minaccia e poi entra in cooldown.
    public int tryToDestroyCriminalActivities(int currentTick) {
        int removed = 0;

        for (Construction construction : grid.getConstructions()) {
            if (construction instanceof PoliceStation) {
                PoliceStation policeStation = (PoliceStation) construction;

                if (policeStation.isPowered() && policeStation.canRemoveCriminalActivity(currentTick)) {
                    if (destroyPoliceTarget(currentTick)) {
                        policeStation.registerCriminalActivityRemoval(currentTick);
                        removed++;
                    }
                }
            }
        }

        return removed;
    }
}
