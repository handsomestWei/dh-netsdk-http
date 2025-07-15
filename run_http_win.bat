@echo off
setlocal

REM 启动Spring Boot fat jar，dll与jar在同一目录
set JAR=dh-netsdk-http.jar

REM 业务进程模式启动（开发/调试）
REM java -jar %JAR%

REM 守护进程模式启动（推荐生产环境）
java -jar %JAR% guard

endlocal
pause 