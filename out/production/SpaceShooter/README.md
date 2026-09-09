# 2D Space Shooter (Java, Swing + AWT, zero external dependencies)

## How to compile & run

From inside the `SpaceShooter` folder, simplest option:

```bash
javac $(find . -name "*.java") -d out && cp -r images out/ && cd out && java Main
```

Requires JDK 17+ **with AWT/X11 support** (a `-headless` JDK package will fail
to open the window — use `openjdk-17-jdk`, not `openjdk-17-jdk-headless`).

### Running from IntelliJ IDEA (or any IDE)
Image loading works no matter what your IDE's working directory is set to —
`util/ImageLoader.java` tries three locations in order and prints a clear
`[ImageLoader]` message to the **Run** console if a file truly can't be
found. If ships render as plain colored shapes instead of artwork, check
that console message first.

## Controls

Full reference is also in-game: Main Menu → **How To Play**.

| Button (controller) | Action           | Player 1  | Player 2   |
|---|---|---|---|
| RT                   | Shoot            | Space     | `/`        |
| R3                   | Aim gun          | `[` / `]` | `,` / `.`  |
| L3                   | Move             | WASD      | Arrows     |
| RB                   | Upgrade weapon   | Q         | `\`        |
| A                    | OK / Select      | Enter     | Enter      |
| B                    | Cancel           | Escape    | Escape     |
| ☰                    | Settings / Pause | P         | P          |

In 1-player mode either column moves the one ship (WASD **or** Arrows both
work, same as before). `M` is a quick mute toggle in addition to the
Settings menu.

### About "play on controller"
Core Java (the JDK) has no built-in API for reading physical gamepads —
that requires a third-party native library (e.g. JInput), which would add
a real dependency and setup risk (native `.dll`/`.so` files, classpath
config) to a project that's been "just open and run" the whole way through.
Instead, every control above is a real keyboard key with a controller-style
label. To use an actual gamepad, map its buttons to these keys with free,
no-code software — **Steam Input** (add the game as a non-Steam game, set
a custom keyboard-mapping profile) is the easiest route and works on any
controller Steam recognizes.

## Features
- **1 or 2 player co-op** — pick from Main Menu → Start → 1 Player / 2
  Player. Both ships fight the same wave and share score/level/coins;
  Player 2's ship has a green tint to tell them apart. The round ends
  when every player still standing has run out of lives.
- **Aimable gun ("R3")** — hold aim-left/right to swivel your shots up to
  25° off vertical, independent of ship movement; a small line on the
  nose shows the current aim angle. Every weapon mode's spread rotates
  together with it.
- **Coins**: final score converts to coins 1-for-1 at Game Over and adds
  to a persistent total (`coins.txt`, plain int, via `util/CoinManager`).
  Shown on the Main Menu and the Game Over screen.
- **How To Play screen** — the full button table above, in-game.
- **Settings screen** — sound on/off, reachable from the Main Menu or
  mid-game from the Pause menu (returns to wherever you opened it from).
- **Main Menu** — Start, How To Play, High Scores, Achievements, Settings,
  Exit, all keyboard-navigable
- **Pause Menu** — Resume, Restart, Settings, Main Menu, Exit
- **6 enemy types** via inheritance/polymorphism (`Enemy` base class):
  Basic, Fast, Tank, Zigzag, Shooter (fires back), Boss (every 500 points,
  fires spread shots)
- **4 weapon modes**, cycled with Q/`\` or via the Weapon Upgrade power-up
- **7 power-ups**: Life, Speed Boost, Rapid Fire, Shield, Double Score,
  Bomb, Weapon Upgrade (18% drop chance per kill)
- **Achievements**: First Blood, Centurion, Untouchable, Survivor, Boss
  Slayer — unlocking one shows an in-game popup and plays a sound
- **Animated parallax background**: three star layers at different speeds,
  drifting rotating asteroids, and the project's nebula art scrolling
  seamlessly underneath
- **Sprite-sheet explosions**: the death-burst animation is a hand-drawn
  34-frame sprite sheet (`images/explosion.png`), rendered via
  `util/SpriteSheet.java`, with a few flying spark/debris particles on
  top. Non-lethal hits get a small, quick `Explosion.hitSpark(...)`
  instead of the full burst. Falls back to procedural particles
  automatically if the art is ever missing.
- **Synthesized sound effects** — every sound is generated at runtime with
  `javax.sound.sampled`, so there are no audio files to ship. If no audio
  device is present the game plays silently instead of crashing.
- **Persistent high scores** via `util/HighScoreManager` (plain-text file,
  `FileWriter`/`BufferedWriter`/`Scanner`)
- **Live HUD**: score, level, weapon(s), FPS, mute status, lives per player
- Progressive difficulty (spawn rate & enemy speed scale with level)

## Project structure
```
SpaceShooter/
├── Main.java
├── GamePanel.java
├── GameLoop.java
├── KeyHandler.java              (P1/P2 key sets, aim keys, Cancel/Escape)
├── Explosion.java               (sprite-sheet death burst + hit-spark + particles)
├── Particle.java                (single fire/smoke/spark/debris particle)
├── Starfield.java               (animated parallax background)
├── player/Player.java           (aim angle, P2 tint, setPosition)
├── bullet/Bullet.java           (angled flight for aimed shots)
├── collision/CollisionChecker.java
├── enemy/
│   ├── Enemy.java               (abstract base)
│   ├── BasicEnemy.java / FastEnemy.java / TankEnemy.java
│   ├── ZigzagEnemy.java / ShooterEnemy.java / BossEnemy.java
│   └── EnemyBullet.java
├── weapon/WeaponMode.java
├── powerup/PowerUp.java
├── manager/
│   ├── SoundManager.java        (synthesized sound effects)
│   ├── Achievement.java
│   └── AchievementManager.java
├── ui/
│   ├── UI.java                  (HUD, menus, screens incl. How To Play/Settings)
│   └── Toast.java               (popup notification data)
├── util/
│   ├── ImageLoader.java
│   ├── SpriteSheet.java
│   ├── HighScoreManager.java
│   └── CoinManager.java         (persistent coin total)
└── images/                      (sprite art, including explosion.png)
```

## Still on the roadmap
- Sprite-sheet animation for the player/enemy ships too (currently only
  the explosion uses real animation frames; ships are still static sprites)
- Named high-score entries (currently numeric scores only)
- A shop to spend coins on upgrades (coins are earned and tracked now,
  but not yet spendable on anything)
