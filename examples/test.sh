#!/bin/sh

./gradlew testDebug --no-build-cache
./gradlew :other-ksp:test :coffee-maker:test :coffee-maker-glob:test :compile-perf:test --no-build-cache
