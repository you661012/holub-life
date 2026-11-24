# 프로젝트 문서(보고서) 원본

제출하는 보고서 PDF의 원본이다. 장마다 HTML 조각으로 쓰고, `build.mjs`가 이어 붙여 Chrome의 PDF 인쇄로 만든다.

## PDF 만들기

`holub-life` 폴더에서 실행한다. `docs/report/report.pdf`가 생긴다.

```bash
node docs/report/build.mjs
```

PDF를 한 번 찍은 뒤 장·절 제목이 몇 쪽에 있는지 읽어 목차에 쪽 번호를 넣고 한 번 더 찍는다. 쪽을 읽는 데 macOS의 `swift`(PDFKit)를 쓴다. 15초쯤 걸린다.

만든 PDF와 중간 파일(`report.html`, `usecase.html`)은 저장소에 넣지 않는다.

## 파일

| 파일 | 내용 |
|---|---|
| `chapters/00-cover.html` ~ `08-github.html` | 표지·목차와 1~8장. 파일 이름 순서대로 이어 붙인다 |
| `report.css` | 공통 모양 (A4, 쪽 번호, 표, 코드, 표지) |
| `pagenums.swift` | PDF에서 장·절 제목이 있는 쪽을 찾는다. `build.mjs`가 목차 쪽 번호를 넣을 때 부른다 |
| `uml.mjs` | UML 그림을 그리는 작은 도구. 표기는 수업 자료의 그림을 따른다 |
| `diagrams.mjs` | 보고서의 UML 그림(`fig*.svg`)을 만든다. `build.mjs`가 먼저 실행한다 |
| `usecase.mjs`, `usecase.svg` | 3장 유스케이스 다이어그램 |
| `img/` | 실행 화면, 테스트 결과, GitHub 화면 캡처 |
| `cau-logo.png` | 표지의 중앙대 로고. 학교 홈페이지 상단 로고 파일을 받은 것이다 |

## 내용의 출처

4·5장의 설계 결정은 `docs/decisions/`의 ADR 001~005와 `docs/features/확장2-결말-예측.md`를 옮겨 정리했다. 숫자는 `docs/experiments/`의 실험 코드로 잰 것이다.
