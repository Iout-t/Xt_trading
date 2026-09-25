# Build notes

This repository intentionally does not ship a generated Gradle wrapper JAR.

- Android Studio can import and sync the project directly.
- GitHub Actions installs Gradle 9.5.0 before building.
- If you want a local wrapper, run `gradle wrapper --gradle-version 9.5.0` once on a machine with Gradle installed.
