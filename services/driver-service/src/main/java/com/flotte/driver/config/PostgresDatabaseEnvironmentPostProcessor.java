package com.flotte.driver.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
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
 * Crée les bases PostgreSQL avant l'initialisation du DataSource : soit {@code flotte.postgres.databases},
 * soit le nom de base extrait de {@code spring.datasource.url} (cas Docker / variables d'environnement).
 */
public class PostgresDatabaseEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

	private static final Pattern JDBC_PG = Pattern.compile("jdbc:postgresql://([^/]+)/([^?]+)");

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		Binder binder = Binder.get(environment);
		boolean autoCreate = binder.bind("flotte.postgres.auto-create-databases", Bindable.of(Boolean.class))
			.orElse(true);
		if (!autoCreate) {
			return;
		}
		String rawUrl = binder.bind("spring.datasource.url", Bindable.of(String.class)).orElse(null);
		if (rawUrl == null) {
			rawUrl = environment.getProperty("spring.datasource.url");
		}
		if (rawUrl == null) {
			rawUrl = System.getenv("SPRING_DATASOURCE_URL");
		}
		if (rawUrl == null || !rawUrl.startsWith("jdbc:postgresql:")) {
			return;
		}
		String databases = binder.bind("flotte.postgres.databases", Bindable.of(String.class)).orElse(null);
		if (databases == null || databases.isBlank()) {
			databases = environment.getProperty("flotte.postgres.databases");
		}
		if (databases == null || databases.isBlank()) {
			Matcher urlMatch = JDBC_PG.matcher(rawUrl);
			if (!urlMatch.find()) {
				return;
			}
			databases = urlMatch.group(2);
		}
		String user = binder.bind("spring.datasource.username", Bindable.of(String.class)).orElse(null);
		if (user == null) {
			user = environment.getProperty("spring.datasource.username");
		}
		if (user == null) {
			user = System.getenv("SPRING_DATASOURCE_USERNAME");
		}
		if (user == null) {
			user = "postgres";
		}
		String password = binder.bind("spring.datasource.password", Bindable.of(String.class)).orElse(null);
		if (password == null) {
			password = environment.getProperty("spring.datasource.password");
		}
		if (password == null) {
			password = System.getenv("SPRING_DATASOURCE_PASSWORD");
		}
		if (password == null) {
			password = "";
		}
		String bootstrapUrl = binder.bind("flotte.postgres.bootstrap-url", Bindable.of(String.class)).orElse(null);
		if (bootstrapUrl == null || bootstrapUrl.isBlank()) {
			bootstrapUrl = environment.getProperty("flotte.postgres.bootstrap-url");
		}
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
