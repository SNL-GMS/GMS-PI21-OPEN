# wasm-idc-filters

## Requirements

* [Clang](https://clang.llvm.org/)
* [CMAKE => 3.20](https://cmake.org/)
* [OpenJDK => 11](https://adoptopenjdk.net/)
* [Emscripten's SDK](https://emscripten.org/docs/getting_started/downloads.html)
* [NodeJS >= 16](https://nodejs.org/en/)

## Steps to building the project (C/C++)

* Download the source
* Create a build folder (cmake-build) in the root project directory
* Change directory to the (cmake-build) director
* Execute cmake

  `cmake ..`
* Execute make

  `make`
* Make & run tests

  `make test`

## Steps to building the project (WASM)

* Download the source
* Create a build folder (emcmake-build) in the root project directory
* Ensure that you have the emsdk profile loaded into BASH
  
  `source {PATH_TO_EMSDK_DIR}/emsdk_env.sh`
* Execute the emscripten version of cmake

    `emcmake cmake ..`
* Execute the emscripten version of make & run tests

    `emmake make`

## Steps to building the project (JNI)

* Download the source
* Open the 'jni' folder in your Java IDE
* Run the Gradle build
* Run the tests

## Steps to building the project (JNA)

* Download the source
* Open the 'jna' folder in your Java IDE
* Run the Gradle build
* Run the tests