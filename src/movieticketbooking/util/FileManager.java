package movieticketbooking.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public final class FileManager {
    private FileManager() {}

    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        ensureFileExists(filePath);
        Path path = Paths.get(filePath);
        try (BufferedReader reader = new BufferedReader(
                Files.newBufferedReader(path, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read file " + filePath, e);
        }
        return lines;
    }

    public static void writeLines(String filePath, List<String> lines) {
        ensureFileExists(filePath);
        Path destination = Paths.get(filePath).toAbsolutePath();
        Path parentDirectory = destination.getParent();
        Path temporaryFile = null;

        try {
            temporaryFile = Files.createTempFile(
                parentDirectory,
                destination.getFileName().toString() + ".",
                ".tmp"
            );

            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            }

            try {
                Files.move(
                    temporaryFile,
                    destination,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException e) {
                // Some file systems do not support atomic moves. The fallback still
                // replaces the destination only after the temporary file is complete.
                Files.move(temporaryFile, destination, StandardCopyOption.REPLACE_EXISTING);
            }
            temporaryFile = null;
        } catch (IOException e) {
            deleteTemporaryFile(temporaryFile, e);
            throw new UncheckedIOException("Could not write file " + filePath, e);
        } catch (RuntimeException e) {
            deleteTemporaryFile(temporaryFile, e);
            throw e;
        }
    }

    private static void deleteTemporaryFile(Path temporaryFile, Throwable originalFailure) {
        if (temporaryFile == null) {
            return;
        }
        try {
            Files.deleteIfExists(temporaryFile);
        } catch (IOException cleanupFailure) {
            originalFailure.addSuppressed(cleanupFailure);
        }
    }

    public static void ensureFileExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path parentDir = path.getParent();
            if (parentDir != null) {
                Files.createDirectories(parentDir);
            }
            if (Files.notExists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create file " + filePath, e);
        }
    }
}
