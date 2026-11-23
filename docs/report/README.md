# 프로젝트 문서(보고서) 원본

제출하는 보고서 PDF의 원본을 모아 둔다. 각 쪽은 HTML로 쓰고 Chrome의 PDF 인쇄로 만든다.

| 파일 | 내용 |
|---|---|
| `cover.html` | 표지 |
| `cau-logo.png` | 표지의 중앙대 로고. 학교 홈페이지 상단 로고 파일을 받은 것이다 |
| `usecase.mjs` | 유스케이스 다이어그램을 그리는 코드. `usecase.svg`와 `usecase.html`을 만든다 |
| `usecase.svg` | 유스케이스 다이어그램 (3장) |

## PDF 만들기

`holub-life` 폴더에서 실행한다.

```bash
node docs/report/usecase.mjs docs/report
```

```bash
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
"$CHROME" --headless=new --no-pdf-header-footer --print-to-pdf=/tmp/cover.pdf "file://$PWD/docs/report/cover.html"
"$CHROME" --headless=new --no-pdf-header-footer --print-to-pdf=/tmp/usecase.pdf "file://$PWD/docs/report/usecase.html"
```

만든 PDF는 저장소에 넣지 않는다. 제출할 때 한 번에 만든다.
