package com.jackkolb.empire.EmpireGui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GuiSlider extends Slider {
    public GuiSlider(float location_x_percent, float location_y_percent) {
        super(0.0f, 1.0f, 0.01f, false, new SliderStyle());
        Sprite slider_knob_sprite = new Sprite(new Texture(Gdx.files.internal("images/slider_indicator.png"), true));
        float size = Gdx.graphics.getHeight() * .1f;
        slider_knob_sprite.setSize(size, size);
        SpriteDrawable slider_knob_drawable = new SpriteDrawable(slider_knob_sprite);

        SliderStyle slider_style = new SliderStyle(new TextureRegionDrawable(new Texture(Gdx.files.internal("images/slider_background.png"))), slider_knob_drawable);
        super.setStyle(slider_style);
        super.setPosition(Gdx.graphics.getWidth() * location_x_percent,Gdx.graphics.getHeight() * location_y_percent);
    }

    @Override
    public void setSize(float scale_x_percent, float scale_y_percent) {
        super.setSize(Gdx.graphics.getWidth() * scale_x_percent,Gdx.graphics.getHeight() * scale_y_percent);
    }
}
