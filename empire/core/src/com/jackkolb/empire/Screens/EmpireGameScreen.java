package com.jackkolb.empire.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.jackkolb.empire.Empire;
import com.jackkolb.empire.EmpireGame;
import com.jackkolb.empire.EmpireGui.GuiBackground;
import com.jackkolb.empire.EmpireGui.GuiImageButton;
import com.jackkolb.empire.EmpireGui.GuiLabel;
import com.jackkolb.empire.EmpireGui.GuiSlider;
import com.jackkolb.empire.EmpireGui.MilitaryLabel;
import com.jackkolb.empire.EmpireObjects.Map;
import com.jackkolb.empire.Helper.GuiHelper;
import com.jackkolb.empire.Utilities;

import java.util.HashMap;

public class EmpireGameScreen implements Screen, GestureDetector.GestureListener {
    final Empire game;

    static private int WORLD_WIDTH = 1000; // set to 1000 by default, will change in create()
    static private int WORLD_HEIGHT = (int) (WORLD_WIDTH * 0.75f);  // scale the world height to .75 the world width (this aspect ratio should be maintained for maps!)

    private final float INITIAL_ZOOM = .5f;  // the initial "zoom" (not actually camera zoom, this is simulated)
    private final float MAX_ZOOM = 1.0f;  // the maximum zoom
    private final float MIN_ZOOM = .25f;  // the minimum zoom
    private float currentZoom = INITIAL_ZOOM;  // set the current zoom to the initial zoom

    protected AssetManager manager = new AssetManager();  // asset manager for textures

    protected static OrthographicCamera camera;  // the camera, the camera stays fixed
    private SpriteBatch mapBatch;  // the sprite batch for the map underlay
    private InputMultiplexer inputMultiplexer;  // allows for multiple inputs (GUI and Gestures) so we can have mobile use
    private GestureDetector gestureDetector;  // a gesture detector (tap, pinch, etc)
    private final EarClippingTriangulator trisolver = new EarClippingTriangulator();  // used to solve for e
    private final Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    private Texture textureSolid;
    private TextureRegion textureRegion;

    public static final float GUI_BACKGROUND_HEIGHT = .25f;

    // zoom variables
    private float initial_zoom_gesture = currentZoom;

    protected static Sprite mapSprite;
    private float rotationSpeed;

    private GuiHelper gui_helper;

    protected static java.util.Map<String, PolygonSpriteBatch> batches = new HashMap<String, PolygonSpriteBatch>();

    protected static Texture attack_arrow;
    protected static Stage gui;
    private Stage military_stage;
    private MilitaryLabel[] military_labels;


    protected static Utilities util;
    protected static Map map_data;
    protected static Map map_data_original;  // for use when player does their turn
    protected static EmpireGame game_data;

    protected static double[][][] provinces_vertices_copy;

    protected static int map_xloc, map_yloc;
    private int drag_xold, drag_yold;

    protected static int player_id = 0;

    protected static String player_state;

    protected static int attack_origin_province;
    protected static int province_selected;

    protected static Array<Integer> flash_provinces;
    private static long flash_point_timestamp;

    protected static Label.LabelStyle gui_font_style;
    protected static GuiBackground gui_background;
    protected static GuiLabel province_name_label;
    protected static GuiLabel province_owner_label;
    protected static GuiLabel province_population_label;
    protected static GuiLabel province_economy_label;

    private String information_text;
    protected static GuiLabel information_label;

    protected static GuiLabel player_name_label;
    protected static GuiLabel player_money_label;
    protected static GuiLabel game_year_label;

    protected static GuiSlider general_slider;
    protected static GuiLabel general_slider_label;

    protected static GuiImageButton general_confirm_button;
    protected static GuiImageButton general_cancel_button;

    protected static GuiImageButton next_turn_button;
    protected static GuiImageButton attack_button;
    protected static GuiImageButton recruit_button;
    protected static GuiImageButton dismiss_button;

    protected static GuiImageButton endgame_button;
    protected static GuiLabel endgame_message;

    protected static GuiImageButton menu_button;  // currently, this just exits the game
    protected static GuiImageButton actions_button;  // button to display the actions that turn

    public EmpireGameScreen() {
        this.game = new Empire();
    }

    public EmpireGameScreen(final Empire game) {
        this.game = game;

        // init stages
        gui = new Stage();
        military_stage = new Stage();

        // init gesture/input detectors
        inputMultiplexer = new InputMultiplexer();
        gestureDetector = new GestureDetector(this);
        inputMultiplexer.addProcessor(gestureDetector);
        inputMultiplexer.addProcessor(gui);
        Gdx.input.setInputProcessor(inputMultiplexer);

        // set the world width/height
        WORLD_WIDTH = Gdx.graphics.getWidth();
        WORLD_HEIGHT = (int) (WORLD_WIDTH * .75);

        // zoom
        initial_zoom_gesture = INITIAL_ZOOM;

        // load map
        String map_name = "europe_modern";
        mapSprite = new Sprite(new Texture(Gdx.files.internal("maps/" + map_name + "/" + map_name + ".png")));
        mapBatch = new SpriteBatch();

        // Constructs a new OrthographicCamera
        camera = new OrthographicCamera(1, (Gdx.graphics.getHeight() / (float) Gdx.graphics.getWidth()));
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(camera.viewportWidth / 2f, camera.viewportHeight / 2f, 0);
        camera.zoom = INITIAL_ZOOM;
        camera.update();

        // create sprite batches
        batches.put("Provinces", new PolygonSpriteBatch());
        batches.put("Military", new PolygonSpriteBatch());
        batches.put("Military Actions", new PolygonSpriteBatch());

        // init utilities
        util = new Utilities();

        // init gui helper
        gui_helper = new GuiHelper();
        gui_helper.initializeGeneralGui();
        gui_helper.initializeGeneralGuiInteractions();
        gui_helper.initializeSliderGui();
        gui_helper.initializeSliderInteractions();
        gui_helper.setDefaultGuiState();

        // init states
        player_state = "idle";
        flash_provinces = new Array<Integer>();

        // init graphics
        attack_arrow = new Texture("images/attack_arrow.png");

        // init map and game data
        map_data = util.getMapData(map_name);
        map_data_original = map_data; // make this copy to prep for the first turn
        util.setProvinceTextures();
        game_data = util.getGameData(map_name);
        game_data.actions = new HashMap<Integer, Array<Array<Integer>>>();

        // Creating the color filling (but textures would work the same way)
        pix.setColor(0xDEADBEFF); // DE is red, AD is green and BE is blue.
        pix.fill();
        textureSolid = new Texture(pix);
        textureRegion = new TextureRegion(textureSolid);

        provinces_vertices_copy = new double[map_data.province_count][][];

        // init military labels
        military_labels = new MilitaryLabel[map_data.provinces.length];
        for (int i = 0; i < map_data.provinces.length; i++) {
            int[] label_position = util.getProvinceCenter(util.verticesDoubleInt(map_data.verticeConversion(map_data.provinces[i], WORLD_WIDTH, WORLD_HEIGHT)));
            military_labels[i] = new MilitaryLabel(Integer.toString(map_data.provinces[i].military), gui_font_style, label_position[0], label_position[1]);
            military_stage.addActor(military_labels[i]);
        }

        // reset all actions
        util.resetActions();
    }

    @Override
    public void render(float delta) {
        // runs every frame
        camera.update();
        manager.update();

        Gdx.gl.glClearColor(1, 1, 1, 1); // setup background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // map overlay
        mapBatch.begin();
        mapSprite.setSize(WORLD_WIDTH / currentZoom, WORLD_HEIGHT / currentZoom);
        mapSprite.setPosition(map_xloc, map_yloc);
        mapSprite.draw(mapBatch); // due to textures, need to do sprite.draw
        mapBatch.end();

        // set up the province vertice conversion
        // province sprites
        batches.get("Provinces").begin();

        for (int province_id = 0; province_id < map_data.province_count; province_id++) {
            provinces_vertices_copy[province_id] = map_data.verticeConversion(map_data.provinces[province_id], WORLD_WIDTH / currentZoom, WORLD_HEIGHT / currentZoom);

            int owner_id = util.getProvinceOwnerId(province_id);
            batches.get("Provinces").setColor(1, 1, 1, 0);
            if (owner_id != -1) {
                batches.get("Provinces").setColor(Color.valueOf(game_data.players[owner_id].color));
                batches.get("Provinces").draw(polygen(util.verticesDoubleInt(provinces_vertices_copy[province_id])), map_xloc, map_yloc);
            }
        }
        // flashing provinces
        for (int province_id : flash_provinces) {
            if (province_id == -1) {
                continue;
            }
            batches.get("Provinces").setColor(new Color(1, 1, 1, (float) Math.round(((System.currentTimeMillis() - flash_point_timestamp + 750) % 1500) / 1500.0f) * 0.3f));
            batches.get("Provinces").draw(polygen(util.verticesDoubleInt(provinces_vertices_copy[province_id])), map_xloc, map_yloc);
        }

        batches.get("Provinces").end();

        // Military Action Arrows
        batches.get("Military Actions").begin();
        for (int player = 0; player < game_data.players.length; player++) {
            for (Array<Integer> action : game_data.actions.get(player)) {
                // if action is a military move
                if (action.get(0) == 1) {
                    gui_helper.drawMilitaryArrow(action.get(1), action.get(2));
                }
            }
        }
        batches.get("Military Actions").end();

        // military indicators
        military_stage.act();
        for (int i = 0; i < map_data.provinces.length; i++) {
            military_labels[i].setText(map_data.provinces[i].military);
            military_labels[i].setPosition(military_labels[i].original_x / currentZoom + map_xloc, military_labels[i].original_y / currentZoom + map_yloc);
        }
        military_stage.draw();

        // GUI
        gui.act();

        player_name_label.setText(game_data.players[player_id].name);
        player_money_label.setText("Treasury: " + game_data.players[player_id].money);
        game_year_label.setText("Year: " + game_data.year + " (action)");
        if (game_data.next_turn_visual) {
            game_year_label.setText("Year: " + game_data.year + " (review)");
        }
        if (game_data.players[player_id].money < 0) {
            information_text = "Treasury depleted, dismiss troops! ";
        }
        information_label.setText(information_text);
        gui.draw();
    }

    public void nextLevel() {
        map_data.nextTurn();
        game_data.nextTurn();
    }

    public PolygonRegion polygen(int[][] vertices_orig) {
        // convert the int[][] into int[]
        float[] vertices = new float[vertices_orig.length * 2];
        int counter = 0;
        for (int i = 0; i < vertices_orig.length; i++) {
            for (int j = 0; j < 2; j++) {
                vertices[counter] = (float) vertices_orig[i][j];
                counter++;
            }
        }

        short[] triangles = trisolver.computeTriangles(vertices).toArray();
        return new PolygonRegion(textureRegion, vertices, triangles);
    }

    public void boundsClamp() {
        // make sure left/right edges are within bounds
        if (map_xloc > 0) {
            map_xloc = 0;
        }
        if (map_xloc < Gdx.graphics.getWidth() - WORLD_WIDTH / currentZoom + 1) {
            map_xloc = (int) (Gdx.graphics.getWidth() - WORLD_WIDTH / currentZoom);
        }

        // make sure top/down edges are with bounds
        if (map_yloc > Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT) {
            map_yloc = (int) (Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT);
        }
        if (map_yloc < Gdx.graphics.getHeight() - WORLD_HEIGHT / currentZoom) {
            map_yloc = (int) (Gdx.graphics.getHeight() - WORLD_HEIGHT / currentZoom);
        }
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean tap(float x, float y, int count, int button) {
        int[] point = new int[] {(int) x, (int) (Gdx.graphics.getHeight() - y)};
        // check if tapped a province
        if (point[1] > Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT) {
            // if tapped a province, mark it as selected
            province_selected = util.checkProvinceClick(point);

            // if the user is not idle but pressed no province (-1), set to idle
            if (province_selected == -1) {
                player_state = "idle";
                flash_provinces.clear();
                gui_helper.hideSliderInterface();
                gui_helper.setDefaultGuiState();
                return false;
            }

            // if user is in idle state, add the province to be flashed
            if (player_state.equals("idle") && province_selected != -1) {
                flash_provinces.clear();
                flash_provinces.add(province_selected);
                gui_helper.hideSliderInterface();  // hide any sliders if applicable
                gui_helper.showSelectedProvinceGuiInformation();  // show the selected province info
                return false;
            }

            // if the user is attacking a target, select this province to attack
            if (player_state.equals("attack target") && province_selected != -1) {
                flash_provinces.clear();
                flash_provinces.add(attack_origin_province);
                flash_provinces.add(province_selected);
                player_state = "attack quantity";
                gui_helper.showSliderInterface();  // show the sliders
                gui_helper.showSelectedProvinceGuiInformation();  // show the province GUI (has logic to only show action buttons when needed)
            }


        }
        return false;
    }

    @Override
    public boolean pan(float x0, float y0, float deltaX, float deltaY) {
        if (y0 < Gdx.graphics.getHeight() * (1 - GUI_BACKGROUND_HEIGHT)) {
            if (map_xloc + deltaX <= 0 && map_xloc + deltaX >= Gdx.graphics.getWidth() - WORLD_WIDTH / currentZoom) {  // make sure you can't pan too far left/right
                map_xloc += deltaX;
            }
            if (map_yloc - deltaY <= Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT && map_yloc - deltaY >= Gdx.graphics.getHeight() - WORLD_HEIGHT / currentZoom) {
                map_yloc -= deltaY;
            }
        }

        boundsClamp();
        return false;
    }

    @Override
    public boolean zoom(float initialDistance, float distance) {
        float zoomAmount = distance / initialDistance;
        double new_camera_zoom = initial_zoom_gesture / zoomAmount;

        if (new_camera_zoom > MIN_ZOOM && new_camera_zoom < MAX_ZOOM) {
            float projX = (Gdx.graphics.getWidth()/2.0f - map_xloc) * currentZoom / (2*INITIAL_ZOOM);
            float projY = (Gdx.graphics.getHeight()/2.0f - map_yloc) * currentZoom / (2*INITIAL_ZOOM);
            double deltaX = projX / new_camera_zoom - projX / currentZoom;
            double deltaY = projY / new_camera_zoom - projY / currentZoom;

            // prevent from zooming out on the right edge
            if (map_xloc - deltaX <= Gdx.graphics.getWidth() - WORLD_WIDTH / currentZoom) {
                float fixed_right_projX = (Gdx.graphics.getWidth() - map_xloc) * currentZoom / (2*INITIAL_ZOOM);
                map_xloc -= fixed_right_projX / new_camera_zoom - fixed_right_projX / currentZoom;
            }
            // prevent from zooming out on the left edge
            else if (map_xloc - deltaX < 0) {
                map_xloc -= deltaX;
            }

            // prevent from zooming out on the top edge
            if (map_yloc - deltaY <= Gdx.graphics.getHeight() - WORLD_HEIGHT / currentZoom) {
                float fixed_right_projY = (Gdx.graphics.getHeight() - map_yloc) * currentZoom / (2*INITIAL_ZOOM);
                map_yloc -= fixed_right_projY / new_camera_zoom - fixed_right_projY / currentZoom;
            }

            // prevent from zooming out on the bottom edge
            else if (map_yloc - deltaY < Gdx.graphics.getHeight() * GUI_BACKGROUND_HEIGHT) {
                map_yloc -= deltaY;
            }

            currentZoom = (float) new_camera_zoom;
        }

        boundsClamp();

        return false;
    }

    @Override
    public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
        return false;
    }

    @Override
    public void pinchStop() {
        initial_zoom_gesture = currentZoom;
    }

    @Override
    public boolean panStop(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public boolean longPress(float x, float y) {
        return false;
    }

    @Override
    public boolean fling(float velocityX, float velocityY, int button) {
        return false;
    }

    @Override
    public boolean touchDown(float x, float y, int pointer, int button) {
        return false;
    }

    @Override
    public void show() {
        // start music here
    }

    @Override
    public void resize(int width, int height) {
        //viewport.update(width, height);
        //camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        //camera.update();
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
}
