// 정적 데모 보조 스크립트: 실시간 스트림 재생, 저장 요청 차단, 데모 안내
(function () {
  "use strict";

  var TOTAL = 20000;
  var FINAL = {
    status: { messageCreateMessage: "DONE", batchKafkaSent: "DONE", batchJob: "DONE", messageSentMessage: "PENDING" },
    counts: { batchCount: 19998, kafkaSentCount: 19998, kafkaReceiveCount: 0, createMessageCount: 19998, sentMessageCount: 19998, totalCount: TOTAL, settlementMonth: "2025-12" },
  };

  function rate(n) {
    return Math.round((n / TOTAL) * 10000) / 100;
  }

  // 정산 → Kafka 전송 → 메시지 생성 → 발송 단계별 진행 재생
  function processFrames() {
    var frames = [];
    var steps = 16;
    for (var i = 1; i <= steps; i++) {
      var f = i / steps;
      var stage = function (offset) {
        return Math.max(0, Math.min(1, (f - offset) / 0.55));
      };
      var b = Math.round(FINAL.counts.batchCount * stage(0));
      var k = Math.round(FINAL.counts.kafkaSentCount * stage(0.15));
      var c = Math.round(FINAL.counts.createMessageCount * stage(0.3));
      var s = Math.round(FINAL.counts.sentMessageCount * stage(0.45));
      var st = function (n, done) {
        return n >= done ? "DONE" : n > 0 ? "PENDING" : "WAITED";
      };
      frames.push({
        status: {
          batchJob: st(b, FINAL.counts.batchCount),
          batchKafkaSent: st(k, FINAL.counts.kafkaSentCount),
          messageCreateMessage: st(c, FINAL.counts.createMessageCount),
          messageSentMessage: i === steps ? FINAL.status.messageSentMessage : st(s, FINAL.counts.sentMessageCount),
        },
        counts: {
          batchCount: b, kafkaSentCount: k, kafkaReceiveCount: 0, createMessageCount: c, sentMessageCount: s, totalCount: TOTAL,
          batchRate: rate(b), kafkaSentRate: rate(k), kafkaReceiveRate: 0, createMessageRate: rate(c), sentMessageRate: rate(s),
          settlementMonth: "2025-12",
        },
      });
    }
    return frames;
  }

  // 배치 파티션 10개 처리 진행 재생 (스킵 2건, 실행 #5 기준)
  function batchFrames() {
    var frames = [];
    var steps = 12;
    for (var i = 0; i <= steps; i++) {
      var f = i / steps;
      var done = Math.round(19998 * f);
      var parts = [];
      for (var p = 0; p < 10; p++) {
        var w = Math.round(2000 * Math.min(1, f * (0.9 + p * 0.02)));
        parts.push({ stepName: "workerStep:partition" + p, status: f >= 1 ? "COMPLETED" : "STARTED", readCount: w, writeCount: w, commitCount: Math.ceil(w / 100), skipCount: 0 });
      }
      var left = Math.max(0, Math.round((1 - f) * 6));
      frames.push({
        progress: ((done / TOTAL) * 100).toFixed(2) + "%",
        totalProcessed: done,
        partitions: parts,
        tps: f < 1 ? 3300 + Math.round(Math.sin(i) * 400) : 0,
        etc: "00:00:0" + left,
        skipCount: f >= 1 ? 2 : 0,
        recentErrors: null,
        currentJobId: 5,
        completed: false,
        isCompleted: false,
      });
    }
    return frames;
  }

  function DemoEventSource(url) {
    this.url = url;
    this.readyState = 1;
    this.onmessage = null;
    this.onerror = null;
    this.onopen = null;
    this._listeners = {};
    this._timers = [];
    var self = this;
    setTimeout(function () {
      self._play();
    }, 400);
  }
  DemoEventSource.prototype.addEventListener = function (type, fn) {
    (this._listeners[type] = this._listeners[type] || []).push(fn);
  };
  DemoEventSource.prototype.removeEventListener = function () {};
  DemoEventSource.prototype.close = function () {
    this.readyState = 2;
    this._timers.forEach(clearTimeout);
  };
  DemoEventSource.prototype._emit = function (type, payload) {
    var ev = { type: type, data: JSON.stringify(payload) };
    if (type === "message" && typeof this.onmessage === "function") this.onmessage(ev);
    (this._listeners[type] || []).forEach(function (fn) {
      fn(ev);
    });
  };
  DemoEventSource.prototype._play = function () {
    var self = this;
    if (this.url.indexOf("/admin/batch/realtime/stream") === 0) {
      batchFrames().forEach(function (fr, i) {
        self._timers.push(setTimeout(function () {
          self._emit("batchUpdate", fr);
        }, i * 500));
      });
      return;
    }
    // 정산 전체 흐름 스트림
    processFrames().forEach(function (fr, i) {
      self._timers.push(setTimeout(function () {
        self._emit("message", fr.status);
        self._emit("message", fr.counts);
      }, i * 450));
    });
  };
  window.EventSource = DemoEventSource;

  function notice(msg) {
    var el = document.getElementById("demo-toast");
    if (!el) {
      el = document.createElement("div");
      el.id = "demo-toast";
      el.style.cssText = "position:fixed;left:50%;bottom:28px;transform:translateX(-50%);background:#1f2937;color:#fff;padding:10px 16px;border-radius:10px;font-size:14px;z-index:99999;box-shadow:0 6px 20px rgba(0,0,0,.25);transition:opacity .3s";
      document.body.appendChild(el);
    }
    el.textContent = msg;
    el.style.opacity = "1";
    clearTimeout(el._t);
    el._t = setTimeout(function () {
      el.style.opacity = "0";
    }, 2600);
  }

  document.addEventListener("DOMContentLoaded", function () {
    // 데모 안내 배지
    var badge = document.createElement("div");
    badge.innerHTML = "<b>DEMO</b> · 2025년 12월 정산 스냅샷 · 합성 데이터 2만 명";
    badge.style.cssText = "position:fixed;right:16px;bottom:16px;background:#fff7e6;color:#8a5d00;border:1px solid #f3d29a;padding:6px 12px;border-radius:999px;font-size:12px;z-index:99998;font-family:system-ui,sans-serif";
    document.body.appendChild(badge);

    // 템플릿 조회는 정적 JSON, 저장·수정·삭제는 안내만
    if (window.jQuery) {
      var orig = window.jQuery.ajax;
      window.jQuery.ajax = function (opts) {
        var o = typeof opts === "string" ? { url: opts } : opts || {};
        var method = (o.type || o.method || "GET").toUpperCase();
        var m = (o.url || "").match(/^\/admin\/process\/templates\/(\d+)$/);
        if (method === "GET" && m) {
          o.url = "/demo-data/templates/" + m[1] + ".json";
          o.dataType = "json";
          return orig.call(this, o);
        }
        if (method !== "GET") {
          notice("데모에서는 저장·수정·삭제가 반영되지 않습니다.");
          var d = window.jQuery.Deferred();
          d.reject({ status: 0, responseText: "demo" }, "demo");
          return d.promise();
        }
        return orig.apply(this, arguments);
      };
    }

    // 검색 폼 제출 차단
    document.querySelectorAll("form").forEach(function (form) {
      form.addEventListener("submit", function (e) {
        e.preventDefault();
        notice("데모에서는 검색이 지원되지 않습니다. 목록에서 파일을 눌러 확인해 주세요.");
      });
    });
  });
})();
