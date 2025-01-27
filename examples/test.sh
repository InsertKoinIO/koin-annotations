#!/bin/sh

./gradlew testDebug --no-build-cache
./gradlew :other-ksp:test :coffee-maker:test :compile-perf:test --no-build-cache
