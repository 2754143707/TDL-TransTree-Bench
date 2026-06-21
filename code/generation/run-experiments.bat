@echo off
cd /d "%~dp0"
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ========================================
echo    TDL-TransTree-Bench Experiment Runner
echo ========================================
echo.

set JAR_FILE=target\tdl-transtree-bench-1.0-SNAPSHOT.jar
set MODEL=gemini

if not exist "!JAR_FILE!" (
    echo ❌ 找不到 JAR 文件: !JAR_FILE!
    echo 请先编译项目: mvn clean package -DskipTests
    pause
    exit /b 1
)

echo 请选择大模型:
echo 1. Gemini (Gemini 3.1 Pro)
echo 2. GPT (GPT-OSS 120B)
echo 3. Qwen-plus
echo 4. Claude (Claude Opus 4.6 Thinking)
echo.

set /p model_choice="请输入模型选项 (1-4): "

if "!model_choice!"=="1" set MODEL=gemini
if "!model_choice!"=="2" set MODEL=gpt-5.2
if "!model_choice!"=="3" set MODEL=qwen-plus
if "!model_choice!"=="4" set MODEL=claude

echo.
echo ----------------------------------------
echo 当前已选择模型: !MODEL!
echo ----------------------------------------
echo.

echo 请选择要运行的实验 (1-5):
echo 1. Run 1: Baseline [Input: raw_images] (k=0)
echo 2. Run 2: Ablation Color [Input: vcot_color] (k=0)
echo 3. Run 3: Ablation Anchor [Input: vcot_anchor] (k=0)
echo 4. Run 4: Ours-vCoT Initial [Input: vcot_full] (k=0)
echo 5. Run 5: Proposed Loop [Input: vcot_full] (k=3)
echo.

set /p choice="请输入实验选项 (1-5): "

if "!choice!"=="1" goto RUN_BASELINE
if "!choice!"=="2" goto RUN_COLOR
if "!choice!"=="3" goto RUN_ANCHOR
if "!choice!"=="4" goto RUN_VCOT
if "!choice!"=="5" goto RUN_PROPOSED

echo 无效选项
pause
goto END

:RUN_BASELINE
echo 正在运行 Baseline...
java -Dfile.encoding=UTF-8 -cp "!JAR_FILE!" ExperimentRunner --mode baseline --model !MODEL!
if errorlevel 1 pause
goto END

:RUN_COLOR
echo 正在运行 Color Ablation...
java -Dfile.encoding=UTF-8 -cp "!JAR_FILE!" ExperimentRunner --mode color --model !MODEL!
if errorlevel 1 pause
goto END

:RUN_ANCHOR
echo 正在运行 Anchor Ablation...
java -Dfile.encoding=UTF-8 -cp "!JAR_FILE!" ExperimentRunner --mode anchor --model !MODEL!
if errorlevel 1 pause
goto END

:RUN_VCOT
echo 正在运行 vCoT Initial...
java -Dfile.encoding=UTF-8 -cp "!JAR_FILE!" ExperimentRunner --mode vcot_k0 --model !MODEL!
if errorlevel 1 pause
goto END

:RUN_PROPOSED
echo 正在运行 Proposed Loop...
java -Dfile.encoding=UTF-8 -cp "!JAR_FILE!" ExperimentRunner --mode proposed_k3 --model !MODEL!
if errorlevel 1 pause
goto END

:END
echo.
echo 脚本运行结束
pause
