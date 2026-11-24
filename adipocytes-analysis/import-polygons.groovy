import qupath.ext.ndpa.NdpaTools;

QP.selectAnnotations();
QP.removeSelectedObjects();

var imageData = QP.getCurrentImageData();
NdpaTools.readNDPA(imageData, null);

QP.selectAnnotations();
QP.classifySelected("Trash");
QP.mergeSelectedAnnotations();
QP.deselectAll();