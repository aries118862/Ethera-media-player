package com.example.etheramediaplayer.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class FavoritesDatabase {
    private final Path databasePath;
    private boolean available;

    public FavoritesDatabase() {
        databasePath = Path.of(System.getProperty("user.home"), ".ethera", "favorites.db");
        initialize();
    }

    public boolean isFavorite(File file) {
        if (!available || file == null) {
            return false;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT 1 FROM favorites WHERE path = ?")) {
            statement.setString(1, file.getAbsolutePath());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException exception) {
            return false;
        }
    }

    public void setFavorite(File file, boolean favorite) {
        if (!available || file == null) {
            return;
        }
        try (Connection connection = openConnection()) {
            if (favorite) {
                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT OR IGNORE INTO favorites(path, title) VALUES (?, ?)")) {
                    statement.setString(1, file.getAbsolutePath());
                    statement.setString(2, file.getName());
                    statement.executeUpdate();
                }
            } else {
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM favorites WHERE path = ?")) {
                    statement.setString(1, file.getAbsolutePath());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Unable to update favorites", exception);
        }
    }

    public List<File> findFavorites() {
        List<File> favorites = new ArrayList<>();
        if (!available) {
            return favorites;
        }
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(
                 "SELECT path FROM favorites ORDER BY title COLLATE NOCASE");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                File file = new File(result.getString("path"));
                if (file.isFile()) {
                    favorites.add(file);
                }
            }
        } catch (SQLException exception) {
            return favorites;
        }
        return favorites;
    }

    private void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            Files.createDirectories(databasePath.getParent());
            try (Connection connection = openConnection(); Statement statement = connection.createStatement()) {
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS favorites ("
                    + "path TEXT PRIMARY KEY, title TEXT NOT NULL, created_at TEXT DEFAULT CURRENT_TIMESTAMP)");
            }
            available = true;
        } catch (Exception exception) {
            available = false;
        }
    }

    public boolean isAvailable() {
        return available;
    }

    private Connection openConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath);
    }
}
