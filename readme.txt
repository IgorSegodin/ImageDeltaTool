Tested on
Java: 1.8
Environment: Windows 7
Gradle version: 2.12

Program shows differences between two images. Difference is highlighted with red rectangles.
Comparable images should have same size.
If comparable images have too many differences (more than 50% different pixels), comparison will be rejected.
Pixels are treated as different when one of RGB color components exceeds allowed difference delta. This delta is computed as average value between all not equal pixel components.

To zoom selected image use: CTRL + mouse wheel
To scroll horizontally use: ALT + mouse wheel



TODO Select exclude zones
TODO centrize images (when images have different sizes or offset, need to fix this offset)
