package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

public class GuiButton extends TextButton {
    public GuiButton(String text, float x_percent, float y_percent) {
        super(text, new Skin(Gdx.files.internal("skins/uiskin.json")));

        super.setSize(200.0f, 75.0f);
        super.setPosition(Gdx.graphics.getWidth() * x_percent,Gdx.graphics.getHeight() * y_percent);
    }

    public GuiButton(String text, float x_percent, float y_percent, float width_percent, float height_percent) {
        super(text, new Skin(Gdx.files.internal("skins/uiskin.json")));

        super.setSize(Gdx.graphics.getWidth() * width_percent, Gdx.graphics.getHeight() * height_percent);
        super.setPosition(Gdx.graphics.getWidth() * x_percent,Gdx.graphics.getHeight() * y_percent);
    }

}
