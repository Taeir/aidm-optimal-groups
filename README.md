# aidm-optimal-groups
Algorithms for Intelligent Decision Making - project for making "optimal" student groups &amp; project matching

# Setup / Install
This project depends on Google OR-Tools.
Unfortunately, the tool itself is a .so / .dll c++ compiled library and interfacing with it happens through JNI.

Maven contains the Google OR-Tools Java wrapper but is not packaged together with the tool itself. Therefore, you need to do some minimal setup:

Please download the binary distribution for your platform here: https://developers.google.com/optimization/install/#binary

Then, make a 'lib' directory in the root of this repository and place the 'com.google.ortools.jar' file in the 'lib' directory of the downloaded archive therein.

## Running with IntelliJ
Add `-Djava.library.path=lib` (or an absolute/relative path to where you've extracted the binaries to) to the 'VM options' of your run configuration.

If you don't have a run configuration yet, simply click the green run arrow left of the `void main(String[] args)` function. IntelliJ will create a corresponding configuration and use it to run the application.
It will probably crash without the abovementioned addition to the vm options as it cannot find the necessary libraries. But now you can make the change and run.

## Gradle
It is possible to configure this through grade. However, even through my IntelliJ delegates run/debug to gradle, it didn't on my machine. Hence: future todo. 