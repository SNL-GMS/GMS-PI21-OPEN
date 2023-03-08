cmake_minimum_required(VERSION 3.20)
include(CMakePrintHelpers)

PROJECT(GMS LANGUAGES C CXX VERSION 1.0.1 DESCRIPTION "GMS Filters Provider")

SET(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_STANDARD_REQUIRED ON)
SET(GMS_LIB_FOLDER ${CMAKE_BINARY_DIR}/lib)
OPTION(BUILD_TESTING "Enable project testing" OFF)

SET(GMS_FILTER_PROVIDER_SRC
    src/CascadedFiltersParameters.hh src/CascadedFiltersParameters.cpp
    src/FilterDefinition.hh src/FilterDefinition.cpp
    src/FilterDescription.hh src/FilterDescription.cpp
    src/FilterProvider.hh src/FilterProvider.cpp
    src/IIRFilterParameters.hh src/IIRFilterParameters.cpp
    src/LinearIIRFilterDescription.hh src/LinearIIRFilterDescription.cpp)

ADD_LIBRARY(gmsfilterprovider ${GMS_FILTER_PROVIDER_SRC})
SET_TARGET_PROPERTIES(gmsfilterprovider PROPERTIES PUBLIC_HEADER ./FilterProvider.h)
SET_TARGET_PROPERTIES(gmsfilterprovider PROPERTIES VERSION ${PROJECT_VERSION})
ADD_CUSTOM_COMMAND(TARGET gmsfilterprovider POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:gmsfilterprovider> ${GMS_LIB_FOLDER}
)

if(BUILD_TESTING)
    message(STATUS "CPP tests enabled")
    add_subdirectory(./tests)
endif()

if(CMAKE_CXX_COMPILER_ID STREQUAL "Emscripten")
    SET(GMS_WASM_OUTPUT_FOLDER ${CMAKE_BINARY_DIR}/wasm)
    FILE(MAKE_DIRECTORY ${GMS_WASM_OUTPUT_FOLDER})
    ADD_CUSTOM_COMMAND(TARGET gmsfilterprovider POST_BUILD
        COMMAND em++ --bind -O3 -Wl,--whole-archive ${GMS_LIB_FOLDER}/libgmsfilterprovider.a -Wl,--no-whole-archive ${GMS_LIB_FOLDER}/libgmscfilters.a
        -o ${GMS_WASM_OUTPUT_FOLDER}/gms-filters.mjs
        -s ALLOW_MEMORY_GROWTH=1
        -s EXPORTED_FUNCTIONS='["_cFilterIIRApply","_malloc","_free"]'
        -s EXPORTED_RUNTIME_METHODS=ccall,cwrap,setValue,getValue
        -s LLD_REPORT_UNDEFINED
        -s EXPORT_NAME=gmsfilters
        # -s ASSERTIONS=1
        -s EXPORT_ES6=0
        -s USE_ES6_IMPORT_META=0
        -s MODULARIZE=1)
endif()
