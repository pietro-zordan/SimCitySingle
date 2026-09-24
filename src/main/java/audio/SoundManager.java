package audio;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;
    private final AudioClip demolitionSound;
    private final AudioClip placementSound;

    public SoundManager()
    {
        URL achievementUrl = getClass().getResource(
                "/music-effects/achievement.wav");

        if(achievementUrl == null)
        {
            throw new IllegalStateException("Achievement sound not found");
        }

        achievementSound = new AudioClip(
                achievementUrl.toExternalForm());

        URL demolitionUrl = getClass().getResource(
                "/music-effects/demolition.wav");

        if (demolitionUrl == null)
        {
            throw new IllegalStateException("Demolition sound not found");
        }

        demolitionSound = new AudioClip(
                demolitionUrl.toExternalForm());

        URL placementUrl = getClass().getResource(
                "/music-effects/placement.wav");

        if (placementUrl == null)
        {
            throw new IllegalStateException("Placement sound not found");
        }

        placementSound = new AudioClip(
                placementUrl.toExternalForm());
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

}
