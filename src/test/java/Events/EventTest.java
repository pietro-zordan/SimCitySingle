package Events;

import model.City;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventTest {

    @Mock
    private City city;

    private Event dummyEvent;
    private Event infiniteEvent;

    @BeforeEach
    void setUp() {
        // Creiamo una classe concreta fittizia con durata definita (5 tick)
        dummyEvent = new Event(5, city) {
            {
                this.launchNumber = 10; // Inizializziamo launchNumber per il test
            }

            @Override
            public EventType getType() {
                return null;
            }
        };

        // Creiamo una classe concreta fittizia senza durata definita
        infiniteEvent = new Event(city) {
            @Override
            public EventType getType() {
                return null;
            }
        };
    }

    @Test
    void testCanStart() {
        // canStart() restituisce true se probability == launchNumber - 1[cite: 5]
        dummyEvent.setProbability(9);
        assertTrue(dummyEvent.canStart());

        dummyEvent.setProbability(5);
        assertFalse(dummyEvent.canStart());
    }

    @Test
    void testIsFinishedWithDuration() {
        // La durata impostata nel costruttore è 5[cite: 5]
        assertFalse(dummyEvent.isFinished(3));
        assertFalse(dummyEvent.isFinished(4));
        assertTrue(dummyEvent.isFinished(5));
        assertTrue(dummyEvent.isFinished(10));
    }

    @Test
    void testIsFinishedWithoutDuration() {
        // Se tickDuration è -1, l'evento non finisce in base ai tick[cite: 5]
        assertFalse(infiniteEvent.isFinished(100));
        assertFalse(infiniteEvent.isFinished(0));
    }
}