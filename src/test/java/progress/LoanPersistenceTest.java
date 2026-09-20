package progress;

import controller.Controller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import policies.PolicyType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanPersistenceTest
{
    @TempDir
    Path tempDirectory;

    @Test
    void oldSaveWithoutLoanFieldsUsesSafeDefaults() throws Exception
    {
        Path saveFile =
                tempDirectory.resolve("old-progress.json");

        Files.writeString(
                saveFile,
                """
                {
                  "currentTick": 40,
                  "lastPolicyChangeTick": 36,
                  "policyType": "STANDARD",
                  "budget": 2500,
                  "constructions": []
                }
                """
        );

        Progress loaded =
                new ProgressManager()
                        .load(saveFile.toString());

        assertFalse(loaded.isLoanActive());
        assertEquals(0, loaded.getLoanAmount());
        assertEquals(-15, loaded.getLastLoanTick());
        assertEquals(0, loaded.getRemainingDebt());
    }

    @Test
    void activeLoanSurvivesSaveAndLoad() throws Exception
    {
        Progress original = new Progress(
                40,
                36,
                3500,
                PolicyType.STANDARD,
                true,
                1000,
                39,
                39,
                0,
                List.of()
        );

        Path saveFile =
                tempDirectory.resolve("loan-progress.json");

        ProgressManager manager = new ProgressManager();
        manager.save(original, saveFile.toString());

        Progress loaded =
                manager.load(saveFile.toString());

        assertTrue(loaded.isLoanActive());
        assertEquals(1000, loaded.getLoanAmount());
        assertEquals(39, loaded.getLoanStartTick());
        assertEquals(39, loaded.getLastLoanTick());
        assertEquals(0, loaded.getRemainingDebt());

        Controller controller =
                loaded.restoreController();

        assertFalse(controller.canRequestLoan());

        controller.updateOfOneTick();
        controller.updateOfOneTick();

        assertEquals(500, controller.getBudget());
    }
}
