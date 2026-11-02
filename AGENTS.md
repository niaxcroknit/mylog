# 마이로그 — 작업 인계 문서

불렛저널 앱 "마이로그"입니다. 화면 전체가 `docs/index.html` 한 파일(HTML·CSS·순수 JavaScript, 빌드 도구·프레임워크·외부 라이브러리 없음)이고, 이 저장소 루트는 그 화면을 WebView로 감싼 안드로이드 스튜디오 프로젝트입니다.

- `docs/index.html`: 원본. 깃허브 페이지(https://niaxcroknit.github.io/mylog/) 첫 화면으로 그대로 공개됩니다.
- `app/src/main/assets/index.html`: 안드로이드 앱 화면. `python3 tools/build_app_page.py`로 원본에서 만듭니다(구글 폰트 링크만 뺌). **원본을 고친 뒤 꼭 다시 실행하세요.**
- `store/`: 플레이 스토어 이미지와 등록 문구. 화면이 크게 바뀌면 스크린샷도 다시 찍어야 합니다.
- 패키지 이름 `com.nia.mylog`(출시 후 변경 불가). 업데이트마다 `app/build.gradle.kts`의 `versionCode`를 1 올립니다.

이 앱은 원래 Claude 아티팩트(claude.ai에 올린 웹페이지)로 만들었습니다. 그래서 계정 동기화에 Claude 전용 기능을 쓰는 부분이 있고, 아래 "저장 방식"에 정리해 두었습니다.

## 사용자와 방향

- 폰에서는 간단하게 보고, 아이패드에서는 더 자세하게 봅니다.
- **부담 없는 저널**이 핵심입니다. 매일 써야 한다는 압박을 주지 않습니다.
  - 연속 일수(streak), 완료율 %, 빨간 미완료 배지, 진행 막대처럼 재촉하는 숫자는 일부러 뺐습니다. 다시 넣지 마세요.
  - 빈 날은 그냥 빈 날로 둡니다. 지난 할 일은 "놓아주기"로 정리할 수 있습니다.
- 사용자가 정한 디자인 규칙 (꼭 지켜 주세요)
  - **흑백만** 씁니다. 흰색·검은색·회색 외의 색은 쓰지 않습니다.
  - **도트(점선) 배경**: 바깥 책상, 흰 페이지, 회색 칸 모두에 점 모눈이 있습니다. 회색 칸 안의 점(`--boxdot`)은 페이지 점보다 조금 연합니다.
  - 페이지는 **A6 비율(105×148)** 입니다. 테두리 선 없이 은은한 그림자로 띄웁니다.
  - 글꼴은 깔끔한 고딕체 **Noto Sans KR**(Google Fonts)입니다.
  - 버튼 글자가 세로로 쪼개지지 않게 합니다(`white-space:nowrap`).
  - 기호 버튼 줄처럼 항목이 많은 줄은 "더보기" 대신 **옆으로 밀어 넘기는 방식**을 씁니다.

## 화면 구조

- `.desk`: 점 배경 위에 위쪽 탭 줄(`#tabList`, 오른쪽 끝 ⚙ `#openSettings`)과 노트(`#book`)가 있습니다.
- `#book`: `#pageA` 한 장 또는 `#pageB`까지 두 장입니다.
  - `layout()`이 화면 크기를 보고 A6 크기를 계산합니다.
  - 두 장이 들어갈 만큼 넓으면 펼친 노트가 되고(`spread`), 페이지 폭이 520px 이상이면 상세 모드(`detail`)가 됩니다.
- 각 페이지 뷰 함수 `vXxx()`는 `{a, b, fa, fb}`를 돌려줍니다.
  - `a`, `b`: 왼쪽·오른쪽 페이지 HTML
  - `fa`, `fb`: 바닥줄에 들어갈 글자
  - 한 장 모드에서 `detail`이면 `b`를 `a` 아래에 이어 붙입니다.
- `render()`가 전체를 다시 그립니다.
  - 입력값, 포커스, 커서 위치, 스크롤 위치는 보존합니다.
  - 그래서 입력칸에는 꼭 **고정된 `id`**를 붙여야 합니다.
- 클릭은 위임 방식입니다. `book`에 붙은 리스너 하나가 `data-act` 값으로 분기합니다(`switch`).
- 아래에서 올라오는 편집 창은 `#sheetRoot`의 `root.onclick` 핸들러로 처리합니다.
- 데일리 페이지에서만 기록 입력창(`#composer`)이 보입니다.

## 페이지 (⚙ 설정에서 켜고 끄고, ≡ 손잡이를 끌어 순서 변경)

| id | 이름 | 내용 |
|---|---|---|
| index | 인덱스 | 페이지 목차, 리스트·퓨처로그·회고 모음, 중요(*) 기록 모아보기 |
| daily | 데일리 | 빠른 기록(rapid log), 완료·이월·놓아주기, 아래에 작은 칸(widgets) |
| diary | 만년다이어리 | 월-일 기준, 해마다 한 칸씩 쌓임. 입력하면 자동 저장 |
| weekly | 위클리 | 월~일 한 주 보기 |
| monthly | 먼슬리 | 폰은 날짜 목록, 상세 모드는 7칸 달력 |
| future | 퓨처로그 | 6개월 단위 계획. 원하는 날의 데일리로 보낼 수 있음 |
| habits | 습관 | 폰은 최근 7일, 상세 모드는 한 달 표. 숫자 없음 |
| mood | 기분 | 1~5단계, 동그라미가 차는 정도(conic-gradient)로 표시 |
| mandal | 만다라트 | 9×9. 폰은 핵심 3×3 → 세부 목표 3×3 순서로 들어감 |
| lists | 리스트 | 이름 붙인 목록들 |
| review | 회고 | 월별 3칸(좋았던 것 / 아쉬웠던 것 / 다음 달에 해볼 것). 자동 저장 |
| dump | 브레인 덤프 | 자유 텍스트 하나. 자동 저장 |
| search | 검색 | 모든 저장소에서 글자 검색 |

- 데일리 아래 작은 칸(WIDGETS): 만년다이어리 한 줄, 오늘 기분, 습관 체크
- 설정값은 `S.settings.pages`와 `S.settings.widgets`(`[{id, on}]`)에 저장합니다.
- 설정값이 없을 때 기본으로 켜지는 페이지: daily, diary, monthly, 그리고 이미 데이터가 있는 future·habits·mandal

## 불렛저널 기호

- 기본 기호: `•` 할 일, `○` 일정, `–` 메모, `!` 아이디어, `?` 알아볼 것, `♡` 감사
- 상태 표시
  - `×` 완료
  - `>` 다른 날로 이월
  - `<` 퓨처로그로 보냄
  - 취소선: 안 하기로 함
  - `released`: 놓아줌
- `*`는 중요 표시입니다.
- 입력 맨 앞에 붙이는 단축 기호: `- ` 메모, `o ` 일정, `! ` 아이디어, `? ` 알아볼 것, `* ` 중요
- 사용자가 직접 기호를 만들 수 있습니다(`S.settings.types`). 기본 기호는 숨길 수 있습니다(`S.settings.hidden`). 단, 할 일은 숨길 수 없습니다.

## 데이터 모델 (`S`)

```
S.days[YYYY-MM-DD] = { entries:[{id,type,text,state,pri,from?,sym?,toggle?}] }
  state: open | done | migrated | scheduled | cancelled | released
S.future = { items:[{id,month:'YYYY-MM',type,text,pri,state}] }
S.habits = { list:[{id,name}], log:{ 'YYYY-MM-DD':[habitId...] } }
S.settings = { types:[{id,sym,name,toggle}], hidden:[builtinId], pages:[{id,on}], widgets:[{id,on}] }
S.mandal = { center, subs:[8 × {title, actions:[8 × {text,done}]}] }
S.diary[MM][DD][YYYY] = '텍스트'
S.lists = { lists:[{id,name,items:[{id,text,done}]}] }
S.mood['YYYY-MM-DD'] = 1..5
S.reviews['YYYY-MM'] = { good?, meh?, next? }
S.dump = { text }
```

## 저장 방식

`persist(key)`가 localStorage `bujo.v2`에 저장하고, 그 문서를 "아직 안 올라간 것"(`dirty`, localStorage `bujo.dirty`)으로 표시합니다.

- 계정 동기화(Claude 아티팩트의 `db` 기능, `window.claude.use('db')`)가 있으면 `flush()`가 올립니다. 문서 id: `YYYY-MM-DD`, `future`, `habits`, `settings`, `mandal`, `diary-MM`, `lists`, `mood`, `reviews`, `dump`
- 실패하면 3초부터 최대 60초까지 늘려 가며 다시 시도하고, 연결이 끊기면 다시 연결합니다(`scheduleRetry`, `connect`).
- 날짜 문서와 `future`는 **항목 단위로 합칩니다**(`mergeList`). 지운 항목 id는 `del` 배열(최근 300개)에 남겨서 다른 기기에서 되살아나지 않게 합니다. 이 두 종류는 올린 뒤에도 스냅샷이 내 항목을 모두 담고 있는 걸 확인할 때까지(`covered`) 표시를 유지합니다.
- 나머지 문서는 나중에 쓴 쪽이 이깁니다.
- 처음 연결할 때는 계정 쪽이 비어 있을 때만 이 기기 데이터를 통째로 올립니다. 그 밖에는 `dirty` 문서만 올립니다.
- `window.claude`가 없는 환경(안드로이드 앱, 일반 브라우저)에서는 localStorage만 씁니다.

백업: `exportBackup()`은 `{app:'bujo-journal', version:3, exportedAt, data:S}` JSON을 만듭니다. 안드로이드 앱이면 `window.AndroidBridge.saveBackup(name, json)`, Claude에서는 `downloads` 기능, 둘 다 없으면 복사용 글상자를 보여줍니다. `importBackup()`은 전체를 바꾸고 되돌리기를 제공합니다.

되돌리기: `withUndo(keys, msg, fn)`이 지우기 직전 상태를 들고 있다가 토스트의 "되돌리기"로 되살립니다.

## 안드로이드 앱

저장소 루트가 안드로이드 스튜디오 프로젝트입니다. 앱은 인터넷 권한이 없고, 화면은 `app/src/main/assets/index.html`입니다.

- `MainActivity.kt`: WebView + `WebViewAssetLoader`(`https://appassets.androidplatform.net/assets/index.html`)
- 웹 → 앱: `AndroidBridge.saveBackup(name, json)` (안드로이드 "다른 이름으로 저장")
- 앱 → 웹: 뒤로 가기 때 `window.bujoBack()`(true면 앱이 닫히지 않음), 알림은 `window.bujoToast(msg)`
- 백업 불러오기는 웹의 `<input type=file>`을 `onShowFileChooser`가 받아 처리합니다.

## 알아둘 점

- 날짜 키는 로컬 시간 기준입니다(`todayKey()`). 자정이 지나거나 앱으로 돌아오면 `checkToday()`가 오늘 날짜를 갱신합니다.
- 다크 모드: `prefers-color-scheme`와 `[data-theme]` 둘 다에 같은 토큰을 다시 정의합니다. 검은 바탕에 흰 글씨입니다.
- 이 앱이 쓰던 아티팩트 환경에서는 `alert`/`confirm`이 동작하지 않았습니다. 그래서 삭제는 "한 번 더 누르면 삭제"(`armDelete`) 방식으로 확인합니다.
- 설정의 드래그 정렬은 Pointer Events와 `setPointerCapture`로 만들었습니다. 키보드로는 손잡이에 포커스를 두고 ↑/↓ 키로 옮깁니다.
- 펼친 노트(spread)에서 오른쪽 페이지 짝은 `S.settings.pairs`(`{왼쪽id: 오른쪽id}`)로 정합니다.
- 좌우로 밀면 날짜·주·달이 넘어갑니다(`touchstart`/`touchend`).
