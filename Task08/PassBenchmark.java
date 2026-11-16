import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PassBenchmark {
    private static Connection connect() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL",
                "jdbc:mysql://localhost:3306/buspass_test");
        String user = System.getenv().getOrDefault("DB_USER", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "");
        return DriverManager.getConnection(url, user, password);
    }

    private static List<String[]> generate(int count) {
        List<String[]> rows = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            rows.add(new String[]{"P" + i, "1923" + String.format("%06d", i),
                    "Student" + i, String.valueOf((i % 20) + 1), "4500.00"});
        }
        return rows;
    }

    private static void clear(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.executeUpdate("DELETE FROM bus_pass");
        }
    }

    private static long runStatement(Connection con, List<String[]> rows) throws SQLException {
        clear(con);
        long start = System.nanoTime();
        try (Statement st = con.createStatement()) {
            for (String[] p : rows) {
                String sql = "INSERT INTO bus_pass VALUES ('" + p[0] + "','" + p[1]
                        + "','" + p[2] + "'," + p[3] + "," + p[4] + ")";
                st.executeUpdate(sql);
            }
        }
        return System.nanoTime() - start;
    }

    private static long runPrepared(Connection con, List<String[]> rows) throws SQLException {
        clear(con);
        long start = System.nanoTime();
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : rows) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setBigDecimal(5, new java.math.BigDecimal(p[4]));
                ps.executeUpdate();
            }
        }
        return System.nanoTime() - start;
    }

    private static long runBatch(Connection con, List<String[]> rows) throws SQLException {
        clear(con);
        long start = System.nanoTime();
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";
        boolean old = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int pending = 0;
            for (String[] p : rows) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setBigDecimal(5, new java.math.BigDecimal(p[4]));
                ps.addBatch();
                pending++;
                if (pending == 1000) {
                    ps.executeBatch();
                    ps.clearBatch();
                    pending = 0;
                }
            }
            if (pending > 0) {
                ps.executeBatch();
            }
            con.commit();
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(old);
        }
        return System.nanoTime() - start;
    }

    private static void showMetadata(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM bus_pass LIMIT 1")) {
            ResultSetMetaData md = rs.getMetaData();
            System.out.println("\nColumn metadata:");
            for (int i = 1; i <= md.getColumnCount(); i++) {
                System.out.println(md.getColumnName(i) + " : " + md.getColumnTypeName(i));
            }
        }
    }

    private static void showFailureDifference(Connection con) throws SQLException {
        String sql = "INSERT INTO bus_pass VALUES (?,?,?,?,?)";

        clear(con);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                ps.setString(1, "AUTO" + i);
                ps.setString(2, "1923" + i);
                ps.setString(3, "AutoStudent");
                ps.setInt(4, 1);
                if (i == 4) {
                    throw new NumberFormatException("Forced invalid amount at row 4");
                }
                ps.setBigDecimal(5, new java.math.BigDecimal("4500.00"));
                ps.executeUpdate();
            }
        } catch (NumberFormatException e) {
            System.out.println("Auto-commit failure: " + e.getMessage());
        }
        System.out.println("Rows remaining with auto-commit: " + rowCount(con));

        clear(con);
        boolean old = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 6; i++) {
                ps.setString(1, "TX" + i);
                ps.setString(2, "1923" + i);
                ps.setString(3, "TransactionStudent");
                ps.setInt(4, 1);
                if (i == 4) {
                    throw new NumberFormatException("Forced invalid amount at row 4");
                }
                ps.setBigDecimal(5, new java.math.BigDecimal("4500.00"));
                ps.executeUpdate();
            }
            con.commit();
        } catch (NumberFormatException | SQLException e) {
            con.rollback();
            System.out.println("Transaction rolled back: " + e.getMessage());
        } finally {
            con.setAutoCommit(old);
        }
        System.out.println("Rows remaining with transaction: " + rowCount(con));
    }

    private static int rowCount(Connection con) throws SQLException {
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = connect()) {
            for (int size : new int[]{10, 1000, 10000, 100000}) {
                List<String[]> rows = generate(size);
                if (size == 10) {
                    runStatement(con, rows);
                    runPrepared(con, rows);
                    runBatch(con, rows);
                    showMetadata(con);
                } else {
                    long statement = runStatement(con, rows);
                    long prepared = runPrepared(con, rows);
                    long batch = runBatch(con, rows);
                    System.out.printf("%d rows | Statement %.2f ms | PreparedStatement %.2f ms | Batch %.2f ms%n",
                            size, statement / 1_000_000.0, prepared / 1_000_000.0,
                            batch / 1_000_000.0);
                }
            }
            showFailureDifference(con);
        }
    }
}
