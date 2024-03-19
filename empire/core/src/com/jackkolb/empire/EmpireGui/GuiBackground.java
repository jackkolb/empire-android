package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;

import static com.jackkolb.empire.Screens.EmpireGameScreen.GUI_BACKGROUND_HEIGHT;

public class GuiBackground extends Actor {
    private Texture background_texture = new Texture(Gdx.files.internal("textures/black_marble_gui_background.jpg"));

    public GuiBackground() {
        setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void draw(Batch batch, float alpha){
        batch.draw(background_texture,0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT);
    }
}

