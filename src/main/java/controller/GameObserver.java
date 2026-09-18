package controller;

/* Definisce l'operazione con cui un osservatore
   aggiorna la schermata quando cambia lo stato del gioco. */
public interface GameObserver
{
    // Aggiorna la schermata usando lo stato corrente del gioco.
    void refreshGameView();
}