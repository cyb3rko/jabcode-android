#!/bin/sh
FILES="lib/src/main/cpp/jabcodelib.c"
echo "Formatting '${FILES}'..."
# shellcheck disable=SC2086
clang-format -i ${FILES}
