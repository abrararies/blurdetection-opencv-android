# Blur detection with OpenCV for Android

Just a hacky chunk of code for testing blur detection algorithms.

The original python code and article by Adrian Rosebrock:<br>
https://www.pyimagesearch.com/2015/09/07/blur-detection-with-opencv/

The simplest way to test bluriness of an image is to take the laplacian with a 3*3 matrix with all entries -1 except the centre. Just take the standrad deviation of resultant matrix. Based on the value of stddev, we can comment on blurriness of an image.
