# KGU QUIZ — ref.pdf 기반 Java Swing UI 데모

현재 버전은 사용자가 제공한 `ref.pdf`, 거리뷰 자산, `경기대 소나기 게임.zip`을 현재 레포에 병합한 Java Swing 데모입니다. 첫 번째 선택지는 기존 베이스 창 내부에서 `KGU CAMPUS RAIN` 건물 타자 게임을 열고, 두 번째 선택지는 접시 슈팅, 세 번째 선택지는 건물 퀴즈와 거리뷰 탐색으로 이어집니다.

## 실행

Java 17 이상에서 다음 명령으로 실행합니다. 이미지와 글꼴은 JAR에 포함되어 있어 JAR 하나만 복사해 실행할 수 있습니다.

```sh
java -jar CampusQuiz-demo.jar
```

소스 실행(JDK 17 이상): Linux/macOS는 `./run.sh`, Windows는 `run.bat`. IDE에서는 프로젝트 루트에서 `src/CampusQuiz.java`의 `main()`을 실행하세요. 소나기 게임 단독 실행이 필요하면 `src/CampusRain.java`의 `main()`을 사용할 수 있습니다.

JAR 재생성: `python3 scripts/build_demo.py`.

## PDF 화면과 Academic Life 게임

| PDF | 구현 화면 |
|---|---|
| 1 | 시작: 캠퍼스 사진·원본 손글씨·시작 버튼 |
| 2 | 이용 안내 |
| 3 | 사진을 사용한 3개 파트 선택 |
| 4 | 파트 소개 |
| 5 | 답변 선택 전 문제 (Part 01·03) |
| 6 | 검은 배경으로 표시하는 선택 답안 (Part 01·03) |
| 7 | 정답 해설 |
| 8 | 오답·정답 비교와 해설 |
| 9 | 종료 확인 모달 |
| 10 | 파트 완료 |
| 11 | 최종 결과 |
| 12 | 오답 복습 목록 |
| 13 | 오답 상세 복습 |
| 추가 | ACADEMIC LIFE 접시 슈팅 게임 |
| 추가 | 첫 번째 선택지 `KGU CAMPUS RAIN` 건물 타자 게임·결과 화면 |
| 추가 | BUILDING QUIZ 완료 후 거리뷰 탐색 게임·정답 장소 도착 |

## 화면 체험

시작 → 파트 선택 → (1번) 소나기 게임 또는 (2번) 접시 슈팅 또는 (3번) 건물 퀴즈 → 거리뷰 탐색 → 최종 결과.

첫 번째 카드의 `KGU CAMPUS RAIN`은 압축본의 완성된 게임 로직·UI를 현재 베이스 창 안에 임베드합니다. 기존 1000×1080 창 크기에 맞춰 게임 영역을 위아래로 확장해 표시하며, 건물 이름이 비처럼 떨어집니다. 입력창에 이름을 입력하고 Enter 또는 `입력 ↵` 버튼으로 제출합니다. 정답은 설명과 함께 제거되고, 오타는 생명을 줄이지 않으며, 이름이 제한선을 넘으면 하트가 하나 줄어듭니다. 생명 5개를 모두 잃으면 첫 번째 파트 결과 화면으로 전환되고, 다음 파트 시작 또는 파트 선택을 고를 수 있습니다. 게임의 `종료` 버튼을 누르면 퀴즈의 파트 선택 화면으로 돌아갑니다.

두 번째 파트는 레트로 오락실/도트풍 접시 슈팅입니다. 시작 직후 빈 스테이지에서 준비한 뒤, Swing 타이머가 좌우 양쪽에서 접시를 랜덤하게 생성합니다. 접시는 수평 이동이 빠른 중력 기반 포물선 궤적으로 날아오고, 마우스 에임이 따라오며 클릭 시 총구·트레이서 효과가 표시됩니다. 정답 접시는 10점을 얻고, 오답 접시는 오답으로 기록되며 결과 화면의 버튼을 눌러 다음 문제로 이동합니다.

세 번째 파트는 건물 사진 퀴즈를 완료한 뒤 거리뷰 화살표를 탐색해 `street-answer.jpg`의 정답 장소를 찾는 흐름입니다. 연결이 없는 방향은 숨기고, 정답 위치에 도착하면 최종 결과로 이동합니다. 기존 종료 모달·오답 복습·거리뷰 흐름은 유지됩니다.

UI 데모를 위한 임시 상태만 존재합니다. 소나기 게임의 기록은 해당 창의 현재 회차에만 유지되며, 계정·서버·DB 저장은 없습니다. 최종 소요 시간 `04:32` 등 결과 일부는 데모 값이며, 화면 문구는 디자인을 따라 표시됩니다.

## 크기와 리소스

1000×1080 논리 좌표를 기준으로 전체 화면과 버튼 영역을 같은 비율로 확대·축소합니다. 기본 창은 800×864, 최소 창은 600×648이며 스크롤은 없습니다. 넓은 창에서는 비율을 유지하고 여백을 둡니다.

PDF의 타이틀 바를 포함한 디자인을 재현하기 위해 창 장식은 Swing에서 그립니다. 최소화·최대화·닫기와 타이틀 바 드래그를 지원합니다.

사진, 손글씨, 결과 아이콘을 PDF에서 추출해 로컬 파일로 포함했습니다. 전체 UI를 PNG 한 장으로 붙이지 않고 텍스트·버튼·진행 막대·패널을 직접 구성했습니다. 글꼴은 Noto Sans 계열을 번들링했으며 Java의 글꼴 렌더링 차이로 원본의 모든 글자 윤곽이 동일하지는 않습니다. [리소스 출처와 배치](assets/README.md)

## 미리보기

`./run.sh --preview` 또는 `java -jar CampusQuiz-demo.jar --preview`로 실제 Swing 화면 PNG를 생성합니다.

- [시작](previews/pdf-design/01-welcome.png)
- [파트 선택](previews/pdf-design/03-parts.png)
- [내부 임베드 소나기 게임](previews/pdf-design/rain-game.png)
- [소나기 게임 결과](previews/pdf-design/rain-result.png)
- [ACADEMIC LIFE 접시 게임](previews/pdf-design/plate-game.png)
- [거리뷰 탐색 게임](previews/pdf-design/street-game.png)
- [거리뷰 정답 도착](previews/pdf-design/street-found.png)
- [선택 답안](previews/pdf-design/06-selected.png)
- [오답 해설](previews/pdf-design/08-incorrect.png)
- [종료 확인](previews/pdf-design/09-exit.png)
- [최종 결과](previews/pdf-design/11-final.png)
- [오답 복습](previews/pdf-design/13-review_detail.png)

기존 `previews/light-*`, `previews/dark-*`, `design/` 자료는 이전 버전 기록이며 현재 기준은 `ref.pdf`와 `previews/pdf-design/`입니다.

## 검증

Java 17 타깃 컴파일, 18개 화면 시각 검토 및 4개 창 크기에서 총 72회 렌더링을 확인했습니다. 소나기 게임은 별도 테스트에서 89개 규칙·Swing·렌더링 검사를 통과했습니다. 메인 UI는 답안 선택 전 비활성 상태, 정답/오답 해설, 모달의 배경 조작 차단·취소·종료, 접시 5회 명중, 소나기 게임 세로 확장·결과 전환·다음 파트 연결, 거리뷰 연결·정답 도착, 최종 결과, 복습 이동과 초기화를 검증했습니다.

```sh
javac --release 17 -encoding UTF-8 -cp out -d out checks/UiSmoke.java
java -cp out UiSmoke
```

현재 환경은 X11 디스플레이 연결이 불가능해 실제 운영체제 창의 수동 조작은 확인하지 못했습니다. 자동 검증은 메인 UI 72회 화면 렌더링·거리뷰 경로 탐색과 소나기 게임 89개 검사를 포함합니다.
