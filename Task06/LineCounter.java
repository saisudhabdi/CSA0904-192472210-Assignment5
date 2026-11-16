import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class LineCounter {
    private static long countLines(InputStream in) throws IOException {
        long lines = 0;
        int value;
        while ((value = in.read()) != -1) {
            if (value == '\n') {
                lines++;
            }
        }
        return lines;
    }

    private static long countUnbuffered(Path file) throws IOException {
        try (InputStream in = new FileInputStream(file.toFile())) {
            return countLines(in);
        }
    }

    private static long countBuffered(Path file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file.toFile()), 65536)) {
            return countLines(in);
        }
    }

    private static void createFile(Path file, long bytes) throws IOException {
        byte[] block = new byte[8192];
        java.util.Arrays.fill(block, (byte) 'x');
        block[block.length - 1] = '\n';
        try (var out = Files.newOutputStream(file)) {
            long written = 0;
            while (written < bytes) {
                int size = (int) Math.min(block.length, bytes - written);
                out.write(block, 0, size);
                written += size;
            }
        }
    }

    private static long timedCount(Path file, boolean buffered) throws IOException {
        long start = System.nanoTime();
        long count = buffered ? countBuffered(file) : countUnbuffered(file);
        long elapsed = System.nanoTime() - start;
        System.out.printf("%s: %,d lines, %.2f ms%n",
                buffered ? "Buffered" : "Unbuffered", count,
                elapsed / 1_000_000.0);
        return elapsed;
    }

    public static void main(String[] args) throws IOException {
        long[] sizes = {1L, 10L, 50L};
        for (long mb : sizes) {
            Path file = Files.createTempFile("attendance-" + mb + "MB", ".log");
            createFile(file, mb * 1024 * 1024);
            System.out.println("\nFile size: " + mb + " MB");
            timedCount(file, false);
            timedCount(file, true);
            Files.deleteIfExists(file);
        }
    }
}
