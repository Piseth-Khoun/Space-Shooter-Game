package manager;

import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Every sound effect here is synthesized on the fly with javax.sound.sampled
 * - no audio files, no external dependencies, consistent with the rest of
 * the project. Each play() call renders a short PCM waveform in memory and
 * plays it on its own daemon thread so it never blocks the game loop.
 *
 * If no audio device is available (some sandboxes/CI machines), every
 * method fails silently instead of crashing the game.
 */
public class SoundManager {

    private static final float SAMPLE_RATE = 44100f;
    private static final Random RAND = new Random();
    private static volatile boolean muted = false;

    private SoundManager() {
        // static utility class, no instances
    }

    public static void setMuted(boolean value) { muted = value; }
    public static boolean isMuted() { return muted; }
    public static void toggleMuted() { muted = !muted; }

    // ---- game events ----

    public static void playShoot() {
        playAsync(() -> tone(new double[]{1400}, new int[]{45}, 0.16));
    }

    public static void playExplosion() {
        playAsync(() -> noise(160, 0.30, 0.35));
    }

    public static void playBigExplosion() {
        playAsync(() -> playBuffer(mix(noiseBuffer(260, 0.35, 0.22), toneBuffer(new double[]{110}, new int[]{260}, 0.22))));
    }

    public static void playHit() {
        playAsync(() -> tone(new double[]{220, 160}, new int[]{70, 90}, 0.22));
    }

    public static void playPowerUp() {
        playAsync(() -> tone(new double[]{660, 990}, new int[]{70, 110}, 0.20));
    }

    public static void playWeaponUpgrade() {
        playAsync(() -> tone(new double[]{523, 659, 880}, new int[]{60, 60, 120}, 0.20));
    }

    public static void playLevelUp() {
        playAsync(() -> tone(new double[]{523, 659, 784, 1046}, new int[]{70, 70, 70, 160}, 0.22));
    }

    public static void playBossAlert() {
        playAsync(() -> tone(new double[]{130, 98, 130, 98}, new int[]{140, 140, 140, 220}, 0.24));
    }

    public static void playAchievement() {
        playAsync(() -> tone(new double[]{784, 988, 1174, 1568}, new int[]{55, 55, 55, 180}, 0.22));
    }

    public static void playGameOver() {
        playAsync(() -> tone(new double[]{392, 349, 294, 220}, new int[]{160, 160, 160, 320}, 0.24));
    }

    public static void playMenuMove() {
        playAsync(() -> tone(new double[]{700}, new int[]{30}, 0.12));
    }

    public static void playMenuSelect() {
        playAsync(() -> tone(new double[]{700, 1050}, new int[]{40, 70}, 0.18));
    }

    // ---- synthesis internals ----

    private interface Job {
        void run();
    }

    private static void playAsync(Job job) {
        if (muted) return;
        Thread t = new Thread(job::run, "sfx");
        t.setDaemon(true);
        t.start();
    }

    private static void tone(double[] freqs, int[] durationsMs, double volume) {
        playBuffer(toneBuffer(freqs, durationsMs, volume));
    }

    private static void noise(int durationMs, double volume, double smoothing) {
        playBuffer(noiseBuffer(durationMs, volume, smoothing));
    }

    /** Sequenced sine-wave tones (a simple melody/chord progression) with a fade envelope on each note to avoid clicks. */
    private static byte[] toneBuffer(double[] freqs, int[] durationsMs, double volume) {
        int[] segSamples = new int[freqs.length];
        int totalSamples = 0;
        for (int i = 0; i < freqs.length; i++) {
            segSamples[i] = (int) (SAMPLE_RATE * durationsMs[i] / 1000.0);
            totalSamples += segSamples[i];
        }

        short[] samples = new short[totalSamples];
        int idx = 0;
        for (int s = 0; s < freqs.length; s++) {
            int n = segSamples[s];
            double freq = freqs[s];
            int fade = Math.max(1, Math.min(n / 6, (int) (SAMPLE_RATE * 0.006)));
            for (int i = 0; i < n; i++) {
                double t = i / SAMPLE_RATE;
                double envelope = 1.0;
                if (i < fade) envelope = i / (double) fade;
                else if (i > n - fade) envelope = (n - i) / (double) fade;
                double sample = Math.sin(2 * Math.PI * freq * t) * volume * envelope;
                samples[idx++] = (short) (sample * Short.MAX_VALUE);
            }
        }
        return toBytes(samples);
    }

    /** Filtered white noise with a fade envelope - used for explosions. */
    private static byte[] noiseBuffer(int durationMs, double volume, double smoothing) {
        int n = (int) (SAMPLE_RATE * durationMs / 1000.0);
        short[] samples = new short[n];
        double prev = 0;
        int fade = Math.max(1, Math.min(n / 6, (int) (SAMPLE_RATE * 0.008)));
        for (int i = 0; i < n; i++) {
            double white = RAND.nextDouble() * 2 - 1;
            prev = prev * smoothing + white * (1 - smoothing);
            double envelope = 1.0;
            if (i < fade) envelope = i / (double) fade;
            else if (i > n - fade) envelope = (n - i) / (double) fade;
            double sample = Math.max(-1, Math.min(1, prev)) * volume * envelope;
            samples[i] = (short) (sample * Short.MAX_VALUE);
        }
        return toBytes(samples);
    }

    private static byte[] mix(byte[] a, byte[] b) {
        int len = Math.max(a.length, b.length);
        byte[] out = new byte[len];
        for (int i = 0; i + 1 < len; i += 2) {
            int sa = sampleAt(a, i);
            int sb = sampleAt(b, i);
            int mixed = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, sa + sb));
            out[i] = (byte) (mixed & 0xFF);
            out[i + 1] = (byte) ((mixed >> 8) & 0xFF);
        }
        return out;
    }

    private static int sampleAt(byte[] buf, int i) {
        if (i + 1 >= buf.length) return 0;
        return (short) ((buf[i] & 0xFF) | (buf[i + 1] << 8));
    }

    private static byte[] toBytes(short[] samples) {
        byte[] bytes = new byte[samples.length * 2];
        for (int i = 0; i < samples.length; i++) {
            bytes[i * 2] = (byte) (samples[i] & 0xFF);
            bytes[i * 2 + 1] = (byte) ((samples[i] >> 8) & 0xFF);
        }
        return bytes;
    }

    private static void playBuffer(byte[] audioBytes) {
        if (audioBytes == null || audioBytes.length == 0) return;
        SourceDataLine line = null;
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();
            line.write(audioBytes, 0, audioBytes.length);
            line.drain();
        } catch (LineUnavailableException | IllegalArgumentException | SecurityException ex) {
            // No audio device available in this environment - fail silently.
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }
}
