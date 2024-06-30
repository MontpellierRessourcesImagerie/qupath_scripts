import qupath.ext.biop.cellpose.Cellpose2D;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONObject;


def settings_available() {
    def filePath = QP.getProject().getPath().getParent().resolve("segmentation-settings" + ".json").toString();
    if (Files.exists(Paths.get(filePath))) {
        return filePath;
    }
    return null;
}


def import_settings() {
    def filePath = settings_available();
    if (filePath == null) {
        return null;
    }
    def jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
    return new JSONObject(jsonContent);
}


def prepare_model(settings) {
    def p_size = settings.getDouble("pixelSize");
    def diameter = settings.getDouble("diameter");
    def diameter_pixels = diameter / p_size;
    def cellpose = Cellpose2D.builder(settings.getString("model"))
                   .pixelSize(p_size)
                   .channels(settings.getString("channel"))
                   .normalizePercentilesGlobal(
                        settings.getDouble("percentile"), 
                        100.0 - settings.getDouble("percentile"), 
                        5)
                   .cellprobThreshold(0.0)
                   .flowThreshold(0.5)
                   .diameter((int)diameter_pixels)
                   .measureShape()
                   .measureIntensity();
    
    if (settings.getBoolean("useExpansion")) {
        cellpose.cellExpansion(settings.getDouble("expansionDistance"));
    }

    if (settings.getBoolean("createAnnotations")) {
        cellpose.createAnnotations();
    }

    if (settings.has("assignClass") && settings.getString("assignClass")) {
        cellpose.classify(settings.getString("assignClass"));
    }

    cellpose = cellpose.build();
    return cellpose;
}


def get_working_area(settings) {
    // We work in priority on the selected objects
    def pathObjects = getSelectedObjects(); // To process only selected annotations, useful while testing
    if (!pathObjects.isEmpty()) {
        print("Working on selected annotations (" + pathObjects.size() + " items).");
        return pathObjects; 
    }

    // Then we try if we have a target class
    resetSelection();
    if (settings.has("targetClass")) {
        def tgt_cls = settings.getString("targetClass");
        selectObjectsByClassification(tgt_cls);
        pathObjects = getSelectedObjects();
        print("Working on annotations with target class [" + tgt_cls + "] (" + pathObjects.size() + " items).");
    }
    if (!pathObjects.isEmpty()) {
        return pathObjects;
    }

    // Otherwise, we segment in every annotation
    pathObjects = getAnnotationObjects();
    if (!pathObjects.isEmpty()) {
        return pathObjects;
    }

    // In the last case, we won't segment anything.
    Dialogs.showErrorMessage("Cellpose", "Please select a parent object!")
    return null;
}


def run_cellpose() {
    def settings  = import_settings();
    def model     = prepare_model(settings);
    def imageData = getCurrentImageData();
    def roi       = get_working_area(settings);
    model.detectObjects(imageData, roi);
}

run_cellpose();