package progress;

import Events.EventType;
import controller.Controller;
import model.ConstructionType;
import model.FreemasonryChoice;
import model.Grid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import policies.PolicyType;
import policies.StandardPolicy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProgressManagerTest {

    private ProgressManager progressManager;

    // Cartella temporanea eliminata automaticamente al termine dei test.
    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        progressManager = new ProgressManager();
    }

    // Verifica che salvataggio e caricamento non causino perdite di dati.
    @Test
    void saveAndLoadPreserveProgress() throws IOException {
        Progress originalProgress = createProgress();
        Path file = tempDirectory.resolve("progress.json");

        progressManager.save(
                originalProgress,
                file.toString()
        );

        Progress loadedProgress =
                progressManager.load(file.toString());

        assertTrue(Files.exists(file));
        assertEquals(20, loadedProgress.getCurrentTick());
        assertEquals(14, loadedProgress.getLastPolicyChangeTick());
        assertEquals(1800, loadedProgress.getBudget());
        assertEquals(
                PolicyType.ENVIRONMENTAL,
                loadedProgress.getPolicyType()
        );
        assertEquals(1, loadedProgress.getConstructions().size());

        ConstructionProgress loadedConstruction =
                loadedProgress.getConstructions().get(0);

        assertEquals(
                ConstructionType.ROAD,
                loadedConstruction.getType()
        );
        assertEquals(5, loadedConstruction.getRow());
        assertEquals(5, loadedConstruction.getColumn());
    }

    @Test
    void saveRejectsNullProgress() {
        Path file = tempDirectory.resolve("progress.json");

        assertThrows(
                IllegalArgumentException.class,
                () -> progressManager.save(null, file.toString())
        );
    }

    // Un file esistente ma privo di contenuto non rappresenta un salvataggio valido.
    @Test
    void loadRejectsEmptyFile() throws IOException {
        Path file = tempDirectory.resolve("empty.json");
        Files.createFile(file);

        assertThrows(
                IOException.class,
                () -> progressManager.load(file.toString())
        );
    }

    // Scrive intenzionalmente un contenuto che Gson non può convertire.
    @Test
    void loadRejectsInvalidJson() throws IOException {
        Path file = tempDirectory.resolve("invalid.json");
        Files.writeString(file, "{ invalid json");

        assertThrows(
                IOException.class,
                () -> progressManager.load(file.toString())
        );
    }

    // Verifica che saveGame richieda al controller lo stato corrente della partita.
    @Test
    void saveGameUsesControllerProgress() throws IOException {
        Controller controller = mock(Controller.class);
        Progress progress = createProgress();
        Path file = tempDirectory.resolve("save-game.json");

        when(controller.createProgress()).thenReturn(progress);

        progressManager.saveGame(
                controller,
                file.toString()
        );

        Progress loadedProgress =
                progressManager.load(file.toString());

        verify(controller).createProgress();
        assertEquals(20, loadedProgress.getCurrentTick());
        assertEquals(1800, loadedProgress.getBudget());
    }

    @Test
    void invitationChoiceSurvivesSaveAndLoad() throws IOException {
        Controller controller = new Controller(
                new Grid(), new StandardPolicy(), 2000, 500, 0
        );
        Path file = tempDirectory.resolve("invitation.json");

        assertTrue(controller.isFreemasonryInvitationPending());
        assertTrue(controller.chooseFreemasonry(
                FreemasonryChoice.ACCEPTED
        ));

        progressManager.saveGame(controller, file.toString());
        Controller loaded = progressManager.loadGame(file.toString());

        assertEquals(FreemasonryChoice.ACCEPTED,
                loaded.getFreemasonryChoice());
        assertFalse(loaded.isFreemasonryInvitationPending());
    }

    @Test
    void pendingInvitationSurvivesSaveAndLoad() throws IOException {
        Controller controller = new Controller(
                new Grid(), new StandardPolicy(), 2000, 500, 0
        );
        Path file = tempDirectory.resolve("pending-invitation.json");

        progressManager.saveGame(controller, file.toString());
        Controller loaded = progressManager.loadGame(file.toString());

        assertEquals(FreemasonryChoice.PENDING,
                loaded.getFreemasonryChoice());
        assertTrue(loaded.isFreemasonryInvitationPending());
    }

    @Test
    void saveGameRejectsActiveEvent()
    {
        Controller controller = mock(Controller.class);
        Path file = tempDirectory.resolve("event-save.json");

        when(controller.getActiveEventType())
                .thenReturn(EventType.FIRE);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> progressManager.saveGame(
                                controller,
                                file.toString()
                        )
                );

        assertEquals(
                "Cannot save while an event is in progress.",
                exception.getMessage()
        );
        verify(controller, never()).createProgress();
        assertFalse(Files.exists(file));
    }

    @Test
    void saveGameRejectsNullController() {
        Path file = tempDirectory.resolve("save-game.json");

        assertThrows(
                IllegalArgumentException.class,
                () -> progressManager.saveGame(null, file.toString())
        );
    }

    // Verifica che loadGame ricostruisca un controller utilizzabile.
    @Test
    void loadGameRestoresController() throws IOException {
        Path file = tempDirectory.resolve("load-game.json");

        progressManager.save(
                createProgress(),
                file.toString()
        );

        Controller controller =
                progressManager.loadGame(file.toString());

        assertEquals(1800, controller.getBudget());
        assertEquals(20, controller.getCurrentTick());
        assertEquals(6, controller.getTicksToPolicyChange());
        assertEquals(
                "Environmental policy",
                controller.getCurrentPolicyName()
        );
        assertEquals(
                ConstructionType.ROAD,
                controller.getCellState(5, 5).type()
        );
    }

    // Crea un salvataggio valido riutilizzato dai diversi test.
    private Progress createProgress() {
        ConstructionProgress road = new ConstructionProgress(
                ConstructionType.ROAD,
                5,
                5,
                0,
                0,
                0,
                0.0,
                0
        );

        return new Progress(
                20,
                14,
                1800,
                PolicyType.ENVIRONMENTAL,
                List.of(road)
        );
    }
}
