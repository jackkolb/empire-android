package com.jackkolb.empire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.jackkolb.empire.EmpireObjects.Map;
import com.jackkolb.empire.Screens.EmpireGameScreen;
import com.sun.org.apache.xml.internal.utils.StringToStringTable;

import java.util.Arrays;


public class Utilities extends EmpireGameScreen {

    public EmpireGame getGameData (String map_name) {
        Json json = new Json();
        EmpireGame gamedata;
        FileHandle handle = Gdx.files.internal("maps/" + map_name + "/" + map_name + ".data");
        gamedata = json.fromJson(EmpireGame.class, handle.readString());

        for (int empire_id = 0; empire_id < gamedata.players.length; empire_id++) {
            gamedata.players[empire_id].player_ai = new AI();
        }
        return gamedata;
    }

    public Map getMapData (String map_name) {
        Json json = new Json();
        Map provinces;

        FileHandle handle = Gdx.files.internal("maps/" + map_name + "/" + map_name + ".map");
        provinces = json.fromJson(Map.class, handle.readString());
        return provinces;
    }

    public void setProvinceTextures () {
        for (int province_id = 0; province_id < map_data.province_count; province_id++) {
            try {
                map_data.provinces[province_id].texture = new Texture("maps/" + map_data.name + "/province_images/province_" + (province_id+1) + ".png");
            }
            catch (Exception e) {
                System.out.print("ERROR::" + e.toString());
            }
        }
    }

    // converts the double[][] to int[][]
    public int[][] verticesDoubleInt(double[][] vertices) {
        int[][] result = new int[vertices.length][2];
        for (int i = 0; i < vertices.length; i++) {
            result[i][0] = (int) (vertices[i][0]);
            result[i][1] = (int) (vertices[i][1]);
        }
        return result;
    }

    public boolean polygonTest(int[] test, int[][] points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.length - 1; i < points.length; j = i++) {
            if ((points[i][1] > test[1]) != (points[j][1] > test[1]) &&
                    (test[0] < (points[j][0] - points[i][0]) * (test[1] - points[i][1]) / (points[j][1]-points[i][1]) + points[i][0])) {
                result = !result;
            }
        }
        return result;
    }

    public int checkProvinceClick(int[] point) {
        int[][][] visualLocations = getVisualProvinceLocations();
        for (int province_id = 0; province_id < map_data.province_count; province_id++) {
            if (polygonTest(point, visualLocations[province_id])) {
                return province_id;
            }
        }
        return -1;
    }

    public void resetActions() {
        game_data.actions.clear();
        for (int i = 0; i < game_data.players.length; i++) {
            game_data.actions.put(i, new Array<Array<Integer>>());
        }
    }

    public int getProvinceOwnerId(int province_id) {
        for (int player_id = 0; player_id < game_data.players.length; player_id++) {
            for (int i=0; i < game_data.players[player_id].provinces.size; i++) {
                int province_checking = game_data.players[player_id].provinces.get(i);
                if (province_checking == province_id) {
                    return player_id;
                }
            }
        }
        return -1;
    }

    public int[] getProvinceCenter(int[][] points) {
        double x = 0.0f;
        double y = 0.0f;
        int pointCount = points.length;


        int max_x = points[0][0]; int min_x = max_x;
        int max_y = points[0][1]; int min_y = max_y;

        for (int i = 0; i < pointCount - 1; i++){
            int[] point = points[i];
            if (point[0] > max_x) {
                max_x = point[0];
            }
            if (point[0] < min_x) {
                min_x = point[0];
            }
            if (point[1] > max_y) {
                max_y = point[1];
            }
            if (point[1] < min_y) {
                min_y = point[1];
            }
        }

        x = (max_x + min_x) / 2.0;
        y = (max_y + min_y) / 2.0;

        return new int[]{(int) x, (int) y};
    }

    public int[][][] getVisualProvinceLocations() {
        int[][][] results = new int[map_data.province_count][][];
        for (int i=0; i< map_data.province_count; i++) {
            results[i] = new int[provinces_vertices_copy[i].length][2];
            for (int j=0; j<provinces_vertices_copy[i].length; j++) {
                results[i][j][0] = (int) provinces_vertices_copy[i][j][0] + map_xloc;
                results[i][j][1] = (int) provinces_vertices_copy[i][j][1] + map_yloc;
            }
        }
        return results;
    }

    public double euclidDistance(Vector2 p1, Vector2 p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

}

