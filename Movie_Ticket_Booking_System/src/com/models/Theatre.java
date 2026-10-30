package model;

import java.util.List;

public class Theatre {
    private int id;
    private String name;
    private String city;
    private List<Screen> screens;

    public Theatre(int id, String name, String city, List<Screen> screens) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.screens = screens;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCity() { return city; }
    public List<Screen> getScreens() { return screens; }
}