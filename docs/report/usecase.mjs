import fs from "fs";
const R = process.argv[2];
// 기존 기능 8개와 확장 기능 E1~E3를 한 줄로 두고, E4는 세대 진행 옆에 둔다. 확장 기능만 색으로 칠한다.
const BLUE = "#2155A4", FILL = "#E3EBF7";
const ucs = [["세포 놓기·지우기"], ["세대 진행"], ["한 세대씩 진행"], ["진행 멈추기"], ["판 비우기"], ["판 저장"], ["판 불러오기"], ["종료"],
             ["규칙 선택", "E1"], ["규칙 직접 입력", "E2"], ["결말 예측 보기", "E3"]];
const W = 900, top = 92, gap = 56, cx = 420, rx = 118, ry = 21;
const lastY = top + gap * (ucs.length - 1), H = lastY + 140;
const a = { x: 92, y: (top + lastY) / 2 };
const T = (x, y, t, o = "") => `<text x="${x}" y="${y}" fill="#000" stroke="none" ${o}>${t}</text>`;
const ellipse = (x, y, rxx, label, e) => {
  if (!e) return `<ellipse cx="${x}" cy="${y}" rx="${rxx}" ry="${ry}" fill="#fff"/>` + T(x, y + 5, label, 'text-anchor="middle"');
  // 확장 기능: 채운 타원 + 굵은 테두리 + E 딱지
  const bw = 34, gapx = 8, tw = label.length * 14;          // 글자 폭은 대략 14px로 잡았다
  const startX = x - (bw + gapx + tw) / 2;
  return `<ellipse cx="${x}" cy="${y}" rx="${rxx}" ry="${ry}" fill="${FILL}" stroke="${BLUE}" stroke-width="2"/>`
    + `<rect x="${startX}" y="${y - 11}" width="${bw}" height="22" rx="11" fill="${BLUE}" stroke="none"/>`
    + `<text x="${startX + bw / 2}" y="${y + 5}" text-anchor="middle" fill="#fff" stroke="none" font-weight="bold">${e}</text>`
    + T(startX + bw + gapx, y + 5, label, 'font-weight="bold"');
};
let s = `<svg xmlns="http://www.w3.org/2000/svg" width="${W}" height="${H}" viewBox="0 0 ${W} ${H}" font-family="Arial, 'Apple SD Gothic Neo', sans-serif" font-size="14" fill="none" stroke="#000" stroke-width="1">`;
s += `<defs><marker id="open" viewBox="0 0 12 12" refX="11" refY="6" markerWidth="12" markerHeight="12" orient="auto"><path d="M1,1 L11,6 L1,11" fill="none" stroke="${BLUE}" stroke-width="1.5"/></marker></defs>`;
s += `<rect x="190" y="30" width="${W - 210}" height="${lastY + 50}" fill="#fff"/>` + T(206, 54, "Life Game", 'font-weight="bold"');
// 액터
s += `<circle cx="${a.x}" cy="${a.y - 46}" r="11" fill="#fff"/><line x1="${a.x}" y1="${a.y - 35}" x2="${a.x}" y2="${a.y}"/>`
   + `<line x1="${a.x - 20}" y1="${a.y - 22}" x2="${a.x + 20}" y2="${a.y - 22}"/><line x1="${a.x}" y1="${a.y}" x2="${a.x - 16}" y2="${a.y + 26}"/><line x1="${a.x}" y1="${a.y}" x2="${a.x + 16}" y2="${a.y + 26}"/>`
   + T(a.x, a.y + 46, "사용자", 'text-anchor="middle"');
ucs.forEach(([name, e], i) => {
  const y = top + gap * i;
  s += `<line x1="${a.x + 24}" y1="${a.y - 18}" x2="${cx - rx}" y2="${y}"/>` + ellipse(cx, y, rx, name, e);
});
// E4 자동 정지: 세대 진행을 확장
const by = top + gap, ex = 745, erx = 92, x1 = ex - erx, x2 = cx + rx, mx = (x1 + x2) / 2;
s += ellipse(ex, by, erx, "자동 정지", "E4");
s += `<line x1="${x1}" y1="${by}" x2="${x2}" y2="${by}" stroke="${BLUE}" stroke-width="1.5" stroke-dasharray="5 4" marker-end="url(#open)"/>`;
s += T(mx, by - 8, "«extend»", 'text-anchor="middle" font-size="13"');
const nw = 230, nh = 50, nx = mx - 25, ny = by + 44;
s += `<path d="M${nx},${ny} h${nw - 12} l12,12 v${nh - 12} h-${nw} z" fill="#fff"/><path d="M${nx + nw - 12},${ny} v12 h12"/>`
   + T(nx + 10, ny + 21, "조건: 예측한 결말이 소멸·정지이고", 'font-size="12.5"') + T(nx + 10, ny + 39, "판이 그 세대에 도달함", 'font-size="12.5"')
   + `<line x1="${mx}" y1="${ny}" x2="${mx}" y2="${by + 2}" stroke-dasharray="2 3"/>`;
// 범례
const ly = lastY + 112;
s += `<ellipse cx="226" cy="${ly}" rx="26" ry="11" fill="#fff"/>` + T(262, ly + 5, "기존 기능 (원본 Life Game)")
   + `<ellipse cx="496" cy="${ly}" rx="26" ry="11" fill="${FILL}" stroke="${BLUE}" stroke-width="2"/>`
   + `<rect x="532" y="${ly - 10}" width="30" height="20" rx="10" fill="${BLUE}" stroke="none"/>` + `<text x="547" y="${ly + 5}" text-anchor="middle" fill="#fff" stroke="none" font-weight="bold" font-size="12">E</text>`
   + T(570, ly + 5, "확장한 기능 (이번 프로젝트에서 추가)", 'font-weight="bold"');
s += `</svg>`;
fs.writeFileSync(`${R}/usecase.svg`, s);
fs.writeFileSync(`${R}/usecase.html`, `<!doctype html><meta charset="utf-8"><title>유스케이스</title><style>@page{size:A4;margin:15mm}body{margin:0;display:flex;justify-content:center}svg{width:100%;height:auto}</style>${s}`);
