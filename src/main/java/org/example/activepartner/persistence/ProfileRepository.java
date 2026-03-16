package org.example.activepartner.persistence;

import org.example.activepartner.domain.AutomationProfile;

import java.util.List;

public interface ProfileRepository {
    List<String> listProfiles();

    AutomationProfile load(String name);

    void save(AutomationProfile profile);

    void delete(String name);

    String loadLastUsed();

    void saveLastUsed(String name);
}
