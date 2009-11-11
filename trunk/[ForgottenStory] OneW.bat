@echo off
color b
cls

title MapleZtory Starter
echo MapleZtory has started:
start /b launch_world.bat
echo World Launched

ping localhost -w 10000 >nul
ping localhost -w 10000 >nul
start /b launch_login.bat
echo Login Launched

ping localhost -w 10000 >nul
ping localhost -w 10000 >nul
start /b launch_channel.bat
echo Channel Launched

