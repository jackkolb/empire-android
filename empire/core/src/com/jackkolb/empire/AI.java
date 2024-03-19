package com.jackkolb.empire;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.jackkolb.empire.EmpireObjects.Player;
import com.jackkolb.empire.Screens.EmpireGameScreen;

import java.util.HashMap;


public class AI  extends EmpireGameScreen {
    private int budget;
    private Array<Array<Integer>> ai_actions = new Array<Array<Integer>>();  // 0: recruit 1: attack 2: dismiss
    private Player empire;


    public void generateActions(int empire_id) {
        ai_actions.clear();
        empire = game_data.players[empire_id];

        // By default, keep 5% of total treasury on reserve (may change to total economy)
        budget = (int) (empire.money * .95f);
        //Gdx.app.error("AI", empire.id + ":" + empire.name + ":" + empire.money);

        // Move troops in the 1 interior regions to borders (by highest need to meet 10% higher than opponent)
        generateReinforcements();

        // Increase border territory defenses to be ~10% higher than opponent's
        increaseBorderDefenses();

        // Disband troops in the 2+ interior region
        dismissInteriorTroops();

        // Blow the rest of the budget on recruiting/attacking the weakest neighbors (wipe out empires > weakest provinces)
        generateAttacks();

        game_data.actions.put(empire.id, ai_actions);
    }

    private void dismissInteriorTroops() {
        for (int province_id: empire.provinces) {
            if (!provinceIsIsolated(province_id)) {
                continue;
            }
            boolean double_locked = true;
            for (int connection = 0; connection < map_data.provinces[province_id].connections.length; connection++) {
                if (!provinceIsIsolated(map_data.provinces[province_id].connections[connection])) {
                    double_locked = false;
                }
            }
            if (double_locked) {
                Array<Integer> action = new Array<Integer>();
                action.add(2, province_id, map_data.provinces[province_id].military, empire.id);
                ai_actions.add(action);
            }
        }
    }

    // recruit along borders to match opponents by 10%
    private void increaseBorderDefenses() {
        for (int province_id: empire.provinces) {
            if (!provinceIsIsolated(province_id)) {
                Array<Integer> action = new Array<Integer>();
                action.add(0, province_id, (int) (getLargestNeighborMilitary(province_id) * 1.10f), empire.id);
                Gdx.app.error("AI", "Increasing border defenses!");
                ai_actions.add(action);
            }
        }
    }

    private void generateReinforcements() {
        Array<Integer> reinforcing_provinces = new Array<Integer>();

        // for each province the player owns, check its connections, if its isolated and has a military add it to the reinforcing provinces
        for (int province_id: empire.provinces) {
            boolean add_province = true;
            // for each province that province connects to
            for (int connection: map_data.provinces[province_id].connections) {
                // if the province connects to a province not belonging to the player, or it doesn't have a military, or it is isolated, disregard it
                if (util.getProvinceOwnerId(connection) != empire.id || map_data.provinces[province_id].military == 0 || provinceIsIsolated(province_id)) {
                    add_province = false;
                }
            }
            // otherwise, reinforce
            if (add_province) {
                reinforcing_provinces.add(province_id);
            }
        }

        // for each reinforcing province, determine where best to send reinforcements, and send reinforcements
        for (int province_id: reinforcing_provinces) {
            int military_allowance = map_data.provinces[province_id].military;

            Array<Integer> reinforcement_order = new Array<Integer>();
            Array<Integer> military_requirements = new Array<Integer>();

            // for each connection, add to the military requirements list
            for (int connection: map_data.provinces[province_id].connections) {
                int military_requirement = getLargestNeighborMilitary(connection) - map_data.provinces[connection].military;
                reinforcement_order.add(connection);
                Gdx.app.error("AI1", "reinforcement order:" + connection);
                military_requirements.add(military_requirement);
            }

            // sort the military requirements list
            Array<Integer> military_requirement_original = new Array<Integer>();
            for (int i : military_requirements) {
                military_requirement_original.add(i);
                Gdx.app.error("AI1", "military requirements:" + i);
            }
            military_requirements.sort();

            // match the reinforcement order with the military requirements list
            Array<Integer> reinforcement_order_new = new Array<Integer>();
            for (int requirement: military_requirements) {
                int index = military_requirement_original.indexOf(requirement, false);
                reinforcement_order_new.add(reinforcement_order.get(index));
            }

            reinforcement_order = reinforcement_order_new;

            // fill reinforcements as per the requirements list
            for(int connection: reinforcement_order) {
                int requirement = getLargestNeighborMilitary(connection) - map_data.provinces[connection].military;
                // if military allowance exceeds requirement, give it the requirement
                if (requirement <= military_allowance) {
                    reinforceProvince(province_id, connection, requirement);
                    military_allowance -= requirement;
                }
                // if military allowance isn't enough, give it all available military
                if (requirement > military_allowance && military_allowance != 0) {
                    reinforceProvince(province_id, connection, military_allowance);
                    military_allowance = 0;
                }
            }
        }
    }

    private void reinforceProvince(int origin_province, int target_province, int military_amount) {
        Gdx.app.error("AI", "AI " + empire.name + " is reinforcing " + target_province + " from " + origin_province);
        Array<Integer> action = new Array<Integer>();
        action.add(1, origin_province, target_province, military_amount);
        action.add(empire.id);
        ai_actions.add(action);
        map_data.provinces[origin_province].military -= military_amount;
    }

    // returns the largest neighboring military amount (non-player)
    private int getLargestNeighborMilitary(int province) {
        int highest_military = 0;
        for (int connection: map_data.provinces[province].connections) {
            // if the neighbor isn't the player and they have a larger military
            if (util.getProvinceOwnerId(map_data.provinces[connection].id) != empire.id && map_data.provinces[connection].military > highest_military) {
                highest_military = map_data.provinces[connection].military;
            }
        }
        Gdx.app.error("AI", "Highest military is" + highest_military);
        return highest_military;
    }

    private void generateAttacks() {
        // Get neighboring targets
        Array<Array<Integer>> targets = new Array<Array<Integer>>();
        for (int province_id: empire.provinces) {
            for (int connection : map_data.provinces[province_id].connections) {
                if (util.getProvinceOwnerId(connection) == empire.id) {
                    continue;
                }

                Array<Integer> target_information = new Array<Integer>();
                target_information.add(connection, map_data.provinces[connection].military);
                //Gdx.app.error("AI", "Debug: Adding target " + connection + " : " + map_data.provinces[connection-1].military);
                targets.add(target_information);
            }
        }

        // See if any targets will wipe out an empire:
        // cycle through all the players
        for (Player player: game_data.players) {
            // see if any only have one province
            if (player.provinces.size ==1) {
                // create a target (target province, target military)
                Array<Integer> target = new Array<Integer>();
                target.add(player.provinces.get(0), map_data.provinces[player.provinces.get(0)].military);
                // if it isn't possible to attack the target province, forget it
                if (!targets.contains(target, false)) {
                    Gdx.app.error("AI", "AI wants to conquer " + target.get(0) + ":" + map_data.provinces[target.get(0)].military + " but can't reach it");
                    continue;
                }
                Gdx.app.error("AI", "AI can conquer " + player.name);
                // since it's possible to attack the target province, get the best origin province
                int origin_province = chooseBestAttackOriginProvince(player.provinces.get(0));
                Gdx.app.error("AI", "AI can best conquer from " + origin_province);
                // if there is a good origin province, attack!
                if (origin_province != -1 && checkAttackFeasibility(origin_province, target.get(0))) {
                    Gdx.app.error("AI", "AI " + empire.name + " is conquering " + target.get(0) + " from " + origin_province);
                    attackProvince(origin_province, target.get(0));
                    targets.removeValue(target,false);
                }
            }
        }

        // Attack targets by lowest military:
        // sort the targets by lowest military
        Array<Array<Integer>> sorted_target_provinces = new Array<Array<Integer>>();
        for (Array<Integer> target: targets) {
            int military = target.get(1);
            int index = 0;
            for (Array<Integer> i: sorted_target_provinces) {
                if (i.get(1) < military) {
                    break;
                }
                index++;
            }
            sorted_target_provinces.insert(index, target);
        }

        // while budget and population allows, attack targets one by one
        for (Array<Integer> target: sorted_target_provinces) {
            int target_province = target.get(0);
            // get best origin province
            int origin_province = chooseBestAttackOriginProvince(target_province);
            Gdx.app.error("AI", "AI is going to attack from " + getAttackRecruitmentRequirement(origin_province, target_province) + ":" + budget);
            // if there is a good origin province, and budget allows, attack it
            if (origin_province != -1 && target_province != -1 && checkAttackFeasibility(origin_province, target_province) && getAttackRecruitmentRequirement(origin_province, target_province) < budget) {
                Gdx.app.error("AI", "AI " + empire.name + " is attacking " + target_province + " from " + origin_province);
                attackProvince(origin_province, target_province);
            }
            if (budget <= 0) {
                break;
            }
        }

        game_data.actions.put(empire.id, ai_actions);
    }

    // formats the attack, places it in the ai actions, decreases the budget
    private void attackProvince(int origin_province, int target_province) {
        Gdx.app.error("AI", "Adding entry for attack from " + origin_province + " to " + target_province);
        int military_amount = (int) (map_data.provinces[target_province].military * 1.3f + 1);
        Array<Integer> action = new Array<Integer>();
        action.add(0, origin_province, military_amount - map_data.provinces[origin_province].military, empire.id);  // recruit the required troops
        ai_actions.add(action);
        action.clear();
        action.add(1, origin_province, target_province, military_amount);  // attack the target province
        action.add(empire.id);
        ai_actions.add(action);  // add the action to the ai actions
        budget -= military_amount - map_data.provinces[origin_province].military;  // decrease the expense from the budget
    }

    // choose province with the highest population that connects to the target, otherwise returns -1
    private int chooseBestAttackOriginProvince(int target_province) {
        // see all territories that connect to the target province, make a list of those the player owns
        HashMap<Integer, Integer> possible_connections = new HashMap<Integer, Integer>();
        for (int province_id: map_data.provinces[target_province].connections) {
            if (util.getProvinceOwnerId(map_data.provinces[province_id].id) == empire.id) {
                possible_connections.put(province_id, map_data.provinces[province_id].population);
            }
        }

        int origin_province = -1;
        int max_population = -1;
        for (int key: possible_connections.keySet()) {
            if (possible_connections.get(key) > max_population) {
                max_population = possible_connections.get(key);
                origin_province = key;
            }
        }
        return origin_province;
    }

    // checks if the budget will enable an attack with a 30% buffer
    private boolean checkAttackFeasibility(int origin_province, int target_province) {
        if (map_data.provinces[origin_province].population + map_data.provinces[origin_province].military > map_data.provinces[target_province].military * 1.3f && budget > map_data.provinces[target_province].military * 1.3f - map_data.provinces[origin_province].military) {
            return true;
        }
        return false;
    }

    private int getAttackRecruitmentRequirement(int origin_province, int target_province) {
        if (origin_province == -1 || target_province == -1) {
            return -1;
        }

        int requirement = (int) (map_data.provinces[target_province].military * 1.3f - map_data.provinces[origin_province].military);
        if (requirement < 0) {
            requirement = 0;
        }
        return requirement;
    }

    private boolean provinceIsIsolated(int source_province) {
        //Gdx.app.error("AI", "source province: " + source_province);
        for (int province = 0; province < map_data.provinces[source_province].connections.length; province++) {
            if (util.getProvinceOwnerId(map_data.provinces[source_province].connections[province]) != empire.id) {
                return false;
            }
        }
        return true;
    }
}
