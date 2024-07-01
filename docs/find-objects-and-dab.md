# Find objects and measure DAB

<div id="shields">
    <img class="modality ihc" src="https://img.shields.io/badge/modality-IHC-fc8803">
    <a href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/tree/main/find-objects-and-dab"><img class="scripts" src="https://img.shields.io/badge/code-Groovy-6495ED?logo=github"></a>
    <img class="version" src="https://img.shields.io/badge/qupath_version-0.5.1-ffee00">
    <a style="vertical-align: top;" href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/issues"><img src='https://img.shields.io/github/issues/MontpellierRessourcesImagerie/qupath_scripts'></a>
    <img class="project" src="https://img.shields.io/badge/project-%232018-cd1818?logo=redmine">
    <img class="status" src="https://img.shields.io/badge/status-deployed-6495ED">
</div>

-------------

<img alt="Demo" src="https://dev.mri.cnrs.fr/attachments/download/3405/find-obj-dab.gif">

## 1. What is it?

- On each slide, several different organs are present, not touching each other.
- The `find_objects_and_dab.groovy` script was created to find and create an independent annotation for every organ on each slide. Then, it runs a pixel classifier into each newly created annotation to measure the DAB-positive area.
- Since the total area of each annotation (== the total area of each organ) is known, we just have to use a spreadsheet software to process the ratio (area DAB / total area) to have the percentage of positivity within an organ.

## 2. How to use this script

Before starting don't forget to do the color deconvolution of your images ("Estimate stain vectors") and to apply the same one to all your batch.

### a. Use the script

- We make the assumption that pixel classifiers named `find-objects` and `find-dab` are available in your project.
- Open the script and adjust the settings:
    - `_SMALLEST_HOLE`: Area of the smallest hole. Any hole smaller than this area in the detected objects will be filled.
    - `_SMALLEST_OBJECT`: Area of the smallest object. Anything smaller will be considered as some debris and be discarded.
    - `_TARGET_CLASS`: Class predicted by your `find-objects` classifier, that will also be the class of the new annotations.
- Run the script for the project, it will produce an annotation this the class `_TARGET_CLASS` for each object.
- Take a look at every slide to remove every debris that my have been caught.
- You can now find a "find DAB: positive area" measurement in every annotation.

### b. Export measures

- Create new classes for every type of organ you have (liver, lung, kidney, ...)
- Replace the `_TARGET_CLASS` class by the class corresponding to the correct organ.
- Now, it is possible to go in `Measure` > `Export measurements`.
- Select the correct images, choose your output path, in 'export type' use 'Annotation'. Press the `Export` button.
- In your TSV, you have a column containing the class of each ROI (liver, lung, ...), a column with the total area of the organ, and the area of positivity to the DAB staining.



## Tags

<span class="script_tag">IHC</span>
<span class="script_tag">Find objects</span>
<span class="script_tag">Instances</span>
<span class="script_tag">H-DAB</span>
<span class="script_tag">Measure</span>
<span class="script_tag">Positive area</span>