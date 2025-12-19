import javax.sound.sampled.*;

public class SoundEngine {
    private static final int SAMPLE_RATE = 44100;

    public static void playClick() {
        new Thread(() -> playTone(450, 40, 0.4)).start();
    }

    public static void playVictory() {
        new Thread(() -> {
            int[] notes = {523, 659, 784, 1046};
            for (int n : notes) playTone(n, 120, 0.5);
        }).start();
    }

    private static void playTone(double freq, int durationMs, double volume) {
        try {
            byte[] buf = new byte[SAMPLE_RATE * durationMs / 1000];
            for (int i = 0; i < buf.length; i++) {
                double angle = i / (SAMPLE_RATE / freq) * 2.0 * Math.PI;
                buf[i] = (byte) (Math.sin(angle) * 127.0 * volume);
            }
            AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();
            sdl.write(buf, 0, buf.length);
            sdl.drain();
            sdl.close();
        } catch (Exception ignored) {
        }
    }
}