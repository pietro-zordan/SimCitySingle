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

    public SoundManager()
    {
        URL achievementUrl = getClass().getResource(
                "/music-effects/achievement.wav");

        if (achievementUrl == null)
        {
            throw new IllegalStateException(
                    "Achievement sound not found"
            );
        }

        achievementSound = new AudioClip(
                achievementUrl.toExternalForm()
        );

        URL demolitionUrl = getClass().getResource(
                "/music-effects/demolition.wav");

        if (demolitionUrl == null)
        {
            throw new IllegalStateException(
                    "Demolition sound not found"
            );
        }

        demolitionSound = new AudioClip(
                demolitionUrl.toExternalForm()
        );

        URL placementUrl = getClass().getResource(
                "/music-effects/placement.wav");

        if (placementUrl == null)
        {
            throw new IllegalStateException(
                    "Placement sound not found"
            );
        }

        placementSound = new AudioClip(
                placementUrl.toExternalForm()
        );

        URL missileUrl = getClass().getResource(
                "/music-effects/missile_flyby.wav");

        if (missileUrl == null)
        {
            throw new IllegalStateException(
                    "Missile sound not found"
            );
        }

        missileSound = new AudioClip(
                missileUrl.toExternalForm()
        );

        URL fireUrl = getClass().getResource(
                "/music-effects/fire.wav");

        if (fireUrl == null)
        {
            throw new IllegalStateException(
                    "Fire sound not found"
            );
        }

        fireSound = new AudioClip(
                fireUrl.toExternalForm()
        );

        fireSound.setCycleCount(
                AudioClip.INDEFINITE
        );
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
    }

    public void stopFireSound()
    {
        fireSound.stop();
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
