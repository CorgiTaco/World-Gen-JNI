cmake_minimum_required(VERSION 3.27)
project(LandMod)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_FLAGS_DEBUG "-g")
set(CMAKE_CXX_FLAGS_RELEASE "-Ofast")
set(CMAKE_CXX_FLAGS "-Wall -Wextra -static")

find_package(JNI REQUIRED)
include_directories(${JNI_INCLUDE_DIRS})

if (JNI_FOUND)
    message (STATUS "JNI_INCLUDE_DIRS=${JNI_INCLUDE_DIRS}")
    message (STATUS "JNI_LIBRARIES=${JNI_LIBRARIES}")
endif()

add_library(LandMod SHARED library.cpp
        JNIBinding.h)

add_executable(LandModExe library.cpp
        JNIBinding.h)

set(FASTNOISE2_NOISETOOL OFF CACHE BOOL "Build Noise Tool" FORCE) #example if don't need the graph tool
add_subdirectory(FastNoise2)

target_link_libraries(LandMod PUBLIC FastNoise)
target_link_libraries(LandModExe PUBLIC FastNoise)