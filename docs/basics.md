# Basic scripts for QuPath

## 1. Filter detection by area

- Using the script `select_biggest.groovy`, you can filter the nuclei by area to keep the top N% biggest.
- To use the script, you just have to open it in your script editor, edit the settings and run it.
- The settings are as follows:
  - Line 11 (*source_annotations*): The list of polygons into which you have your detections. For example, if you segmented some nuclei in polygons of tumor and want to filter them, you should have `["Tumor"]` as value. If you have several types of polygons, you can add as many classes as you need `["Tumor", "MySuperClass"]`.
  - Line 12 (*percentage*): The percentage of objects that will be conserved after they were sorted by area. Percentage is a number between 0 (0.00%) and 1 (100.0%).
  - Line 13 (*use_cell*): If your detections are cells (expanded nuclei) you can pass this to true to base the area on the cell's area rather than the nucleus' area.
- After the script execution is over, the biggest nuclei will have a new class representing their status of "top-N%".

## 2. Multiplex classification 
