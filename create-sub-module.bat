@echo off
REM Create deps folder if it does not exist
if not exist deps (
    mkdir deps
)

REM Go into deps folder
cd deps

REM Clone repositories
git clone git@github.com:ofte-ch/java-commons.git Commons
git clone git@github.com:ofte-ch/RPCServer.git RPCServer

REM Done
echo Repositories cloned successfully.
pause
