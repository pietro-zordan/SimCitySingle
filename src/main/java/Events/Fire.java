package Events;

import model.Cell;
import model.City;
import model.Grid;
import model.Road;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.HashSet;
import java.util.Set;

/* Gestisce l'evento incendio.
   Distrugge le costruzioni (tranne le strade) e può propagarsi alle celle vicine. */
public class Fire extends Event{

    // Riduzione della felicità per ogni cella bruciata.
    private final static int HAPPINESS_DECREASE = 5; // 5 per casella
    private Grid grid;

    // Celle che verranno bruciate durante il tick successivo
    private List<Cell> cellsToBurnNextTick = new ArrayList<>();

    // Celle già coinvolte nell'incendio
    private final Set<Cell> involvedCells = new HashSet<>();

    // Inizializza l'incendio e il valore necessario per il suo avvio casuale.
    public Fire(City city, Grid grid) {
        super(city);
        this.grid = grid;
        launchNumber=23;
    }
    //Fornisce una lista delle celle con un edificio bruciabili (diverse da road)
    private List<Cell> getBurnableCells()
    {
        List<Cell> burnableCells = new ArrayList<>();

        for (int row = 0; row < grid.getNumberOfRows(); row++)
        {
            for (int column = 0;
                 column < grid.getNumberOfColumns();
                 column++)
            {
                Cell cell = grid.getCell(row, column);

                if (!cell.isEmpty()
                        && !(cell.getConstruction() instanceof Road))
                {
                    burnableCells.add(cell);
                }
            }
        }

        return burnableCells;
    }



    /* Distrugge la costruzione presente nella cella
       e prova a propagare l'incendio alle celle vicine. */
    public void burn(Cell cell) {

        if (cell.isEmpty())
        {
            return;
        }

        int row = cell.getRow();
        int col = cell.getColumn();

        grid.removeConstruction(row, col);


        int[][] directions = {
                {-1, 0}, // up
                {1, 0},  // down
                {0, -1}, // left
                {0, 1}   // right
        };

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newColumn = col + dir[1];

            if (grid.isInside(newRow, newColumn)) {
                Cell neighbor = grid.getCell(newRow, newColumn);
                if (!neighbor.isEmpty() && !(neighbor.getConstruction() instanceof Road)) {
                    willItBurn(neighbor);
                }
            }
        }
    }

    // Decide se la cella prende fuoco e in caso la salva per il tick successivo
    public void willItBurn(Cell cell) {
        Random random = new Random();

        if (!involvedCells.contains(cell)
                && random.nextBoolean())
        {
            cellsToBurnNextTick.add(cell);
            involvedCells.add(cell);
        }
    }

    /* Brucia le celle previste per il tick corrente
     e riduce la felicità in base al numero di celle distrutte. */
    @Override
    public void updateOfOneTick()
    {
        List<Cell> current =
                new ArrayList<>(cellsToBurnNextTick);

        cellsToBurnNextTick.clear();

        int burnedCells = 0;

        for (Cell cell : current)
        {
            if (!cell.isEmpty())
            {
                burn(cell);
                burnedCells++;
            }
        }

        city.refreshStatistics();

        city.decreaseGlobalHappiness(
                HAPPINESS_DECREASE * burnedCells
        );
    }

    // Verifica se la cella indicata è coinvolta nell'incendio.
    public boolean isCellOnFire(int row, int column) {
        for (Cell cell : cellsToBurnNextTick)
        {
            if (cell.getRow() == row && cell.getColumn() == column)
            {
                return true;
            }
        }

        return false;
    }
    //l'evento finisce quando la lista che contiene le celle che bruceranno il prossimo turno è vuota
    @Override
    public boolean isFinished(int tickPassed){
        return (cellsToBurnNextTick.isEmpty());
    }

    //aggiunge un controllo se non ci sono edifici bruciabili l'evento non parte
    @Override
    public boolean canStart()
    {
        return super.canStart() && !getBurnableCells().isEmpty();
    }

    //Sceglie casualmente la cella da cui far partire l'evento e la mette nella lista cellsToBurnNextTick
    @Override
    public void start()
    {
        List<Cell> burnableCells = getBurnableCells();

        Random random = new Random();
        int selectedCell = random.nextInt(burnableCells.size());

        Cell startCell = burnableCells.get(selectedCell);

        cellsToBurnNextTick.add(startCell);
        involvedCells.add(startCell);
    }

    // Restituisce il tipo corrispondente all'incendio.
    @Override
    public EventType getType()
    {
        return EventType.FIRE;
    }
}