set WORKSPACE=..\..\..\..
set LUBAN_DLL=.\Tools\Luban\Luban.dll
set CONF_ROOT=.

dotnet %LUBAN_DLL% ^
    -t server ^
    -c java-json ^
    -d json ^
    --conf %CONF_ROOT%\luban.conf ^
    -x outputCodeDir=..\java\cfg ^
    -x outputDataDir=%WORKSPACE%\excel-json


pause