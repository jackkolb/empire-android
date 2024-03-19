package com.jackkolb.empire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.jackkolb.empire.EmpireObjects.Player;
import com.jackkolb.empire.EmpireObjects.Province;
import com.jackkolb.empire.EmpireObjects.Map;
import com.jackkolb.empire.Screens.EmpireGameScreen;

import java.util.HashMap;

public class EmpireGame extends EmpireGameScreen {
	public Player[] players;
	private int start_money;
	private int start_army;

	private int user_money_freeze;

	public int year;

	public boolean next_turn_visual = true;

	public HashMap<Integer, Array<Array<Integer>>> actions;

	private HashMap<Integer, Array<Array<Integer>>> processed_actions;

	public void nextTurn() {
		// if player has won the game, end
		boolean victory = true;
		for (Player player : players) {
			if (player.id != player_id && player.provinces.size > 0) {
				victory = false;
			}
		}
		if (victory) {
			Gdx.app.error("Jack", "The human player has won the game");
			endgame_button.setVisible(true);
			endgame_message.setVisible(true);
			endgame_message.setText("Congratulations! You have conquered the map.\n\nTap here to exit.");
		}

		// if player has lost the game, end
		if (players[player_id].provinces.size == 0) {
			Gdx.app.error("Jack", "The human player has lost the game");
			endgame_button.setVisible(true);
			endgame_message.setVisible(true);
			endgame_message.setText("You have been defeated.\n\nTap here to exit.");
		}

		// now actually execute the next turns

		// after player inputs actions
		if (!next_turn_visual) {
			map_data = new Map(map_data_original);  // revert map to pre-user actions (so everything plays out at once)
			game_data.players[player_id].money = user_money_freeze;
			next_turn_visual = true;

			// generate AI actions
			for (int empire_id = 0; empire_id < players.length; empire_id++) {
				Player player = players[empire_id];

				// generate AI actions for each non-human player and if the player has territory
				if (empire_id != player_id && players[empire_id].provinces.size != 0) {
					player.player_ai.generateActions(empire_id);
				}
			}

		}

		// after player sees what's happening
		else {
			next_turn_visual = false;
			year += 1;

			// process actions by type
			processed_actions = new HashMap<Integer, Array<Array<Integer>>>();
			processed_actions.put(0, new Array<Array<Integer>>());
			processed_actions.put(1, new Array<Array<Integer>>());
			processed_actions.put(2, new Array<Array<Integer>>());
			for (int player_id = 0; player_id < players.length; player_id++) {
				for (Array<Integer> action : actions.get(player_id)) {
					int action_type = action.get(0);
					processed_actions.get(action_type).add(action);
				}
			}

			// execute actions

			// execute troop recruitments (code 0)
			for (Array<Integer> action : processed_actions.get(0)) {
				// action is a troop recruitment
				int source_province = action.get(1);
				int recruitment_amount = action.get(2);
				int player_id = action.get(3);

				// check if the province is still owned by that empire (it may have been conquered)
				if (util.getProvinceOwnerId(source_province) != player_id) {
					continue;
				}

				// otherwise, continue with the recruitment
				// limit the recruitment by province population
				recruitment_amount = Math.min(recruitment_amount, map_data.provinces[source_province].population);

				map_data.provinces[source_province].military += recruitment_amount;
				map_data.provinces[source_province].population -= recruitment_amount;
				game_data.players[player_id].money -= recruitment_amount;
			}

			// execute troop dismissals (code 2)
			for (Array<Integer> action : processed_actions.get(2)) {
				//Gdx.app.error("AI", "Action Execution: Dismiss!");
				int source_province = action.get(1);
				int military_amount = action.get(2);
				int player_id = action.get(3);

				// check if the province is still owned by that empire (it may have been conquered)
				if (util.getProvinceOwnerId(source_province) != player_id) {
					continue;
				}

				// decrease troops in province
				map_data.provinces[source_province].population += Math.min(military_amount, map_data.provinces[source_province].military);
				map_data.provinces[source_province].military -= Math.min(military_amount, map_data.provinces[source_province].military);

			}

			// execute troop attacks (code 1)
			for (Array<Integer> action : processed_actions.get(1)) {
				//Gdx.app.error("AI", "Action Execution: Attack!");
				int origin_province = action.get(1);
				int target_province = action.get(2);
				int military_amount = action.get(3);
				int player_id = action.get(4);

				// if the owner of this province is no longer the given player, do not execute action
				if (util.getProvinceOwnerId(origin_province) != player_id) {
					continue;
				}

				// if there are fewer troops than intended, choose the max of the fewer
				military_amount = Math.min(military_amount, map_data.provinces[origin_province].military);

				// if the empire is "attacking" their own province (reinforcing)
				if (util.getProvinceOwnerId(origin_province) == util.getProvinceOwnerId(target_province)) {
					map_data.provinces[origin_province].military -= military_amount;
					map_data.provinces[target_province].military += military_amount;
					continue;
				}

				// if the empire successfully takes over the territory (and not its own territory)
				if (map_data.provinces[target_province].military < military_amount) {
					// remove the province if the owner was a player (not wilderness)
					if (util.getProvinceOwnerId(target_province) != -1) {
						game_data.players[util.getProvinceOwnerId(target_province)].provinces.removeValue(target_province, true);
					}
					game_data.players[util.getProvinceOwnerId(origin_province)].provinces.add(target_province);
					map_data.provinces[origin_province].military -= military_amount;
					map_data.provinces[target_province].military = military_amount - map_data.provinces[target_province].military;
				}
				// if the empire fails to take over the territory
				else {
					map_data.provinces[origin_province].military -= military_amount;
					map_data.provinces[target_province].military -= military_amount;
				}
			}

			// update economies and populations
			for (Province province : map_data.provinces) {
				// only increase economies up to 20k
				if (province.economy < 20000) {
					province.economy += province.economy * 0.01f + (province.population - province.economy) * 0.3f;
				}
				province.population += (province.population * 0.01f) + (province.economy - province.population) * 0.2f;
			}

			// update treasuries
			for (int empire_id = 0; empire_id < players.length; empire_id++) {
				Player player = players[empire_id];
				for (int province_id : player.provinces) {
					player.money += map_data.provinces[province_id].economy * 0.10f;
					player.money -= map_data.provinces[province_id].military * 0.2f;
					Gdx.app.error("Jack", "MONEY #" + player.id + " " + player.money + " +" + map_data.provinces[province_id].economy * 0.10f + " -" + map_data.provinces[province_id].military * 0.2f);

				}
			}

			util.resetActions();
			map_data_original = new Map(map_data);
			user_money_freeze = game_data.players[player_id].money;
		}
	}

	// used to scale the sliders
	public int getRecruitMax() {
		// catch the rare UI error where no province is selected
		if (province_selected == -1) {
			return 0;
		}

		int amount = map_data.provinces[province_selected].population;
		if (amount > game_data.players[player_id].money) {
			amount = game_data.players[player_id].money;
		}
		if (game_data.players[player_id].money < 0) {
			amount = 0;
		}
		return amount;
	}

	public void playerRecruitMilitary(float proportion) {
		int amount = (int) (getRecruitMax() * proportion);
		map_data.provinces[province_selected].military += amount;
		map_data.provinces[province_selected].population -= amount;
		game_data.players[player_id].money -= amount;

		// adds the action to the game actions
		if (amount != 0) {
			Array<Integer> action = new Array<Integer>();
			action.add(0, province_selected, amount, player_id);
			actions.get(player_id).add(action);
		}
	}

	public void playerAttack(float proportion) {
		int amount = (int) (map_data.provinces[attack_origin_province].military * proportion);
		map_data.provinces[attack_origin_province].military -= amount;

		if (amount != 0) {
			Array<Integer> action = new Array<Integer>();
			action.add(1, attack_origin_province, province_selected, amount);
			action.add(player_id);
			actions.get(player_id).add(action);
		}
	}

	public void playerDismissMilitary(float proportion) {
		int amount = (int) (map_data.provinces[province_selected].military * proportion);
		map_data.provinces[province_selected].military -= amount;
		map_data.provinces[province_selected].population += amount;

		if (amount != 0) {
			Array<Integer> action = new Array<Integer>();
			action.add(2, province_selected, amount, player_id);
			actions.get(player_id).add(action);
		}
	}
}
