package model;

public enum Achievement {

    FIRST_NUCLEAR_PLANT(
            "First Steps into the Atomic Age",
            "Place your first Nuclear Power Plant"
    ),

    FIRST_1000_INHABITANTS("A small group of friends",
    " Reach a population of one thousand "),

    A_LOT_OF_MONEY("Loaded", "Reach a budget of 50000"),

    WHY_WOULD_YOU_DO_THAT("Why would you do that",
            "Try to destroy a nuclear plant");


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
