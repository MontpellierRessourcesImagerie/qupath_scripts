# Find objects and DAB

<div id="shields">
    <img class="modality ihc" src="https://img.shields.io/badge/modality-IHC-fc8803">
    <a href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/tree/main/find-objects-and-dab"><img class="scripts" src="https://img.shields.io/badge/code-Groovy-6495ED?logo=github"></a>
    <img class="version" src="https://img.shields.io/badge/qupath_version-0.5.1-ffee00">
    <a style="vertical-align: top;" href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/issues"><img src='https://img.shields.io/github/issues/MontpellierRessourcesImagerie/qupath_scripts'></a>
    <img class="project" src="https://img.shields.io/badge/project-%232018-cd1818?logo=redmine">
    <img class="status" src="https://img.shields.io/badge/status-deployed-6495ED">
</div>

-------------

## 1. What is it?

This pair of scripts was created to work on some H-DAB images.
On each slide, several different organs are present, not touching each other.
The first script (`find-objects.groovy`) was created to find and create an independent annotation for every organ on each slide.
The second one (`find-dab.groovy`) takes the annotations produced by the first script and measures the area (in µm²) positive to DAB.
Since the total area of each annotation (== the total area of each organ) is known, we just have to use a spreadsheet software to process the ratio (area DAB / total area) to have the percentage of positivity within an organ.

## 2. How to use these scripts

Before starting don't forget to do the color deconvolution of your images (Estimate stain vectors) and to apply the same one to all your batch. The default ones are certainly wrong as they are based on the whole image, and are re-processed for every image.

### a. First script: find-objects

- Makes the assumption that a pixel classifier named `find-objects` is available in your project.
- Open it in your script editor, tune the settings for the **smallest hole** and the **smallest object**.
- Run the script for the project, it will produce an `Organ` annotation for each object.
- Take a look at every slide to remove every debris that my have been caught.

### b. Second script: find-dab

- Makes the assumption that a pixel classifier named `find-dab` is available in your project.
- Open it in your script editor, and run it for the whole project.
- Now, each annotation has an area of positivity to the DAB stain.

### c. Last phase

- Create new classes for every type of organ you have (liver, lung, kidney, ...)
- Replace the `Organ` class by the class corresponding to the correct organ.
- Now, it is possible to go in `Measure` > `Export measurements`.
- Select the correct images, choose your output path, in 'export type' use 'Annotation'. Press the `Export` button.
- In your TSV, you have a column containing the class of each ROI (liver, lung, ...), a column with the total area of the organ, and the area of positivity to the DAB staining.
