package com.jackkolb.empire.EmpireObjects;


import com.badlogic.gdx.graphics.Texture;

public class Province {
    public int id;  // province id
    public String name;

    public int xloc;  // x location on the map
    public int yloc;  // y location on the map

    public Texture texture;

    public int population;  // population of this province
    public int economy;  // economy value of this province
    public int military;

    public double[][] vertices;
    public int[] connections;  // provinces this province connects to

    Province() {
        id = 0;
        xloc = 0;
        yloc = 0;
        population = 0;
        economy = 0;
        military = 0;
    }

    public Province(Province other) {
        // just assign all the subvalues
        id = other.id;
        name = other.name;
        xloc = other.xloc;
        yloc = other.yloc;
        texture = other.texture;
        population = other.population;
        economy = other.economy;
        military = other.military;
        vertices = other.vertices;
        connections = other.connections;
    }
}
