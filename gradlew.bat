@rem Gradle wrapper - requires gradle/wrapper/gradle-wrapper.jar
@rem Run 'gradle wrapper' to generate if missing (requires Gradle installed)
@if "%DEBUG%"=="" @echo off

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
set APP_HOME=%DIRNAME%

if not exist "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" (
    echo gradle-wrapper.jar not found.
    echo Please run: gradle wrapper
    echo Or download from: https://docs.gradle.org/current/userguide/gradle_wrapper.html
    exit /b 1
)

set JAVA_EXE=java.exe
if defined JAVA_HOME set JAVA_EXE=%JAVA_HOME%\bin\java.exe

"%JAVA_EXE%" -Dfile.encoding=UTF-8 -Xmx64m -Xms64m -classpath "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
