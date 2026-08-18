#!/bin/bash
# Compile l'application (sans Maven) : javac direct, classes -> dossier "classes"
set -e
cd "$(dirname "$0")"

echo "Compilation de Gestion CEPE..."
mkdir -p classes
find mg -name "*.java" > .sources.tmp
javac -encoding UTF-8 -cp "lib/*" -d classes @.sources.tmp
rm -f .sources.tmp

echo "Compilation terminée. Classes générées dans ./classes"
echo "Lancez ensuite : ./run.sh"
