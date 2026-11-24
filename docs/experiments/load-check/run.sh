#!/bin/bash
# 원본(648d9bd)과 지금 코드에서 Load 경로로 놓은 가로 3칸이 어떻게 진행하는지 비교한다 (11/24).
# 원본에는 LiveCellCollector가 없어서 지금 것을 복사해 넣는다.
# holub-life 폴더에서 실행한다: bash docs/experiments/load-check/run.sh
set -e
OUT=${TMPDIR:-/tmp}/life-load-check
rm -rf "$OUT"
mkdir -p "$OUT/original/src" "$OUT/original/out" "$OUT/now/out"
git archive 648d9bd src/main/java | tar -x -C "$OUT/original/src"
cp src/main/java/com/holub/life/LiveCellCollector.java "$OUT/original/src/src/main/java/com/holub/life/"
javac -nowarn -d "$OUT/original/out" $(find "$OUT/original/src" -name "*.java") docs/experiments/load-check/LoadBoundary.java 2>/dev/null
javac -nowarn -d "$OUT/now/out" $(find src/main/java -name "*.java") docs/experiments/load-check/LoadBoundary.java 2>/dev/null
echo "== 원본 648d9bd";  java -Djava.awt.headless=true -cp "$OUT/original/out" LoadBoundary
echo "== 지금 코드";     java -Djava.awt.headless=true -cp "$OUT/now/out" LoadBoundary
