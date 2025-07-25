@echo off

REM 安装本地jna.jar（如有需要）
echo Installing local jna.jar ...
call mvn install:install-file -Dfile=./libs/jna.jar -DgroupId=com.dahua -DartifactId=jna -Dversion=1.0 -Dpackaging=jar -DgeneratePom=true

REM 打包Spring Boot HTTP服务版

echo Packaging HTTP服务版 ...
call mvn clean package -Phttp -DskipTests
if exist target\dh-netsdk-http.jar echo HTTP fat jar: target\dh-netsdk-http.jar

REM 打包GUI桌面版

echo Packaging GUI桌面版 ...
call mvn clean package -Pgui -DskipTests
if exist target\dh-netsdk-gui.jar echo GUI fat jar: target\dh-netsdk-gui.jar

echo All done.
pause
