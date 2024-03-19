package com.jackkolb.empire.Helper;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.jackkolb.empire.Empire;
import com.jackkolb.empire.EmpireGui.GuiBackground;
import com.jackkolb.empire.EmpireGui.GuiImageButton;
import com.jackkolb.empire.EmpireGui.GuiLabel;
import com.jackkolb.empire.EmpireGui.GuiSlider;
import com.jackkolb.empire.Screens.EmpireGameScreen;


public class GuiHelper extends EmpireGameScreen {

    public void initializeGeneralGui() {
        gui_background = new GuiBackground();

        gui_font_style = new Label.LabelStyle();
        gui_font_style.font = new BitmapFont();
        gui_font_style.fontColor = Color.GOLD;

        //province_id_label = new GuiLabel(gui_font_style, .03f, .30f);
        province_name_label = new GuiLabel(gui_font_style, .03f, .25f);
        province_owner_label = new GuiLabel(gui_font_style, .03f, .20f);
        province_population_label = new GuiLabel(gui_font_style, .03f, .15f);
        province_economy_label = new GuiLabel(gui_font_style, .03f, .10f);

        player_name_label = new GuiLabel(gui_font_style, .70f, .20f);
        game_year_label = new GuiLabel(gui_font_style, .70f, .15f);
        player_money_label = new GuiLabel(gui_font_style, .70f, .10f);
        information_label = new GuiLabel(gui_font_style, .15f, .05f);

        next_turn_button = new GuiImageButton("images/next_turn_button.png", .925f, .05f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);
        attack_button = new GuiImageButton("images/attack_button.png", 0.30f, 0.10f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);
        recruit_button = new GuiImageButton("images/recruit_button.png", 0.40f, 0.10f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);
        dismiss_button = new GuiImageButton("images/dismiss_button.png", 0.50f, 0.10f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);

        endgame_button = new GuiImageButton("textures/black_marble_gui_background.jpg",0.25f, 0.25f, Gdx.graphics.getWidth() * 0.5f, Gdx.graphics.getHeight() * 0.6f);
        endgame_button.setVisible(false);  // set to invisible until the endgame
        endgame_message = new GuiLabel(gui_font_style, 0.4f, 0.5f);
        endgame_message.setVisible(false);  // set to invisible until the endgame

        menu_button = new GuiImageButton("images/menu_button.png",0.02f, 0.875f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);

        gui.addActor(gui_background);

        gui.addActor(province_name_label);
        gui.addActor(province_owner_label);
        gui.addActor(province_population_label);
        gui.addActor(province_economy_label);

        gui.addActor(player_name_label);
        gui.addActor(player_money_label);
        gui.addActor(game_year_label);
        gui.addActor(information_label);

        gui.addActor(next_turn_button);
        gui.addActor(attack_button);
        gui.addActor(recruit_button);
        gui.addActor(dismiss_button);

        gui.addActor(endgame_button);
        gui.addActor(endgame_message);

        gui.addActor(menu_button);
    }

    public void initializeGeneralGuiInteractions() {
        next_turn_button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent click, float x, float y) {
                Gdx.app.error("Jack", "Next level!");
                nextLevel();
            }
        });

        attack_button.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent click, float x, float y) {
                // if the treasury is empty/negative, soldiers will not fight
                if (game_data.players[player_id].money < 0) {
                    return;
                }

                player_state = "attack target";
                Gdx.app.error("Jack", "Select Target!");

                // otherwise, flash the provinces targetable
                attack_origin_province = province_selected;
                for (int province_id: map_data.provinces[province_selected].connections) {
                    flash_provinces.add(province_id);
                }
                flash_provinces.removeValue(province_selected, true);
            }
        });

        recruit_button.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent click, float x, float y) {
                // if the treasury is empty/negative, cannot recruit new soldiers
                if (game_data.players[player_id].money < 0) {
                    return;
                }

                player_state = "recruit";
                Gdx.app.error("Jack", "Recruiting soldiers!");
                showSliderInterface();
                general_slider_label.setText("Recruiting Soldiers!");
            }
        });

        dismiss_button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent click, float x, float y) {
                player_state = "dismiss";
                showSliderInterface();
                general_slider_label.setText("Dismissing Soldiers!");
            }
        });

        endgame_button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent click, float x, float y) {
                Gdx.app.error("Jack", "Exiting via endgame button!");
                Gdx.app.exit();
            }
        });

        menu_button.addListener(new ClickListener() {
            @Override
            public void clicked (InputEvent click, float x, float y) {
                Gdx.app.error("Jack", "Exiting!");
                Gdx.app.exit();
            }
        });

    }

    public void initializeSliderGui() {
        general_slider = new GuiSlider(0.3f, 0.10f);
        general_slider.setSize(0.4f, 0.05f);
        general_slider_label = new GuiLabel(gui_font_style, 0.40f, 0.370f);

        general_confirm_button = new GuiImageButton("images/confirm_button.png", 0.925f, 0.17f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);
        general_cancel_button = new GuiImageButton("images/cancel_button.png", 0.925f, 0.00f, Gdx.graphics.getHeight() * 0.15f, Gdx.graphics.getHeight() * 0.15f);

        gui.addActor(general_slider);
        gui.addActor(general_slider_label);

        gui.addActor(general_confirm_button);
        gui.addActor(general_cancel_button);
    }

    public void initializeSliderInteractions() {
        general_confirm_button.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent click, float x, float y) {
                hideSliderInterface();
                if (player_state.matches("recruit")) {
                    game_data.playerRecruitMilitary(general_slider.getValue());
                }

                if (player_state.matches("attack quantity")) {
                    game_data.playerAttack(general_slider.getValue());
                }

                if (player_state.matches("dismiss")) {
                    game_data.playerDismissMilitary(general_slider.getValue());
                }
                player_state = "idle";
                setDefaultGuiState();
                flash_provinces.clear();
            }
        });

        general_cancel_button.addListener(new ClickListener(){
            @Override
            public void clicked (InputEvent click, float x, float y) {
                player_state = "idle";
                setDefaultGuiState();
                flash_provinces.clear();
            }
        });

        general_slider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player_state.matches("recruit")) {
                    general_slider_label.setText("Recruiting " + (int) (general_slider.getValue() * game_data.getRecruitMax()) + " Soldiers!");
                }

                if (player_state.matches("attack quantity")) {
                    general_slider_label.setText("Sending " + (int) (general_slider.getValue() * map_data.provinces[attack_origin_province].military) + " Soldiers!");
                }

                if (player_state.matches("dismiss")) {
                    general_slider_label.setText("Dismissing " + (int) (general_slider.getValue() * map_data.provinces[province_selected].military) + " Soldiers!");
                }
            }
        });
    }

    public void setDefaultGuiState() {
        province_name_label.setVisible(false);
        province_owner_label.setVisible(false);
        province_population_label.setVisible(false);
        province_economy_label.setVisible(false);

        recruit_button.setVisible(false);
        attack_button.setVisible(false);
        dismiss_button.setVisible(false);

        general_slider.setVisible(false);
        general_slider_label.setVisible(false);
        general_confirm_button.setVisible(false);
        general_cancel_button.setVisible(false);

        next_turn_button.setVisible(true);
        game_year_label.setVisible(true);

        menu_button.setVisible(true);
    }

    public void showSliderInterface() {
        next_turn_button.setVisible(false);
        game_year_label.setVisible(false);

        general_slider.setVisible(true);
        general_slider.setValue(0.0f);
        general_slider_label.setVisible(true);

        general_confirm_button.setVisible(true);
        general_cancel_button.setVisible(true);

        attack_button.setVisible(false);
        recruit_button.setVisible(false);
        dismiss_button.setVisible(false);
        Gdx.app.error("GUI", "in showSliderInterface()");
    }

    public void hideSliderInterface() {
        next_turn_button.setVisible(true);
        game_year_label.setVisible(true);

        general_slider.setVisible(false);
        general_slider_label.setVisible(false);

        general_confirm_button.setVisible(false);
        general_cancel_button.setVisible(false);

        attack_button.setVisible(true);
        recruit_button.setVisible(true);
        dismiss_button.setVisible(true);
    }

    public void showSelectedProvinceGuiInformation() {
        // set the GUI to the selected province state
        Gdx.app.error("Jack", "Showing selected province information " + String.valueOf(province_selected));

        if (province_selected == -1) {
            player_state = "idle";
            // set the GUI to the default (non select) state
            setDefaultGuiState();
            return;
        }

        province_name_label.setText("\"" + map_data.provinces[province_selected].name + "\"");

        // if the province is controlled by a player, state so, otherwise state it is wilderness
        if (util.getProvinceOwnerId(province_selected) != -1) {
            province_owner_label.setText("Controlled by " + game_data.players[util.getProvinceOwnerId(province_selected)].name);
        }
        else {
            province_owner_label.setText("Untamed wilderness");
        }

        province_population_label.setText("Population: " + map_data.provinces[province_selected].population);
        province_economy_label.setText("Economy: " + map_data.provinces[province_selected].economy);

        province_name_label.setVisible(true);
        province_owner_label.setVisible(true);
        province_population_label.setVisible(true);
        province_economy_label.setVisible(true);

        // if the user selects their own province, it is not the visual turn, and the player is not choosing an attack target province, show the action buttons
        if (util.getProvinceOwnerId(province_selected) == player_id && !game_data.next_turn_visual && !player_state.equals("attack quantity")) {
            // if the user is broke, don't show the recruit and attack buttons
            if (game_data.players[player_id].money < 0) {
                recruit_button.setVisible(false);
                attack_button.setVisible(false);
            }
            else {
                recruit_button.setVisible(true);
                attack_button.setVisible(true);
            }

            // show the dismiss soldiers button if the province has soldiers
            if (map_data.provinces[province_selected].military > 0) {
                dismiss_button.setVisible(true);
            }
            else {
                dismiss_button.setVisible(false);
            }
        }
        else {
            recruit_button.setVisible(false);
            attack_button.setVisible(false);
            dismiss_button.setVisible(false);
        }


    }

    public void drawMilitaryArrow(int origin_province, int target_province) {
        int[][][] visual_province_locations = util.getVisualProvinceLocations();
        int[] source_location = util.getProvinceCenter(visual_province_locations[origin_province]);
        int[] target_location = util.getProvinceCenter(visual_province_locations[target_province]);
        double length = Math.sqrt((float) Math.pow(target_location[0] - source_location[0], 2) + (float) Math.pow(target_location[1] - source_location[1], 2));

        float angle = (float) Math.PI / 2;
        if (target_location[0] - source_location[0] != 0) {
            angle = (float) Math.atan2(target_location[1] - source_location[1],  target_location[0] - source_location[0]);
        }
        batches.get("Military Actions").draw(attack_arrow, source_location[0], source_location[1], 0, 0, (float) length, (float) length / 8.0f, 1.0f, 1.0f, (float) (angle / Math.PI * 180.0f), 0, 0, 800, 100, false, false);
    }
}
