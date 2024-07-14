import qupath.lib.scripting.QP;
import qupath.fx.dialogs.Dialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONObject;

import groovy.lang.GroovyShell;
import groovy.lang.Binding;


/// Returns the list of channels for fluo images, and the list of stains for IHC images.
List<String> get_channel_names() {
    def im_data = QP.getCurrentImageData();
    def channels = null;
    if (im_data.isFluorescence()) {
        channels = im_data.getServer().getMetadata().getChannels().collect { it.getName(); };
    }
    if (im_data.isBrightfield()) {
        channels = im_data.getColorDeconvolutionStains().getStains(true).collect { it.getName(); } + ["RGB"];
    }
    return channels;
}


/// Returns the list of classes available in this project.
List<String> get_classes() {
    return QP.getProject().getPathClasses().collect { it.getName(); };
}


/// Transforms a string into a valid name for a settings file.
Path settingsPath(String name) {
    def file_name = "cnrs-mri-cia-cpsd." + name + ".json";
    return QP.getProject().getPath().getParent().resolve(file_name);
}


/// Import and returns some settings as a JSON object.
def import_settings(Path filePath) {
    if (filePath == null) {
        return null;
    }
    def jsonContent = new String(Files.readAllBytes(filePath));
    return new JSONObject(jsonContent);
}


/// Checks that the settings are valid for the current project.
boolean sanityCheck(settings) {
    if (settings == null) {
        Dialogs.showErrorMessage("CPSD settings", "Settings cannot be null.");
        return false;
    }

    // Checks that the desired channel exists.
    if (!get_channel_names().contains(settings.channel) && settings.channel != "RGB") {
        Dialogs.showErrorMessage("CPSD settings", "Missing channel: " + settings.channel);
        return false;
    }

    // Check that all the mentioned classes exist.
    def all_classes = get_classes();
    if (settings.has("assignClass") && !all_classes.contains(settings.assignClass)) {
        Dialogs.showErrorMessage("CPSD settings", "A class named '" + settings.assignClass + "' is required.");
        return false;
    }

    if (!settings.targetClass.startsWith("::") && !all_classes.contains(settings.targetClass)) {
        Dialogs.showErrorMessage("CPSD settings", "A class named '" + settings.targetClass + "' is required.");
        return false;
    }

    // Check that we can import the desired model.
    if (settings.network == "CellPose") {
        try {
            Class.forName("qupath.ext.biop.cellpose.Cellpose2D");
        } catch (ClassNotFoundException e) {
            Dialogs.showErrorMessage("CPSD settings", "It looks like you didn't install CellPose for QuPath.");
            return false;
        }
        if (QP.getProject().getScripts().get("segment-cellpose") == null) {
            Dialogs.showErrorMessage("CPSD settings", "CellPose's worker (segment-cellpose.groovy) is missing.");
            return false;
        }
    }

    if (settings.network == "StarDist") {
        try {
            Class.forName("qupath.ext.stardist.StarDist2D");
        } catch (ClassNotFoundException e) {
            Dialogs.showErrorMessage("CPSD settings", "It looks like you didn't install StarDist for QuPath.");
            return false;
        }
        if (QP.getProject().getScripts().get("segment-stardist") == null) {
            Dialogs.showErrorMessage("CPSD settings", "StarDist's worker (segment-stardist.groovy) is missing.");
            return false;
        }
    }
    
    // Check that the model is present if it is a custom model.
    def model = settings.model;
    if (model.endsWith(".pb")) {
        def model_path = QP.getProject().getPath().getParent().resolve(settings.network.toLowerCase()).resolve(model);
        if (!File.exists(model_path)) {
            Dialogs.showErrorMessage("CPSD settings", "The custom model '" + model + "' is missing from: models/" + settings.network.toLowerCase() + "/");
            return false;
        }
    }

    return true;
}



void main() {
    // 'settings_name' is received through an environment binding from the caller script.
    Path settings_path = settingsPath(settings_name);
    if (!Files.exists(settings_path)) {
        Dialogs.showErrorMessage("Missing '" + settings_name + "'", "Couldn't find the file: " + settings_path);
    }
    def settings = import_settings(settings_path);
    if (sanityCheck(settings)) {
        Binding binding = new Binding();
        binding.setVariable("settings", settings);
        def shell = new GroovyShell(this.class.classLoader, binding);
        if (settings.network == "CellPose") { shell.evaluate(QP.getProject().getScripts().get("segment-cellpose")); }
        if (settings.network == "StarDist") { shell.evaluate(QP.getProject().getScripts().get("segment-stardist")); }
    }
    Dialogs.showInfoNotification("Worker done", "Finished worker for settings: " + settings_name);
}


main();