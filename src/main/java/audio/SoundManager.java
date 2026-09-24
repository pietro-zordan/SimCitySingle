package audio;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;
    private final AudioClip demolitionSound;
<<<<<<< Updated upstream
    private final AudioClip placementSound;
=======
    private final AudioClip missileSound;
>>>>>>> Stashed changes

    public SoundManager()
    {
        URL achievementUrl = getClass().getResource(
                "/music-effects/achievement.wav");

        if(achievementUrl == null)
        {
            throw new IllegalStateException("Achievement sound not found");
        }

        achievementSound = new AudioClip(achievementUrl.toExternalForm());

        URL demolitionUrl = getClass().getResource(
                "/music-effects/demolition.wav");

        if (demolitionUrl == null)
        {
            throw new IllegalStateException("Demolition sound not found");
        }

        demolitionSound = new AudioClip(
                demolitionUrl.toExternalForm());

<<<<<<< Updated upstream
        URL placementUrl = getClass().getResource(
                "/music-effects/placement.wav");

        if (placementUrl == null)
        {
            throw new IllegalStateException("Placement sound not found");
        }

        placementSound = new AudioClip(
                placementUrl.toExternalForm());
=======
        URL missileUrl = getClass().getResource("/music-effects/missile_flyby.wav");

        if (missileUrl == null)
            throw new IllegalStateException("Missile sound not found");

        missileSound = new AudioClip(achievementUrl.toExternalForm());

>>>>>>> Stashed changes
    }

    public void playAchievementSound()
    {
        achievementSound.play();
    }

    public void playDemolitionSound()
    {
        demolitionSound.play();
    }

<<<<<<< Updated upstream
    public void playPlacementSound()
    {
        placementSound.play();
    }

=======
    public void playMissileSound()
    {
        missileSound.play();
    }

    public void urlChecker(URL url)
    {
        if(url == null)
            throw new IllegalStateException("source not found");

    }



>>>>>>> Stashed changes
}
