"""docs/index.html(웹 버전)에서 안드로이드 앱 화면(app/src/main/assets/index.html)을 만들어요.
앱은 인터넷 권한이 없어서 구글 폰트 링크만 빼요.  사용법: python3 tools/build_app_page.py"""
from pathlib import Path
root = Path(__file__).resolve().parent.parent
s = (root / "docs/index.html").read_text(encoding="utf-8")
keep = [line for line in s.splitlines(keepends=True) if "fonts.googleapis.com" not in line and "fonts.gstatic.com" not in line]
(root / "app/src/main/assets/index.html").write_text("".join(keep), encoding="utf-8")
print("app/src/main/assets/index.html 을 새로 만들었어요")
