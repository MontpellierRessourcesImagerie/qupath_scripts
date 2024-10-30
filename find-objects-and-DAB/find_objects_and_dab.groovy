import qupath.lib.projects.Project
import java.nio.file.Files
import java.nio.file.Paths
import qupath.lib.scripting.QP

// = = = = = = = = = =  SETTINGS = = = = = = = = = = = = = = = = = =

_SMALLEST_OBJECT = 640000.0; // Smallest area to reach for a detection to become an object: value in µm²
_SMALLEST_HOLE   = 380000.0; // Below this area, a hole in the detection will be filled: value in µm²
_TARGET_CLASS    = "Organ";

// = = = = = = = = = =  CONSTANTS = = = = = = = = = = = = = = = = = =

_OBJ_CLASSIFIER = "find-objects";

// = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = = =


main();


/**
 * Checks whether the pixel classifier required to find objects is present or not.
 * Probes the folder ./classifiers/pixel_classifiers to seek for it.
 * 
 * Args:
 *     - obj_classifier (string) the name of the pixel classifier without its JSON extension.
 * Returns:
 *     - (bool) true if the classifier is present, false otherwise.
 */
def checkForClassifier(obj_classifier) {
    def project = QP.getProject();
    if (project == null) {
        Dialogs.showErrorMessage("Check Classifier", "No project is open!");
        return false;
    }

    // Get the project path and construct the path to the classifiers
    def projectPath = project.getPath().getParent();
    def classifiersFile = Paths.get(projectPath.toString(), "classifiers", "pixel_classifiers", obj_classifier+".json");

    // Check if the classifier file exists
    if (Files.exists(classifiersFile)) {
        Dialogs.showInfoNotification("Check Classifier", "Classifier '" + obj_classifier + "' is present.");
        return true;
    } else {
        Dialogs.showErrorMessage("Check Classifier", "Classifier '" + obj_classifier + "' not found.");
        return false;
    }
}


def dumpMemory() {
    Thread.sleep(100);
    javafx.application.Platform.runLater {
        getCurrentViewer().getImageRegionStore().cache.clear();
        System.gc();
    }
    Thread.sleep(100);
}


def removeAnnotations() {
    resetSelection();
    selectObjectsByClassification("training-set", _TARGET_CLASS);
    clearSelectedObjects();
}


def getNObjects() {
    resetSelection();
    selectObjectsByClassification(_TARGET_CLASS);
    def nOrgans = getSelectedObjects().size();
    resetSelection();
    return nOrgans;
}


def main() {
    dumpMemory();
    removeAnnotations();
    checkForClassifier(_OBJ_CLASSIFIER);
    createAnnotationsFromPixelClassifier("find-objects", _SMALLEST_OBJECT, _SMALLEST_HOLE, "SPLIT");
   
    resetSelection();
    selectObjectsByClassification(_TARGET_CLASS);
    addPixelClassifierMeasurements("find-dab", "find-dab");
    resetSelection();
    
    Dialogs.showInfoNotification(_TARGET_CLASS + " segmented", getNObjects().toString() + " chunks found in " + QP.getCurrentImageName());
}
