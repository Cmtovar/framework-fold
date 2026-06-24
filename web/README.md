# Web Modules

## index.html
Main framework-fold app. 4x4 grid visualization with BFS distance, WASD + chute navigation, connection drawing, and tree panel. Includes `/=0` toggle for zero-cost chute BFS.

## module.html
Chute pair viewer. Displays each chute connection as two groups of 4 columns (8 total), with fill heights from traversal order data. Sidebar for selecting between all 8 chute pairs. Spotlight advance, reset, and flat toggle.

## grid.html
6x6 expanded grid. (In progress)

## distance.html
Eight-square depth module. Presents the slot words as a receding path from
`on` in the foreground to `at` at the farthest point.
