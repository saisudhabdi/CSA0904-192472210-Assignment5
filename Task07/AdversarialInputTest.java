import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AdversarialInputTest {
    private static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/buspass_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static boolean unsafeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id='" + id + "' AND pwd='" + pwd + "'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    private static boolean safeLogin(Connection con, String id, String pwd) throws SQLException {
        String sql = "SELECT * FROM users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, pwd);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void inspectDamagedFile(Path file) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            int number = 0;
            while ((line = reader.readLine()) != null) {
                number++;
                if (line.isBlank()) {
                    System.out.println("Line " + number + ": rejected - empty line");
                    continue;
                }

                String[] fields = line.split(",", -1);
                if (fields.length != 5) {
                    System.out.println("Line " + number + ": rejected - expected 5 fields");
                    continue;
                }

                try {
                    Integer.parseInt(fields[3]);
                    double amount = Double.parseDouble(fields[4]);
                    if (amount < 0) {
                        throw new NumberFormatException();
                    }
                    System.out.println("Line " + number + ": valid record");
                } catch (NumberFormatException e) {
                    System.out.println("Line " + number + ": rejected - invalid route or amount");
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = connect()) {
            String attackId = "' OR '1'='1' -- ";
            System.out.println("Unsafe login accepts attack: " + unsafeLogin(con, attackId, "wrong"));
            System.out.println("PreparedStatement accepts attack: " + safeLogin(con, attackId, "wrong"));
        }

        Path file = Files.createTempFile("damaged-passes", ".csv");
        Files.writeString(file,
                "P1001,192311001,Arun,12,4500.0\n"
                + "P1002,Damaged\n"
                + "P1003,192311003,Divya,7,not-a-number\n"
                + "\n");
        inspectDamagedFile(file);
        Files.deleteIfExists(file);
    }
}
