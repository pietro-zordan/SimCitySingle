# Audio sources

- `missile_flyby.wav`: "Rocket Fly By (8-bit)" by Person, from
  https://opengameart.org/content/rocket-fly-8-bit (CC0 1.0).
  Converted from the original 44.1 kHz mono WAV to 22.05 kHz mono PCM WAV.
  Time-compressed and faded to 1.2 seconds to match the missile flight.
  The clip is the rocket flying past; it contains no impact effect.
- `demolition.wav`: "paper_crushed_-_2.mp3" by Luckius, from
  https://opengameart.org/content/various-paper-sound-effects (CC0 1.0).
  Converted to 22.05 kHz mono PCM WAV, with a short fade and gain adjustment.
- `placement.wav`: "Click" by qubodup, from
  https://opengameart.org/content/click (CC0 1.0).
  Converted from 44.1 kHz 24-bit mono WAV to 22.05 kHz 16-bit mono PCM WAV.
- `fire.wav`: "Fire Crackling" by AntumDeluge, from
  https://opengameart.org/content/fire-crackling (CC0 1.0).
  Used as a looping fire ambience while the Fire event is active.
- `fire_roar.mp3`: converted copy of "The Fireplace 3.wav" by NoOneIsReal, from
  https://freesound.org/people/NoOneIsReal/sounds/387128/ (CC0 1.0).
  Used as the main, stronger fire layer while the Fire event is active.
- `missile_grid_impact.wav`: "Explosion" by TinyWorlds, from
  https://opengameart.org/content/explosion-0 (CC0 1.0).
  Converted to 22.05 kHz mono PCM WAV, softened and faded.
- `missile_shield_impact.wav`: "space shield sounds - 4.wav" by bart, from
  https://opengameart.org/content/space-ship-shield-sounds (CC0 1.0).
  Converted to 22.05 kHz mono PCM WAV, with a short fade and gain adjustment.
- `tsunami_wave.wav`: "wave_02_cc0-11505__transitking__wavesound.flac"
  by transitking, from https://opengameart.org/content/water-waves (CC0 1.0).
  Layered with "underwater_or_space_engine.ogg" by gmason, a filtered
  recording of the ocean on a windy day, from
  https://opengameart.org/content/underwater-or-space-engine-rumble
  (CC0 1.0). Mixed with generated low rumbles and surf, converted to
  22.05 kHz mono PCM WAV, and timed to the three-second animation.
- `hacker_intrusion.wav`: Original synthesized digital interference, low
  sweep, glitch clicks and warning tones; 22.05 kHz mono PCM WAV.
- `economic_boom.wav`: "Correct Bell" by Fupi, from
  https://opengameart.org/content/correct-bell (CC0 1.0), mixed with
  "Purchasing Sound Effect" by Spring Spring, from
  https://opengameart.org/content/purchasing-sound-effect (CC0 1.0).
  Converted to 22.05 kHz mono PCM WAV with a short fade.
- `energy_crisis_alarm.wav`: original synthesized electrical grid-failure cue
  created for this project; 22.05 kHz mono PCM WAV. It uses a descending power
  hum, short electrical crackles and a low shutdown impact instead of a siren.
  It plays once when an energy crisis begins.

- `nuclear_explosion.wav`: "Muffled Distant Explosion" by NenadSimic, from
  https://opengameart.org/content/muffled-distant-explosion (CC0 1.0).
  Used for nuclear-plant destruction. During this sound, long-running event
  audio such as the tsunami and fire ambience is temporarily ducked so the
  nuclear explosion remains dominant.
- `tick_advance.wav`: trimmed "Tick and Tock" recording by cemkalyoncu,
  from https://opengameart.org/content/tick-and-tock (CC0 1.0).
  A soft, very short clock-like transient used for Next Turn; playback is
  restarted instead of layered when the player clicks rapidly.
- `blackout.wav`: original synthesized power-collapse cue created for this
  project; 22.05 kHz mono PCM WAV. It plays once when a construction that was
  powered at the beginning of a tick is still present but loses power.
- `insurance_rebuild.wav`: original synthesized two-tone restoration chime
  created for this project; 22.05 kHz mono PCM WAV. It plays once for each
  insured construction actually restored after a tsunami.

- `grid_expansion.wav`: original synthesized futuristic expansion cue created
  for this project; 22.05 kHz mono PCM WAV. A rising electronic sweep and
  short high-frequency chime play exactly when the grid grows to its next size.
- `mail_notification.wav`: `ui-chime-24` from SFXMint (CC0 1.0),
  https://sfxmint.com/sounds/ui-chime-24 . The included PCM16 WAV copy is
  the publicly mirrored/resampled version documented by the AIQSA project;
  it is used as the short incoming-mail bell.
- `mail_dock.wav`: `rollover1.wav` from Kenney's "UI SFX Set" (CC0 1.0),
  mirrored in the V-Sekai repository. It is used as the subtle UI cue while
  the fully-read mailbox folds into the bottom-right corner.
- `save_game_camera.wav`: `Shutter-02.wav` from the Kinoma camera sample,
  https://github.com/Kinoma/KPR-examples/tree/master/camera (Apache License 2.0).
  This is a short, clean camera-app shutter cue used when a manual game save
  completes successfully.


## Earthquake

- `earthquake_rumble.wav`: "Cinematic Rumble.flac" by qubodup.
- Source: https://freesound.org/people/qubodup/sounds/184936/
- Download: https://cdn.freesound.org/previews/184/184936_71257-hq.mp3
- License: CC0 1.0, https://creativecommons.org/publicdomain/zero/1.0/
- Adaptation: high-quality MP3 preview converted to mono PCM WAV, 44.1 kHz,
  trimmed to 5.8 seconds, with a 0.18-second fade-in and 1.3-second fade-out.
  Gain increased by 40%, with peaks limited to 0.95 to avoid clipping.
  The sound begins with the shake and ends with the animation.
