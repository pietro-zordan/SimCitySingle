package Events;

import model.City;

import java.util.Random;

/* Classe base di tutti gli eventi del gioco.
   Gestisce la probabilità di avvio e la durata
   mentre ogni sottoclasse definisce gli effetti specifici dell'evento. */
public abstract class Event
{
    protected int probability = 0;
    private int tickDuration;
    protected City city;
    protected int launchNumber;

    // Inizializza un evento che termina automaticamente dopo un numero prestabilito di tick.
    public Event(int tickDuration, City city)
    {
        this.tickDuration = tickDuration;
        this.city = city;
    }

    /* Inizializza un evento senza una durata prestabilita.
       Il valore -1 impedisce a isFinished() di terminarlo automaticamente. */
    public Event(City city)
    {
        tickDuration = -1;
        this.city = city;
    }

    // Genera l'esito casuale che verrà successivamente valutato da canStart().
    public void randomProbability()
    {
        Random dice = new Random();
        probability = dice.nextInt(launchNumber);
    }

    // Imposta il valore che canStart() userà per decidere se l'evento può iniziare.
    public void setProbability(int probability)
    {
        this.probability = probability;
    }

    /* Rappresenta il comportamento eseguito a ogni tick.
       L'implementazione vuota permette agli eventi che non ne hanno bisogno di non ridefinirlo. */
    public void updateOfOneTick()
    {
    }

    // Restituisce il numero di tick previsto per la durata dell'evento.
    public int getTickDuration()
    {
        return tickDuration;
    }

    /* Rappresenta le operazioni da eseguire alla conclusione dell'evento.
       Le sottoclassi lo ridefiniscono quando devono rimuovere effetti temporanei. */
    public void end()
    {
    }

    // Controlla la durata, escludendo gli eventi contrassegnati con durata indefinita.
    public boolean isFinished(int tickPassed)
    {
        return tickDuration != -1
                && tickPassed >= tickDuration;
    }

    /* Rappresenta le operazioni da eseguire all'avvio dell'evento.
       Le sottoclassi lo ridefiniscono quando devono applicare subito un effetto. */
    public void start()
    {
    }

    /* Consente l'avvio soltanto quando l'estrazione casuale produce l'ultimo
       valore possibile, ottenendo una probabilità pari a 1 su launchNumber. */
    public boolean canStart()
    {
        return probability == launchNumber - 1;
    }

    // Restituisce il tipo concreto dell'evento per consentirne il riconoscimento esterno.
    public abstract EventType getType();
}