@echo off
chcp 65001 >nul
title GMP System - 全部服务启动

echo ============================================
echo   GMP 系统 - 一键启动所有服务
echo   [前置] 基础设施已启动: MySQL, Redis, Nacos, RabbitMQ, MinIO
echo ============================================

set "PROJECT_DIR=%~dp0"
set "LOG_DIR=%PROJECT_DIR%logs"

:: ---------- 基础设施连接配置（按实际情况修改）----------
set "NACOS_SERVER=localhost:8848"
set "NACOS_USERNAME=nacos"
set "NACOS_PASSWORD=nacos"
set "REDIS_HOST=localhost"
set "REDIS_PORT=6379"
set "REDIS_PASSWORD=123456"
set "DB_HOST=localhost"
set "DB_PORT=3306"
set "DB_USER=root"
set "DB_PASS=root"
set "MINIO_ENDPOINT=http://localhost:9000"
set "MINIO_ACCESS_KEY=admin"
set "MINIO_SECRET_KEY=12345678"
:: ---------------------------------------------------

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"

echo.
echo [1/8] 启动网关 gmp-gateway (8000)...
start "gmp-gateway" java -jar "%PROJECT_DIR%gmp-gateway\target\gmp-gateway-1.0.0-SNAPSHOT.jar" ^
    --server.port=8000 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\gateway.log" 2>&1

echo [2/8] 启动认证 gmp-auth (8001)...
start "gmp-auth" java -jar "%PROJECT_DIR%gmp-auth\target\gmp-auth-1.0.0-SNAPSHOT.jar" ^
    --server.port=8001 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\auth.log" 2>&1

echo [3/8] 启动系统管理 gmp-system-server (8003)...
start "gmp-system-server" java -jar "%PROJECT_DIR%gmp-system-server\target\gmp-system-server-1.0.0-SNAPSHOT.jar" ^
    --server.port=8003 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\system-server.log" 2>&1

echo [4/8] 启动表单 gmp-form-server (8004)...
start "gmp-form-server" java -jar "%PROJECT_DIR%gmp-form-server\target\gmp-form-server-1.0.0-SNAPSHOT.jar" ^
    --server.port=8004 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\form-server.log" 2>&1

echo [5/8] 启动流程 gmp-workflow-server (8005)...
start "gmp-workflow-server" java -jar "%PROJECT_DIR%gmp-workflow-server\target\gmp-workflow-server-1.0.0-SNAPSHOT.jar" ^
    --server.port=8005 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\workflow-server.log" 2>&1

echo [6/8] 启动文件 gmp-file-server (8006)...
start "gmp-file-server" java -jar "%PROJECT_DIR%gmp-file-server\target\gmp-file-server-1.0.0-SNAPSHOT.jar" ^
    --server.port=8006 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\file-server.log" 2>&1

echo [7/8] 启动业务 gmp-business-server (8007)...
start "gmp-business-server" java -jar "%PROJECT_DIR%gmp-business-server\target\gmp-business-server-1.0.0-SNAPSHOT.jar" ^
    --server.port=8007 ^
    --spring.cloud.nacos.discovery.server-addr=%NACOS_SERVER% ^
    > "%LOG_DIR%\business-server.log" 2>&1

echo [8/8] 启动前端 gmp-frontend...
cd /d "%PROJECT_DIR%gmp-frontend"
start "gmp-frontend" npm run dev > "%LOG_DIR%\frontend.log" 2>&1

echo.
echo ============================================
echo   全部启动完成!
echo   日志目录: %LOG_DIR%
echo.
echo   前端:     http://localhost:5173
echo   API网关:  http://localhost:8000
echo   Nacos:    http://localhost:8848/nacos   [nacos/nacos]
echo   RabbitMQ: http://localhost:15672        [gmp/gmp123]
echo   MinIO:    http://localhost:9001         [minioadmin/minioadmin]
echo.
echo   查看日志: tail -f logs/[服务名].log
echo   停止全部: 关闭弹出的命令行窗口即可
echo ============================================
pause
