@echo off
if "%ANTHROPIC_API_KEY%"=="" (
  echo Please set ANTHROPIC_API_KEY first.
  exit /b 1
)
set APP_AI_MOCK=false
gradle bootRun
