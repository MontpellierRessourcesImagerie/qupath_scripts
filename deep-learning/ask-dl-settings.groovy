import qupath.lib.color.ColorDeconvolutionStains;
import qupath.lib.objects.classes.PathClass;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import qupath.lib.gui.scripting.QPEx;
import java.awt.Desktop;
import java.net.URI;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;

_NETWORK = "CellPose"; // "CellPose" or "StarDist"

/// Returns the physical size of a pixel in µm.
def get_pixel_size() {
    return QP.getCurrentImageData().getServer().getPixelCalibration().getPixelWidthMicrons();
}

/// Returns the list of classes available in this project.
def get_classes() {
    return QP.getProject().getPathClasses().collect { it.getName(); };
}

/// Returns the path of the "models" directory located in the project's directory.
def get_models_path() {
    def p_path = QP.getProject().getPath().getParent().resolve("models");
    if (!Files.exists(p_path)) {
        Files.createDirectories(p_path);
    }
    return p_path;
}

/// Returns the list of models available in the "models" directory + the default models for CellPose.
def get_models_list() {
    def p_path = get_models_path();
    def models = Files.list(p_path).map { it.getFileName().toString(); }.collect();
    if (_NETWORK == "CellPose") {
        models += ["cyto", "cyto2", "cyto3", "nuclei"];
    }
    models = [null] + models;
    return models;
}

/// Returns the list of channels for fluo images, and the list of stains for IHC images.
def get_channel_names() {
    def im_data = QP.getCurrentImageData();
    def channels = null;
    if (im_data.isFluorescence()) {
        channels = im_data.getServer().getMetadata().getChannels().collect { it.getName(); };
    }
    if (im_data.isBrightfield()) {
        channels = im_data.getColorDeconvolutionStains().getStains(true).collect { it.getName(); };
    }
    return channels;
}

/// Exports the parameters as a JSON file named "segmentation-settings.json" in the project's directory.
def export_as_json(params) {
    def filePath = QP.getProject().getPath().getParent().resolve("segmentation-settings" + ".json").toString();
    def jsonContent = new JSONObject(params).toString(4);
    try {
        Files.write(Paths.get(filePath), jsonContent.bytes);
    } catch (Exception e) {
        Dialogs.showErrorMessage("Error", "Could not save the settings file.");
    }
}

/// Opens a URL in the default browser.
def openURL(String url) {
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

/// Displays a dialog to ask the user for the segmentation settings.
def show_dialog() {
    if (QPEx.getCurrentImageData() == null) {
        Dialogs.showErrorMessage("No image open", "You need to be on an image before launching this script.");
        return;
    }
    def channels = get_channel_names();
    def classes  = get_classes();
    def models   = get_models_list();
    Platform.runLater {
        // Window title
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle(_NETWORK + " settings (MRI-CIA)");
        // Confirm button
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType helpButtonType = new ButtonType("Help", ButtonBar.ButtonData.HELP)
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, helpButtonType, ButtonType.CANCEL);
        // Model combo box
        ComboBox<String> modelsComboBox = new ComboBox<>();
        modelsComboBox.getItems().addAll(models);
        modelsComboBox.setValue(models[0]);
        // Channel combo box
        ComboBox<String> channelComboBox = new ComboBox<>();
        channelComboBox.getItems().addAll(channels);
        channelComboBox.setValue(channels[0]);
        // Class to segment combo box
        ComboBox<String> targetClassComboBox = new ComboBox<>();
        targetClassComboBox.getItems().addAll(classes);
        targetClassComboBox.setValue(classes[0]);
        // Normalization percentile slider
        Slider percentileSlider = new Slider(0.01, 49.9, 0.01);
        percentileSlider.setShowTickLabels(true);
        percentileSlider.setShowTickMarks(true);
        percentileSlider.setMajorTickUnit(10);
        percentileSlider.setBlockIncrement(1);
        // Use expansion checkbox
        CheckBox useExpansionCheckBox = new CheckBox();
        // Expansion distance slider
        Slider expansionDistanceField = new Slider(0.01, 49.9, 5.0);
        expansionDistanceField.setShowTickLabels(true);
        expansionDistanceField.setShowTickMarks(true);
        expansionDistanceField.setMajorTickUnit(10);
        expansionDistanceField.setBlockIncrement(0.1);
        // Class combo box
        ComboBox<String> classComboBox = new ComboBox<>();
        classComboBox.getItems().addAll(classes);
        classComboBox.setValue(classes[0]);
        // Create annotations checkbox
        CheckBox createAnnotationsCheckBox = new CheckBox();
        // Median cell diameter field
        Slider diameterField = new Slider(0.01, 49.9, 0.01);
        diameterField.setShowTickLabels(true);
        diameterField.setShowTickMarks(true);
        diameterField.setMajorTickUnit(10);
        diameterField.setBlockIncrement(0.1);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        grid.add(new Label("Channel to segment"), 0, 0);
        grid.add(channelComboBox, 1, 0);

        grid.add(new Label("Class to segment"), 0, 1);
        grid.add(targetClassComboBox, 1, 1);

        grid.add(new Label("Model to use"), 0, 2);
        grid.add(modelsComboBox, 1, 2);

        grid.add(new Label("Normalization percentile"), 0, 3);
        grid.add(percentileSlider, 1, 3);

        grid.add(new Label("Use segmentation expansion?"), 0, 4);
        grid.add(useExpansionCheckBox, 1, 4);

        grid.add(new Label("Expansion distance (µm)"), 0, 5);
        grid.add(expansionDistanceField, 1, 5);

        grid.add(new Label("Classify as..."), 0, 6);
        grid.add(classComboBox, 1, 6);

        grid.add(new Label("Create annotations?"), 0, 7);
        grid.add(createAnnotationsCheckBox, 1, 7);

        grid.add(new Label("Median cell diameter (µm)"), 0, 8);
        grid.add(diameterField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(new Callback<ButtonType, Map<String, Object>>() {
            @Override
            Map<String, Object> call(ButtonType dialogButton) {
                if (dialogButton == okButtonType) {
                    return [
                        channel           : channelComboBox.getValue(),
                        model             : modelsComboBox.getValue(),
                        percentile        : percentileSlider.getValue(),
                        useExpansion      : useExpansionCheckBox.isSelected(),
                        expansionDistance : expansionDistanceField.getValue(),
                        assignClass       : classComboBox.getValue(),
                        createAnnotations : createAnnotationsCheckBox.isSelected(),
                        diameter          : diameterField.getValue(),
                        pixelSize         : get_pixel_size(),
                        network           : _NETWORK,
                        targetClass       : targetClassComboBox.getValue()
                    ];
                } else if (dialogButton == helpButtonType) {
                    openURL("http://www.example.com");
                    return null;
                }
                return null;
            }
        });

        def result = dialog.showAndWait();
        result.ifPresent { params ->
            export_as_json(params);
        };
    }
}

show_dialog();