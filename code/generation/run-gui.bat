@echo off
cd /d "%~dp0"
chcp 65001 >nul
echo ========================================
echo    Link16 DSL 转换工具 - 翻译树(消息转发)
echo ========================================
echo.

REM 如果存在 shaded jar 则优先使用（包含所有依赖）
set SHADED=target\link16-dsl-parser-1.0-SNAPSHOT-shaded.jar
REM 注释掉 shaded jar 的使用，因为我们想运行最新的源码更改
REM if exist "%SHADED%" (
REM    echo Found shaded JAR: %SHADED%
REM    echo Starting ModernDslConverterGUI using shaded JAR...
REM    java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar "%SHADED%"
REM    if errorlevel 1 (
REM        echo.
REM        echo Program exited with error.
REM        pause
REM    )
REM    goto :eof
REM )

echo 🔨 正在检查代码更新并编译...
call mvn compile
if errorlevel 1 (
    echo ❌ 编译失败，请检查错误信息
    pause
    exit /b 1
)

REM 简化版本不编译外部功能模型解析器

if not exist "target\dependency" (
    echo 📦 正在复制依赖...
    call mvn dependency:copy-dependencies
)

echo ✅ 准备完成
echo 🚀 正在启动现代化图形界面...
echo.

java -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -cp "target/classes;target/dependency/*" ModernDslConverterGUI

if errorlevel 1 (
    echo.
    echo ❌ 程序运行出错
    pause
)

