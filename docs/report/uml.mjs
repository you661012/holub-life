// 보고서의 UML 그림을 그리는 작은 도구.
// 표기는 수업 자료의 그림을 따른다: 클래스는 칸 셋(이름/필드/메서드), «interface», 인터페이스·추상은 기울임,
// 관계는 1 OOP Review 24쪽 (의존 점선 화살표, 연관 실선 열린 화살촉, 집합 빈 마름모, 합성 찬 마름모, 상속 빈 삼각형),
// 실현은 점선 + 빈 삼각형(3 Observer 17쪽), 시퀀스는 밑줄 이름·점선 생명선·실행 막대·꽉 찬 화살촉(14 Builder 4쪽),
// 상태는 둥근 사각형·검은 시작점·열린 화살촉(10 State 3쪽). 이번 프로젝트에서 새로 만든 클래스는 파란 테두리로 표시한다.

export const BLUE = "#2155A4", BLUE_FILL = "#EEF3FB", INK = "#000";
const FONT = "Arial, 'Apple SD Gothic Neo', sans-serif";

// 글자 폭 어림: 한글은 글자 크기만큼, 그 밖은 0.56배
export function textWidth(s, size = 13) {
  let w = 0;
  for (const ch of s) w += /[ㄱ-힝]/.test(ch) ? size * 0.98 : (/[A-Z]/.test(ch) ? size * 0.66 : size * 0.55);
  return w;
}
const esc = s => String(s).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");

export class Diagram {
  constructor(w, h) { this.w = w; this.h = h; this.parts = []; this.defs = new Set(); }
  add(s) { this.parts.push(s); return this; }
  text(x, y, s, o = {}) {
    const a = [`x="${x}"`, `y="${y}"`, `font-size="${o.size || 13}"`, `fill="${o.color || INK}"`];
    if (o.anchor) a.push(`text-anchor="${o.anchor}"`);
    if (o.italic) a.push(`font-style="italic"`);
    if (o.bold) a.push(`font-weight="bold"`);
    if (o.underline) a.push(`text-decoration="underline"`);
    return this.add(`<text ${a.join(" ")} stroke="none">${esc(s)}</text>`);
  }
  svg() {
    const defs = `<defs>
<marker id="m-open" viewBox="0 0 14 14" refX="13" refY="7" markerWidth="14" markerHeight="14" orient="auto-start-reverse" markerUnits="userSpaceOnUse"><path d="M1,1 L13,7 L1,13" fill="none" stroke="${INK}" stroke-width="1"/></marker>
<marker id="m-tri" viewBox="0 0 16 16" refX="15" refY="8" markerWidth="16" markerHeight="16" orient="auto-start-reverse" markerUnits="userSpaceOnUse"><path d="M1,1 L15,8 L1,15 Z" fill="#fff" stroke="${INK}" stroke-width="1"/></marker>
<marker id="m-fill" viewBox="0 0 14 14" refX="13" refY="7" markerWidth="12" markerHeight="12" orient="auto-start-reverse" markerUnits="userSpaceOnUse"><path d="M1,1 L13,7 L1,13 Z" fill="${INK}" stroke="none"/></marker>
<marker id="m-dia-fill" viewBox="0 0 20 12" refX="1" refY="6" markerWidth="20" markerHeight="12" orient="auto" markerUnits="userSpaceOnUse"><path d="M1,6 L10,1 L19,6 L10,11 Z" fill="${INK}" stroke="${INK}"/></marker>
<marker id="m-dia" viewBox="0 0 20 12" refX="1" refY="6" markerWidth="20" markerHeight="12" orient="auto" markerUnits="userSpaceOnUse"><path d="M1,6 L10,1 L19,6 L10,11 Z" fill="#fff" stroke="${INK}"/></marker>
</defs>`;
    return `<svg xmlns="http://www.w3.org/2000/svg" width="${this.w}" height="${this.h}" viewBox="0 0 ${this.w} ${this.h}" font-family="${FONT}" fill="none" stroke="${INK}" stroke-width="1">${defs}<rect width="${this.w}" height="${this.h}" fill="#fff" stroke="none"/>${this.parts.join("")}</svg>`;
  }
}

// ── 클래스 ────────────────────────────────────────────────
// box: { x, y, w?, name, stereo?, abstract?, fields:[], methods:[], isNew?, role? }
// fields/methods 항목은 문자열. 앞에 "_"를 붙이면 static(밑줄), "/"를 붙이면 기울임(추상 메서드)
export function classBox(d, b) {
  const lh = 16, pad = 6, size = 12.5;
  const head = (b.stereo ? 1 : 0) + 1;
  const strip = s => s.replace(/^[_\/]+/, "");
  const widest = Math.max(textWidth(b.name, 13.5) + 24, ...(b.fields || []).map(f => textWidth(strip(f), size) + 16), ...(b.methods || []).map(m => textWidth(strip(m), size) + 16), 120);
  const w = b.w || Math.ceil(widest);
  const hHead = head * lh + pad * 2, hF = Math.max(1, (b.fields || []).length) * lh + pad, hM = Math.max(1, (b.methods || []).length) * lh + pad;
  const noFields = b.fields === null, noMethods = b.methods === null;
  const h = hHead + (noFields ? 0 : hF) + (noMethods ? 0 : hM);
  const stroke = b.isNew ? `stroke="${BLUE}" stroke-width="1.8"` : "";
  d.add(`<rect x="${b.x}" y="${b.y}" width="${w}" height="${h}" fill="${b.isNew ? BLUE_FILL : "#fff"}" ${stroke}/>`);
  let y = b.y + pad + lh - 3;
  if (b.stereo) { d.text(b.x + w / 2, y, `«${b.stereo}»`, { anchor: "middle", size: 12 }); y += lh; }
  d.text(b.x + w / 2, y, b.name, { anchor: "middle", size: 13.5, italic: b.stereo === "interface" || b.abstract, bold: !(b.stereo === "interface" || b.abstract) });
  let yy = b.y + hHead;
  if (!noFields) {
    d.add(`<line x1="${b.x}" y1="${yy}" x2="${b.x + w}" y2="${yy}" ${stroke}/>`);
    (b.fields || []).forEach((f, i) => d.text(b.x + 7, yy + pad / 2 + lh * (i + 1) - 3, strip(f), { size, underline: f.startsWith("_"), italic: f.startsWith("/") }));
    yy += hF;
  }
  if (!noMethods) {
    d.add(`<line x1="${b.x}" y1="${yy}" x2="${b.x + w}" y2="${yy}" ${stroke}/>`);
    (b.methods || []).forEach((m, i) => d.text(b.x + 7, yy + pad / 2 + lh * (i + 1) - 3, strip(m), { size, underline: m.startsWith("_"), italic: m.startsWith("/") || b.stereo === "interface" }));
  }
  if (b.role) d.text(b.x + w, b.y - 6, b.role, { anchor: "end", size: 12, italic: true, color: BLUE });
  const r = { x: b.x, y: b.y, w, h, cx: b.x + w / 2, cy: b.y + h / 2 };
  r.top = (dx = 0) => [r.cx + dx, r.y]; r.bottom = (dx = 0) => [r.cx + dx, r.y + h];
  r.left = (dy = 0) => [r.x, r.cy + dy]; r.right = (dy = 0) => [r.x + w, r.cy + dy];
  return r;
}

// 관계선. pts: [[x,y], ...] (첫 점 = 출발 클래스 쪽, 끝 점 = 화살표가 가리키는 쪽)
// kind: inherit | realize | assoc | depend | compose | aggregate | line
export function relation(d, pts, kind, o = {}) {
  const dash = (kind === "realize" || kind === "depend") ? `stroke-dasharray="6 4"` : "";
  const end = { inherit: "m-tri", realize: "m-tri", assoc: "m-open", depend: "m-open", compose: "m-open", aggregate: "m-open" }[kind];
  const start = { compose: "m-dia-fill", aggregate: "m-dia" }[kind];
  const p = pts.map(q => q.join(",")).join(" ");
  d.add(`<polyline points="${p}" ${dash} ${end ? `marker-end="url(#${end})"` : ""} ${start ? `marker-start="url(#${start})"` : ""} ${o.color ? `stroke="${o.color}"` : ""}/>`);
  const [ax, ay] = pts[pts.length - 2], [bx, by] = pts[pts.length - 1];
  if (o.role) { // 역할 이름: 화살표 끝 근처
    const tx = bx + (o.roleDx ?? (ax < bx ? -10 : ax > bx ? 10 : 8)), ty = by + (o.roleDy ?? (ay < by ? -10 : ay > by ? 18 : -6));
    d.text(tx, ty, o.role, { size: 12, anchor: o.roleAnchor || (ax < bx ? "end" : "start") });
  }
  if (o.label) { const [lx, ly] = o.labelAt || [(pts[0][0] + bx) / 2, (pts[0][1] + by) / 2 - 6]; d.text(lx, ly, o.label, { size: 12, anchor: "middle" }); }
}

// 메모 (접힌 모서리)
export function note(d, x, y, lines, o = {}) {
  const w = o.w || Math.ceil(Math.max(...lines.map(l => textWidth(l, 12))) + 22), h = lines.length * 16 + 12;
  d.add(`<path d="M${x},${y} h${w - 12} l12,12 v${h - 12} h${-w} z" fill="#fff"/><path d="M${x + w - 12},${y} v12 h12"/>`);
  lines.forEach((l, i) => d.text(x + 9, y + 19 + i * 16, l, { size: 12 }));
  if (o.to) d.add(`<line x1="${o.from ? o.from[0] : x + w / 2}" y1="${o.from ? o.from[1] : y + h}" x2="${o.to[0]}" y2="${o.to[1]}" stroke-dasharray="2 3"/>`);
  return { x, y, w, h };
}

// ── 시퀀스 ────────────────────────────────────────────────
// objs: [{ id, name, x }]  (name은 "aClient" 또는 "worker : PredictionWorker"처럼 적는다)
export function lifelines(d, objs, top, bottom) {
  const map = {};
  for (const o of objs) {
    if (o.actor) { // 액터: 사람 모양 + 이름 (밑줄 없음)
      const x = o.x, y = top;
      d.add(`<circle cx="${x}" cy="${y + 7}" r="6" fill="#fff"/><line x1="${x}" y1="${y + 13}" x2="${x}" y2="${y + 25}"/><line x1="${x - 9}" y1="${y + 17}" x2="${x + 9}" y2="${y + 17}"/><line x1="${x}" y1="${y + 25}" x2="${x - 7}" y2="${y + 33}"/><line x1="${x}" y1="${y + 25}" x2="${x + 7}" y2="${y + 33}"/>`);
      d.text(x, y + 46, o.name, { anchor: "middle", size: 12 });
      d.add(`<line x1="${x}" y1="${top + 50}" x2="${x}" y2="${bottom}" stroke-dasharray="5 4"/>`);
      map[o.id] = x; continue;
    }
    const w = Math.max(textWidth(o.name, 12.5) + 18, 90);
    d.add(`<rect x="${o.x - w / 2}" y="${top}" width="${w}" height="30" fill="${o.isNew ? BLUE_FILL : "#fff"}" ${o.isNew ? `stroke="${BLUE}" stroke-width="1.6"` : ""}/>`);
    d.text(o.x, top + 20, o.name, { anchor: "middle", size: 12.5, underline: true });
    d.add(`<line x1="${o.x}" y1="${top + 30}" x2="${o.x}" y2="${bottom}" stroke-dasharray="5 4"/>`);
    map[o.id] = o.x;
  }
  return map;
}
export function activation(d, x, y1, y2, dx = 0) { d.add(`<rect x="${x - 6 + dx}" y="${y1}" width="12" height="${y2 - y1}" fill="#fff"/>`); }
// 메시지: 호출은 실선 + 꽉 찬 화살촉, 돌려주기는 점선 + 열린 화살촉
export function message(d, x1, x2, y, label, o = {}) {
  if (o.self) { // 자기 호출. 중첩된 실행 막대(오른쪽으로 6 밀린 막대)에 꽂힌다
    d.add(`<polyline points="${x1 + 6},${y} ${x1 + 46},${y} ${x1 + 46},${y + 18} ${x1 + 12},${y + 18}" marker-end="url(#${o.async ? "m-open" : "m-fill"})"/>`);
    d.text(x1 + 52, y + 13, label, { size: 12 });
    return;
  }
  const ret = o.ret;
  // 비동기(기다리지 않는) 메시지는 실선 + 열린 화살촉
  const head = ret ? `stroke-dasharray="5 4" marker-end="url(#m-open)"` : o.async ? `marker-end="url(#m-open)"` : `marker-end="url(#m-fill)"`;
  d.add(`<line x1="${x1}" y1="${y}" x2="${x2}" y2="${y}" ${head}/>`);
  d.text(o.labelX ?? (x1 + x2) / 2, y - 5, label, { anchor: o.anchor || "middle", size: 12 });
}

// ── 상태 ──────────────────────────────────────────────────
export function stateBox(d, s) {
  const lines = s.label.split("\n"), w = s.w || Math.max(...lines.map(l => textWidth(l, 13))) + 34, h = s.h || lines.length * 18 + 22;
  d.add(`<rect x="${s.x}" y="${s.y}" width="${w}" height="${h}" rx="14" fill="#fff"/>`);
  lines.forEach((l, i) => d.text(s.x + w / 2, s.y + h / 2 + (i - (lines.length - 1) / 2) * 18 + 5, l, { anchor: "middle", size: 13 }));
  const r = { x: s.x, y: s.y, w, h, cx: s.x + w / 2, cy: s.y + h / 2 };
  r.top = (dx = 0) => [r.cx + dx, r.y]; r.bottom = (dx = 0) => [r.cx + dx, r.y + h];
  r.left = (dy = 0) => [r.x, r.cy + dy]; r.right = (dy = 0) => [r.x + w, r.cy + dy];
  return r;
}
export function transition(d, pts, label, o = {}) {
  const path = o.curve ? `M${pts[0]} Q${pts[1]} ${pts[2]}` : `M${pts.map(p => p.join(",")).join(" L")}`;
  d.add(`<path d="${path}" marker-end="url(#m-open)"/>`);
  if (label) { const [lx, ly] = o.at; (Array.isArray(label) ? label : [label]).forEach((l, i) => d.text(lx, ly + i * 15, l, { size: 12, anchor: o.anchor || "middle" })); }
}
export function initial(d, x, y) { d.add(`<circle cx="${x}" cy="${y}" r="8" fill="${INK}"/>`); }
