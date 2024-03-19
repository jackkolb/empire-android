package com.jackkolb.empire.EmpireObjects;

import com.badlogic.gdx.utils.Array;
import com.jackkolb.empire.AI;

public class Player {
    public String name;
    public int id;

    public int money;

    public Array<Integer> provinces;

    public String color;
    public String image;

    public AI player_ai;
    public int[] ai_target_priorities;
}
