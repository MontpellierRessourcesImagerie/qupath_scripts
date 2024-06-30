# Segmentation with CellPose or StarDist

<div id="shields">
    <img class="modality fluo" src="https://img.shields.io/badge/modality-FLUO-fc8803">
    <img class="modality ihc" src="https://img.shields.io/badge/modality-IHC-fc8803">
    <a href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/tree/main/deep-learning"><img class="scripts" src="https://img.shields.io/badge/code-Groovy-6495ED?logo=github"></a>
    <img class="version" src="https://img.shields.io/badge/qupath_version-0.5.1-ffee00">
    <a style="vertical-align: top;" href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/issues"><img src='https://img.shields.io/github/issues/MontpellierRessourcesImagerie/qupath_scripts'></a>
    <img class="project" src="https://img.shields.io/badge/project-%232008-cd1818?logo=redmine">
    <img class="category" src="https://img.shields.io/badge/category-Segmentation-b503fc">
    <img class="status" src="https://img.shields.io/badge/status-in_dev-6495ED">
</div>

-------------

## 1. Problematic

- The goal is to segment cells, nuclei or blob-like shapes on some 2D image (fluo or IHC) in QuPath.
- A fine control is required to choose which annotations we will segment objects into.
- The users don't have to deal with the Groovy code for the settings.
- A solution that still allows us to use the batch mode.

## 2. Use the settings prompt

This script opens a prompt asking you for the settings you want to run CellPose or StarDist with.
Much settings are common to both networks, so this prompt can be used in either case.
However, we still want to be able to run our workflow in batch mode without having a prompt poping for every image.
So this script simply generates a JSON file with the settings you chose, and saves it in the project's folder.
Once this script was run, the last settings you provided will be used every time you call CellPose or StarDist.
Then, you can simply integrate the call to StarDist or CellPose in your worflow, without worrying about the settings.
The available settings are the following:

- `Channel to segment`: The channel on which are the elements that you want to segment (ex: DAPI for nuclei, ...). In case of IHC, this will be the stain marking what you are interested in.
- `Class to segment`: The segmentation is always run within an annotation, never globaly. If you have several annotations in your image, but want to run the segmentation only on some of them, give it a special class and indicate it in this field. (ex: We have a slide on which we have a slice of liver, a slice of heart and a slice of brain. Each organ was previously segmented and given the correct class. At this point we have an annotation around the liver with the class "Liver", an annotation around the heart with the class "Heart", ... However, we are only interested in what is going on in the liver. If we run the segmentation right now, all the nuclei in our organs will be segmented without any difference, but if we give the class "Liver" to the field `Class to segment`, only nuclei in the liver will be segmented.)
- `Model to use`: Model used (network's weights) to segment your image. For StarDist, they have to be located in a folder named `models` in your project's folder. For both CellPose and StarDist, if you want to use a custom model, it has to be in the `models` folder.
- `Normalization percentage`: placeholder
- `Use cell expansion?`: If only your nuclei are stained but you want to measure some properties in the entire cells, you can you cell expansion. Basically, the nuclei will be segmented on your image and will be dilated of a given distance to generate the cytoplasm. This approach relies only on the dilation distance and the collisions, the pixel values are not used to deduce the cytoplasm.
- `Expansion distance`: Only useful if the previous checkbox is ticked. It is the distance by which your nucleus will grow in every direction to try to deduce the boundaries of the cytoplasm.
- `Classify as...`: The class that every segmented element will receive.
- `Create annotations?`: placeholder.
- `Median cell diameter`: Median diameter of the object (not necessarily a cell) that you want to segment. You use the ruler in the bottom-left of the screen to try to approximate this value. (Note: Only CellPose uses this parameter).

## 3. Launch a worker

The workers will try to read and used the values stored in `segmentation-settings.json` located in the project's folder.

### a. Use CellPose

\note 
- CellPose depends on a Python runner. You will need a Python environment with CellPose installed (`pip install cellpose`) to run the corresponding script. Don't forget to indicate the Python's path into your QuPath settings (Edit > Preferences > CellPose).
- CellPose requires you to download and install the '[qupath-extension-cellpose](https://github.com/BIOP/qupath-extension-cellpose/releases)' plugin, which you can do by downloading the most recent ".jar" file and drag-n-droping it in the QuPath window.

If you validated the settings in the previous step, you can simply run the `segment-cellpose.groovy` script.


### b. Use StarDist

\note 
- StarDist's models are not bundled in the JAR and are not automatically downloaded either (contrary to CellPose), so don't forget to download the models you need (.pb files) from the [qupath-stardist repository](https://github.com/qupath/models/tree/main/stardist). The models have to be placed in a folder named `models` in your project's folder.
- StarDist requires you to download and install the '[qupath-extension-stardist](https://github.com/qupath/qupath-extension-stardist/releases)' plugin, which you can do by downloading the most recent ".jar" file and drag-n-droping it in the QuPath window.

If you validated the settings in the previous step, you can simply run the `segment-stardist.groovy` script.


## TO-DO

- Finish the StarDist worker.
- Add an option "Run everywhere" to automatically create a rectangle taking the whole image and run the segmentation in there.
- Add a check for the settings file in the workers.


## Tags

<span class="script_tag">CellPose</span>
<span class="script_tag">StarDist</span>
<span class="script_tag">Deep-learning</span>
<span class="script_tag">Segmentation</span>
<span class="script_tag">Nuclei</span>
<span class="script_tag">Batch</span>