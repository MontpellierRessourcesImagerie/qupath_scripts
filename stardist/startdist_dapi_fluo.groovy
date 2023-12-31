import qupath.ext.stardist.StarDist2D
org.locationtech.jts.geom.GeometryOverlay.isOverlayNG = true
// Specify the model file (you will need to change this!)
var pathModel = '/home/baecker/Programs/QuPath-0.4.3-Linux/Script/dsb2018_heavy_augment.pb'

var stardist = StarDist2D.builder(pathModel)
        .threshold(0.5)              // Probability (detection) threshold
        .channels('Channel 1')            // Specify detection channel
        .normalizePercentiles(1, 99) // Percentile normalization
        .pixelSize(0.5)              // Resolution for detection
//        .cellExpansion(5.0)          // Approximate cells based upon nucleus expansion
//        .cellConstrainScale(1.5)     // Constrain cell expansion using nucleus size
        .measureShape()              // Add shape measurements
        .measureIntensity()          // Add cell measurements (in all compartments)
        .build()

// Run detection for the selected objects
var imageData = getCurrentImageData()
var pathObjects = getSelectedObjects()
if (pathObjects.isEmpty()) {
    Dialogs.showErrorMessage("StarDist", "Please select a parent object!")
    return
}
stardist.detectObjects(imageData, pathObjects)
println 'Done!'