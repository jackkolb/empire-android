import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.Group;
import javafx.stage.Stage;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Arrays;
import java.util.List;
import java.io.File;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.Circle;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import java.io.FileInputStream;

public class EmpireWorldbuilder extends Application {
    Label label_title;
    Label label_subtitle;
    Label label_description;
    Label label_instructions;

    Button button_open_background;
    FileInputStream input_image;

    Button button_import;
    FileChooser filechooser_open_mapdata;

    Button button_export;

    Button button_add_province;
    Button button_set_boundary_province;
    Button button_reset_boundary_province;
    Button button_set_connections_province;
    Button button_save_province;

    ChoiceBox choicebox_choose_province;
    String selected_province;

    boolean collecting_vertices;
    boolean selecting_connection_provinces;

    ArrayList<ArrayList<ArrayList<Integer>>> all_vertices;  // [ provinces [ points [ ] ] ] 
    ArrayList<ArrayList<Integer>> current_selection_vertices;
    ArrayList<Polygon> province_polygons;
    ArrayList<Integer> current_province_connections;

    ObservableList<String> choicebox_items;
    ArrayList<Map<String, String>> province_information;

    /*
    {
        "class": "com.jackkolb.empire.EmpireObjects.Province",
        "id": "1"
        "name": "Province 1",
        "economy": "10000",
        "population": "10000",
        "military": "0",
        "connections": "1 2 3 4",
        "vertices": [[0, 0], [10, 0], [10, 10], [0, 10]]
    }
    */

    Label label_map_name;
    TextField entry_map_name;

    Label label_map_author;
    TextField entry_map_author;

    Label label_province_id;

    Label label_province_name;
    TextField entry_province_name;

    Label label_province_economy;
    TextField entry_province_economy;

    Label label_province_population;
    TextField entry_province_population;

    Label label_province_military;
    TextField entry_province_military;

    Label label_province_connections;
    TextField entry_province_connections;

    Label label_province_vertices;

    int vertice_left_adjust;

    Image image;
    ImageView mapview;

    Pane map;
    Scene scene;
    Group overlays_provinces;

    Group overlays_selection_points;


    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Empire Worldbuilder");

        input_image = new FileInputStream("./Example/europe.png");
        image = new Image(input_image); mapview = new ImageView(image);

        label_title = new Label("Empire Worldbuilder");
        label_subtitle = new Label("(C) 2021 Jack Kolb");
        label_description = new Label("\nUse this tool to create maps for\nthe mobile game Empire.");

        label_instructions = new Label("\nRead the included INSTRUCTIONS.txt\nfile for instructions on how\n to use the worldbuilder!");

        button_open_background = new Button("Open Background Image");

        button_import = new Button("Import Map Data");
        filechooser_open_mapdata = new FileChooser();
        FileChooser.ExtensionFilter empire_filter = new FileChooser.ExtensionFilter("EMPIRE files (*.empire)", "*.empire");
        filechooser_open_mapdata.getExtensionFilters().add(empire_filter);

        button_export = new Button("Export Data");

        label_map_name = new Label("Map Name");
        entry_map_name = new TextField();

        label_map_author = new Label("Map Author");
        entry_map_author = new TextField();

        button_add_province = new Button("Add Province");
        button_save_province = new Button("Save Province");
        button_set_connections_province = new Button("Set Connections");
        button_set_boundary_province = new Button("Set Boundary");
        button_reset_boundary_province = new Button("Reset Boundary");

        choicebox_choose_province = new ChoiceBox();
        choicebox_items = FXCollections.observableArrayList();
        choicebox_choose_province.setItems(choicebox_items);

        label_province_id = new Label("Province ID:");
        label_province_name = new Label("Province Name:");
        label_province_economy = new Label("Province Economy:");
        label_province_population = new Label("Province Population:");
        label_province_military = new Label("Province Military:");
        label_province_connections = new Label("Province Connections:");
        label_province_vertices = new Label("Province Vertices:");

        entry_province_name = new TextField();
        entry_province_economy = new TextField();
        entry_province_population = new TextField();
        entry_province_military = new TextField();
        entry_province_connections = new TextField();

        province_information = new ArrayList<>();
        province_polygons = new ArrayList<>();
        all_vertices = new ArrayList<>();

        collecting_vertices = false;
        selecting_connection_provinces = false;
        current_selection_vertices = new ArrayList<>();
        current_province_connections = new ArrayList<>();

        selected_province = "";

        overlays_provinces = new Group();
        overlays_selection_points = new Group();

        // place the buttons and labels in the left panel
        VBox left = new VBox(
            label_title,
            label_subtitle,
            label_description,
            label_instructions,
            button_open_background,
            button_import,
            button_export,
            label_map_name,
            entry_map_name,
            label_map_author,
            entry_map_author,
            button_add_province,
            choicebox_choose_province,
            label_province_id,
            label_province_name,
            entry_province_name,
            label_province_economy,
            entry_province_economy,
            label_province_population,
            entry_province_population,
            label_province_military,
            entry_province_military,
            label_province_connections,
            entry_province_connections,
            button_set_connections_province,
            button_set_boundary_province,
            button_reset_boundary_province,
            button_save_province
            );

        map = new Pane(mapview, overlays_provinces, overlays_selection_points);
        HBox hbox = new HBox(left, map);
        scene = new Scene(hbox);

        handlers();

        stage.setScene(scene);
        stage.show();

        vertice_left_adjust = (int) left.localToScene(left.getBoundsInLocal()).getWidth();
    }

    public void reset_data() {
        province_information.clear();
        province_polygons.clear();
        all_vertices.clear();
        choicebox_items.clear();
        overlays_provinces.getChildren().clear();
        selected_province = "0";
    }

    public void newProvince() {
        Map<String, String> province_data = new HashMap<>();
        province_data.put("name", "Province " + Integer.toString(province_information.size()+1));
        province_data.put("id", Integer.toString(province_information.size()));
        province_data.put("economy", "10000");
        province_data.put("population", "10000");
        province_data.put("military", "0");
        province_data.put("connections", "-");
        province_data.put("connections array", "[]");
        province_data.put("vertices", "");

        all_vertices.add(new ArrayList<ArrayList<Integer>>());
        Polygon polygon = baseProvincePolygon();
        province_polygons.add(polygon);

        overlays_provinces.getChildren().addAll(polygon);

        province_information.add(province_data);
        choicebox_items.add(province_data.get("id"));

        // bring new province to forfront
        saveProvinceData();
        selected_province = Integer.toString(province_information.size()-1);
        choicebox_choose_province.setValue(selected_province);
        return;
    }

    public Polygon baseProvincePolygon() {
        Polygon polygon = new Polygon();
        polygon.setStroke(Color.BLACK);
        polygon.setFill(new Color(0, 0.2, 0, 0.3));
        return polygon;
    }

    public Circle addSelectionMarker(int x, int y) {
        Circle circle = new Circle();
        circle.setStroke(Color.YELLOW);
        circle.setFill(Color.BLACK);
        circle.setCenterX((float) x);
        circle.setCenterY((float) y);
        circle.setRadius(5.0f);
        return circle;
    }

    public Circle addRawSelectionMarker(int x, int y) {
        Circle circle = new Circle();
        circle.setStroke(Color.RED);
        circle.setFill(Color.BLACK);
        circle.setCenterX((float) x);
        circle.setCenterY((float) y);
        circle.setRadius(5.0f);
        return circle;
    }

    public void resetPolygonFills() {
        for (Polygon polygon : province_polygons) {
            polygon.setFill(new Color(0, 0.2, 0, 0.3));
        }
    }

    public void resetFlags() {
        collecting_vertices = false;
        selecting_connection_provinces = false;
    }

    public void loadProvinceData() {
        int province_id = Integer.parseInt(selected_province);
        label_province_id.setText("Province ID: " + province_information.get(province_id).get("id"));
        entry_province_name.setText(province_information.get(province_id).get("name"));
        entry_province_economy.setText(province_information.get(province_id).get("economy"));
        entry_province_population.setText(province_information.get(province_id).get("population"));
        entry_province_military.setText(province_information.get(province_id).get("military"));
        entry_province_connections.setText(province_information.get(province_id).get("connections"));

        resetPolygonFills();
        province_polygons.get(province_id).setFill(new Color(0, 0, 0.2, 0.3));

        // load province connections, color them
        setConnectionsList();
    }

    public void setConnectionsList() {
        current_province_connections.clear();
        int province_id = Integer.parseInt(selected_province);
        String raw_connections_string = province_information.get(province_id).get("connections");
        if (raw_connections_string == "-") { return; }
        for (String i : raw_connections_string.split(",")) {
            current_province_connections.add(Integer.parseInt(i));
            // when importing a map, the polygons arent guaranteed yet, hence this check
            if (Integer.parseInt(i) < province_polygons.size()) {
                province_polygons.get(Integer.parseInt(i)).setFill(new Color(0.2, 0, 0, 0.3));
            }
        }
    }

    public void saveProvinceData() {
        //System.out.println("Saving province data");
        if (selected_province == "") { return; }
        int province_id = Integer.parseInt(selected_province);
        province_information.get(province_id).put("name", entry_province_name.getText());
        province_information.get(province_id).put("economy", entry_province_economy.getText());
        province_information.get(province_id).put("population", entry_province_population.getText());
        province_information.get(province_id).put("military", entry_province_military.getText());

        String connection_text = Arrays.toString(current_province_connections.toArray()).replace(" ", "");
        connection_text = connection_text.substring(1, connection_text.length()-1);
        if (current_province_connections.size() == 0) { connection_text = "-"; }
        province_information.get(province_id).put("connections", connection_text);
        province_information.get(province_id).put("connections array", Arrays.toString(current_province_connections.toArray()));//.substring(1, Arrays.toString(current_province_connections.toArray()).length()-1));
        //System.out.println(province_information.get(province_id));
    }

    public void handlers() throws Exception {
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                if (collecting_vertices) {
                    ArrayList<Integer> point = new ArrayList<Integer>();
                    point.add((int) mouseEvent.getX() - vertice_left_adjust);
                    point.add((int) mouseEvent.getY());
                    
                    Circle marker;
                    // if button is left mouse
                    if (mouseEvent.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                        point.add((int) 0);  // 0 indicates snap point
                        // get the closest snap point
                        ArrayList<Integer> tempPoint = new ArrayList<Integer>();
                        tempPoint.add((int) mouseEvent.getX() - vertice_left_adjust);
                        tempPoint.add((int) mouseEvent.getY());

                        ArrayList<Integer> closePoint = snapVertice(tempPoint);
                        point.set(0, closePoint.get(0));
                        point.set(1, closePoint.get(1));

                        marker = addSelectionMarker(closePoint.get(0), closePoint.get(1));
                        System.out.println("Added snap point");
                    }
                    // if button is right mouse
                    else {
                        point.add((int) 1);  // 1 indicates raw point
                        marker = addRawSelectionMarker((int) mouseEvent.getX() - vertice_left_adjust, (int) mouseEvent.getY());
                    }
                    current_selection_vertices.add(point);
                    overlays_selection_points.getChildren().add(marker);
                    return;
                }
                if (selecting_connection_provinces) {
                    // check is the point is within any polygons
                    for (int i = 0; i < province_polygons.size(); i++) {
                        Point2D click = new Point2D(mouseEvent.getX() - vertice_left_adjust, mouseEvent.getY());
                        if (province_polygons.get(i).contains(click)) {
                            if (i == Integer.parseInt(selected_province)) {
                                //System.out.println("Cannot add own province");
                            }
                            else if (!current_province_connections.contains(i)) {
                                //System.out.println("Added province " + i + " to connections");
                                province_polygons.get(i).setFill(new Color(0.2, 0.0, 0.0, 0.2));
                                current_province_connections.add(i);
                            }
                            else {
                                //System.out.println("Removing province " + i + " from connections");
                                province_polygons.get(i).setFill(new Color(0.2, 0.2, 0.2, 0.2));
                                current_province_connections.remove(Integer.valueOf(i));
                            }
                            return;
                        }
                    }
                    //System.out.println("Click did not hit any provinces");
                }
            }
        });

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE || e.getCode() == KeyCode.ENTER) {
                if (collecting_vertices) {
                    //System.out.println("Ending vertice selection");
                    endBoundarySelection();
                }
                if (selecting_connection_provinces) {
                    //System.out.println("Ending connection selection");
                    endProvinceConnectionSelection();
                }
            }

            if (e.getCode() == KeyCode.C) {
                startConnectionSelection();
            }
            if (e.getCode() == KeyCode.B) {
                startBoundarySelection();
            }
            if (e.getCode() == KeyCode.N) {
                newProvince();
            }
            if (e.getCode() == KeyCode.R) {
                resetProvinceBoundary();
            }
            if (e.getCode() == KeyCode.S) {

            }
            if (e.getCode() == KeyCode.P) {
                entry_province_population.requestFocus();
            }
            if (e.getCode() == KeyCode.E) {
                entry_province_economy.requestFocus();
            }
            if (e.getCode() == KeyCode.M) {
                entry_province_military.requestFocus();
            }
            if (e.getCode() == KeyCode.I) {
                entry_province_name.requestFocus();
            }
            if (e.getCode() == KeyCode.RIGHT) {
                //System.out.println("Right");
                resetFlags();
                int new_province_id = Integer.parseInt(selected_province) + 1;
                if (new_province_id > province_information.size()-1) { new_province_id = 0; }
                choicebox_choose_province.setValue(Integer.toString(new_province_id));
            }
            if (e.getCode() == KeyCode.LEFT) {
                //System.out.println("Left");
                resetFlags();
                int new_province_id = Integer.parseInt(selected_province) - 1;
                if (new_province_id < 0) { new_province_id = province_information.size()-1; }
                choicebox_choose_province.setValue(Integer.toString(new_province_id));
            }
        });

        button_add_province.setOnAction(event -> {
            newProvince();
        });

        button_save_province.setOnAction(event -> {
            saveProvinceData();
        });

        button_set_boundary_province.setOnAction(event -> {
            startBoundarySelection();
        });

        button_reset_boundary_province.setOnAction(event -> {
            resetProvinceBoundary();
        });

        button_set_connections_province.setOnAction(event -> {
            startConnectionSelection();
        });

        button_import.setOnAction(event -> {
            try {handle_load_map();}
            catch (Exception e) {e.printStackTrace();}
        });

        button_export.setOnAction(event -> {
            try {
                exportMap();
            }
            catch (Exception e) {}
        });

        button_open_background.setOnAction(event -> {
            try {
                handle_open_background();
            }
            catch (Exception e) {}
        });

        ChangeListener<String> changeListener = new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                resetFlags();
                if (newValue != null) {
                    selected_province = newValue;
                    loadProvinceData();
                }
            }
        };

        // Selected Item Changed.
        choicebox_choose_province.getSelectionModel().selectedItemProperty().addListener(changeListener);

    }

    public void handle_load_map() throws Exception, FileNotFoundException {
        // select map
        FileChooser filechooser_open_background = new FileChooser();
        FileChooser.ExtensionFilter png_filter = new FileChooser.ExtensionFilter("Empire Map files (*.empiremap)", "*.empiremap");
        filechooser_open_background.getExtensionFilters().add(png_filter);

        File selectedFile = filechooser_open_background.showOpenDialog(null);
        if (selectedFile != null) {
        }
        else {
            return;
        }

        // delete current data
        reset_data();

        // import map
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(selectedFile.getName()));
            // stages: BASE, PROVINCE
            String stage = "BASE";

            while (true) {
                String line = reader.readLine();
                if (line == null) { break; }
                String[] items = line.split("\"");
                if (stage == "BASE") {
                    if (items.length == 1) {
                        continue;
                    }
                    // if "class" in line, check if correct class
                    if (items[1].equals("class")) {
                        // if not the correct class, break
                        if (!(items[3].equals("com.jackkolb.empire.EmpireObjects.Map"))) {
                            System.out.println("Invalid formatting: incorrect BASE class");
                            return;
                        }
                    }
                    // if "name", change map name
                    if (items[1].equals("name")) {
                        entry_map_name.setText(items[3]);
                    }
                    // if "author", change map author
                    if (items[1].equals("author")) {
                        entry_map_author.setText(items[3]);
                    }
                    // if "provinces", change collection mode
                    if (items[1].equals("provinces")) {
                        stage = "PROVINCE";
                    }
                }
                else if (stage == "PROVINCE") {
                    // if line is "{", add new province
                    if (items.length == 1) {
                        if (line.contains("{")) {
                            newProvince();
                        }
                        continue;
                    }

                    // if "class" is incorrect, break
                    if (items[1].equals("class")) {
                        if (!(items[3].equals("com.jackkolb.empire.EmpireObjects.Province"))) {
                            System.out.println("Invalid formatting: incorrect PROVINCE class");
                            return;
                        }
                    }
                    // if "name", change province name
                    if (items[1].equals("name")) {
                        entry_province_name.setText(items[3]);
                    }
                    // if "vertices", change province vertices
                    if (items[1].equals("vertices")) {
                        startBoundarySelection();

                        String array_string = items[2].substring(2);
                        ArrayList<String> raw_list = new ArrayList<String>(Arrays.asList(array_string.substring(1, array_string.length()-1).replace(" ", "").replace("[", "").replace("]", "").replace(",", " ").split(" ")));

                        for (int i = 0; i < raw_list.size(); i += 3) {
                            ArrayList<Integer> point = new ArrayList();
                            point.add(convertPcPxX(raw_list.get(i)));
                            point.add(convertPcPxY(raw_list.get(i+1)));
                            point.add(Integer.parseInt(raw_list.get(i+2)));
                          
                            current_selection_vertices.add(point);
                            overlays_selection_points.getChildren().add(addSelectionMarker(convertPcPxX(raw_list.get(i)), convertPcPxY(raw_list.get(i+1))));
                        }
                        endBoundarySelection();
                    }
                    // if "population", change province population
                    if (items[1].equals("population")) {
                        entry_province_population.setText(items[3]);
                    }
                    // if "economy", change province economy
                    if (items[1].equals("economy")) {
                        entry_province_economy.setText(items[3]);
                    }
                    // if "military", change province military
                    if (items[1].equals("military")) {
                        entry_province_military.setText(items[3]);
                    }
                    // if "connections", change province connections
                    if (items[1].equals("connections")) {
                        String array_string = items[2].substring(2);
                        entry_province_connections.setText(array_string);
                        startConnectionSelection();
                        ArrayList<String> connection_list = new ArrayList<String>(Arrays.asList(array_string.replace(" ", "").replace(",", " ").replace("[", "").replace("]", "").split(" ")));
                        for (String i : connection_list) {
                            if (i == "") { continue; }
                            System.out.println(":::" + connection_list);
                            current_province_connections.add(Integer.parseInt(i));
                        }
                        resetFlags();
                        saveProvinceData();

                    }
                    // if "}", store province, change stage to BASE
                    if (line.contains("}")) {
                        saveProvinceData();
                        System.out.println("Saved province");
                        stage = "BASE";
                    }
                }
            }
            reader.close();
            System.out.println("Imported " + selectedFile.getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle_open_background() throws Exception, FileNotFoundException {
        // get file name
        FileChooser filechooser_open_background = new FileChooser();
        FileChooser.ExtensionFilter png_filter = new FileChooser.ExtensionFilter("PNG files (*.png)", "*.png");
        filechooser_open_background.getExtensionFilters().add(png_filter);

        File selectedFile = filechooser_open_background.showOpenDialog(null);
        if (selectedFile != null) {
            System.out.println("File selected: " + selectedFile.getName());
            FileInputStream ffs = new FileInputStream(selectedFile);
            image = new Image(ffs);
        }
        else {
            System.out.println("File selection cancelled.");
        }

        // open the background
        input_image = new FileInputStream(selectedFile);
        image = new Image(input_image); mapview.setImage(image);

    }

    // handler for exporting the map data
    public void exportMap() throws IOException {
        // select map
        System.out.println("Choosing Map Export location");
        FileChooser filechooser_open_background = new FileChooser();
        FileChooser.ExtensionFilter png_filter = new FileChooser.ExtensionFilter("Empire Map files (*.empiremap)", "*.empiremap");
        filechooser_open_background.getExtensionFilters().add(png_filter);

        File selectedFile = filechooser_open_background.showSaveDialog(null);
        if (selectedFile != null) {
            //System.out.println("File selected: " + selectedFile.getName());
        }
        else {
            //System.out.println("File selection cancelled.");
        }

        // export
        FileWriter fileWriter = new FileWriter(selectedFile.getName());
        PrintWriter printWriter = new PrintWriter(fileWriter);

        System.out.println("Exporting Map");
        printWriter.printf("{\n" +
                "    \"class\":" +
                "    \"com.jackkolb.empire.EmpireObjects.Map\",\n" +
                "    \"name\": \"%s\",\n" +
                "    \"author\": \"%s\",\n\n" +
                "    \"province_count\": \"%s\",\n\n" +
                "    \"provinces\": [\n", entry_map_name.getText(), entry_map_author.getText(), province_information.size());

        for (int i = 0; i < province_information.size(); i++) {
            Map<String, String> province = province_information.get(i);
            printWriter.printf("        {\n" +
                    "            \"class\": \"com.jackkolb.empire.EmpireObjects.Province\",\n" +
                    "            \"id\": \"%s\",\n" +
                    "            \"name\": \"%s\",\n" +
                    "            \"vertices\": %s,\n" +
                    "            \"population\": \"%s\",\n" +
                    "            \"economy\": \"%s\",\n" +
                    "            \"military\": \"%s\",\n" +
                    "            \"connections\": %s\n" +
                    "        }", i+1, province.get("name"), getProvinceVerticeString(i), province.get("population"), province.get("economy"), province.get("military"), province.get("connections array"));
            if (i < province_information.size() - 1) {
                printWriter.printf(",");
            }
            printWriter.printf("\n\n");
        }

        printWriter.printf("    ]\n");
        printWriter.printf("}\n");
        printWriter.close();
        System.out.println("Done exporting, to " + selectedFile.getName());
    }

    public String getProvinceVerticeString(int province_id) {
        ArrayList<ArrayList<Integer>> province_data = all_vertices.get(province_id);
        String result = "[";

        for (int i = 0; i < province_data.size(); i++) {
            ArrayList<Integer> vertice = province_data.get(i);
            String point = "[" + convertPxPcX(vertice.get(0)) + "," + convertPxPcY(vertice.get(1)) + "," + vertice.get(2) + "]";
            result += point;
            if (i < province_data.size() - 1) {
                result += ", ";
            }
        }
        result += "]";
        return result;
    }

    public void resetProvinceBoundary() {
        //System.out.println("Resetting province boundary");
        resetFlags();
        if (selected_province == "") { return; }
        all_vertices.get(Integer.parseInt(selected_province)).clear();
        overlays_provinces.getChildren().set(Integer.parseInt(selected_province), baseProvincePolygon());
    }

    public void startConnectionSelection() {
        if (selecting_connection_provinces) { return; }
        setConnectionsList();
        if (selected_province == "") { return; }
        resetFlags();
        selecting_connection_provinces = true;
    }

    public void startBoundarySelection() {
        if (collecting_vertices) { return; }
        if (selected_province == "") { return; }
        resetFlags();
        collecting_vertices = true;
        current_selection_vertices = new ArrayList<ArrayList<Integer>>();
    }

    // width percent to pixel
    public int convertPcPxX(String perc_x) {
        int width = (int) mapview.getImage().getWidth();
        int pix_x = (int) (Double.parseDouble(perc_x) * width);
        return pix_x;
    }

    // width pixel to percent of width
    public double convertPxPcX(int pix_x) {
        int width = (int) mapview.getImage().getWidth();
        double perc_x = (pix_x * 1000 / width) / 1000.0;
        return perc_x;
    }

    // height percent to pixel
    public int convertPcPxY(String perc_y) {
        int height = (int) mapview.getImage().getHeight();
        int pix_y = (int) (Double.parseDouble(perc_y) * height);
        return pix_y;
    }

    // height pixel to percent of height
    public double convertPxPcY(int pix_y) {
        int height = (int) mapview.getImage().getHeight();
        double perc_y = (pix_y * 1000 / height) / 1000.0;
        return perc_y;
    }

    public void endBoundarySelection() {
        //System.out.println("The vertice selection is over");
        //processVertices();

        all_vertices.set(Integer.parseInt(selected_province), current_selection_vertices);

        resetFlags();

        // add to polygons
        List<Double> raw_points = new ArrayList<Double>();
        for (ArrayList<Integer> point : current_selection_vertices) {
            raw_points.addAll(Arrays.asList((double) point.get(0), (double) point.get(1)));
        }

        Node somenode = overlays_provinces.getChildren().get(Integer.parseInt(selected_province));
        Polygon province = (Polygon) somenode;
        province.getPoints().clear();
        province.getPoints().addAll(raw_points);
        overlays_provinces.getChildren().set(Integer.parseInt(selected_province), province);

        overlays_selection_points.getChildren().clear();
    }

    public void endProvinceConnectionSelection() {
        System.out.println("Done selecting province connections");
        resetFlags();
        saveProvinceData();
        loadProvinceData();
    }

    // for processVertices, checks snap
    public boolean checkProximity(ArrayList<Integer> trial, ArrayList<Integer> ref) {
        // checks if point is at most 2.5% away
        if (Math.abs((trial.get(0) - ref.get(0)) / image.getWidth()) < .025 && Math.abs((trial.get(1) - ref.get(1)) / image.getHeight()) < .025) {
            return true;
        }
        return false;
    }

    // group vertices that are super close to others in a sweet O(n^3) algorithm
    public void processVertices() {
        for (int province_index = 0; province_index < all_vertices.size(); province_index++) {
            for (int ref_point_index = 0; ref_point_index < all_vertices.get(province_index).size(); ref_point_index++) {
                for (int trial_point_index = 0; trial_point_index < current_selection_vertices.size(); trial_point_index++) {
                    if (current_selection_vertices.get(trial_point_index).get(2) == 0 && checkProximity(current_selection_vertices.get(trial_point_index), all_vertices.get(province_index).get(ref_point_index))) {
                        current_selection_vertices.set(trial_point_index, all_vertices.get(province_index).get(ref_point_index));
                    }
                } 
            } 
        }
    }

    // determines the closest point to snap to
    public ArrayList<Integer> snapVertice(ArrayList<Integer> testPoint) {
        ArrayList<Integer> closestPoint = new ArrayList<Integer>();
        closestPoint.add(0);
        closestPoint.add(0);

        float dist = -1.0f;  // set to a default -1

        // for each province
        for (int province_index = 0; province_index < all_vertices.size(); province_index++) {
            // for each point in the province
            for (int ref_point_index = 0; ref_point_index < all_vertices.get(province_index).size(); ref_point_index++) {
                // if the distance from this trial point to the reference point is less than the current distance, match it
                float testDist = getDistance(testPoint, all_vertices.get(province_index).get(ref_point_index));
                if (dist == -1.0f || testDist < dist) {
                    dist = testDist;
                    closestPoint = all_vertices.get(province_index).get(ref_point_index);
                }
            } 
        }

        return closestPoint;
    }

    public float getDistance(ArrayList<Integer> a, ArrayList<Integer> b) {
        return (float) Math.sqrt(Math.pow(a.get(0) - b.get(0), 2) + Math.pow(a.get(1) - b.get(1), 2));
    }
    
    public static void main(String[] args) {
        Application.launch(args);
    }

}