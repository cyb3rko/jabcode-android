cmake_minimum_required(VERSION 3.22.1)
project(jabcodelib)

set(JABCODE_SRC_PATH ${CMAKE_SOURCE_DIR}/native/src/)
set(JABCODE_EXTENSIONS_PATH ${CMAKE_SOURCE_DIR}/src/main/cpp/)
set(JABCODE_LIBS_PATH ${CMAKE_SOURCE_DIR}/native-libs/)

set(SOURCES
  ${JABCODE_SRC_PATH}/jabcode/binarizer.c
  ${JABCODE_SRC_PATH}/jabcode/decoder.c
  ${JABCODE_SRC_PATH}/jabcode/detector.c
  ${JABCODE_SRC_PATH}/jabcode/encoder.c
  ${JABCODE_SRC_PATH}/jabcode/image.c
  ${JABCODE_SRC_PATH}/jabcode/interleave.c
  ${JABCODE_SRC_PATH}/jabcode/ldpc.c
  ${JABCODE_SRC_PATH}/jabcode/mask.c
  ${JABCODE_SRC_PATH}/jabcode/pseudo_random.c
  ${JABCODE_SRC_PATH}/jabcode/sample.c
  ${JABCODE_SRC_PATH}/jabcode/transform.c
  ${JABCODE_SRC_PATH}/jabcodeReader/jabreader.c
  ${JABCODE_EXTENSIONS_PATH}/jabcodelib.c
  ${JABCODE_EXTENSIONS_PATH}/jabreader.h
)

add_library(libpng STATIC IMPORTED)
set_target_properties(
  libpng
  PROPERTIES
  IMPORTED_LOCATION
  ${JABCODE_LIBS_PATH}/${ANDROID_ABI}/libpng.a
)

add_library(libtiff STATIC IMPORTED)
set_target_properties(
  libtiff
  PROPERTIES
  IMPORTED_LOCATION
  ${JABCODE_LIBS_PATH}/${ANDROID_ABI}/libtiff.a
)

add_library(${PROJECT_NAME} SHARED
  ${SOURCES}
)

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -Wl,-Bsymbolic")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wl,-Bsymbolic")

target_link_libraries(${PROJECT_NAME} PRIVATE
  android
  log
  libpng
  libtiff
  z
)

target_include_directories(${PROJECT_NAME} PRIVATE
  ${JABCODE_SRC_PATH}/jabcode/include/
)
