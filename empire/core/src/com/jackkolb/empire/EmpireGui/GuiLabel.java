package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class GuiLabel extends Label {
    public GuiLabel(Label.LabelStyle style, float x_percent, float y_percent) {
        super("", style);
        super.setFontScale(3.0f);
        super.setPosition(Gdx.graphics.getWidth() * x_percent,Gdx.graphics.getHeight() * y_percent);
    }

}
