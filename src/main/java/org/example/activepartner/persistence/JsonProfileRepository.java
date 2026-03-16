package org.example.activepartner.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.activepartner.domain.AutomationProfile;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JsonProfileRepository implements ProfileRepository {
    private static final String ROOT_FOLDER = ".active-partner";
    private static final String PROFILES_FOLDER = "profiles";
    private static final String LAST_USED_FILE = "last-profile.txt";
    private static final String EXTENSION = ".json";

    private final Gson gson;
    private final Path rootPath;
    private final Path profilesPath;
    private final Path lastUsedPath;

    public JsonProfileRepository() {
        this(Paths.get(System.getProperty("user.home"), ROOT_FOLDER));
    }

    JsonProfileRepository(Path rootPath) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.rootPath = rootPath;
        this.profilesPath = rootPath.resolve(PROFILES_FOLDER);
        this.lastUsedPath = rootPath.resolve(LAST_USED_FILE);
        ensureDirectories();
    }

    @Override
    public List<String> listProfiles() {
        ensureDirectories();
        List<String> names = new ArrayList<String>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(profilesPath, "*" + EXTENSION)) {
            for (Path path : stream) {
                String filename = path.getFileName().toString();
                names.add(filename.substring(0, filename.length() - EXTENSION.length()));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to list profiles", exception);
        }
        Collections.sort(names);
        return names;
    }

    @Override
    public AutomationProfile load(String name) {
        Path profilePath = profilePath(name);
        if (!Files.exists(profilePath)) {
            throw new IllegalArgumentException("Profile not found: " + name);
        }

        try (Reader reader = Files.newBufferedReader(profilePath, StandardCharsets.UTF_8)) {
            AutomationProfile profile = gson.fromJson(reader, AutomationProfile.class);
            if (profile == null) {
                throw new IllegalStateException("Profile file is empty or invalid: " + name);
            }
            if (profile.getName() == null || profile.getName().trim().isEmpty()) {
                profile.setName(name);
            }
            return profile;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load profile: " + name, exception);
        }
    }

    @Override
    public void save(AutomationProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException("Profile must not be null");
        }

        ensureDirectories();
        Path profilePath = profilePath(profile.getName());
        try (Writer writer = Files.newBufferedWriter(profilePath, StandardCharsets.UTF_8)) {
            gson.toJson(profile, writer);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save profile: " + profile.getName(), exception);
        }
    }

    @Override
    public void delete(String name) {
        Path profilePath = profilePath(name);
        try {
            Files.deleteIfExists(profilePath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to delete profile: " + name, exception);
        }
    }

    @Override
    public String loadLastUsed() {
        if (!Files.exists(lastUsedPath)) {
            return null;
        }

        try {
            String content = new String(Files.readAllBytes(lastUsedPath), StandardCharsets.UTF_8).trim();
            return content.isEmpty() ? null : content;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read last used profile", exception);
        }
    }

    @Override
    public void saveLastUsed(String name) {
        ensureDirectories();
        try {
            Files.write(lastUsedPath, name.getBytes(StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save last used profile", exception);
        }
    }

    private Path profilePath(String name) {
        String safeName = sanitizeName(name);
        return profilesPath.resolve(safeName + EXTENSION);
    }

    private String sanitizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name must not be blank");
        }
        return name.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private void ensureDirectories() {
        try {
            Files.createDirectories(profilesPath);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create profile directories", exception);
        }
    }
}
