package org.example.activepartner.domain;

import java.util.ArrayList;
import java.util.List;

public class AutomationProfile {
    private String name;
    private List<ActionPoint> points;
    private RunConfig config;

    public AutomationProfile() {
        this("Default", new ArrayList<ActionPoint>(), new RunConfig());
    }

    public AutomationProfile(String name, List<ActionPoint> points, RunConfig config) {
        setName(name);
        setPoints(points);
        setConfig(config);
    }

    public static AutomationProfile defaultProfile() {
        return new AutomationProfile();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name must not be blank");
        }
        this.name = name.trim();
    }

    public List<ActionPoint> getPoints() {
        return points;
    }

    public void setPoints(List<ActionPoint> points) {
        this.points = points == null ? new ArrayList<ActionPoint>() : new ArrayList<ActionPoint>(points);
    }

    public RunConfig getConfig() {
        return config;
    }

    public void setConfig(RunConfig config) {
        this.config = config == null ? new RunConfig() : config;
    }
}
