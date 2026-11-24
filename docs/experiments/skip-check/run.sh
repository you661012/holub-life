#!/bin/bash
# 원본의 건너뛰기 조건을 되살린 사본 두 개를 만들어, 세대마다 계산한 칸 수를 센다 (11/24).
#   bug   : 건너뛰기 + 원본 Resident.transition() (ADR 002를 정할 때의 상태)
#   fixed : 건너뛰기 + 고친 Resident.transition() (11/17 수정 뒤)
# holub-life 폴더에서 실행한다: bash docs/experiments/skip-check/run.sh
set -e
OUT=${TMPDIR:-/tmp}/life-skip-check
rm -rf "$OUT"
for v in bug fixed; do
  mkdir -p "$OUT/$v/src" "$OUT/$v/out"
  cp -R src/main/java/. "$OUT/$v/src/"
  git show 648d9bd:src/main/java/com/holub/life/Neighborhood.java > "$OUT/$v/src/com/holub/life/Neighborhood.java"
done
sed -i '' 's/boolean changed = !isStable();/boolean changed = isStable();/' "$OUT/bug/src/com/holub/life/Resident.java"
for v in bug fixed; do
  javac -nowarn -d "$OUT/$v/out" $(find "$OUT/$v/src" -name "*.java") 2>/dev/null
  javac -cp "$OUT/$v/out" -d "$OUT/$v/out" docs/experiments/skip-check/SkipCount.java
  echo "== $v"
  for b in ship blinker blinker+ship; do java -cp "$OUT/$v/out" SkipCount $b; done
done
