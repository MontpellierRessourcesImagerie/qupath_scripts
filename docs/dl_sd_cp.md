# Segmentation with CellPose or StarDist

<div id="shields">
    <img class="modality fluo" src="https://img.shields.io/badge/modality-FLUO-fc8803">
    <img class="modality ihc" src="https://img.shields.io/badge/modality-IHC-fc8803">
    <a href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/tree/main/deep-learning"><img class="scripts" src="https://img.shields.io/badge/code-Groovy-6495ED?logo=github"></a>
    <img class="version" src="https://img.shields.io/badge/qupath_version-0.5.1-ffee00">
    <a style="vertical-align: top;" href="https://github.com/MontpellierRessourcesImagerie/qupath_scripts/issues"><img src='https://img.shields.io/github/issues/MontpellierRessourcesImagerie/qupath_scripts'></a>
    <img class="project" src="https://img.shields.io/badge/project-%232008-cd1818?logo=redmine">
    <img class="status" src="https://img.shields.io/badge/status-in_dev-6495ED">
</div>

-------------

<img alt="Demo" src="https://dev.mri.cnrs.fr/attachments/download/3429/sdcp-gui.gif">

## 1. Functional specifications

- The goal is to segment some objects (cells, nuclei, ...) on some 2D images (fluo or IHC) in QuPath using CellPose and/or StarDist.
- We want to prevent the final users from having to edit the settings through some Groovy code.
- We need to integrate this segmentation step into a script that can be run in batch mode ("Run for project").
- For reproducibility purposes, a history of used settings is required.

## 2. Install

### a. Install the scripts

To use these scripts, you simply need to download all the `.groovy` files from this repository and place them in the `scripts` folder within your project's folder. If the `scripts` folder doesn't exist, create it. Your project's hierarchy should look like that:

```bash
. 📁 my-project-folder/
│    ├─ 📄 project.qpproj
│    ├─ 📁 models/
│    ├─ 📁 scripts/
│    │     ├─ 📄 ask-dl-settings.groovy
│    │     ├─ 📄 launch-cpsd.groovy
│    │     ├─ 📄 segment-cellpose.groovy
│    │     ├─ 📄 segment-stardist.groovy
│    │     ├─ 📄 workflow.groovy
```

The `workflow.groovy` is just a basic template containing the skeleton of a script launching the segmentation process, it is not required.

### b. Install CellPose

Before being able to use CellPose from QuPath, don't forget to install the Python package `cellpose` with `pip`, and the bridge between QuPath and the CellPose server.

- [CellPose module](https://pypi.org/project/cellpose/)
- [CellPose-QuPath bridge](https://github.com/BIOP/qupath-extension-cellpose)

### c. Install StarDist

Before being able to use StarDist from QuPath, don't forget to install the bridge between QuPath and StarDist.

- [StarDist-QuPath bridge](https://github.com/qupath/qupath-extension-stardist)

## 3. Use the settings prompt

This script, which can be found under the name `ask-dl-settings` in Automate > Project scripts, opens a prompt asking for the settings you want to run CellPose or StarDist with.
It allows you to set up common settings for both CellPose and StarDist, giving you control in either case.
One goal is to run our workflow in batch mode without a prompt appearing for every image. For this purpose, this script generates a JSON file with your chosen settings and saves it in the project's folder.
Once you press "OK" on the window this script opens, your future runs of CellPose or StarDist will use these settings.
Then, you can integrate the call to StarDist or CellPose in your workflow without worrying about the settings.
You can reuse the generated file in any other project or even make it available for other users.
The available settings are the following:

- `Channel to segment`: In the case of fluorescence, it is the channel on which our objects are. Otherwise, for Brightfield (H-DAB, H&E, ...), the three channels (red, green, and blue) are used.
- `Input annotation(s)`: Annotations in which our objects of interest are. If you choose a class name within the list, we will only segment the annotations having that class. Otherwise, you have some other choices:
    - `:: Active annotation`: Segment only within the active annotation.
    - `:: Full image`: Creates a rectangle annotation over the whole image, makes it active, and runs the segmentation.
    - `:: All annotations`: Will segment the objects within every annotation.
- `Network`: The network to use is either CellPose or StarDist.
- `Model`: The model that we use for the selected network. The list includes the basic models for this network and the models within the "models" folder in the project's folder. If you have a custom model for StarDist, place it in `models/stardist`; for CellPose, in `models/cellpose`.
- `Normalization percentile`: If you choose the value α for this setting, the global image normalization will be between (α, 100.0-α).
- `Use cell expansion?`: In the case where we segment nuclei without cytoplasm staining, using this option will take each nucleus polygon and expand it on a certain distance to roughly estimate the area of the whole cell.
- `Expansion distance (µm)`: Distance on which the nuclei polygons will grow to turn them into cytoplasm polygons.
- `Classify results as`: Each segmented object will receive your chosen class here. If you leave it blank, the results won't be of any specific class.
- `Create annotations?`: It allows us to choose whether we should create annotations or detections.
- `Median cell diameter (µm)`: This parameter only affects CellPose. You can use the ruler in the viewer's lower-left corner to help estimate this size.
- `Save settings as`: Name that will receive the file containing the settings within the project's folder. It must only contain letters, numbers, dashes, and underscores.

## 4. Use the launcher in a workflow

### a. What is it?

The launcher will try to access and extract the settings from any `cnrs-mri-cia-cpsd.XXX.json` file that it will find in the project's directory. Then, it will pass them over to a worker and run it. Here, `XXX` corresponds to the name that you provided in the `Save settings as` field. The launcher is shared by both CellPose and StarDist; however, the workers are specialized.

### b. How to use the launcher

Usually, segmenting your objects is only a fragment of your project. You will certainly want to measure some features in what you segmented, count your objects or anything else.
To execute your workflow, you will very likely want to create a script and run it for all the images in your project.

From this point, we consider that you found the correct settings with the settings prompt script.

To integrate the segmentation to your workflow, you can use either `workflow.groovy` or the following snippet:

```java
// At the very begining of your script:
import groovy.lang.GroovyShell;
import groovy.lang.Binding;

// First step of your workflow:
// ...

// Second step of your workflow:
// ...

// Time to segment your objects
// Don't forget to replace "XXX" by the name of your settings file
Binding binding = new Binding();
binding.setVariable("settings_name", "XXX");
GroovyShell shell = new GroovyShell(this.class.classLoader, binding);
shell.evaluate(QP.getProject().getScripts().get("launch-cpsd"));

// Following steps of your workflow:
// ...
```


## Tags

<span class="script_tag">CellPose</span>
<span class="script_tag">StarDist</span>
<span class="script_tag">Deep-learning</span>
<span class="script_tag">Segmentation</span>
<span class="script_tag">Nuclei</span>
<span class="script_tag">Batch</span>
<span class="script_tag">GUI</span>
<span class="script_tag">Cells</span>