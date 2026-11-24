// 보고서 PDF를 만든다.  사용(holub-life 폴더에서): node docs/report/build.mjs
// 1) UML 그림을 다시 그리고  2) chapters/의 조각을 이름 순서로 이어 report.html을 만든 뒤  3) Chrome으로 report.pdf를 찍는다.
import fs from "fs";
import path from "path";
import { execFileSync } from "child_process";
import { fileURLToPath } from "url";

const DIR = path.dirname(fileURLToPath(import.meta.url));
execFileSync("node", [path.join(DIR, "diagrams.mjs"), DIR], { stdio: "inherit" });

const parts = fs.readdirSync(path.join(DIR, "chapters")).filter(f => f.endsWith(".html")).sort()
  .map(f => fs.readFileSync(path.join(DIR, "chapters", f), "utf8"));
const html = `<!doctype html>
<html lang="ko"><head><meta charset="utf-8"><title>Holub Life Game 기능 확장 및 설계 개선</title>
<link rel="stylesheet" href="report.css"></head><body>
${parts.join("\n")}
</body></html>`;
fs.writeFileSync(path.join(DIR, "report.html"), html);

const chrome = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
execFileSync(chrome, ["--headless=new", "--no-pdf-header-footer", `--print-to-pdf=${path.join(DIR, "report.pdf")}`, "file://" + path.join(DIR, "report.html")], { stdio: "ignore" });
console.log("report.pdf를 만들었다:", path.join(DIR, "report.pdf"));
