# 로컬에서 실행 중인 billing_api 화면을 정적 데모(demo/)로 내보내기
# 사용: python3 scripts/export_static_demo.py http://localhost:8083
import html
import json
import os
import re
import shutil
import sys
import urllib.request

BASE = sys.argv[1].rstrip("/") if len(sys.argv) > 1 else "http://localhost:8083"
ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), "demo")
REPO = os.path.dirname(ROOT)
SETTLE_PAGES = 10


def get(path):
    with urllib.request.urlopen(BASE + path, timeout=60) as r:
        return r.read().decode("utf-8")


def save(rel, text):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)


# 원본 URL → 정적 경로 (cleanUrls)
pages = {
    "/admin/process/dashboard": "admin/process/dashboard",
    "/admin/message/dashboard": "admin/message/dashboard",
    "/admin/batch/dashboard": "admin/batch/dashboard",
    "/admin/users/products/bar": "admin/users/products/bar",
    "/admin/users/products/donut": "admin/users/products/donut",
    "/admin/users/settlements": "admin/users/settlements",
    "/admin/process/templates/management": "admin/process/templates/management",
    "/admin/process/templates/management?messageType=SMS": "admin/process/templates/management/sms",
    "/admin/process/templates/management?messageType=EMAIL": "admin/process/templates/management/email",
    "/admin/logs": "admin/logs",
}
for n in range(SETTLE_PAGES):
    pages[f"/admin/users/settlements?settlementMonth=2025-12&page={n}"] = f"admin/users/settlements/page-{n}"

logs_html = get("/admin/logs")
log_files = sorted(set(re.findall(r'/admin/logs/view/([\w.]+\.log)', logs_html)))
for name in log_files:
    pages[f"/admin/logs/view/{name}"] = f"admin/logs/view/{name[:-4]}"

history = json.loads(get("/admin/batch/history"))
job_ids = sorted({int(x) for x in re.findall(r'"(?:executionId|jobExecutionId|id)"\s*:\s*(\d+)', json.dumps(history))})
for jid in job_ids:
    pages[f"/admin/batch/error/list/{jid}"] = f"admin/batch/error/list/{jid}"

link_map = {k: "/" + v for k, v in pages.items()}
for name in log_files:
    link_map[f"/admin/logs/download/{name}"] = f"/logs/{name}"
link_map["/"] = "/admin/process/dashboard"

SHIM = '<script src="/assets/demo-shim.js"></script>'


def rewrite(text):
    text = text.replace("<head>", "<head>\n" + SHIM, 1)

    def repl(m):
        attr, url = m.group(1), html.unescape(m.group(2))
        if url in link_map:
            return f'{attr}="{link_map[url]}"'
        sm = re.match(r"/admin/users/settlements\?settlementMonth=2025-12&page=(\d+)$", url)
        if sm:  # 내보내지 않은 페이지는 마지막 페이지로
            return f'{attr}="/admin/users/settlements/page-{min(int(sm.group(1)), SETTLE_PAGES - 1)}"'
        return m.group(0)

    text = re.sub(r'(href|action)="([^"]+)"', repl, text)
    # 템플릿 유형 필터 → 정적 경로
    text = text.replace("if(type) url += '?messageType=' + type;", "if(type) url += '/' + type.toLowerCase();")
    # 배치 화면 총건수: 원 설계값 100만 → 데모 데이터 2만
    text = text.replace("1,000,000", "20,000").replace("1000000", "20000")
    return text


for url, rel in pages.items():
    save(rel + ".html", rewrite(get(url)))
    print("page", rel)

# 템플릿 상세 (수정 모달용 JSON)
mgmt = get("/admin/process/templates/management")
for tid in sorted(set(re.findall(r"(?:openEditModal|editTemplate|templateId)\D{0,6}(\d+)", mgmt))):
    try:
        save(f"demo-data/templates/{tid}.json", get(f"/admin/process/templates/{tid}"))
    except Exception as e:  # 없는 ID 무시
        print("skip template", tid, e)

# 정적 자원·로그 원본
shutil.copytree(os.path.join(REPO, "billing_api/src/main/resources/static/css"), os.path.join(ROOT, "css"), dirs_exist_ok=True)
os.makedirs(os.path.join(ROOT, "logs"), exist_ok=True)
for name in log_files:
    shutil.copy(os.path.join(REPO, "logs/billing_audit", name), os.path.join(ROOT, "logs", name))
print(f"pages={len(pages)} logs={len(log_files)} jobs={job_ids}")
