guiscript=true
setImageType('FLUORESCENCE');
setChannelNames(
     'DAPI',
     'CD44v6',
     'Ki67',
);
def pathClasses = getQuPath().getAvailablePathClasses()
print(pathClasses.getClass())
listOfClasses = [
getPathClass("DAPI",ColorTools.makeRGB(0,0,255)),
getPathClass("Vimentin",ColorTools.makeRGB(0,255,0)),
getPathClass("PTK7",ColorTools.makeRGB(255,0,0))
]
pathClasses.addAll(listOfClasses)
createFullImageAnnotation(true) 
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', 
    '{"detectionImage":"DAPI", \
    "requestedPixelSizeMicrons":0.5, \
    "backgroundRadiusMicrons":8.0, \
    "backgroundByReconstruction":true, \
    "medianRadiusMicrons":0.0, \
    "sigmaMicrons":1.5, \
    "minAreaMicrons":10.0, \
    "maxAreaMicrons":400.0, \
    "threshold":100.0, \
    "watershedPostProcess":true, \
    "cellExpansionMicrons":5.0, \
    "includeNuclei":true, \
    "smoothBoundaries":true, \
    "makeMeasurements":true}')


runObjectClassifier("composite_CD44_Ki67")
