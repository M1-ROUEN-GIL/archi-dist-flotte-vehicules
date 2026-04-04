package com.flotte.driver.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Crée les bases PostgreSQL listées dans {@code flotte.postgres.databases} en se connectant au catalogue {@code postgres},
 * avant l'initialisation du DataSource Spring.
 */
public class PostgresDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor {

	private static final Pattern JDBC_PG = Pattern.compile("jdbc:postgresql://([^/]+)/([^?]+)");

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		if (!"true".equalsIgnoreCase(environment.getProperty("flotte.postgres.auto-create-databases", "true"))) {
			return;
		}
		String rawUrl = environment.getProperty("spring.datasource.url");
		if (rawUrl == null || !rawUrl.startsWith("jdbc:postgresql:")) {
			return;
		}
		String databases = environment.getProperty("flotte.postgres.databases");
		if (databases == null || databases.isBlank()) {
			return;
		}
		String user = environment.getProperty("spring.datasource.username", "postgres");
		String password = environment.getProperty("spring.datasource.password", "");
		String bootstrapUrl = environment.getProperty("flotte.postgres.bootstrap-url");
		if (bootstrapUrl == null || bootstrapUrl.isBlank()) {
			bootstrapUrl = toBootstrapUrl(rawUrl);
		}

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException("Driver PostgreSQL introuvable pour la création des bases", e);
		}

		String[] names = Arrays.stream(databases.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toArray(String[]::new);

		try (Connection conn = DriverManager.getConnection(bootstrapUrl, user, password)) {
			conn.setAutoCommit(true);
			for (String db : names) {
				createDatabaseIfMissing(conn, db);
			}
		} catch (SQLException e) {
			throw new IllegalStateException(
				"Impossible de créer les bases PostgreSQL (bootstrap-url=" + bootstrapUrl + ")", e);
		}
	}

	static String toBootstrapUrl(String jdbcUrl) {
		Matcher m = JDBC_PG.matcher(jdbcUrl);
		if (!m.find()) {
			throw new IllegalArgumentException("URL JDBC PostgreSQL invalide: " + jdbcUrl);
		}
		return "jdbc:postgresql://" + m.group(1) + "/postgres";
	}

	private static void createDatabaseIfMissing(Connection conn, String databaseName) throws SQLException {
		if (!databaseName.matches("[a-zA-Z0-9_]+")) {
			throw new IllegalArgumentException("Nom de base invalide: " + databaseName);
		}
		try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM pg_database WHERE datname = ?")) {
			ps.setString(1, databaseName);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return;
				}
			}
		}
		try (Statement st = conn.createStatement()) {
			st.executeUpdate("CREATE DATABASE " + databaseName);
		}
	}
}
