import qupath.lib.scripting.QP;
import qupath.fx.dialogs.Dialogs;

import qupath.ext.stardist.StarDist2D;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.net.URL;
import java.io.File;
import java.nio.file.Path;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;


/// Returns the physical size of a pixel in µm for the current image.
double get_pixel_size() {
    return QP.getCurrentImageData().getServer().getPixelCalibration().getPixelWidthMicrons();
}


boolean downloadFile(String fileUrl, String destinationPath) {
    try {
        URL url = new URL(fileUrl);
        File destinationFile = new File(destinationPath);
        url.openConnection().with { conn ->
            conn.connectTimeout = 10000;
            conn.readTimeout = 10000;
            
            conn.inputStream.withStream { input ->
                destinationFile.withOutputStream { output ->
                    input.eachByte(4096) { buffer, bytesRead ->
                        output.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        if (destinationFile.exists()) {
            return true
        } else {
            Dialogs.showErrorNotification("CPSD Network", "Failed to download the model.");
            return false
        }
    } catch (FileNotFoundException e) {
        Dialogs.showErrorNotification("CPSD Network", "Resource not found: ${e.message}");
    } catch (MalformedURLException e) {
        Dialogs.showErrorNotification("CPSD Network", "Invalid URL: ${e.message}");
    } catch (IOException e) {
        Dialogs.showErrorNotification("CPSD Network", "Network error: ${e.message}");
    } catch (Exception e) {
        Dialogs.showErrorNotification("CPSD Network", "An unexpected error occurred: ${e.message}");
    }
    return false;
}


def prepare_model(settings) {
    def p_size = get_pixel_size();
    def model = settings.getString("model");
    def model_path = null;
    def channel = settings.getString("channel");
    
    if (model.endsWith(".pb")) { // The model is local.
        model_path = QP.getProject().getPath().getParent().resolve("models").resolve("stardist").resolve(model);
    }
    else { // The model must be downloaded.
        def url = "https://github.com/qupath/models/raw/main/stardist/" + model + ".pb";
        model_path = QP.getProject().getPath().getParent().resolve("models").resolve("stardist").resolve(model + ".pb");
        if (!Files.exists(model_path) && downloadFile(url, model_path.toString())) {
            Dialogs.showInfoNotification("CPSD", "Downloaded model: " + model);
        }
    }
    
    if (!Files.exists(model_path)) {
        return null;
    }
    
    def stardist = StarDist2D.builder(model_path.toString())
          .threshold(0.5)
          // .channels(settings.getString("channel"))
          .normalizePercentiles(
                        settings.getDouble("percentile"), 
                        100.0 - settings.getDouble("percentile"))
          .pixelSize(p_size)
          .measureIntensity()
          .measureShape();
    
    if (channel != "RGB") {
        stardist.channels(channel);
    }
    
    if (settings.getBoolean("useExpansion")) {
        stardist.cellExpansion(settings.getDouble("expansionDistance"));
    }

    if (settings.getBoolean("createAnnotations")) {
        stardist.createAnnotations();
    }

    if (settings.has("assignClass") && settings.getString("assignClass")) {
        stardist.classify(settings.getString("assignClass"));
    }

    return stardist.build();
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


def run_stardist() {
    def model     = prepare_model(settings);
    def imageData = QP.getCurrentImageData();
    def roi       = get_working_area(settings);
    if (roi == null || roi.size() == 0) {
        Dialogs.showWarningNotification("StarDist", "No working area defined for: " + QP.getCurrentImageName());
        return;
    }
    model.detectObjects(imageData, roi);
}

run_stardist();
