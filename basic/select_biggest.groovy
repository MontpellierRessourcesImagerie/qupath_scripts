// # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #
// #                              SETTINGS                                     #
// # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

import qupath.lib.objects.PathDetectionObject

// > source_annotations: List of annotations in which we are going to work.
// > percentage: Value between 0.0 and 1.0 representing a percentage.
// > use_cell: If cell expansion was used, do you want to take the area of the whole cell or only the nucleus.

def source_annotations = ["Tumor"];
def percentage = 0.05;
def use_cell = false;

// # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

// Select only the annotations of interest
QP.deselectAll();
QP.selectObjectsByClassification(*source_annotations);
def objects = QP.getSelectedObjects();
def new_class_name = "top-" + (percentage * 100).toInteger().toString() + "%";

// Iterate over each annotation and get each area in an array
def property = use_cell ? "Cell: Area µm^2" : "Area µm^2";
println("Using property: " + property);

for (PathObject o: objects) {
    def cells = o.getChildObjects();
    def values = [];
    // Accumulate areas
    for (c: cells) {
        def m = c.getMeasurementList();
        def p = m.get(property);
        println(p);
        values << p;
    }
    // Sort areas and search for the top X%
    values.sort();
    def threshold_index = (values.size() - percentage * values.size()).toInteger();
    def threshold_area = values[threshold_index];
    println("Threshold area: " + threshold_area.toString());
    for (c: cells) {
        def m = c.getMeasurementList();
        if (m.get(property) >= threshold_area) {
            c.setClassification(new_class_name);
        }
    }
}

// Deselect everything and tell the user the status
QP.deselectAll();
QP.selectObjectsByClassification(new_class_name);
println("DONE");