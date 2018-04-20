# Duck

This is a practice project to build an android project manually without gradle or other build systems. However this project is heavily based on [android build tools](https://android.googlesource.com/platform/tools/base/+/studio-master-dev/build-system/).

Similarly to Gradle's build.gradle script, duck use a json file as android project's config file, name as `build.json`. All the dependencies needed by an android project are manually downloaded and saved to a local path, duck does nothing with dependency management, since dependency management is not an easy thing, and not the goal of this project.


### Try it:

build from source code

```
./gradlew runApp
```
or 

```
java -jar duck-1.0.jar android-project-example/build.json 
```

### Not implemented:

+ Code shrinking
+ Resource shrinking
+ Muti-dexing
+ Library project
+ Build Types
+ JNI
+ ...
