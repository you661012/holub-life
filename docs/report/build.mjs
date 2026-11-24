// 보고서 PDF를 만든다.  사용(holub-life 폴더에서): node docs/report/build.mjs
// 1) UML 그림을 다시 그리고  2) chapters/의 조각을 이름 순서로 이어 report.html을 만든 뒤  3) Chrome으로 report.pdf를 찍는다.
// 4) 찍은 PDF에서 장·절 제목이 몇 쪽에 있는지 읽어 목차에 쪽 번호를 넣고 한 번 더 찍는다.
import fs from "fs";
import path from "path";
import { execFileSync } from "child_process";
import { fileURLToPath } from "url";

const DIR = path.dirname(fileURLToPath(import.meta.url));
execFileSync("node", [path.join(DIR, "diagrams.mjs"), DIR], { stdio: "inherit" });

const parts = fs.readdirSync(path.join(DIR, "chapters")).filter(f => f.endsWith(".html")).sort()
  .map(f => fs.readFileSync(path.join(DIR, "chapters", f), "utf8"))
  // 장·절 제목에 번호로 id를 붙인다 ("4." → s4, "4-3." → s4-3)
  .map(s => s.replace(/<(h[12])>(\d+(?:-\d+)?)\. /g, (m, h, n) => `<${h} id="s${n}">${n}. `))
  // 목차 항목을 그 id로 가는 링크로 만든다
  .map(s => s.includes('class="chapter toc"')
    ? s.replace(/<li>(\d+(?:-\d+)?)\. ([^<\n]+)/g, (m, n, t) => `<li><a href="#s${n}"><span class="t">${n}. ${t.trim()}</span><span class="pg" data-for="${n}. ${t.trim()}"></span></a>`)
    : s)
  // 표의 머리줄을 thead로 감싼다. 표가 다음 쪽으로 넘어가면 머리줄이 다시 찍힌다.
  // 행이 여섯 개 이하인 표(대안 비교표 등)는 쪽 사이에서 자르지 않는다.
  .map(s => s.replace(/<table(?: class="([^"]*)")?>(\s*)<tr>((?:(?!<\/tr>)[\s\S])*?<th[\s\S]*?)<\/tr>([\s\S]*?)<\/table>/g, (m, cls, sp, head, body) => {
    const rows = (body.match(/<tr>/g) || []).length;
    const c = [cls, rows <= 6 ? "keep" : ""].filter(Boolean).join(" ");
    return `<table${c ? ` class="${c}"` : ""}>${sp}<thead><tr>${head}</tr></thead><tbody>${body}</tbody></table>`;
  }))
  // UML 그림과 바로 뒤의 설명 문단을 한 쪽에 둔다
  .map(s => s.replace(/(<figure>\s*<img src="fig[\s\S]*?<\/figure>)\s*(<p>[\s\S]*?<\/p>)/g, `<div class="figblock">$1\n$2</div>`));

const page = body => `<!doctype html>
<html lang="ko"><head><meta charset="utf-8"><title>Holub Life Game 기능 확장 및 설계 개선</title>
<link rel="stylesheet" href="report.css"></head><body>
${body}
</body></html>`;

const chrome = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome";
const html = path.join(DIR, "report.html"), pdf = path.join(DIR, "report.pdf");
const print = text => {
  fs.writeFileSync(html, page(text));
  execFileSync(chrome, ["--headless=new", "--no-pdf-header-footer", `--print-to-pdf=${pdf}`, "file://" + html], { stdio: "ignore" });
};

let body = parts.join("\n");
print(body);

// 목차 쪽 번호. 목차는 한 쪽이라 번호를 넣어도 뒤쪽의 쪽 나눔은 바뀌지 않는다
const headings = [...body.matchAll(/data-for="([^"]+)"/g)].map(m => m[1]);
// PDF에서 읽은 글자는 순서가 섞일 때가 있어("Life Game"이 "Life G / me / a"로), 번호와 제목 앞 두 글자만으로 찾는다
const keys = headings.map(h => { const [, n, t] = h.match(/^(\S+) (.*)$/); return n + t.replace(/\s/g, "").slice(0, 2); });
const numbers = execFileSync("swift", [path.join(DIR, "pagenums.swift"), pdf, ...keys], { encoding: "utf8" }).trim().split("\n");
headings.forEach((h, i) => { body = body.replace(`data-for="${h}"></span>`, `data-for="${h}">${numbers[i] === "0" ? "" : numbers[i]}</span>`); });
const missing = headings.filter((h, i) => numbers[i] === "0");
if (missing.length) console.log("쪽을 못 찾은 목차 항목:", missing);
print(body);
console.log("report.pdf를 만들었다:", pdf);
