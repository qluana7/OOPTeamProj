# ref.pdf 디자인 리소스

퀴즈 화면의 사진과 손글씨·결과 아이콘 원본은 사용자가 제공한 `ref.pdf`입니다. 거리뷰와 소나기 게임 자산도 모두 레포에 로컬로 포함하며, 실행 중 Figma나 외부 서버에 접속하지 않습니다.

| 파일 | PDF 사용 위치 | 구현 슬롯(1000×1080 논리 좌표) |
|---|---|---|
| images/welcome-campus.png | 1페이지 배경 사진 | 300,40 / 700×1040 |
| images/part-campus.png | 3페이지 01 카드 사진 | 38,295 / 924×225 |
| images/academic.png | 3페이지 02 카드 사진 | 38,535 / 924×225 |
| images/program.png | 3페이지 03 카드 사진 | 38,775 / 924×225 |
| images/intro-campus.png | 4페이지 파트 소개 사진 | 58,156 / 884×345 |
| images/question-campus.png | 5·6·9페이지 문제 사진 | 442,172 / 498×276 |
| images/feedback-campus.png | 7·8페이지 해설 배경 | 0,40 / 510×1040 |
| images/result-campus.png | 11페이지 결과 배경 | 630,40 / 370×1040 |
| images/good-start.png | 1페이지 손글씨 | 680,245 / 190×175 |
| images/start-script.png | 1페이지 시작 손글씨 | 235,780 / 230×110 |
| images/nice-work.png | 11페이지 결과 손글씨 | 740,235 / 170×145 |
| images/trophy.png | 11페이지 정답 아이콘 | 241,605 / 48×47 |
| images/clock.png | 11페이지 시간 아이콘 | 475,605 / 48×47 |
| images/star.png | 11페이지 등급 아이콘 | 709,605 / 48×47 |
| images/celebration.png | 11페이지 축하 아이콘 | 421,134 / 29×29 |
| images/street-1.jpg … street-13.jpg | 압축본에서 추가된 거리뷰 탐색 사진 | 58,160 / 884×540 |
| images/street-answer.jpg | 거리뷰에서 찾아야 하는 정답 장소 | 760,746 / 160×94 및 정답 화면 |
| rain/campus-background.png | `KGU CAMPUS RAIN` 배경 | 소나기 게임 1280×800 화면 전체 |
| rain/buildings.txt | 소나기 게임 건물 이름·설명·별칭 데이터 | `CampusRain.loadBuildings()` |
| rain/background-prompt.txt | 소나기 게임 배경 생성 프롬프트 기록 | 실행에는 사용하지 않음 |

사진은 PDF에 내장된 래스터의 실제 사진 영역만 원래 배치 행렬에 따라 추출했습니다. 손글씨·아이콘은 PDF 벡터 경로를 개별 SVG로 추출하고 투명 PNG로 렌더링했습니다. 전체 화면 이미지를 UI 배경으로 사용하지 않습니다.

실제 화면은 `src/CampusQuiz.java`의 Java2D 도형·텍스트와 Swing 버튼으로 구성됩니다. PDF의 낮은 원본 사진 해상도는 유지됩니다.

거리뷰 사진은 사용자가 제공한 `reference/객프 (2).zip`의 수정본에서 가져왔으며, JAR에 로컬 자산으로 포함됩니다. 런타임에는 외부 서버나 Figma에 접속하지 않습니다.

소나기 게임 자산은 사용자가 제공한 `reference/경기대 소나기 게임.zip`의 수정본에서 가져왔습니다. `src/CampusRain.java`가 입력·낙하·생명·일시정지 규칙을 담당하고, 현재 퀴즈의 파트 선택 첫 번째 카드에서 별도 Swing 창으로 실행됩니다.

글꼴은 Noto Sans CJK KR Regular/Bold/Black에서 UI가 사용하는 문자를 추린 서브셋입니다. 배포 라이선스는 `fonts/NOTO-LICENSE.txt`입니다. PDF는 글꼴을 FigmaGlyphs Type 3 윤곽선으로 내보냈으므로 원본 폰트 파일명이 보존되지 않았으며, 기존 리소스 내 안내의 Noto Sans KR 계열에 맞췄습니다. 손글씨는 대체 폰트 대신 PDF 원본 경로를 사용합니다.
