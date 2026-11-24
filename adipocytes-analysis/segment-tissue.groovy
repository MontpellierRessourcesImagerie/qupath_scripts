def checkName(candidate) {
    return candidate ==~ /C?[0-9]{5}/
}

def parseName() {
    def name = QP.getCurrentImageName()
    def splitDate = " - "
    if (!name.contains(splitDate)) {
        print("Date not found.")
        return null
    }
    name = name.split(java.util.regex.Pattern.quote(splitDate))[0]
    def chunks = name.split(/\s*\+\s*/)*.trim()   // "40836+40866" marche aussi
    for (c in chunks) {
        if (!checkName(c)) {
            print("Invalid chunk: " + c)
            return null
        }
    }
    return chunks
}

def renameIslands(name_chunks) {
    if (name_chunks == null) { return; }
    def im_width = QP.getCurrentServer().getWidth();
    QP.deselectAll();
    QP.selectObjectsByClassification("Organ");
    def objs = QP.getSelectedObjects();
    for (obj: objs) {
        def r = obj.getROI();
        def x = r.getCentroidX();
        def rel = x / im_width;
        def idx = (int)Math.floor((float)name_chunks.size() * rel);
        obj.setName(name_chunks[idx]);
    }
}

def subtract(classToSubtract, classMain, removeOriginal) {
    def annoMain = getAnnotationObjects().find{it.getPathClass() == getPathClass(classMain)}
    def annoToSubtract = getAnnotationObjects().findAll{it.getPathClass() == getPathClass(classToSubtract)}
    
    def roiMain = annoMain.getROI()
    def roisToSubtract = RoiTools.union(annoToSubtract.collect{it.getROI()})
    
    def subtractedROI = RoiTools.combineROIs(roiMain, roisToSubtract, RoiTools.CombineOp.SUBTRACT)
    def subtractedObj = PathObjects.createAnnotationObject(subtractedROI, annoMain.getPathClass())
    addObject(subtractedObj)
    
    if (removeOriginal) {
        QP.removeObjects(annoMain)
        QP.removeObjects(annoToSubtract)
    }
}

def find_target_areas() {
    def trash_classes = ["Trash", "Muscle", "Blood"];
    
    // Find tissue on the whole slide
    resetSelection();
    createAnnotationsFromPixelClassifier("find-organ", 0, 0);
    
    // Fill holes and break bridges
    def rad = 45;
    def op1 = (1.0 * rad).toString();
    def op2 = (-2.0 * rad).toString();
    
    selectObjectsByClassification("Organ");
    simplifySelectedAnnotations(10.0)
    
    selectObjectsByClassification("Organ");
    runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":'+op1+',"lineCap":"ROUND","removeInterior":false,"constrainToParent":true}')
    removeSelectedObjects()
    
    selectObjectsByClassification("Organ");
    runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":'+op2+',"lineCap":"ROUND","removeInterior":false,"constrainToParent":true}')
    removeSelectedObjects()
    
    selectObjectsByClassification("Organ");
    runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":'+op1+',"lineCap":"ROUND","removeInterior":false,"constrainToParent":true}')
    removeSelectedObjects()
    
    // Remove trash and undesired areas
    for (t: trash_classes) {
        resetSelection();
        selectObjectsByClassification(t);
        runPlugin('qupath.lib.plugins.objects.DilateAnnotationPlugin', '{"radiusMicrons":10.0,"lineCap":"ROUND","removeInterior":false,"constrainToParent":true}')
        removeSelectedObjects()
        subtract(t, "Organ", true);
    }
    
    // Split polygons
    selectObjectsByClassification("Organ");
    runPlugin('qupath.lib.plugins.objects.SplitAnnotationsPlugin', '{}');
    
    // Remove debris
    def min_size = 400000;
    selectObjectsByClassification("Organ");
    runPlugin('qupath.lib.plugins.objects.RefineAnnotationsPlugin', '{"minFragmentSizeMicrons":' + min_size.toString() + ',"maxHoleSizeMicrons":0.0}')
    
    // Rename islands according to X positions
    def names = parseName();
    renameIslands(names);
}

find_target_areas();



















