@echo off
REM Lance l'application directement avec java (aucun Maven requis).
cd /d "%~dp0"

if not exist classes\mg\cepe\Main.class (
    echo Les classes ne sont pas compilees, compilation automatique...
    call build.bat
)

java -cp "classes;lib/*" mg.cepe.Main
