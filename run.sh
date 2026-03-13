#!/bin/bash
# Compile et lance le visualiseur de graphe
# Prérequis : JDK 11+ installé

SRC_DIR="src"
OUT_DIR="out"

echo "==> Compilation..."
mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name "*.java" | xargs javac -d "$OUT_DIR" -sourcepath "$SRC_DIR" 2>&1

if [ $? -ne 0 ]; then
  echo "Erreur de compilation."
  exit 1
fi

echo "==> Lancement..."
java -cp "$OUT_DIR" main.Main
