cmake_minimum_required(VERSION 3.20)
include(CMakePrintHelpers)

PROJECT(GMS LANGUAGES C CXX VERSION 1.0.1 DESCRIPTION "GMS COMMON C/C++ Code")

SET(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_STANDARD_REQUIRED ON)
SET(GMS_OUTPUT_FOLDER ${CMAKE_BINARY_DIR}/lib/)
FILE(MAKE_DIRECTORY ${GMS_OUTPUT_FOLDER})

option(BUILD_TESTING "Enable project testing" ON)

if(CMAKE_CXX_COMPILER MATCHES "/em\\+\\+(-[a-zA-Z0-9.])?$")
    SET(emscripten_dir "/opt/emsdk/")
    message(" * C++ compiler: Emscripten")
    set(CMAKE_CXX_COMPILER_ID "Emscripten")
else()
    message(" * C++ compiler: ${CMAKE_CXX_COMPILER_ID}")
endif()

INCLUDE_DIRECTORIES(${PROJECT_SOURCE_DIR}/gms/gms-filters/src)
add_subdirectory(gms/gms-filters)
add_subdirectory(gms/gms-filters-provider)