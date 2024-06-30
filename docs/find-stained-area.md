# Measure stained areas

<div id="shields">
    <img class="modality fluo" src="https://img.shields.io/badge/modality-FLUO-fc8803">
    <a href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/tree/main/find-stained-area"><img class="scripts" src="https://img.shields.io/badge/code-Groovy-6495ED?logo=github"></a>
    <img class="version" src="https://img.shields.io/badge/qupath_version-0.5.1-ffee00">
    <a style="vertical-align: top;" href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/issues"><img src='https://img.shields.io/github/issues/MontpellierRessourcesImagerie/qupath_scripts'></a>
    <img class="project" src="https://img.shields.io/badge/project-%231994-cd1818?logo=redmine">
    <img class="status" src="https://img.shields.io/badge/status-deployed-6495ED">
</div>

-------------

## 1. Problems

- Only tested on fluo images.
- We want to measure (in µm²) the area covered by every stain on the whole image (== the positive area on each channel).
- Using a pixel classifier by stain (== by channel), we will process an area per channel.
- These measures will be attached to an annotation covering the whole image (automatically generated).

## 2. How does it work?

- This script starts by creating a rectangle annotation taking the whole image.
- Then it runs a collection of pixel classifier. Each of these classifiers produces an area of positivity to something, saved in µm².
- The result of the classifier is attached to the annotation, so you can visualize it by going in the `Annotations` tab and clicking on the annotation.
- Once you will export the results, you will have a column of area for each element to locate, and the area of the whole rectangular annotation. If you are interested in a percentage of positivity, you can simply divide the area of positivity by the area of the global annotation.

## 3. How to use it?

1. Make sure that all your classifiers are in the `classifiers` folder located in the folder of your QuPath project.
2. Take the `stain-per-area.groovy` script provided here and move it in the `scripts` folder located in your QuPath project (along side the `classifiers` folder). If it doesn't exist, create it.
3. In Qupath, go in `Automate` > `Project scripts`. The script should appear in the list and you can click on it. The script editor should show up, displaying the script.
4. In the script, locate the line looking like `def classifiers = [...];`.
5. This is the list of classifiers to apply to your images. This list can have an arbitrary size, from 1 to N. This list must start with a square bracket (`[`), contain only strings (values starting and finishing with `"`) and the values must be coma-separated. Eventually, close the list with a square bracket (`]`).
6. To find the name of your classifiers, you can get back in the `classifiers` folder of your project, in the `pixel_classifiers` sub-folder, you will find all the classifiers available for this project. To use it in the list, you must copy the name without its `.json` extension. For example, if you find `my-classifier.json` in this folder, you can add `"my-classifier"` to your list.
7. Save by using [Ctrl]+[S].
8. In the bottom right corner of the script editor, you will find a `Run` button. On its right, there are three little dots. In the menu, you can click on `Run for project` to apply the script to each image.
9. To export the results (aggregated in a unique table for the whole project), you can go in `Measure` > `Export measures`. Once again, indicate all the images you are interested in. In the field `Export type`, choose `Annotation`.
10. The produced file is a TSV (Tab Separated Values) so don't forget to indicate that in your spreadsheet software when you will open it.
