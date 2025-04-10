# Export Tiles

<img src='https://github.com/user-attachments/assets/0b7c4f1d-8b77-463c-8d6f-9b600a5268c2' height='200' align='right'/>

The script will export the unconnected regions of each image as one image and also cut each region into a number of tiles and export them.

Create a QuPath project and import your images into it. Download the script [export_tiles.groovy](https://raw.githubusercontent.com/MontpellierRessourcesImagerie/qupath_scripts/refs/heads/main/export/export_tiles.groovy) and open it in the Script Editor of QuPath. Run the script with the command ``Run>Run for Project`` from the menu of the script editor.

The results will be in the subfolders ``export/`` and ``export/tiles/``of the project folder. 

## Options

<dl>
  <dt>MIN_SIZE = 10000.0</dt>
<dd>The minimum size of a connected region.</dd>

<dt>THRESHOLD = 0.0</dt>
<dd>The threshold value used to crate annotations with the pixel classfier.</dd>

<dt>def downsample = 1.0</dt>
<dd>The downsample factor. Should be 1.</dd>

<dt>def tileLength = 1850</dt>
<dd>The approximative tile length. It is used to determine how many rows and columns there are.</dd> 

<dt>def removeEmptyTiles = true</dt>
<dd>If true empty tiles will not be exported.</dd>
</dl>
