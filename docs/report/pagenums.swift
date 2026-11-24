// PDF에서 각 쪽의 글자를 읽어, 목차에 적을 장·절 제목이 몇 쪽에 있는지 찾는다. build.mjs가 부른다.
// 사용: swift pagenums.swift report.pdf "4-3.적용" "5.확장" ...   →  한 줄에 하나씩 쪽 번호(못 찾으면 0)
import PDFKit

let args = CommandLine.arguments
guard let doc = PDFDocument(url: URL(fileURLWithPath: args[1])) else { exit(1) }
// 공백과 줄바꿈을 모두 지우고 비교한다. 제목이 줄 끝에서 나뉘어도 찾을 수 있게
let pages = (0..<doc.pageCount).map { ($0 + 1, (doc.page(at: $0)?.string ?? "").filter { !$0.isWhitespace }) }
for heading in args.dropFirst(2) {
  let key = heading.filter { !$0.isWhitespace }
  // 1·2쪽(표지, 목차)은 건너뛴다. 목차에도 같은 글자가 있기 때문이다
  let found = pages.first { $0.0 >= 3 && $0.1.contains(key) }
  print(found?.0 ?? 0)
}
