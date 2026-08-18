#!/bin/bash
# Lance l'application directement avec java (aucun Maven requis).
set -e
cd "$(dirname "$0")"

if [ ! -d classes ] || [ -z "$(find classes -name '*.class' 2>/dev/null)" ]; then
    echo "Les classes ne sont pas compilées, compilation automatique..."
    ./build.sh
fi

java -cp "classes:lib/*" mg.cepe.Main
