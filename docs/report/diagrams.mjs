// 보고서의 UML 그림을 만든다.  사용: node docs/report/diagrams.mjs docs/report
// 연관선에 역할 이름을 적은 필드는 클래스 칸에 다시 적지 않는다 (수업 자료 3 Observer 17쪽과 같은 방식).
import fs from "fs";
import { Diagram, classBox, relation, note, lifelines, activation, message, stateBox, transition, initial, BLUE } from "./uml.mjs";
const OUT = process.argv[2] || ".";
const save = (name, d) => fs.writeFileSync(`${OUT}/${name}.svg`, d.svg());
const region = (d, x, y, w, h, label, right = false) => { d.add(`<rect x="${x}" y="${y}" width="${w}" height="${h}" fill="none" stroke="#777" stroke-dasharray="2 3"/>`); d.text(right ? x + w - 6 : x + 6, y + h - 6, label, { size: 11.5, color: "#555", anchor: right ? "end" : "start" }); };

// ── 그림 2-1. 원본 Life Game의 주요 클래스 ─────────────────────
{
  const d = new Diagram(990, 700);
  const life = classBox(d, { x: 10, y: 20, w: 150, name: "Life", fields: [], methods: ["+main(String[])"] });
  const uni = classBox(d, { x: 220, y: 20, w: 270, name: "Universe", fields: ["_-theInstance : Universe"], methods: ["_+instance() : Universe", "+paint(Graphics)", "-refreshNow()"] });
  const clock = classBox(d, { x: 720, y: 20, w: 240, name: "Clock", fields: ["_-instance : Clock"], methods: ["_+instance() : Clock", "+addClockListener(Listener)", "+startTicking(int)", "+stop()", "+tick()"] });
  const pub = classBox(d, { x: 740, y: 220, w: 200, name: "Publisher", fields: [], methods: ["+subscribe(Object)", "+publish(Distributor)"] });
  const lis = classBox(d, { x: 760, y: 360, w: 180, name: "Clock.Listener", stereo: "interface", fields: null, methods: ["+tick()"] });
  const anon = classBox(d, { x: 560, y: 360, w: 140, name: "Universe$7", fields: null, methods: ["+tick()"] });
  const cell = classBox(d, { x: 220, y: 250, w: 300, name: "Cell", stereo: "interface", fields: null,
    methods: ["+figureNextState(Cell × 8) : boolean", "+transition() : boolean", "+userClicked(Point, Rectangle)", "+isAlive() : boolean", "+widthInCells() : int", "+create() : Cell", "+clear()", "+transfer(Storable, Point, boolean) : boolean"] });
  const nb = classBox(d, { x: 100, y: 530, w: 280, name: "Neighborhood", fields: ["-gridSize : int"], methods: ["+figureNextState(Cell × 8) : boolean", "+create() : Cell"] });
  const res = classBox(d, { x: 430, y: 530, w: 260, name: "Resident", fields: ["-amAlive : boolean", "-willBeAlive : boolean"], methods: ["+figureNextState(Cell × 8) : boolean", "+create() : Cell"] });
  relation(d, [life.right(), uni.left(-10)], "depend", { label: "instance()", labelAt: [190, life.cy - 16] });
  relation(d, [uni.bottom(-40), [uni.cx - 40, cell.y]], "assoc", { role: "outermostCell", roleDx: 8, roleDy: -8, roleAnchor: "start" });
  relation(d, [nb.top(-40), [nb.cx - 40, 500], [cell.cx - 60, 500], [cell.cx - 60, cell.y + cell.h]], "realize");
  relation(d, [res.top(40), [res.cx + 40, 500], [cell.cx + 60, 500], [cell.cx + 60, cell.y + cell.h]], "realize");
  relation(d, [nb.left(), [50, nb.cy], [50, cell.cy], cell.left()], "compose", { role: "grid", roleDx: -8, roleDy: -8 });
  d.text(cell.x - 8, cell.cy + 18, "64", { size: 12, anchor: "end" });
  relation(d, [uni.right(-14), clock.left(-14)], "depend", { label: "addClockListener()", labelAt: [605, uni.cy - 20] });
  relation(d, [clock.bottom(), pub.top(-10)], "assoc", { role: "publisher", roleDx: 8, roleDy: -10, roleAnchor: "start" });
  relation(d, [clock.right(20), [978, clock.cy + 20], [978, lis.cy], lis.right()], "depend", { label: "tick() 전달", labelAt: [935, 340] });
  relation(d, [uni.right(14), [540, uni.cy + 14], [540, 330], [anon.cx, 330], anon.top()], "depend", { label: "«create»", labelAt: [590, 322] });
  relation(d, [anon.right(), lis.left()], "realize");
  // 메모는 Resident의 실현선(x≈600)과 겹치지 않게 오른쪽 아래에 둔다
  note(d, 735, 470, ["Universe 생성자 안의 익명 클래스.", "이름은 컴파일러가 붙인 번호다.", "addClockListener()로 구독하고,", "울릴 때마다 한 세대를 진행한다"], { to: [anon.x + anon.w - 20, anon.y + anon.h], from: [760, 470] });
  save("fig2-1-original", d);
}

// ── 그림 4-1. 확장 1 변경 전과 후 (확장 1을 마친 시점) ─────────
{
  const d = new Diagram(980, 560);
  d.text(20, 26, "변경 전 (원본)", { bold: true, size: 14 });
  const rb = classBox(d, { x: 20, y: 50, w: 250, name: "Resident", fields: ["-amAlive : boolean", "-willBeAlive : boolean"], methods: ["+figureNextState(Cell × 8) : boolean", "+create() : Cell"] });
  note(d, 20, rb.y + rb.h + 34, ["willBeAlive =", "  (neighbors==3 ||", "   (amAlive && neighbors==2));", "Conway 규칙이 Resident 안에 있다"], { to: [rb.cx, rb.y + rb.h], from: [rb.cx, rb.y + rb.h + 34] });
  d.add(`<line x1="300" y1="20" x2="300" y2="540" stroke="#999" stroke-dasharray="3 4"/>`);
  d.text(325, 26, "변경 후", { bold: true, size: 14 });
  const uni = classBox(d, { x: 325, y: 50, w: 200, name: "Universe", fields: [], methods: ["+Universe()"] });
  const res = classBox(d, { x: 325, y: 250, w: 250, name: "Resident", role: "Context", fields: ["-amAlive : boolean", "-willBeAlive : boolean"], methods: ["+Resident(RuleHolder)", "+figureNextState(Cell × 8) : boolean", "+create() : Cell"] });
  const hold = classBox(d, { x: 640, y: 50, w: 220, name: "RuleHolder", isNew: true, fields: [], methods: ["+current() : Rule", "+change(Rule)"] });
  const rule = classBox(d, { x: 640, y: 250, w: 330, name: "Rule", stereo: "interface", isNew: true, role: "Strategy", fields: null, methods: ["+nextState(alive : boolean, neighbors : int) : boolean"] });
  const cw = classBox(d, { x: 340, y: 450, w: 170, name: "ConwayRule", isNew: true, fields: null, methods: ["+nextState(…) : boolean"] });
  const hl = classBox(d, { x: 560, y: 450, w: 170, name: "HighLifeRule", isNew: true, fields: null, methods: ["+nextState(…) : boolean"] });
  const bs = classBox(d, { x: 780, y: 450, w: 190, name: "BirthSurvivalRule", isNew: true, fields: ["-birth : String", "-survival : String"], methods: ["+BirthSurvivalRule(String)", "+nextState(…) : boolean"] });
  d.text(bs.x + bs.w, bs.y + bs.h + 16, "ConcreteStrategy (셋 모두)", { anchor: "end", size: 12, italic: true, color: BLUE });
  relation(d, [uni.right(), hold.left()], "assoc", { role: "ruleHolder" });
  note(d, 325, 150, ["Rule 메뉴: ruleHolder.change(new HighLifeRule())"], { to: [uni.cx, uni.y + uni.h], from: [uni.cx, 150] });
  relation(d, [res.right(-20), [610, res.cy - 20], [610, hold.y + hold.h - 18], hold.left(22)], "assoc", { role: "holder", roleDx: -6, roleDy: -6 });
  relation(d, [hold.bottom(), [hold.cx, 230], [rule.cx, 230], rule.top()], "assoc", { role: "current  {volatile}", roleDx: 8, roleDy: -6, roleAnchor: "start" });
  relation(d, [res.right(10), [rule.x, res.cy + 10]], "depend", { label: "nextState()", labelAt: [615, res.cy + 4] });
  const yb = 425;
  relation(d, [cw.top(), [cw.cx, yb], [rule.cx - 20, yb], [rule.cx - 20, rule.y + rule.h]], "realize");
  d.add(`<line x1="${hl.cx}" y1="${hl.y}" x2="${hl.cx}" y2="${yb}" stroke-dasharray="6 4"/>`);
  d.add(`<polyline points="${bs.cx},${bs.y} ${bs.cx},${yb} ${rule.cx - 20},${yb}" stroke-dasharray="6 4"/>`);
  d.text(20, 545, "파란 테두리 = 이번 프로젝트에서 새로 만든 클래스.  파란 기울임 글자 = Strategy 패턴에서의 역할", { size: 12, color: BLUE });
  save("fig4-1-rule-class", d);
}

// ── 그림 4-2. 규칙을 바꾼 뒤 한 세대 (지금 코드 기준) ──────────
{
  const d = new Diagram(990, 640);
  const L = lifelines(d, [
    { id: "user", name: "사용자", x: 40, actor: true }, { id: "clock", name: "aClock", x: 140 }, { id: "uni", name: "aUniverse", x: 270 },
    { id: "out", name: "outermostCell", x: 400 }, { id: "inn", name: "innerBlock", x: 520 }, { id: "res", name: "aResident", x: 640 },
    { id: "hold", name: "aRuleHolder", x: 770 }, { id: "rule", name: "aHighLifeRule", x: 905, isNew: true }], 20, 630);
  activation(d, L.uni, 75, 160); message(d, L.user, L.uni - 6, 80, "Rule > HighLife 선택");
  activation(d, L.uni, 100, 150, 6); message(d, L.uni + 6, 0, 98, "changeRule(new HighLifeRule())", { self: true });
  activation(d, L.hold, 125, 140); message(d, L.uni + 12, L.hold - 6, 130, "change(rule)");
  d.text(L.uni + 40, 185, "─ ─ 다음 시계 울림 ─ ─", { size: 12, color: "#555" });
  activation(d, L.clock, 200, 610); message(d, 4, L.clock - 6, 205, "tick()", { anchor: "start", labelX: 60 });
  activation(d, L.uni, 220, 605); message(d, L.clock + 6, L.uni - 6, 225, "tick()  (Listener)");
  activation(d, L.out, 245, 437); message(d, L.uni + 6, L.out - 6, 250, "figureNextState(DUMMY × 8)");
  activation(d, L.inn, 265, 424); message(d, L.out + 6, L.inn - 6, 270, "figureNextState(…)");
  activation(d, L.res, 285, 410); message(d, L.inn + 6, L.res - 6, 290, "figureNextState(이웃 8칸)");
  d.text(L.res + 12, 310, "이웃 수를 센다", { size: 11.5, color: "#555" });
  activation(d, L.hold, 328, 343); message(d, L.res + 6, L.hold - 6, 333, "current()"); message(d, L.hold - 6, L.res + 6, 352, "rule", { ret: true });
  activation(d, L.rule, 372, 387); message(d, L.res + 6, L.rule - 6, 377, "nextState(amAlive, neighbors)"); message(d, L.rule - 6, L.res + 6, 396, "boolean", { ret: true });
  message(d, L.res - 6, L.inn + 6, 410, "boolean", { ret: true });
  message(d, L.inn - 6, L.out + 6, 424, "boolean", { ret: true });
  message(d, L.out - 6, L.uni + 6, 437, "true (바뀔 칸이 있다)", { ret: true });
  activation(d, L.out, 465, 555); message(d, L.uni + 6, L.out - 6, 470, "transition()");
  activation(d, L.inn, 485, 545); message(d, L.out + 6, L.inn - 6, 490, "transition()");
  activation(d, L.res, 505, 530); message(d, L.inn + 6, L.res - 6, 510, "transition()");
  d.text(L.res + 12, 527, "amAlive = willBeAlive", { size: 11.5, color: "#555" });
  note(d, 665, 560, ["4,096개의 Resident가 같은 RuleHolder를 본다.", "규칙을 바꿔도 Resident 코드는 그대로다"]);
  save("fig4-2-rule-seq", d);
}

// ── 그림 5-1. 확장 2 클래스 ─────────────────────────────────
{
  const d = new Diagram(1000, 760);
  const mem = classBox(d, { x: 20, y: 20, w: 230, name: "Cell.Memento", stereo: "interface", fields: null, methods: ["+markAsAlive(Point)"] });
  const col = classBox(d, { x: 20, y: 140, w: 230, name: "LiveCellCollector", isNew: true, fields: ["-cells : Set<Point>"], methods: ["+markAsAlive(Point)", "+cells() : Set<Point>"] });
  const uni = classBox(d, { x: 330, y: 20, w: 300, name: "Universe", fields: ["-boardLock : Object", "-generationNumber : long", "-changeNumber : long"],
    methods: ["-changeRule(Rule)", "-readBoard() : PredictionRequest", "-predictAgain(PredictionRequest)", "-stopClockIfFinished()"] });
  const st = classBox(d, { x: 710, y: 20, w: 270, name: "StatusLine", isNew: true, fields: ["-generation : long", "-change : long"],
    methods: ["+update(generation, cells, rule, change)", "+accept(PredictionRequest, Ending)", "+stateText() : String", "+endingText() : String", "+endingColor() : Color", "+isFinished() : boolean"] });
  const wk = classBox(d, { x: 20, y: 300, w: 250, name: "PredictionWorker", isNew: true, fields: ["-working : boolean"], methods: ["+request(PredictionRequest)", "-work()"] });
  const req = classBox(d, { x: 345, y: 300, w: 270, name: "PredictionRequest", isNew: true, fields: ["-generation : long", "-change : long"], methods: ["+start() : Generation", "+rule() : Rule", "+generation() : long", "+change() : long"] });
  const pr = classBox(d, { x: 345, y: 500, w: 270, name: "EndingPredictor", isNew: true, fields: ["-limit : int"], methods: ["+predict(Generation, Rule) : Ending"] });
  const gen = classBox(d, { x: 345, y: 620, w: 270, name: "Generation", isNew: true, fields: ["-liveCells : Set<Point>", "-width : int"], methods: ["+next(Rule) : Generation", "+liveCount() : int"] });
  const end = classBox(d, { x: 710, y: 420, w: 230, name: "Ending", isNew: true, fields: ["-after : int", "-period : int"], methods: ["+kind() : Kind", "+after() : int", "+period() : int"] });
  const kind = classBox(d, { x: 710, y: 610, w: 230, name: "Ending.Kind", stereo: "enumeration", isNew: true, fields: ["EXTINCT", "STILL", "REPEATING", "UNDECIDED"], methods: null });
  const rule = classBox(d, { x: 40, y: 640, w: 200, name: "Rule", stereo: "interface", fields: null, methods: ["+nextState(boolean, int) : boolean"] });
  relation(d, [col.top(), mem.bottom()], "realize");
  relation(d, [uni.left(-20), [280, uni.cy - 20], [280, col.cy], col.right()], "depend", { label: "«create»", labelAt: [298, col.cy - 8] });
  relation(d, [uni.right(-30), [st.x, uni.cy - 30]], "assoc", { role: "statusLine" });
  relation(d, [[uni.x + 40, uni.y + uni.h], [uni.x + 40, 260], [wk.cx, 260], wk.top()], "assoc", { role: "worker", roleDx: 8, roleDy: -6, roleAnchor: "start" });
  relation(d, [uni.bottom(20), [uni.cx + 20, req.y]], "depend", { label: "«create»", labelAt: [uni.cx + 52, 285] });
  relation(d, [[uni.x + uni.w - 20, uni.y + uni.h], [uni.x + uni.w - 20, 280], [660, 280], [660, pr.cy], pr.right()], "assoc", { role: "predictor", roleDx: 12, roleDy: -6, roleAnchor: "start" });
  relation(d, [wk.right(-20), [req.x, wk.cy - 20]], "assoc", { role: "waiting" });
  relation(d, [[st.x + 40, st.y + st.h], [st.x + 40, 330], [req.x + req.w, 330]], "assoc", { role: "request", roleDx: 8, roleDy: -6, roleAnchor: "start" });
  relation(d, [[st.x + st.w - 40, st.y + st.h], [st.x + st.w - 40, end.y]], "assoc", { role: "ending", roleDx: 6, roleDy: -6, roleAnchor: "start" });
  relation(d, [end.bottom(), kind.top()], "assoc", { role: "kind", roleDx: 6, roleDy: -6, roleAnchor: "start" });
  relation(d, [[req.x + 30, req.y + req.h], [req.x + 30, 480], [325, 480], [325, gen.cy - 10], gen.left(-10)], "assoc", { role: "start", roleDx: -6, roleDy: -6 });
  relation(d, [[req.x, req.y + req.h - 20], [300, req.y + req.h - 20], [300, 620], [rule.cx + 30, 620], [rule.cx + 30, rule.y]], "assoc", { role: "rule", roleDx: 8, roleDy: -6, roleAnchor: "start" });
  relation(d, [pr.right(10), [690, pr.cy + 10], [690, end.cy], end.left()], "depend", { label: "«create»", labelAt: [668, pr.cy + 32] });
  relation(d, [pr.bottom(), gen.top()], "depend", { label: "next()", labelAt: [pr.cx + 28, 612] });
  relation(d, [gen.left(20), [rule.x + rule.w, gen.cy + 20]], "depend", { label: "nextState()", labelAt: [292, gen.cy + 14] });
  save("fig5-1-predict-class", d);
}

// ── 그림 5-2. 클릭부터 예측 표시까지 ───────────────────────
{
  const d = new Diagram(1000, 590);
  const L = lifelines(d, [
    { id: "user", name: "사용자", x: 40, actor: true }, { id: "uni", name: "aUniverse", x: 190 }, { id: "nb", name: "outermostCell", x: 340 },
    { id: "st", name: "aStatusLine", x: 490, isNew: true }, { id: "wk", name: "aPredictionWorker", x: 650, isNew: true }, { id: "pr", name: "anEndingPredictor", x: 840, isNew: true }], 20, 580);
  region(d, 110, 70, 300, 300, "Swing 스레드");
  activation(d, L.uni, 85, 350); message(d, L.user, L.uni - 6, 90, "판 클릭 (mousePressed)");
  region(d, 150, 102, 250, 128, "boardLock 안");
  activation(d, L.nb, 108, 125); message(d, L.uni + 6, L.nb - 6, 113, "userClicked(point, bounds)");
  activation(d, L.uni, 140, 215, 6); message(d, L.uni + 6, 0, 138, "readBoardAfterChange()  // 변경 번호 + 1", { self: true });
  activation(d, L.nb, 170, 190); message(d, L.uni + 12, L.nb - 6, 175, "transfer(collector, STORE)"); message(d, L.nb - 6, L.uni + 12, 202, "살아 있는 칸 좌표", { ret: true });
  activation(d, L.uni, 250, 340, 6); message(d, L.uni + 6, 0, 248, "predictAgain(request)", { self: true });
  activation(d, L.st, 275, 292); message(d, L.uni + 12, L.st - 6, 280, "update(세대, 칸 수, 규칙, 변경 번호)");
  d.text(L.st + 10, 305, "→ calculating...", { size: 11.5, color: "#555" });
  activation(d, L.wk, 312, 328); message(d, L.uni + 12, L.wk - 6, 317, "request(request)"); message(d, L.wk - 6, L.uni + 12, 336, "바로 돌아온다", { ret: true });
  region(d, 560, 380, 400, 100, "prediction 스레드", true);
  activation(d, L.wk, 395, 470); activation(d, L.pr, 405, 440); message(d, L.wk + 6, L.pr - 6, 410, "predict(start, rule)"); message(d, L.pr - 6, L.wk + 6, 452, "ending", { ret: true });
  d.text(L.wk + 14, 430, "Universe가 넘겨준 함수로 부른다", { size: 11.5, color: "#555" });
  d.text(L.pr + 12, 428, "최대 500세대", { size: 11.5, color: "#555" });
  message(d, L.wk - 6, L.uni + 6, 465, "invokeLater(결과 반영)", { async: true });
  region(d, 110, 478, 440, 90, "Swing 스레드");
  activation(d, L.uni, 485, 555); activation(d, L.st, 500, 518); message(d, L.uni + 6, L.st - 6, 505, "accept(request, ending)");
  d.text(L.st + 10, 530, "변경 번호가 다르면 버린다", { size: 11.5, color: "#555" });
  message(d, L.uni + 6, 0, 530, "showStatusText()", { self: true });
  save("fig5-2-predict-seq", d);
}

// ── 그림 5-3. 세대 진행과 자동 정지 ────────────────────────
{
  const d = new Diagram(1000, 450);
  const L = lifelines(d, [{ id: "clock", name: "aClock", x: 80 }, { id: "uni", name: "aUniverse", x: 290 }, { id: "nb", name: "outermostCell", x: 490 }, { id: "st", name: "aStatusLine", x: 710, isNew: true }], 20, 440);
  region(d, 20, 62, 560, 178, "시계 스레드");
  activation(d, L.clock, 70, 228); message(d, 4, L.clock - 6, 75, "tick()", { anchor: "start", labelX: 24 });
  activation(d, L.uni, 90, 222); message(d, L.clock + 6, L.uni - 6, 95, "tick()  (Listener)");
  region(d, 240, 108, 320, 92, "boardLock 안");
  activation(d, L.nb, 120, 136); message(d, L.uni + 6, L.nb - 6, 125, "figureNextState(DUMMY × 8)");
  activation(d, L.nb, 146, 162); message(d, L.uni + 6, L.nb - 6, 151, "[바뀔 칸이 있으면] transition()");
  message(d, L.uni + 6, 0, 168, "++generationNumber; readBoard()", { self: true });
  message(d, L.uni + 6, 0, 205, "invokeLater(상태 반영)  // 기다리지 않는다", { self: true, async: true });
  region(d, 20, 255, 960, 160, "Swing 스레드 (나중에)");
  activation(d, L.uni, 268, 395);
  activation(d, L.st, 285, 302); message(d, L.uni + 6, L.st - 6, 290, "update(세대, 칸 수, 규칙, 변경 번호)  // 남은 세대 − 1");
  activation(d, L.st, 320, 336); message(d, L.uni + 6, L.st - 6, 325, "isFinished()"); message(d, L.st - 6, L.uni + 6, 345, "true  (예측한 소멸·정지에 도달)", { ret: true });
  activation(d, L.clock, 368, 384); message(d, L.uni - 6, L.clock + 6, 373, "stop()");
  save("fig5-3-autostop-seq", d);
}

// ── 그림 5-4. 창 아랫줄(예측한 결말)의 상태 ─────────────────
{
  const d = new Diagram(1000, 590);
  d.add(`<rect x="60" y="30" width="925" height="440" rx="18" fill="#fff"/>`);
  d.text(78, 52, "창 아랫줄 (예측한 결말)", { bold: true, size: 13 });
  d.add(`<line x1="60" y1="62" x2="985" y2="62"/>`);
  initial(d, 92, 125);
  const calc = stateBox(d, { x: 130, y: 95, w: 170, label: "계산 중\ncalculating..." });
  const left = stateBox(d, { x: 470, y: 95, w: 230, label: "결말까지 남음\nstill / extinct / repeats\nin N generations" });
  const reach = stateBox(d, { x: 770, y: 95, w: 200, label: "결말에 도달\nstill / extinct / repeating" });
  const und = stateBox(d, { x: 470, y: 330, w: 230, label: "미확정\nundecided within\n500 generations" });
  transition(d, [[100, 125], calc.left(-5)]);
  transition(d, [calc.right(-14), left.left(-14)], ["결과 도착", "[같은 변경 번호, 소멸·정지·반복, N > 0]"], { at: [385, 128] });
  d.add(`<polyline points="${calc.cx + 40},${calc.y} ${calc.cx + 40},80 ${reach.cx},80 ${reach.cx},${reach.y}" marker-end="url(#m-open)"/>`);
  d.text(600, 76, "결과 도착 [같은 변경 번호, 소멸·정지·반복, N ≤ 0]", { size: 12, anchor: "middle" });
  transition(d, [calc.bottom(30), [calc.cx + 30, und.cy], und.left()], ["결과 도착", "[같은 변경 번호, 미확정]"], { at: [300, und.cy - 30] });
  transition(d, [left.right(), reach.left()], ["세대 진행", "[N = 1]"], { at: [735, left.cy - 22] });
  transition(d, [[left.cx - 60, left.y + left.h], [left.cx - 60, left.y + left.h + 40], [left.cx - 10, left.y + left.h + 40], [left.cx - 10, left.y + left.h]], ["세대 진행 [N > 1] / N ← N − 1"], { at: [left.cx - 35, left.y + left.h + 58] });
  transition(d, [[calc.cx - 50, calc.y + calc.h], [calc.cx - 50, calc.y + calc.h + 40], [calc.cx - 10, calc.y + calc.h + 40], [calc.cx - 10, calc.y + calc.h]], ["결과 도착 [다른 변경 번호]", "/ 버린다"], { at: [calc.cx - 30, calc.y + calc.h + 58] });
  // 어느 하위 상태에서든 일어나는 전이: 상위 상태의 경계에서 나와 계산 중으로
  transition(d, [[330, 470], [330, 515], [36, 515], [36, calc.cy + 12], [calc.x, calc.cy + 12]], ["클릭 · Clear · Load · 규칙 변경 / 변경 번호 + 1   (어느 상태에서든)"], { at: [420, 540] });
  note(d, 760, 250, ["소멸·정지에 도달하면", "Universe가 시계를 멈춘다", "(자동 정지). 반복은 멈추지 않는다"], { to: [reach.cx - 30, reach.y + reach.h], from: [reach.cx - 30, 250] });
  save("fig5-4-status-state", d);
}
