package model;

/* Rappresenta una singola posizione della griglia.
   Memorizza le proprie coordinate e l'eventuale costruzione presente. */
public class Cell {

    private final int row;
    private final int column;
    private Construction construction;

    // Crea una cella vuota nelle coordinate indicate.
    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
        this.construction = null;
    }

    // Restituisce la riga della cella.
    public int getRow() {
        return row;
    }

    // Restituisce la colonna della cella.
    public int getColumn() {
        return column;
    }

    // Restituisce la costruzione presente nella cella.
    public Construction getConstruction() {
        return construction;
    }

    // Controlla se la cella non contiene costruzioni.
    public boolean isEmpty() {
        return construction == null;
    }

    // Inserisce una costruzione soltanto se la cella è vuota.
    public void placeConstruction(Construction construction)
    {
        if (construction == null)
        {
            throw new IllegalArgumentException(
                    "The construction cannot be null"
            );
        }

        if (!isEmpty())
        {
            throw new IllegalStateException(
                    "The cell is already occupied"
            );
        }

        this.construction = construction;
    }

    // Rimuove e restituisce la costruzione presente nella cella.
    public Construction removeConstruction()
    {
        Construction removedConstruction = construction;
        construction = null;

        return removedConstruction;
    }

    /* Aggiorna la costruzione presente e comunica alla griglia
       se deve essere rimossa al termine del tick. */
    public boolean updateOfOneTick()
    {
        boolean mustRemove = false;
        if(!isEmpty())
        {
            construction.updateOfOneTick();
            mustRemove= construction.mustBeRemoved();
        }
        return mustRemove;
    }
}