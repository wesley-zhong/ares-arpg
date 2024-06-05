set WORKSPACE=..
set LUBAN_DLL=%WORKSPACE%\Tools\Luban\Luban.dll
set CONF_ROOT=.

dotnet %LUBAN_DLL% ^
    -t all ^
    -d json ^
	-c java-json  ^
	-x outputCodeDir=src/main/gen/cfg ^
    --conf %CONF_ROOT%\luban.conf ^
    -x outputDataDir=output

pause