import qupath.lib.scripting.QP;
import qupath.fx.dialogs.Dialogs;

import qupath.ext.biop.cellpose.Cellpose2D;

import java.nio.file.Files;
import java.nio.file.Paths;


/// Returns the physical size of a pixel in µm for the current image.
double get_pixel_size() {
    return QP.getCurrentImageData().getServer().getPixelCalibration().getPixelWidthMicrons();
}


def prepare_model(settings) {
    def p_size = get_pixel_size();
    def diameter = settings.getDouble("diameter");
    def diameter_pixels = diameter / p_size;
    def model = settings.getString("model");
    def channel = settings.getString("channel");
    if (model.endsWith(".pb")) {
        model = QP.getProject().getPath().getParent().resolve("cellpose").resolve(model);
    }
    
    def cellpose = Cellpose2D.builder(model)
                   .pixelSize(p_size)
                   // .channels(settings.getString("channel"))
                   .normalizePercentilesGlobal(
                        settings.getDouble("percentile"), 
                        100.0 - settings.getDouble("percentile"), 
                        5)
                   .cellprobThreshold(0.0)
                   .flowThreshold(0.5)
                   .diameter((int)diameter_pixels)
                   .measureShape()
                   .measureIntensity();
    
    if (channel != "RGB") {
        cellpose.channels(channel);
    }
    
    if (settings.getBoolean("useExpansion")) {
        cellpose.cellExpansion(settings.getDouble("expansionDistance"));
    }

    if (settings.getBoolean("createAnnotations")) {
        cellpose.createAnnotations();
    }

    if (settings.has("assignClass") && settings.getString("assignClass")) {
        cellpose.classify(settings.getString("assignClass"));
    }

    return cellpose.build();
}


def get_working_area(settings) {
    def input_area  = settings.getString("targetClass");
    def pathObjects = null;
    
    if (input_area.startsWith(":: ")) {
        if (input_area == ":: All annotations") {
            pathObjects = QP.getAnnotationObjects();
        }
        else if (input_area == ":: Active annotation") {
            pathObjects = QP.getSelectedObjects();
        }
        else if (input_area == ":: Full image") {
            QP.resetSelection();
            QP.createFullImageAnnotation(true);
            pathObjects = QP.getSelectedObjects();
        }
    }
    else {
        QP.resetSelection();
        QP.selectObjectsByClassification(input_area);
        pathObjects = QP.getSelectedObjects();
    }
    
    return pathObjects;
}


def run_cellpose() {
    def model     = prepare_model(settings);
    def imageData = QP.getCurrentImageData();
    def roi       = get_working_area(settings);
    if (roi == null || roi.size() == 0) {
        Dialogs.showWarningNotification("CellPose", "No working area defined for: " + QP.getCurrentImageName());
        return;
    }
    model.detectObjects(imageData, roi);
}

run_cellpose();