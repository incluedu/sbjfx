# Version History

## v0.2.12 - 07.10.2026 - Patric Hollenstein

- **SBFX-51:** Decoupled the FXML classpath path from the Spring bean name inside the `@FXMLView` stereotype annotation. Introduced a dedicated `fxml` parameter to prevent Spring Boot 3 bootstrap validation crashes caused by slashes and paths inside the implicit `@AliasFor` bean name generator (`Component.value`). Existing documentation comments have been fully preserved and modernized.

## v0.2.11 - 07.10.2026 - Patric Hollenstein

- **SBFX-50:** Fixed a critical issue where the application would freeze indefinitely if the primary user interface (`savedInitialView`) could not be resolved from the Spring context. The library now explicitly prints a destructive error to the system error stream, exits the JavaFX platform, and terminates the JVM process with exit code `1`.


## v0.2.10 - 06.10.2026 - Patric Hollenstein

* Upgrade project to Java 21 (LTS) via Gradle Toolchains
* Upgrade Spring Boot framework to version 3.3.4
* Migrate deployment baseline from Java 17 to Java 21
* Upgrade JavaFX dependencies to version 21
* Modernize Kotlin version to 1.9.25 and Kotlin Logging to 5.1.4
* Complete overhaul of the build system infrastructure to Gradle 8.10.2
* Migrate all source and test imports from legacy 'javax.*' to modern 'jakarta.*' (Jakarta EE 10)
* Fix application context bootstrap sequence for Spring Boot 3 in IDE environments
* Resolve test classpath conflicts by removing redundant SLF4J simple binding in favor of Logback
* Complete architectural refactoring of core library classes (`AbstractFxmlView`, `ResourceBundleControl`, `PropertyReaderHelper`, `GUIState`, `SplashScreen`)
* Migrated deprecated Java reflection patterns and legacy `try-catch` structures to idiomatic Kotlin `runCatching` blocks
* Implemented automatic lifecycle tracking properties (`isSceneInitialized`, `isStageInitialized`) within `GUIState` to prevent `UninitializedPropertyAccessException` risks
* Fixed Spring Boot 3 `AnnotationBeanNameGenerator` deprecation warnings by integrating explicit `@AliasFor` component scanning attributes into the `@FXMLView` stereotype annotation
* Introduced auto-closing `.use {}` resource mechanics to prevent potential I/O stream file leaks during encoding translation operations

## V0.0.4

* Update Springboot to version 2.7.6
* Update Kotlin Logging to version 3.0.4
* Change development JDK from temurin-11 to temurin-17
* Update JavaFx to version 17.0.2
* Fix 'AbstractJavaFxApplicationSupportTest'
* Upgrade Gradle to version 7.6
* Implement 'SampleViewTest'
* Implement 'SplashScreenTest'
* Implement 'SLF4JSimpleBinding'

## V0.0.3 - 20.11.2022 - Patric Hollenstein

* fix signing not working at nexus repository

## V0.0.2 - 20.11.2022 - Patric Hollenstein

* update dependencies
* implement Central Maven Repository publishing
* implement GitHub publishing

## V0.0.1 - 19.11.2022 - Patric Hollenstein

* First release
