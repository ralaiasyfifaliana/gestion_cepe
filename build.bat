@echo off
REM Compile l'application (sans Maven) : javac direct, classes -> dossier "classes"
cd /d "%~dp0"

echo Compilation de Gestion CEPE...
if not exist classes mkdir classes

dir /s /b mg\*.java > sources.tmp
javac -encoding UTF-8 -cp "lib/*" -d classes @sources.tmp
del sources.tmp

echo Compilation terminee. Classes generees dans .\classes
echo Lancez ensuite : run.bat
