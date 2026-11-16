import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Migration {
    private static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/buspass_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static List<String[]> readValidRecords(Path input, Path rejected) throws Exception {
        List<String[]> valid = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(input);
             BufferedWriter errors = Files.newBufferedWriter(rejected,
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split(",", -1);
                try {
                    if (fields.length != 5) {
                        throw new IllegalArgumentException("Expected 5 fields");
                    }
                    if (fields[0].isBlank() || fields[1].isBlank() || fields[2].isBlank()) {
                        throw new IllegalArgumentException("Required field is empty");
                    }
                    int route = Integer.parseInt(fields[3]);
                    java.math.BigDecimal amount = new java.math.BigDecimal(fields[4]);
                    if (route <= 0 || amount.signum() < 0) {
                        throw new IllegalArgumentException("Route or amount is out of range");
                    }
                    valid.add(fields);
                } catch (RuntimeException e) {
                    errors.write("Line " + lineNumber + ": " + e.getMessage() + " | " + line);
                    errors.newLine();
                }
            }
        }
        return valid;
    }

    private static void showMetadata(Connection con) throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        System.out.println("Database product: " + md.getDatabaseProductName());
        System.out.println("Database version: " + md.getDatabaseProductVersion());
        System.out.println("JDBC driver: " + md.getDriverName());
        System.out.println("JDBC driver version: " + md.getDriverVersion());

        try (ResultSet tables = md.getTables(con.getCatalog(), null, "%",
                new String[]{"TABLE"})) {
            System.out.println("Tables:");
            while (tables.next()) {
                System.out.println(tables.getString("TABLE_NAME"));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path input = Path.of(args.length > 0 ? args[0] : "passes.csv");
        Path rejected = Path.of("rejected_records.txt");

        List<String[]> valid = readValidRecords(input, rejected);
        System.out.println("Valid records: " + valid.size());
        System.out.println("Rejected records report: " + rejected.toAbsolutePath());

        try (Connection con = connect()) {
            int loaded = PassLoader.loadAll(con, valid);
            System.out.println("Records migrated: " + loaded);
            showMetadata(con);
        }
    }
}
