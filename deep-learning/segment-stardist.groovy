import qupath.ext.stardist.StarDist2D
import qupath.lib.projects.Project
import java.nio.file.Files
import java.nio.file.Paths
import qupath.lib.scripting.QP
import qupath.fx.dialogs.Dialogs;


// = = = = = = = = SETTINGS = = = = = = = = = = = = =

_CLASSIFIERS             = ["find-yellow", "find-red", "find-green"];
_CHANNELS                = ["DaPi", "C2", "Chan3", "4"];
_MODEL                   = 'dsb2018_heavy_augment';
_MIN_CELL_AREA           = 30.0;
_MAX_CELL_AREA           = 1000.0;
_MIN_MEAN_DAPI_INTENSITY = 6.0;

// = = = = = = = = = = = = = = = = = = = = = = = = = =


// https://qupath.readthedocs.io/en/0.4/docs/tutorials/cell_classification.html
// https://qupath.readthedocs.io/en/0.4/docs/concepts/classifications.html

main();


def removeAnnotations() {
    QP.selectAllObjects();
    QP.clearSelectedObjects(false);
    QP.createFullImageAnnotation(true);
}


def getModel(name) {
    def project = QP.getProject();
    if (project == null) {
        Dialogs.showErrorMessage("Check Classifier", "No project is open!");
        return "";
    }

    // Get the project path and construct the path to the classifiers
    def projectPath = project.getPath().getParent();
    def classifiersFile = Paths.get(projectPath.toString(), "models", name+".pb");

    // Check if the classifier file exists
    if (Files.exists(classifiersFile)) {
        return classifiersFile;
    } else {
        Dialogs.showErrorMessage("Check model", "Model '" + name + "' for StarDist was not found.");
        return "";
    }
}


def segmentNuclei(pathModel, pixelSize) {

    def stardist = StarDist2D.builder(pathModel.toString())
          .threshold(0.5)
          .channels('Channel 1')
          .normalizePercentiles(5, 95) 
          .pixelSize(pixelSize)
          .measureIntensity()
          .measureShape()
    //    .createAnnotations()
          .build();
    
    // Run detection for the selected objects
    def imageData = QP.getCurrentImageData();
    def pathObjects = QP.getSelectedObjects();
    
    if (pathObjects.isEmpty()) {
        Dialogs.showErrorMessage("StarDist", "Please select a parent object!");
        return;
    }
    stardist.detectObjects(imageData, pathObjects);
}


def filterNuclei(minArea, maxArea, minIntensity) {
    def objects = getDetectionObjects();
    def count = 0;
    def objectsToRemove = [];
    
    objects.each { detection ->
        def area = detection.getMeasurementList().getMeasurementValue('Area µm^2');
        def intensity = detection.getMeasurementList().getMeasurementValue('DAPI: Mean');
        if (area < minArea || area > maxArea || intensity < minIntensity) {
            objectsToRemove.add(detection);
            count++;
        }
    }
    
    def imageData = getCurrentImageData();
    def hierarchy = imageData.getHierarchy();
    hierarchy.removeObjects(objectsToRemove, true);
    
    print("Discarded " + count.toString() + " nuclei.");
    fireHierarchyUpdate();
}


def main() {
    def pathModel = getModel(_MODEL);
    removeAnnotations();
    if (pathModel == "") { return; }
    QP.setChannelNames(*_CHANNELS);
    def pixelSize = QP.getCurrentImageData().getServer().getPixelCalibration().getPixelWidthMicrons();
    segmentNuclei(pathModel, pixelSize);
    // filterNuclei(_MIN_CELL_AREA, _MAX_CELL_AREA, _MIN_MEAN_DAPI_INTENSITY);
    QP.runObjectClassifier(*_CLASSIFIERS);
    Dialogs.showInfoNotification("DONE", "Nuclei segmented and classified for " + QP.getCurrentImageName());
}


