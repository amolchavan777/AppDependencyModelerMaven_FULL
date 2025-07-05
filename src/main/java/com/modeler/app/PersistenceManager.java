package com.modeler.app;

import com.enterprise.dependency.model.core.Application;
import com.enterprise.dependency.model.core.Dependency;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple persistence layer using an embedded H2 database.
 * <p>
 * This class manages table creation and inserts for application
 * dependency data. It is designed for CLI use and does not rely on
 * any Spring components.
 */
public class PersistenceManager implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceManager.class);
    private final Connection conn;

    /**
     * Open or create a database at the provided JDBC URL.
     * Example URL: {@code jdbc:h2:./output/dependencyDB}
     */
    public PersistenceManager(String jdbcUrl) throws SQLException {
        long start = System.currentTimeMillis();
        this.conn = DriverManager.getConnection(jdbcUrl);
        initSchema();
        LOGGER.info("Database connection established in {} ms", System.currentTimeMillis() - start);
    }

    private void initSchema() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS applications (name VARCHAR PRIMARY KEY, type VARCHAR, environment VARCHAR, owner VARCHAR)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS dependencies (from_app VARCHAR, to_app VARCHAR, type VARCHAR, confidence DOUBLE)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS claims (source VARCHAR, from_app VARCHAR, to_app VARCHAR, type VARCHAR, exists_flag BOOLEAN, confidence DOUBLE)");
        }
    }

    /** Persist a list of raw claims. */
    public void saveClaims(List<Claim> claims) throws SQLException {
        String sql = "INSERT INTO claims (source, from_app, to_app, type, exists_flag, confidence) VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Claim c : claims) {
                ps.setString(1, c.source);
                ps.setString(2, c.fromApp);
                ps.setString(3, c.toApp);
                ps.setString(4, c.type);
                ps.setBoolean(5, c.exists);
                ps.setDouble(6, c.confidence);
                ps.addBatch();
            }
            ps.executeBatch();
        }
        LOGGER.info("Persisted {} claims", claims.size());
    }

    /** Persist resolved dependencies. */
    public void saveDependencies(Map<String, Set<String>> deps) throws SQLException {
        String depSql = "INSERT INTO dependencies (from_app, to_app, type, confidence) VALUES (?,?,?,?)";
        String appSql = "MERGE INTO applications (name) KEY(name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(depSql);
             PreparedStatement appPs = conn.prepareStatement(appSql)) {
            for (Map.Entry<String, Set<String>> e : deps.entrySet()) {
                for (String target : e.getValue()) {
                    appPs.setString(1, e.getKey());
                    appPs.addBatch();
                    appPs.setString(1, target);
                    appPs.addBatch();

                    ps.setString(1, e.getKey());
                    ps.setString(2, target);
                    ps.setString(3, "calls");
                    ps.setDouble(4, 1.0); // TODO store actual confidence
                    ps.addBatch();
                }
            }
            ps.executeBatch();
            appPs.executeBatch();
        }
        LOGGER.info("Persisted {} dependencies", deps.size());
    }

    @Override
    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
            LOGGER.info("Database connection closed");
        }
    }
}
