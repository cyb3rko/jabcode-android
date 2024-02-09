#!/bin/sh
FILES="lib/src/main/cpp/jabcodelib.c lib/src/main/cpp/jabreader.h"
echo "Formatting '${FILES}'..."
# shellcheck disable=SC2086
clang-format -i ${FILES}
