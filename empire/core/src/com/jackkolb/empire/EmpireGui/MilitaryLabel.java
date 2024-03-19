package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class MilitaryLabel extends Label{
    public int original_x;
    public int original_y;

    public MilitaryLabel(String text, Label.LabelStyle style, int x, int y) {
        super("", style);
        super.setFontScale(3.0f);
        super.setPosition(x,y);
        original_x = x;
        original_y = y;
    }
}
