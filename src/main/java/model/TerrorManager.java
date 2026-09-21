package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TerrorManager {

    private final City city;
    private final Grid grid;
    private static final int KILLS_PER_GROUP = 15;
    private static final int HAPPINESS_THRESHOLD = -50;
    private static final int NUM_OF_SADNESS_TICKS = 8;
    private static final int HAPPINESS_DROP_THRESHOLD = 1200;
    private static final int SPAWN_COOLDOWN = 10;
    private static final int MAX_GROUPS = 3;

    private int sadnessTicks = 0;
    private int spawnCooldown = 0;
    private int happinessOneTickAgo;
    private int happinessTwoTicksAgo;
    private int happinessThreeTicksAgo;

    public TerrorManager(City city, Grid grid) {
        if (city == null) {
            throw new IllegalArgumentException("The city cannot be null");
        }

        if (grid == null) {
            throw new IllegalArgumentException("The grid cannot be null");
        }

        this.city = city;
        this.grid = grid;

        // All'inizio non esiste ancora uno storico: uso la felicità attuale come riferimento.
        int currentHappiness = city.getGlobalHappiness();
        happinessOneTickAgo = currentHappiness;
        happinessTwoTicksAgo = currentHappiness;
        happinessThreeTicksAgo = currentHappiness;
    }

    // Aggiorna le condizioni di comparsa dei gruppi terroristici e applica i loro effetti.
    public boolean updateOfOneTick() {
        if (spawnCooldown > 0) {
            spawnCooldown--;
        }

        int currentHappiness = city.getGlobalHappiness();

        if (currentHappiness < HAPPINESS_THRESHOLD) {
            sadnessTicks++;
        } else {
            sadnessTicks = 0;
        }

        boolean suddenHappinessDrop = hasSuddenHappinessDrop(currentHappiness);
        boolean groupCreated = false;

        // Il gruppo compare dopo una tristezza prolungata oppure dopo un calo di almeno 1200 punti in massimo 3 tick.
        if ((sadnessTicks >= NUM_OF_SADNESS_TICKS || suddenHappinessDrop) && canPlaceTG()) {
            groupCreated = placeTG();

            if (groupCreated) {
                spawnCooldown = SPAWN_COOLDOWN;
                sadnessTicks = 0;
            }
        }

        updateHappinessHistory(currentHappiness);

        if (getNumOfTG() > 0) {
            killPeople();
        }

        return groupCreated;
    }

    // Controlla se la felicità è scesa di almeno 1200 punti rispetto a uno degli ultimi tre tick.
    private boolean hasSuddenHappinessDrop(int currentHappiness) {
        return happinessOneTickAgo - currentHappiness >= HAPPINESS_DROP_THRESHOLD
                || happinessTwoTicksAgo - currentHappiness >= HAPPINESS_DROP_THRESHOLD
                || happinessThreeTicksAgo - currentHappiness >= HAPPINESS_DROP_THRESHOLD;
    }

    // Fa scorrere lo storico della felicità mantenendo gli ultimi tre valori.
    private void updateHappinessHistory(int currentHappiness) {
        happinessThreeTicksAgo = happinessTwoTicksAgo;
        happinessTwoTicksAgo = happinessOneTickAgo;
        happinessOneTickAgo = currentHappiness;
    }

    public List<Residential> getAllResidentials() {
        List<Residential> residentials = new ArrayList<>();

        for (Construction construction : grid.getConstructions()) {
            if (construction instanceof Residential residential) {
                residentials.add(residential);
            }
        }

        return residentials;
    }

    public void killPeople() {
        List<Residential> houses = getAllResidentials();

        if (houses.isEmpty()) {
            return;
        }

        Random random = new Random();
        int index = random.nextInt(houses.size());
        Residential randomHouse = houses.get(index);

        int numberOfGroups = getNumOfTG();
        int peopleToKill = KILLS_PER_GROUP * numberOfGroups;

        randomHouse.decreasePopulationBy(peopleToKill);

        if (randomHouse.hasNoPopulation()) {
            removeResidential(randomHouse);
        }

        city.refreshStatistics();
    }

    private void removeResidential(Residential residential) {
        for (int row = 0; row < grid.getNumberOfRows(); row++) {
            for (int column = 0; column < grid.getNumberOfColumns(); column++) {
                Cell cell = grid.getCell(row, column);

                if (!cell.isEmpty() && cell.getConstruction() == residential) {
                    grid.removeConstruction(row, column);
                    return;
                }
            }
        }
    }

    public boolean placeTG() {
        List<Cell> cells = grid.getBuildableCells();

        // Non occupa l'ultima cella costruibile: Grid la riserva alle strade.
        if (cells.size() <= 1) {
            return false;
        }

        Random random = new Random();
        Cell cell = cells.get(random.nextInt(cells.size()));
        TerroristicGroup group = new TerroristicGroup();

        // Il gruppo viene generato automaticamente dal gioco e non dipende dal budget del giocatore.
        grid.placeConstruction(group, cell.getRow(), cell.getColumn());
        city.refreshStatistics();

        return true;
    }

    public int getNumOfTG() {
        int count = 0;

        for (Construction construction : grid.getConstructions()) {
            if (construction instanceof TerroristicGroup) {
                count++;
            }
        }

        return count;
    }

    public boolean canPlaceTG() {
        return spawnCooldown == 0
                && getNumOfTG() < MAX_GROUPS
                && grid.getBuildableCells().size() > 1;
    }
}
