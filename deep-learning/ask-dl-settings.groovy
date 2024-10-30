import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.awt.Desktop;
import java.net.URI;

import org.json.JSONObject;

import qupath.lib.color.ColorDeconvolutionStains;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.gui.scripting.QPEx;


class Utils {    
    /// Returns the list of classes available in this project.
    static List<String> get_classes() {
        return QP.getProject().getPathClasses().collect { it.getName(); };
    }
    
    /// Returns the path of the "models" directory located in the project's directory. Creates it if it doesn't exist.
    static Path get_models_path() {
        def p_path = QP.getProject().getPath().getParent().resolve("models");
        if (!Files.exists(p_path)) {
            Files.createDirectories(p_path);
        }
        return p_path;
    }
    
    /// Returns the list of models available in the "models" directory + the default models for CellPose.
    static List<String> get_models_list(String network) {
        Path p_path = get_models_path().resolve(network.toLowerCase());
        if (!Files.exists(p_path)) { Files.createDirectories(p_path); }
        List<String> local_models = Files.list(p_path).map { it.getFileName().toString(); }.collect();
        List<String> basic_models = [];
        
        if (network == "CellPose") {
            basic_models = ["cyto3", "cyto2", "cyto", "nuclei", "tissuenet", "livecell"];
        } else {
            basic_models = ["dsb2018_heavy_augment", "dsb2018_paper", "he_heavy_augment"];
        }
        List<String> models = basic_models;
        for (int i = 0 ; i < local_models.size() ; i++) {
            String current = local_models[i];
            if (!current.endsWith(".pb")) { continue; }
            if (!basic_models.contains(current.replace(".pb", ""))) {
                models.add(current);
            }
        }
        if (models.size() == 0) {
            models = [null];
        }
        return models;
    }
    
    /// Returns the list of channels for fluo images, and the list of stains for IHC images.
    static List<String> get_channel_names() {
        def im_data = QP.getCurrentImageData();
        def channels = null;
        if (im_data.isFluorescence()) {
            channels = im_data.getServer().getMetadata().getChannels().collect { it.getName(); };
        }
        if (im_data.isBrightfield()) {
            // channels = im_data.getColorDeconvolutionStains().getStains(true).collect { it.getName(); };
            channels = ["RGB"];
        }
        return channels;
    }
    
    /// Converts a string representing a file name to a valid path.
    static Path settingsPath(String name) {
        def file_name = "cnrs-mri-cia-cpsd." + name + ".json";
        return QP.getProject().getPath().getParent().resolve(file_name);
    }
    
    /// Exports the parameters as a JSON file named after the pattern "cnrs-mri-cia.*.json" in the project's directory.
    static boolean export_as_json(params) {
        def filePath = settingsPath(params.exportAs).toString();
        def jsonContent = new JSONObject(params).toString(4);
        try {
            Files.write(Paths.get(filePath), jsonContent.bytes);
        } catch (Exception e) {
            Dialogs.showErrorMessage("Error", "Could not save the settings file.");
            return false;
        }
        return true;
    }
    
    /// Opens a URL in the default browser.
    static void openURL(String url) {
        print("Opening URL: " + url);
        if( Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    Desktop.getDesktop().browse(new URI(url));
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }).start();
        }
    }
    
    /// Checks that a string is a valid name for a settings file.
    static boolean isValidString(String text) {
        def pattern = ~'^[a-zA-Z0-9-_]+$';
        return text ==~ pattern;
    }
};


// ------------------------------------------------------


class CPSD extends Dialog<Map<String, Object>> {

    Label channelLabel;
    Label targetClassLabel;
    Label networkLabel;
    Label modelLabel;
    Label percentileLabel;
    Label useExpansionLabel;
    Label expansionDistanceLabel;
    Label classLabel;
    Label createAnnotationsLabel;
    Label diameterLabel;
    Label nameInputLabel;

    ComboBox<String> channelComboBox;
    ComboBox<String> targetClassComboBox;
    ComboBox<String> networkComboBox;
    ComboBox<String> modelsComboBox;
    Slider           percentileSlider;
    CheckBox         useExpansionCheckBox;
    Slider           expansionDistanceField;
    ComboBox<String> classComboBox;
    CheckBox         createAnnotationsCheckBox;
    Slider           diameterField;
    TextField        settingsName;
    
    Font originalFont;
    
    List<String> _NETWORKS;

    CPSD() {
        setTitle("CP/SD settings");

        ButtonType okButtonType   = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType helpButtonType = new ButtonType("Help", ButtonBar.ButtonData.HELP);
        getDialogPane().getButtonTypes().addAll(okButtonType, helpButtonType, ButtonType.CANCEL);

        initializeComponents();

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(channelLabel, 0, 0);
        grid.add(channelComboBox, 1, 0);

        grid.add(targetClassLabel, 0, 1);
        grid.add(targetClassComboBox, 1, 1);
        
        grid.add(networkLabel, 0, 2);
        grid.add(networkComboBox, 1, 2);

        grid.add(modelLabel, 0, 3);
        grid.add(modelsComboBox, 1, 3);

        grid.add(percentileLabel, 0, 4);
        grid.add(percentileSlider, 1, 4);

        grid.add(useExpansionLabel, 0, 5);
        grid.add(useExpansionCheckBox, 1, 5);

        grid.add(expansionDistanceLabel, 0, 6);
        grid.add(expansionDistanceField, 1, 6);

        grid.add(classLabel, 0, 7);
        grid.add(classComboBox, 1, 7);

        grid.add(createAnnotationsLabel, 0, 8);
        grid.add(createAnnotationsCheckBox, 1, 8);

        grid.add(diameterLabel, 0, 9);
        grid.add(diameterField, 1, 9);
        
        grid.add(nameInputLabel, 0, 10);
        grid.add(settingsName, 1, 10);

        getDialogPane().setContent(grid);

        setResultConverter({ dialogButton ->
            if (dialogButton == okButtonType) {
                return [
                    network           : networkComboBox.getValue(),
                    channel           : channelComboBox.getValue(),
                    model             : modelsComboBox.getValue(),
                    percentile        : percentileSlider.getValue(),
                    useExpansion      : useExpansionCheckBox.isSelected(),
                    expansionDistance : expansionDistanceField.getValue(),
                    assignClass       : classComboBox.getValue(),
                    createAnnotations : createAnnotationsCheckBox.isSelected(),
                    diameter          : diameterField.getValue(),
                    network           : networkComboBox.getValue(),
                    targetClass       : targetClassComboBox.getValue(),
                    exportAs          : settingsName.getText()
                ];
            } else if (dialogButton == helpButtonType) {
                Utils.openURL("https://montpellierressourcesimagerie.github.io/qupath_scripts/md_dl_sd_cp.html");
                return null;
            }
            return null;
        } as Callback);

        updateModels(_NETWORKS[0]);
    }

    private void initializeComponents() {
        _NETWORKS = ["CellPose", "StarDist"];
        
        channelLabel           = new Label("Channel to segment");
        targetClassLabel       = new Label("Input annotations");
        networkLabel           = new Label("Network");
        modelLabel             = new Label("Model");
        percentileLabel        = new Label("Normalization percentile");
        useExpansionLabel      = new Label("Use cell expansion?");
        expansionDistanceLabel = new Label("Expansion distance (µm)");
        classLabel             = new Label("Classify results as");
        createAnnotationsLabel = new Label("Create annotations?");
        diameterLabel          = new Label("Median cell diameter (µm)");
        nameInputLabel         = new Label("Save settings as");
        
        networkComboBox = new ComboBox<>();
        networkComboBox.getItems().addAll(_NETWORKS);
        def network = _NETWORKS[0];
        networkComboBox.setValue(network);
        networkComboBox.valueProperty().addListener({ observable, oldValue, newValue ->
            updateModels(newValue);
        } as ChangeListener);
        
        modelsComboBox = new ComboBox<>();
        updateModels(network);

        channelComboBox = new ComboBox<>();
        channelComboBox.getItems().addAll(Utils.get_channel_names());
        channelComboBox.setValue(Utils.get_channel_names()[0]);

        targetClassComboBox = new ComboBox<>();
        def pjt_classes = Utils.get_classes();
        pjt_classes[0] = ":: All annotations";
        pjt_classes = [":: Active annotation", ":: Full image"] + pjt_classes;
        targetClassComboBox.getItems().addAll(pjt_classes);
        targetClassComboBox.setValue(pjt_classes[0]);

        percentileSlider = new Slider(0.00, 50.0, 5.0);
        percentileSlider.setShowTickLabels(true);
        percentileSlider.setShowTickMarks(true);
        percentileSlider.setMajorTickUnit(10);

        useExpansionCheckBox = new CheckBox();
        useExpansionCheckBox.selectedProperty().addListener({ observable, oldValue, newValue ->
            expansionDistanceLabel.setVisible(newValue);
            expansionDistanceField.setVisible(newValue);
        } as ChangeListener);

        expansionDistanceField = new Slider(5.0, 80.0, 5.0);
        expansionDistanceField.setShowTickLabels(true);
        expansionDistanceField.setShowTickMarks(true);
        expansionDistanceField.setMajorTickUnit(15);
        
        expansionDistanceField.setVisible(useExpansionCheckBox.isSelected());
        expansionDistanceLabel.setVisible(useExpansionCheckBox.isSelected());

        classComboBox = new ComboBox<>();
        classComboBox.getItems().addAll(Utils.get_classes());
        classComboBox.setValue(Utils.get_classes()[0]);

        createAnnotationsCheckBox = new CheckBox();

        diameterField = new Slider(5.0, 80.0, 5.0);
        diameterField.setShowTickLabels(true);
        diameterField.setShowTickMarks(true);
        diameterField.setMajorTickUnit(15.0);
        
        def defaultName = "segmentation-settings";
        settingsName = new TextField(defaultName);
        originalFont = settingsName.getFont()
        settingsName.textProperty().addListener({ observable, oldValue, newValue ->
            validateTextField(newValue);
        } as ChangeListener);
        validateTextField(defaultName);
    }
    
    private void validateTextField(String text) {
        if (text.isEmpty() || !Utils.isValidString(text)) {
            settingsName.setFont(originalFont);
            settingsName.setStyle("-fx-background-color: lightcoral;");
        } else if (Files.exists(Utils.settingsPath(text))) {
            settingsName.setFont(Font.font(originalFont.getFamily(), javafx.scene.text.FontPosture.ITALIC, originalFont.getSize()));
            settingsName.setStyle(null);
        }
        else {
            settingsName.setFont(originalFont);
            settingsName.setStyle(null);
        }
    }

    private void updateModels(String network) {
        modelsComboBox.getItems().clear();
        def models = Utils.get_models_list(network);
        modelsComboBox.getItems().addAll(models);
        modelsComboBox.setValue(models[0]);
    }
};


// ---------------------------------------------


if (QPEx.getCurrentImageData() == null) {
    Dialogs.showErrorMessage("No image open", "You need to be on an image before launching this script.")
} else {
    Platform.runLater {
        CPSD dialog = new CPSD();
        def result = dialog.showAndWait();
        result.ifPresent { params ->
            Utils.export_as_json(params);
            Dialogs.showInfoNotification("Settings saved!", "CP/SD settings saved as '" + params.exportAs + "'");
        }
    }
}