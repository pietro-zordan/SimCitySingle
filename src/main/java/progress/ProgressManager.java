package progress;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import com.google.gson.JsonParseException;
import controller.Controller;

import java.io.Reader;
/* Gestisce il salvataggio e il caricamento della partita
   mediante la conversione degli oggetti in formato JSON. */
public class ProgressManager
{
    private final Gson gson;

    // Configura Gson affinché produca un file JSON leggibile e indentato.

    public ProgressManager()
    {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /* Converte lo stato della partita in JSON
   e lo scrive nel percorso specificato. */

    public void save(Progress progress, String filePath)
            throws IOException
    {
        if (progress == null)
        {
            throw new IllegalArgumentException(
                    "progress.Progress cannot be null"
            );
        }

        Path path = Path.of(filePath);

        try (Writer writer = Files.newBufferedWriter(path))
        {
            gson.toJson(progress, writer);
        }
    }

    // Legge un file JSON e ricostruisce l'oggetto contenente lo stato salvato della partita.

    public Progress load(String filePath) throws IOException
    {
        Path path = Path.of(filePath);

        try (Reader reader = Files.newBufferedReader(path))
        {
            Progress progress =
                    gson.fromJson(reader, Progress.class);

            if (progress == null)
            {
                throw new IOException(
                        "The save file is empty"
                );
            }

            return progress;
        }
        catch (JsonParseException exception)
        {
            throw new IOException(
                    "The save file contains invalid JSON",
                    exception
            );
        }
    }
    /* Ottiene lo stato corrente dal controller
       e lo salva nel file specificato. */

    public void saveGame(
            Controller controller,
            String filePath)
            throws IOException
    {
        if (controller == null)
        {
            throw new IllegalArgumentException(
                    "controller.Controller cannot be null"
            );
        }

        if (controller.getActiveEventType() != null)
        {
            throw new IllegalStateException(
                    "Cannot save while an event is in progress."
            );
        }

        Progress progress = controller.createProgress();

        save(progress, filePath);
    }

    /* Carica lo stato dal file e ricostruisce
   il controller completo della partita. */

    public Controller loadGame(String filePath)
            throws IOException
    {
        Progress progress = load(filePath);

        return progress.restoreController();
    }
}