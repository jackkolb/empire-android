package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GuiImageButton extends ImageButton {
    public GuiImageButton(String image_up_path) {
        super(new TextureRegionDrawable(new TextureRegion( new Texture(Gdx.files.internal(image_up_path), true))));
    }

    public GuiImageButton(String image_up_path, float position_x_percent, float position_y_percent, float width, float height) {
        super(new TextureRegionDrawable(new TextureRegion( new Texture(Gdx.files.internal(image_up_path), true))));
        super.setPosition(Gdx.graphics.getWidth() * position_x_percent,Gdx.graphics.getHeight() * position_y_percent);
        super.setSize(width, height);
    }

    public GuiImageButton(String image_up_path, String image_down_path, float position_x_percent, float position_y_percent, float width, float height) {
        super(new TextureRegionDrawable(new TextureRegion( new Texture(Gdx.files.internal(image_up_path), true))), new TextureRegionDrawable(new TextureRegion( new Texture(Gdx.files.internal(image_down_path)))));
        super.setPosition(Gdx.graphics.getWidth() * position_x_percent,Gdx.graphics.getHeight() * position_y_percent);
        super.setSize(width, height);
    }
}
