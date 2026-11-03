"""docs/index.html(웹 버전)에서 안드로이드 앱 화면(app/src/main/assets/index.html)을 만들어요.
앱은 인터넷 권한이 없어서 구글 폰트 링크와 웹 주소용 아이콘 설정만 빼요.  사용법: python3 tools/build_app_page.py"""
from pathlib import Path
root = Path(__file__).resolve().parent.parent
s = (root / "docs/index.html").read_text(encoding="utf-8")
# 앱에는 필요 없는 것: 구글 폰트, 웹 주소용 아이콘·바로가기 설정
skip = ("fonts.googleapis.com", "fonts.gstatic.com", 'rel="icon"', 'rel="apple-touch-icon"', 'rel="manifest"', 'name="theme-color"', 'name="apple-mobile-web-app-title"', 'name="application-name"')
keep = [line for line in s.splitlines(keepends=True) if not any(k in line for k in skip)]
(root / "app/src/main/assets/index.html").write_text("".join(keep), encoding="utf-8")
print("app/src/main/assets/index.html 을 새로 만들었어요")
