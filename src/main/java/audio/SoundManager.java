package audio;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;
    private final AudioClip demolitionSound;
    private final AudioClip placementSound;
    private final AudioClip missileSound;
    private final AudioClip fireSound;
    private final AudioClip fireCrackleSound;
    private final AudioClip missileGridImpactSound;
    private final AudioClip missileShieldImpactSound;
    private final AudioClip tsunamiSound;
    private final AudioClip economicBoomSound;
    private final AudioClip hackerAttackSound;

    public SoundManager()
    {
        achievementSound = loadClip("achievement.wav");
        demolitionSound = loadClip("demolition.wav");
        placementSound = loadClip("placement.wav");
        missileSound = loadClip("missile_flyby.wav");
        missileGridImpactSound = loadClip("missile_grid_impact.wav");
        missileShieldImpactSound = loadClip("missile_shield_impact.wav");
        tsunamiSound = loadClip("tsunami_wave.wav");
        economicBoomSound = loadClip("economic_boom.wav");
        hackerAttackSound = loadClip("hacker_intrusion.wav");
        fireSound = loadClip("fire_roar.mp3");
        fireCrackleSound = loadClip("fire.wav");
        fireSound.setCycleCount(AudioClip.INDEFINITE);
        fireCrackleSound.setCycleCount(AudioClip.INDEFINITE);

        // Il crackle originale resta come secondo livello,
        // più basso, per rendere l'incendio più vivo.
        fireCrackleSound.setVolume(0.55);
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

    public void playAchievementSound()
    {
        achievementSound.play();
    }

    public void playDemolitionSound()
    {
        demolitionSound.play();
    }

    public void playPlacementSound()
    {
        placementSound.play();
    }

    public void playMissileSound()
    {
        missileSound.play();
    }

    public void playFireSound()
    {
        if (!fireSound.isPlaying())
        {
            fireSound.play();
        }

        if (!fireCrackleSound.isPlaying())
        {
            fireCrackleSound.play();
        }
    }

    public void stopFireSound()
    {
        fireSound.stop();
        fireCrackleSound.stop();
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
        tsunamiSound.play();
    }

    public void playEconomicBoomSound()
    {
        economicBoomSound.play();
    }

    public void playHackerAttackSound()
    {
        hackerAttackSound.play();
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
