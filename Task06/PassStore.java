import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PassStore {
    private final Path file;

    public PassStore(Path file) {
        this.file = file;
    }

    public void add(String passId, String regNo, String name, int route, double amount) throws IOException {
        if (passId == null || passId.isBlank() || regNo == null || regNo.isBlank()
                || name == null || name.isBlank() || route <= 0 || amount < 0) {
            throw new IllegalArgumentException("Invalid pass details");
        }

        if (find(passId) != null) {
            throw new IllegalArgumentException("Duplicate pass ID: " + passId);
        }

        try (BufferedWriter w = Files.newBufferedWriter(file,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            w.write(String.join(",", passId, regNo, name.replace(",", " "),
                    String.valueOf(route), String.valueOf(amount)));
            w.newLine();
        }
    }

    public String[] find(String passId) throws IOException {
        if (!Files.exists(file)) {
            return null;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue;
                }

                try {
                    Integer.parseInt(fields[3]);
                    double amount = Double.parseDouble(fields[4]);
                    if (amount < 0) {
                        continue;
                    }
                } catch (NumberFormatException e) {
                    continue;
                }

                if (fields[0].equals(passId)) {
                    return fields;
                }
            }
        }
        return null;
    }

    public List<String[]> listByRoute(int route) throws IOException {
        List<String[]> result = new ArrayList<>();
        if (!Files.exists(file)) {
            return result;
        }

        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    continue;
                }
                try {
                    int recordRoute = Integer.parseInt(fields[3]);
                    double amount = Double.parseDouble(fields[4]);
                    if (recordRoute == route && amount >= 0) {
                        result.add(fields);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return result;
    }

    public static void main(String[] args) throws IOException {
        Path f = Files.createTempFile("passes", ".csv");
        PassStore store = new PassStore(f);
        store.add("P1001", "192311001", "Arun", 12, 4500);
        Files.writeString(f, "P1002,DAMAGED LINE\n", StandardOpenOption.APPEND);
        store.add("P1003", "192311003", "Divya", 7, 3800);

        System.out.println(Arrays.toString(store.find("P1003")));
        System.out.println(Arrays.toString(store.find("P1002")));

        Files.deleteIfExists(f);
    }
}
