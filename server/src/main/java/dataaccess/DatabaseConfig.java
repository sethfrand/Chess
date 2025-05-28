package dataaccess;

import java.sql.SQLException;

public class DatabaseConfig {

    public static void configureDatabase(String[] createStatement) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var connect = DatabaseManager.getConnection()) {
            for (var table : createStatement) {
                try (var prep = connect.prepareStatement(table)) {
                    prep.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect");
        }
    }
}
