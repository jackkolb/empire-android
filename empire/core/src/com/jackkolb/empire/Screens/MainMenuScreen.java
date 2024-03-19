package com.jackkolb.empire.Screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.jackkolb.empire.Empire;
import com.jackkolb.empire.EmpireGui.GuiButton;
import com.jackkolb.empire.EmpireGui.GuiImageButton;

import static java.lang.String.valueOf;

public class MainMenuScreen implements Screen {

    final Empire game;

    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    protected Stage stage;

    private TextureAtlas atlas;
    protected Skin skin;

    public MainMenuScreen(final Empire game) {
        this.game = game;

        atlas = new TextureAtlas("skins/uiskin.atlas");
        skin = new Skin(Gdx.files.internal("skins/uiskin.json"), atlas);

        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        viewport.apply();

        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();

        stage = new Stage(viewport, batch);
    }

    @Override
    public void show() {
        //Stage should controll input:
        Gdx.input.setInputProcessor(stage);

        //Create Table
        Table mainTable = new Table();
        mainTable.debug();
        //Set table to fill stage
        mainTable.setFillParent(false);
        mainTable.setPosition(Gdx.graphics.getWidth() / 2.0f, 0);//Gdx.graphics.getHeight() / 2.0f);
        //Set alignment of contents in the table.
        mainTable.align(Align.bottom);

        // get button scaling
        float button_scale = 1.5f;
        float button_height = Gdx.graphics.getHeight() * .15f * button_scale;
        float button_width = 2 * button_height;

        float button_x = (Gdx.graphics.getWidth()/2.0f - button_width/2.0f) / Gdx.graphics.getWidth();

        // Create title image
        Texture logo_texture = new Texture(Gdx.files.internal("images/empire-logo.png"));
        Image logo = new Image(logo_texture);
        logo.setWidth(button_width * 2);
        logo.setHeight(button_height);
        float logo_x = (Gdx.graphics.getWidth()/2.0f - logo.getWidth()/2.0f) / Gdx.graphics.getWidth();
        Gdx.app.error("Jack", String.valueOf(logo_x));
        logo.setPosition(logo_x * Gdx.graphics.getWidth(), .70f * Gdx.graphics.getHeight());

        //Create buttons
        GuiImageButton newButton = new GuiImageButton("images/newgame_button.png", button_x, .45f, button_width, button_height);
        newButton.pad(0, 0, 0, 0);

        GuiImageButton loadButton = new GuiImageButton("images/loadgame_button.png", button_x, .30f, button_width, button_height);
        loadButton.pad(0, 0, 0, 0);

        GuiImageButton settingsButton = new GuiImageButton("images/settings_button.png", button_x, .15f, button_width, button_height);
        settingsButton.pad(0, 0, 0, 0);

        GuiImageButton exitButton = new GuiImageButton("images/exit_button.png", button_x, .00f, button_width, button_height);
        exitButton.pad(0, 0, 0, 0);

        //Add listeners to buttons
        newButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.error("Jack", "User wants to start the game!");
                ((Game)Gdx.app.getApplicationListener()).setScreen(new EmpireGameScreen(game));
            }
        });

        exitButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        //Add table to stage
        stage.addActor(logo);
        stage.addActor(newButton);
        stage.addActor(loadButton);
        stage.addActor(settingsButton);
        stage.addActor(exitButton);

        Gdx.app.error("Jack", "Padding" + mainTable.getPadBottom() + "," + mainTable.getPadTop());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(.1f, .12f, .16f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        stage.act();
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
    }
}
