@echo off
xcopy /s/Y ".\Projekte\CT-Commons\target\static\CTCommons.jar" ".\Testserver\Paper01\plugins"
xcopy /s/Y ".\Projekte\CT-Commons\target\static\CTCommons.jar" ".\Testserver\Paper02\plugins"
xcopy /s/Y ".\Projekte\CT-Commons\target\static\CTCommons.jar" ".\Testserver\Paper03\plugins"
xcopy /s/Y ".\Projekte\CT-Commons\target\static\CTCommons.jar" ".\Testserver\Waterfall\plugins"
xcopy /s/Y ".\Projekte\CT-Commons\target\static\CTCommons.jar" ".\Testserver\Velocity\plugins"
pause