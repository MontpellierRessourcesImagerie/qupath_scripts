import java.io.File
/**
 * Segment connected components and export them as images and also cut them into 
 * a number of tiles and export the tiles as individual images.
 * 
 */
MIN_SIZE = 10000.0
THRESHOLD = 0.0
def downsample = 1.0
def tileLength = 1850
def removeEmptyTiles = true


def main(tileLength, downsample, removeEmptyTiles) {
    createSectionClass()
    createClassifier()
    def name = getCurrentImageNameWithoutExtension()
    if (name.contains("label image")) return "skipping image ${name}"
    setImageType('FLUORESCENCE')
    clearAllObjects()
    createAnnotationsFromPixelClassifier("segment_sections", MIN_SIZE, THRESHOLD, "SPLIT")
    def dir = buildPathInProject("export")
    mkdirs(dir)
    def server = getCurrentServer()
    def annotations = getAnnotationObjects()
    annotations.eachWithIndex {annotation, index ->
        print("Processing region ${index+1} of ${annotations.size()}")
        def request = RegionRequest.createInstance(server.getPath(), downsample, annotation.getROI())
        def outputName = "${name}-${request.x} [${request.y},${request.width},${request.height}].tif"
        def path = buildFilePath(dir, outputName)
        writeImageRegion(server, request, path)
        exportTiles(name, dir, request, server, downsample, tileLength, removeEmptyTiles)
    }
    return "Export tiles from ${name} finished."
}


def exportTiles(name, dir, request, server, downsample, tileLength, removeEmptyTiles) {
    def outDir = buildFilePath(dir, "tiles")
    mkdirs(outDir)
    def x = request.getX()
    def y = request.getY()
    def w = request.getWidth()
    def h = request.getHeight()
    def numberOfColumns = w.intdiv(tileLength)
    def numberOfRows = h.intdiv(tileLength)
    def tileWidth = Math.round(w / numberOfColumns)
    def tileHeight = Math.round(h / numberOfRows)
    for (def row=0; row<numberOfRows; row++) {
        for (def column=0; column<numberOfColumns; column++) {
            def actualTileHeight = tileHeight
            def actualTileWidth = tileWidth
            if (row == numberOfRows - 1) {
                actualTileHeight = h - (row * tileHeight)
            }
            if (column == numberOfColumns - 1) {
                actualTileWidth = w - (column * tileWidth)
            }
            def tileRequest = RegionRequest.createInstance(
                server.getPath(),
                downsample,   
                (x + (column * tileWidth)).intValue(), 
                y + (row * tileHeight).intValue(),
                actualTileWidth.intValue(),
                actualTileHeight.intValue()
            )
            def imp = IJTools.convertToImagePlus(server, tileRequest).getImage()           
            def intensityAtCenter = imp.getPixel(
                                        (imp.getWidth() / 2).intValue(), 
                                        (imp.getHeight() / 2).intValue())
            if (intensityAtCenter[0]==0) {
                continue
            }
            def outputName = "${name}-${request.x} [${request.y},${request.width},${request.height}]_tile_${column+1}-${row+1}.tif"
            def path = buildFilePath(outDir, outputName)
            writeImageRegion(server, tileRequest, path)
        }
    }
}


def createSectionClass() {
    def pathClasses = getQuPath().getAvailablePathClasses()
    def names = pathClasses.collect{it.toString()}
    if (!names.contains('Section')) {
        sectionClass = getPathClass("Section", ColorTools.makeRGB(226,98,155))
        pathClasses.addAll([sectionClass])
        print("Class Section created!")
    } else {
        print("Class Section found, good!")
    }
}


def getClassifierJSON() {
    classifierJSON = '''{ 
  "pixel_classifier_type": "OpenCVPixelClassifier", 
  "metadata": { 
    "inputPadding": 0, 
    "inputResolution": { 
      "pixelWidth": { 
        "value": 1.3, 
        "unit": "µm" 
      }, 
      "pixelHeight": { 
        "value": 1.3, 
        "unit": "µm" 
      }, 
      "zSpacing": { 
        "value": 1.0, 
        "unit": "z-slice" 
      }, 
      "timeUnit": "SECONDS", 
      "timepoints": [] 
    }, 
    "inputWidth": 512, 
    "inputHeight": 512, 
    "inputNumChannels": 3, 
    "outputType": "CLASSIFICATION", 
    "outputChannels": [], 
    "classificationLabels": { 
      "1": { 
        "name": "Section", 
        "color": [ 
          226, 
          98, 
          155 
        ] 
      } 
    } 
  }, 
  "op": { 
    "type": "data.op.channels", 
    "colorTransforms": [ 
      { 
        "combineType": "MINIMUM" 
      } 
    ], 
    "op": { 
      "type": "op.threshold.constant", 
      "thresholds": [ 
        0.01 
      ] 
    } 
  } 
}'''    
    return classifierJSON
}


def createClassifier() {
    classifierJSON = getClassifierJSON()
    def dir = buildPathInProject("classifiers/pixel_classifiers")
    mkdirs(dir)
    def outPath = buildPathInProject("classifiers/pixel_classifiers/segment_sections.json")
    classifierFile = new File(outPath)
    if (classifierFile.exists()) return false
    classifierFile.write(classifierJSON)
    return true
}


main(tileLength, downsample, removeEmptyTiles)
