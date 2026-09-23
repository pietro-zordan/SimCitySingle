package progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import model.AchievementManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/* Salva e carica gli achievement globali del giocatore
   separatamente dal salvataggio della singola partita. */
public class AchievementProgressManager
{
    private final Gson gson;

    public AchievementProgressManager()
    {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    public void save(
            AchievementManager achievementManager,
            String filePath)
            throws IOException
    {
        if (achievementManager == null)
        {
            throw new IllegalArgumentException(
                    "Achievement manager cannot be null"
            );
        }

        Path path = Path.of(filePath);

        AchievementProgress progress =
                AchievementProgress.fromManager(
                        achievementManager
                );

        try (Writer writer =
                     Files.newBufferedWriter(path))
        {
            gson.toJson(progress, writer);
        }
    }

    public AchievementManager load(String filePath)
            throws IOException
    {
        Path path = Path.of(filePath);

        if (!Files.exists(path))
        {
            return new AchievementManager();
        }

        try (Reader reader =
                     Files.newBufferedReader(path))
        {
            AchievementProgress progress =
                    gson.fromJson(
                            reader,
                            AchievementProgress.class
                    );

            if (progress == null)
            {
                return new AchievementManager();
            }

            return progress.restoreManager();
        }
        catch (JsonParseException exception)
        {
            throw new IOException(
                    "The achievement file contains invalid JSON",
                    exception
            );
        }
    }

    public void reset(
            AchievementManager achievementManager,
            String filePath)
            throws IOException
    {
        if (achievementManager == null)
        {
            throw new IllegalArgumentException(
                    "Achievement manager cannot be null"
            );
        }

        achievementManager.reset();
        save(achievementManager, filePath);
    }
}
