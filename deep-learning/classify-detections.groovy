/**
 * BOOTSTRAP NEURON TRAINING DATA
 * - Detects objects with StarDist
 * - Assigns Neuron / Ignore classes
 * - DOES NOT delete anything
 * - You manually correct → then train Object Classifier
 */

import qupath.ext.stardist.StarDist2D
import qupath.lib.scripting.QP
import qupath.lib.gui.dialogs.Dialogs

// ================= PARAMETERS =================
def modelPath = "/home/baecker/Documents/mri/in/open-desk/open_desk-2026_01_15/neuron_detection/he_heavy_augment.pb"

def probabilityThreshold = 0.65
def pixelSize = 0.5

def runOnSelectedAnnotations = true
// =============================================


// ================= SETUP =================
def imageData = QP.getCurrentImageData()
if (!imageData) {
    Dialogs.showErrorMessage("Error", "No image open")
    return
}

def annotations = runOnSelectedAnnotations ?
        QP.getSelectedObjects().findAll { it.isAnnotation() } :
        QP.getAnnotationObjects()

if (annotations.isEmpty()) {
    Dialogs.showErrorMessage("Error", "No annotations selected")
    return
}
// =======================================


// ================= RUN STARDIST =================
println "Running StarDist..."

def stardist = StarDist2D.builder(modelPath)
        .normalizePercentiles(1, 99)
        .threshold(probabilityThreshold)
        .pixelSize(pixelSize)
        .measureShape()
        .measureIntensity()
        .build()

stardist.detectObjects(imageData, annotations)
stardist.close()

println "StarDist finished."
// =======================================


// ================= RULE-BASED LABELING =================
def neuronClass = getPathClass("Neuron", ColorTools.makeRGB(0,0,255))
def ignoreClass = getPathClass("Ignore", ColorTools.makeRGB(255,0,0))

def pathClasses = getQuPath().getAvailablePathClasses()
listOfClasses = [
    neuronClass,
    ignoreClass
]
pathClasses.addAll(listOfClasses)

int neuronCount = 0
int ignoreCount = 0

def detections = QP.getDetectionObjects().findAll {
    annotations.contains(it.getParent())
}

for (det in detections) {
    roi = det.getROI()
    print("centroid", centroidX, centroidY)
    def ml = det.getMeasurementList()
    def region = det.getParent()?.getPathClass()?.getName()?.toLowerCase()
    if (!region) {
        det.setPathClass(ignoreClass)
        ignoreCount++
        m1.put("class-proxy", 0);
        continue
    }

    Double area = ml.get("Area µm^2")
    Double diam = ml.get("Max diameter µm")
    Double dab  = ml.get("DAB: Mean")
    Double h    = ml.get("Hematoxylin: Mean")

    if ([area, diam, dab, h].any { it == null }) {
        det.setPathClass(ignoreClass)
        ignoreCount++
        m1.put("class-proxy", 0);
        continue
    }

    double ratio = dab / Math.max(h, 0.001)
    boolean isNeuron = false

    // -------- Cortex (permissive) --------
    if (region == "cortex") {
        isNeuron =
            area >= 60 &&
            diam >= 10 &&
            ratio >= 0.9
    }

    // -------- Hippocampus (stricter) --------
    else if (region == "hippocampus") {
        isNeuron =
            area >= 120 &&
            diam >= 14 &&
            ratio >= 1.2
    }

    if (isNeuron) {
        det.setPathClass(neuronClass)
        neuronCount++
        m1.put("class-proxy", 1);
    } else {
        det.setPathClass(ignoreClass)
        ignoreCount++
        m1.put("class-proxy", 0);
    }
}
// =======================================


// ================= REPORT =================
Dialogs.showMessageDialog(
    "Training objects created",
    "Neuron: ${neuronCount}\nIgnore: ${ignoreCount}\n\n" +
    "Now manually correct mistakes,\nthen train Object Classifier."
)

println "DONE — no deletion performed."
return
