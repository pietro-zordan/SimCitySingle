package audio;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;
    private final AudioClip tickAdvanceSound;
    private final AudioClip blackoutSound;
    private final AudioClip insuranceRebuildSound;
    private final AudioClip demolitionSound;
    private final AudioClip placementSound;
    private final AudioClip gridExpansionSound;
    private final AudioClip missileSound;
    private final MediaPlayer firePlayer;
    private final MediaPlayer fireCracklePlayer;
    private final AudioClip missileGridImpactSound;
    private final AudioClip missileShieldImpactSound;
    private final MediaPlayer tsunamiPlayer;
    private final AudioClip nuclearExplosionSound;
    private final AudioClip economicBoomSound;
    private final AudioClip hackerAttackSound;
    private final AudioClip energyCrisisSound;
    private final AudioClip defeatSound;


    public SoundManager()
    {
        achievementSound = loadClip("achievement.wav");
        tickAdvanceSound = loadClip("tick_advance.wav");
        blackoutSound = loadClip("blackout.wav");
        insuranceRebuildSound = loadClip(
                "insurance_rebuild.wav"
        );
        demolitionSound = loadClip("demolition.wav");
        placementSound = loadClip("placement.wav");
        gridExpansionSound = loadClip("grid_expansion.wav");
        missileSound = loadClip("missile_flyby.wav");
        missileGridImpactSound = loadClip("missile_grid_impact.wav");
        missileShieldImpactSound = loadClip("missile_shield_impact.wav");
        tsunamiPlayer = loadPlayer("tsunami_wave.wav");
        nuclearExplosionSound = loadClip("nuclear_explosion.wav");
        economicBoomSound = loadClip("economic_boom.wav");
        hackerAttackSound = loadClip("hacker_intrusion.wav");
        energyCrisisSound = loadClip("energy_crisis_alarm.wav");
        defeatSound = loadFirstAvailableClip(
                new String[] {
                        "defeat.wav",
                        "defeat.mp3",
                        "Defeat.wav",
                        "Defeat.mp3",
                        "defeat_sound.wav",
                        "defeat_sound.mp3",
                        "defeatSound.wav",
                        "defeatSound.mp3",
                        "defeat-sound.wav",
                        "defeat-sound.mp3",
                        "defeat sound.wav",
                        "defeat sound.mp3",
                        "Defeat Sound.wav",
                        "Defeat Sound.mp3",
                        "game_over.wav",
                        "game_over.mp3",
                        "gameover.wav",
                        "gameover.mp3"
                }
        );
        firePlayer = loadPlayer("fire_roar.mp3");
        fireCracklePlayer = loadPlayer("fire.wav");
        firePlayer.setCycleCount(MediaPlayer.INDEFINITE);
        fireCracklePlayer.setCycleCount(MediaPlayer.INDEFINITE);

        // Il crackle originale resta come secondo livello,
        // più basso, per rendere l'incendio più vivo.
        fireCracklePlayer.setVolume(0.55);

        // Il tick deve restare percepibile senza diventare fastidioso
        // quando Next Turn viene premuto rapidamente.
        tickAdvanceSound.setVolume(0.72);
    }

    private AudioClip loadClip(String fileName)
    {
        URL url = getClass().getResource(
                "/music-effects/" + fileName);

        if (url == null)
        {
            throw new IllegalStateException(
                    "Sound not found: " + fileName
            );
        }

        return new AudioClip(url.toExternalForm());
    }

    private MediaPlayer loadPlayer(String fileName)
    {
        URL url = getClass().getResource(
                "/music-effects/" + fileName);

        if (url == null)
        {
            throw new IllegalStateException(
                    "Sound not found: " + fileName
            );
        }

        return new MediaPlayer(
                new Media(url.toExternalForm())
        );
    }

    private AudioClip loadFirstAvailableClip(
            String[] fileNames)
    {
        for (String fileName : fileNames)
        {
            URL url = getClass().getResource(
                    "/music-effects/" + fileName);

            if (url != null)
            {
                return new AudioClip(
                        url.toExternalForm()
                );
            }
        }

        return null;
    }

    public void playAchievementSound()
    {
        achievementSound.play();
    }

    public void playTickAdvanceSound()
    {
        // Riavvia il brevissimo campione invece di sovrapporne molte copie.
        tickAdvanceSound.stop();
        tickAdvanceSound.play();
    }

    public void playBlackoutSound()
    {
        blackoutSound.stop();
        blackoutSound.play();
    }

    public void playInsuranceRebuildSound()
    {
        insuranceRebuildSound.stop();
        insuranceRebuildSound.play();
    }

    public void playDemolitionSound()
    {
        demolitionSound.play();
    }

    public void playPlacementSound()
    {
        placementSound.play();
    }

    public void playGridExpansionSound()
    {
        // Evita che il normale suono di piazzamento si sovrapponga
        // al cue speciale dell'espansione.
        placementSound.stop();
        gridExpansionSound.stop();
        gridExpansionSound.play();
    }

    public void playMissileSound()
    {
        missileSound.play();
    }

    public void playFireSound()
    {
        firePlayer.setVolume(1.0);
        fireCracklePlayer.setVolume(0.55);

        firePlayer.play();
        fireCracklePlayer.play();
    }

    public void stopFireSound()
    {
        firePlayer.stop();
        fireCracklePlayer.stop();
    }

    public void playMissileGridImpactSound()
    {
        missileGridImpactSound.play();
    }

    public void playMissileShieldImpactSound()
    {
        missileShieldImpactSound.play();
    }

    public void playTsunamiSound()
    {
        tsunamiPlayer.stop();
        tsunamiPlayer.setVolume(1.0);
        tsunamiPlayer.play();
    }

    public void stopTsunamiSound()
    {
        tsunamiPlayer.stop();
    }

    public void playNuclearExplosionSound()
    {
        // Se la causa è un missile, il boato nucleare deve dominare
        // l'eventuale coda del normale suono di impatto.
        missileGridImpactSound.stop();
        blackoutSound.stop();
        nuclearExplosionSound.stop();
        nuclearExplosionSound.play();
    }

    public void duckLongEventSoundsForNuclearExplosion()
    {
        // MediaPlayer applica il volume anche alla riproduzione già in corso.
        tsunamiPlayer.setVolume(0.24);
        firePlayer.setVolume(0.24);
        fireCracklePlayer.setVolume(0.14);
    }

    public void restoreLongEventSoundsAfterNuclearExplosion()
    {
        tsunamiPlayer.setVolume(1.0);
        firePlayer.setVolume(1.0);
        fireCracklePlayer.setVolume(0.55);
    }

    public void playEconomicBoomSound()
    {
        economicBoomSound.play();
    }

    public void playHackerAttackSound()
    {
        hackerAttackSound.play();
    }

    public void playEnergyCrisisSound()
    {
        energyCrisisSound.play();
    }

    public void stopGameplaySounds()
    {
        achievementSound.stop();
        tickAdvanceSound.stop();
        blackoutSound.stop();
        insuranceRebuildSound.stop();
        demolitionSound.stop();
        placementSound.stop();
        gridExpansionSound.stop();
        missileSound.stop();
        stopFireSound();
        missileGridImpactSound.stop();
        missileShieldImpactSound.stop();
        tsunamiPlayer.stop();
        nuclearExplosionSound.stop();
        economicBoomSound.stop();
        hackerAttackSound.stop();
        energyCrisisSound.stop();
    }

    public void playDefeatSound()
    {
        if (defeatSound != null)
        {
            defeatSound.play();
        }
    }

    public void urlChecker(URL url)
    {
        if (url == null)
        {
            throw new IllegalStateException(
                    "source not found"
            );
        }
    }
}
