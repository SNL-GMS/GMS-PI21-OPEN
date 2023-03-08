cmake_minimum_required(VERSION 3.20)
include(CMakePrintHelpers)

PROJECT(GMS_CFILTERS LANGUAGES C VERSION 1.0.1 DESCRIPTION "GMS FILTERS")
SET(GMS_CFILTERS_SRC
    src/constants.h src/enums.h
    src/gms_filter.h src/gms_filter.c
    src/filter_iir.c src/filter_iir.h
)
SET(GMS_LIB_FOLDER ${CMAKE_BINARY_DIR}/lib/)
SET(GMS_INCLUDE_FOLDER ${CMAKE_BINARY_DIR}/include/)
ADD_LIBRARY(gmscfilters ${GMS_CFILTERS_SRC})
SET_TARGET_PROPERTIES(gmscfilters PROPERTIES PUBLIC_HEADER gms_filter.h)
SET_TARGET_PROPERTIES(gmscfilters PROPERTIES VERSION ${PROJECT_VERSION})

ADD_CUSTOM_COMMAND(TARGET gmscfilters POST_BUILD
    COMMAND ${CMAKE_COMMAND} -E copy $<TARGET_FILE:gmscfilters> ${GMS_LIB_FOLDER}
)
