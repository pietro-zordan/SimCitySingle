package audio;

import javafx.scene.media.AudioClip;

import java.net.URL;

public class SoundManager
{

    private final AudioClip achievementSound;

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
    }

    public void playAchievementSound()
    {
        achievementSound.play();
    }

}
