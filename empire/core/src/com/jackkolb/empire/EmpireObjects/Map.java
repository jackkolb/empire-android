package com.jackkolb.empire.EmpireObjects;

import com.badlogic.gdx.Gdx;

import java.util.Arrays;

public class Map {
    public String name;
    public String author;

    public int province_count ;

    public Province[] provinces;

    public void nextTurn() {

    }

    public Map() {
        name = "";
        author = "";
        province_count = 0;
    }

    public Map(Map other) {
        name = other.name;
        author = other.author;
        province_count = other.province_count;

        provinces = new Province[other.provinces.length];

        for (int i = 0; i < other.provinces.length; i++) {
            provinces[i] = new Province(other.provinces[i]);
        }

    }

    // converts vertices from originating in bottom left to top left AND removes decimal
    public double[][] verticeConversion(Province province, double map_width, double map_height) {
        double[][] vertices = province.vertices;
        double[][] result = new double[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            result[i][0] = (vertices[i][0] * map_width);
            result[i][1] = map_height - (vertices[i][1] * map_height);
        }
        return result;
    }
}
