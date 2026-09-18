package GUI;

/* Definisce le operazioni utilizzate dalla schermata di gioco
   per tornare alla schermata iniziale o iniziare una nuova partita. */
public interface GameNavigation
{
    // Torna alla schermata iniziale.
    void returnToHome();

    // Interrompe la partita corrente e ne avvia una nuova.
    void restartGame();
}