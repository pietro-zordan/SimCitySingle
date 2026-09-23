package model;

public enum Achievement {

    FIRST_NUCLEAR_PLANT(
            "First Steps into the Atomic Age",
            "Place your first Nuclear Power Plant"
    );

    private final String title;
    private final String description;

    Achievement(String title, String description)
    {
        this.title = title;
        this.description = description;
    }

    public String getTitle()
    {
        return title;
    }

    public String getDescription()
    {
        return description;
    }
}
