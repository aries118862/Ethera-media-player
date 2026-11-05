package com.example.etheramediaplayer.service;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleConsumer;

import javafx.scene.media.Media;

public final class MediaLibraryService {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of(
            ".mp3", ".mp4", ".m4a", ".wav", ".aac", ".flac", ".ogg", ".oga",
            ".opus", ".wma", ".aiff");

    public List<File> discoverLocalMedia(int maximumFiles) {
        return discoverLocalMedia(maximumFiles, ignored -> { });
    }

    public List<File> discoverLocalMedia(int maximumFiles, DoubleConsumer progressConsumer) {
        if (maximumFiles <= 0) {
            return List.of();
        }

        Path home = Paths.get(System.getProperty("user.home"));
        List<Path> roots = List.of(
                home.resolve("Music"),
                home.resolve("Videos"),
                home.resolve("Documents"),
                home.resolve("OneDrive").resolve("Music"),
                home.resolve("OneDrive").resolve("Videos"),
                home.resolve("OneDrive").resolve("Pictures"),
                Paths.get("C:/", "Users", "Public", "Music"),
                Paths.get("C:/", "Users", "Public", "Videos"));

        Set<File> discovered = new LinkedHashSet<>();
        for (int index = 0; index < roots.size(); index++) {
            Path root = roots.get(index);
            discoverRoot(root, maximumFiles, discovered, progressConsumer, index, roots.size());
            if (discovered.size() >= maximumFiles) {
                break;
            }
        }
        return new ArrayList<>(discovered);
    }

    public boolean isSupportedMedia(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        return SUPPORTED_EXTENSIONS.stream().anyMatch(name::endsWith);
    }

    public boolean isVideoMedia(File file) {
        if (file == null) {
            return false;
        }
        String name = file.getName().toLowerCase(Locale.ROOT);
        return Set.of(".mp4")
            .stream().anyMatch(name::endsWith);
    }

    public Map<String, List<File>> detectAlbums(List<File> files) {
        Map<String, List<File>> albums = new LinkedHashMap<>();
        if (files == null || files.isEmpty()) {
            return albums;
        }

        for (File file : files) {
            if (file == null || isVideoMedia(file)) {
                continue;
            }
            String albumName = detectAlbumName(file);
            albums.computeIfAbsent(albumName, ignored -> new ArrayList<>()).add(file);
        }
        return albums;
    }

    public String detectAlbumName(File file) {
        if (file == null || !isSupportedMedia(file) || isVideoMedia(file)) {
            return "Unknown Album";
        }

        try {
            Media media = new Media(file.toURI().toString());
            Object album = media.getMetadata().get("album");
            if (album instanceof String albumName && !albumName.isBlank()) {
                return albumName.trim();
            }
        } catch (RuntimeException ignored) {
            // Some media files do not expose metadata; fall back to the folder name.
        }

        File parent = file.getParentFile();
        if (parent != null && parent.getName() != null && !parent.getName().isBlank()) {
            return parent.getName();
        }
        return "Unknown Album";
    }

    private void discoverRoot(Path root, int maximumFiles, Set<File> discovered,
            DoubleConsumer progressConsumer, int rootIndex, int rootCount) {
        if (!Files.isDirectory(root)) {
            progressConsumer.accept((rootIndex + 1) / (double) rootCount);
            return;
        }

        List<Path> rootFiles;
        try (var paths = Files.walk(root)) {
            rootFiles = paths.filter(Files::isRegularFile)
                    .sorted(Comparator.naturalOrder())
                    .toList();
        } catch (IOException | UncheckedIOException ignored) {
            // A protected or disconnected folder must not prevent the player from starting.
            progressConsumer.accept((rootIndex + 1) / (double) rootCount);
            return;
        }

        int fileCount = Math.max(1, rootFiles.size());
        for (int fileIndex = 0; fileIndex < rootFiles.size(); fileIndex++) {
            if (discovered.size() >= maximumFiles) {
                break;
            }
            Path path = rootFiles.get(fileIndex);
            if (isSupportedMedia(path.toFile())) {
                discovered.add(path.toFile());
            }
            double rootProgress = (fileIndex + 1) / (double) fileCount;
            progressConsumer.accept((rootIndex + rootProgress) / rootCount);
        }
        progressConsumer.accept((rootIndex + 1) / (double) rootCount);
    }
}
