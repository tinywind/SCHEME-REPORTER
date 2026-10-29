@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem
@rem SPDX-License-Identifier: Apache-2.0
@rem

@if "%DEBUG%"=="" @echo off
@rem ##########################################################################
@rem
@rem  gradlew startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables, and ensure extensions are enabled
setlocal EnableExtensions DisableDelayedExpansion
@rem Block the vulnerable Guava serializers shaded by the Kotlin build plugin.
set "SCHEME_REPORTER_BUILD_FILTER=!org.jetbrains.kotlin.com.google.common.collect.CompactHashMap;!org.jetbrains.kotlin.com.google.common.collect.CompactHashSet;!org.jetbrains.kotlin.com.google.common.collect.MapMakerInternalMap$*;*"
setlocal EnableDelayedExpansion
for %%V in (JAVA_OPTS GRADLE_OPTS JDK_JAVA_OPTIONS _JAVA_OPTIONS) do (
    set "SCHEME_REPORTER_JVM_OPTIONS=!%%V!"
    if defined SCHEME_REPORTER_JVM_OPTIONS (
        if not "!SCHEME_REPORTER_JVM_OPTIONS:-Djdk.serialFilter=!"=="!SCHEME_REPORTER_JVM_OPTIONS!" (
            set "SCHEME_REPORTER_FILTER_SOURCE=%%V"
            goto serialFilterConflict
        )
    )
)
if "!SCHEME_REPORTER_SERIAL_FILTER!"=="!SCHEME_REPORTER_BUILD_FILTER!" if "!JAVA_TOOL_OPTIONS!"=="!SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS!" goto serialFilterReady
set "SCHEME_REPORTER_FILTER_SOURCE=JAVA_TOOL_OPTIONS"
if defined JAVA_TOOL_OPTIONS if not "!JAVA_TOOL_OPTIONS:-Djdk.serialFilter=!"=="!JAVA_TOOL_OPTIONS!" goto serialFilterConflict
set "JAVA_TOOL_OPTIONS=!JAVA_TOOL_OPTIONS! -Djdk.serialFilter=!SCHEME_REPORTER_BUILD_FILTER!"
set "SCHEME_REPORTER_SERIAL_FILTER=!SCHEME_REPORTER_BUILD_FILTER!"
set "SCHEME_REPORTER_GUARDED_JAVA_TOOL_OPTIONS=!JAVA_TOOL_OPTIONS!"
goto serialFilterReady
:serialFilterConflict
1>&2 echo A preconfigured serialization filter in %SCHEME_REPORTER_FILTER_SOURCE% requires explicit policy composition; refusing to replace it.
"%COMSPEC%" /c exit 1
goto exitWithErrorLevel
:serialFilterReady
setlocal DisableDelayedExpansion

@rem Catch executions from older scripts and ensure they exit cleanly.
@rem This can be removed once we can be reasonably confident that few people
@rem will be migrating directly to this new wrapper.
goto afterSafetyNet
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
goto exitWithErrorLevel
:afterSafetyNet

set DIRNAME=%~dp0
if "%DIRNAME%"=="" set DIRNAME=.
@rem This is normally unused
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if %ERRORLEVEL% equ 0 goto execute

1>&2 echo.
1>&2 echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
1>&2 echo.
1>&2 echo Please set the JAVA_HOME variable in your environment to match the
1>&2 echo location of your Java installation.

"%COMSPEC%" /c exit 1
goto exitWithErrorLevel

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

1>&2 echo.
1>&2 echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
1>&2 echo.
1>&2 echo Please set the JAVA_HOME variable in your environment to match the
1>&2 echo location of your Java installation.

"%COMSPEC%" /c exit 1
goto exitWithErrorLevel

:execute
@rem Setup the command line



@rem Execute gradlew
@rem Keep the serialization filter in the environment until the JVM exits.
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -jar "%APP_HOME%\gradle\wrapper\gradle-wrapper.jar" %*
set "SCHEME_REPORTER_EXIT_CODE=%ERRORLEVEL%"
endlocal & "%COMSPEC%" /c exit %SCHEME_REPORTER_EXIT_CODE% & call :exitWithErrorLevel & goto exitWithErrorLevel

@rem This label must not be changed. We rely on old scripts being able to jump to this point.
:exitWithErrorLevel
@rem Use "%COMSPEC%" /c exit to allow operators to work properly in scripts
"%COMSPEC%" /c exit %ERRORLEVEL%
