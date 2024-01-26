cmake_minimum_required(VERSION 3.22.1)
project(jabcodelib)

set(JABCODE_SRC_PATH ../../../../jabcode/src/jabcode/)

set(SOURCES
  ${JABCODE_SRC_PATH}/binarizer.c
  ${JABCODE_SRC_PATH}/decoder.c
  ${JABCODE_SRC_PATH}/detector.c
  ${JABCODE_SRC_PATH}/encoder.c
  ${JABCODE_SRC_PATH}/image.c
  ${JABCODE_SRC_PATH}/interleave.c
  ${JABCODE_SRC_PATH}/mask.c
  ${JABCODE_SRC_PATH}/ldpc.c
  ${JABCODE_SRC_PATH}/pseudo_random.c
  ${JABCODE_SRC_PATH}/sample.c
  ${JABCODE_SRC_PATH}/transform.c
  ${JABCODE_SRC_PATH}/../jabcodeReader/jabreader.c
  ${JABCODE_SRC_PATH}/../jabcodeReader/jabreader.h
  jabcodelib.c)

add_library(libpng STATIC IMPORTED)
set_target_properties(
  libpng
  PROPERTIES
  IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/../../../lib/${ANDROID_ABI}/libpng.a)

add_library(libtiff STATIC IMPORTED)
set_target_properties(
  libtiff
  PROPERTIES
  IMPORTED_LOCATION
  ${CMAKE_SOURCE_DIR}/../../../lib/${ANDROID_ABI}/libtiff.a)

add_library(${PROJECT_NAME} SHARED ${SOURCES})

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wl,-Bsymbolic")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-Bsymbolic")

target_link_libraries(${PROJECT_NAME} PRIVATE libpng libtiff z android log)

target_include_directories(${PROJECT_NAME} PRIVATE ${JABCODE_SRC_PATH}/include/)
