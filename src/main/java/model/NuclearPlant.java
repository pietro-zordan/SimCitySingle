package model;

import java.util.ArrayList;
import java.util.List;

public class NuclearPlant extends Construction {
    private static final int NETWORK_CAPACITY_INCREASE = 10000;
    private boolean fireProtected;
    private Grid grid;
    private int row;
    private int column;
    private boolean explosionStarted;

    // Crea una centrale nucleare che estende la capacità massima della rete di 10000.
    public NuclearPlant() {
        super(200, 0, -3000);
    }

    // Restituisce di quanto una centrale nucleare aumenta la capacità massima della rete.
    public static int getNetworkCapacityIncrease() {
        return NETWORK_CAPACITY_INCREASE;
    }

    // Memorizza griglia e posizione della centrale per poter gestire una sua eventuale esplosione.
    @Override
    public void initializeAfterPlacement(Grid grid, int row, int column) {
        this.grid = grid;
        this.row = row;
        this.column = column;
    }

    // Quando la centrale viene rimossa, provoca l'esplosione nel quadrato 9x9 circostante.
    @Override
    public void prepareForRemoval() {
        super.prepareForRemoval();

        if (explosionStarted) {
            return;
        }

        explosionStarted = true;
        makeExplosion();
    }

    // Restituisce le celle occupate nel quadrato 9x9 attorno alla centrale, escludendo centro e strade.
    private List<Cell> getExplosionCells() {
        List<Cell> explosionCells = new ArrayList<>();

        if (grid == null) {
            return explosionCells;
        }

        for (int currentRow = row - 4; currentRow <= row + 4; currentRow++) {
            for (int currentColumn = column - 4; currentColumn <= column + 4; currentColumn++) {
                if (grid.isInside(currentRow, currentColumn)
                        && (currentRow != row || currentColumn != column)) {

                    Cell cell = grid.getCell(currentRow, currentColumn);

                    if (!cell.isEmpty()
                            && !(cell.getConstruction() instanceof Road)) {
                        explosionCells.add(cell);
                    }
                }
            }
        }

        return explosionCells;
    }

    // Distrugge tutte le costruzioni individuate nell'area dell'esplosione.
    private void makeExplosion() {
        List<Cell> explosionCells = getExplosionCells();

        for (Cell cell : explosionCells) {
            if (!cell.isEmpty()
                    && !(cell.getConstruction() instanceof Road)) {
                grid.removeConstruction(cell.getRow(), cell.getColumn());
            }
        }
    }

    // Indica se la centrale possiede la protezione avanzata contro gli incendi.
    public boolean isFireProtected() {
        return fireProtected;
    }

    // Registra la protezione avanzata contro gli incendi.
    public void setFireProtected(boolean fireProtected) {
        this.fireProtected = fireProtected;
    }

    @Override
    public int getPollutionImpact() {
        return 900;
    }

    @Override
    public int getHappinessImpact() {
        return -1500;
    }

    @Override
    public int getMaintenanceCost() {
        return 100;
    }

    @Override
    public ConstructionType getType() {
        return ConstructionType.NUCLEAR_PLANT;
    }
}
