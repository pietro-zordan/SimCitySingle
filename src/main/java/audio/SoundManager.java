package audio;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;
    private final AudioClip demolitionSound;

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
    }

    public void playAchievementSound()
    {
        achievementSound.play();
    }

    public void playDemolitionSound()
    {
        demolitionSound.play();
    }

}
