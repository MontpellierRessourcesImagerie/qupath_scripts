import qupath.ext.stardist.StarDist2D
import qupath.lib.scripting.QP

def modelPath = "/home/clement/Téléchargements/2026-09-08-hsefer/scripts/he_heavy_augment.pb"

def stardist = StarDist2D
    .builder(modelPath)
    .normalizePercentiles(1, 99)
    .threshold(0.4)
    .pixelSize(0.45)
    .measureShape()
    .build()

selectObjectsByClassification("Tissue");
def pathObjects = QP.getSelectedObjects()

def imageData = QP.getCurrentImageData()
if (pathObjects.isEmpty()) {
    QP.getLogger().error("No parent objects are selected!")
    return
}

stardist.detectObjects(imageData, pathObjects)
stardist.close()
println('Done!')