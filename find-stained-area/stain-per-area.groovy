import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.roi.RectangleROI;
import qupath.lib.gui.scripting.QPEx;

// List of the classifiers to use (one per protein to locate).
def classifiers = [
    "find-p1",
    "find-p2",
    "find-p3"
];

// We are working on fluo images.
setImageType('FLUORESCENCE');

// We get the height and width to make an ROI taking the whole image.
def imageData = QPEx.getCurrentImageData();
def server = imageData.getServer();
height = server.getHeight();
width = server.getWidth();

// We create the ROI taking the whole image.
def roi = new RectangleROI(0, 0, width-1, height-1);
def annotation = new PathAnnotationObject(roi, PathClass.fromString("Positive"));
annotation.setName("Measure-stains");

def hierarchy = imageData.getHierarchy();
def selectionModel = hierarchy.getSelectionModel();

// Remove previous attempts and add the ROI to the image.
selectAnnotations();
clearSelectedObjects(true);
hierarchy.addObject(annotation);
selectionModel.setSelectedObject(annotation);

// Measuring every fluo stain.
for (int i = 0 ; i < classifiers.size() ; i++) {
    print("Applying pixel classifier: " + classifiers[i]);
    addPixelClassifierMeasurements(classifiers[i], classifiers[i]);
}

print("DONE.");
