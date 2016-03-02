package it.jira.project;

public class ProjectForTests {
    private final String id;

    private final String key;

    public ProjectForTests(String key, String id) {
        this.key = key;
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }
}
