// 전역 상태 (로그인 여부만 저장)
let CHAT_STATE = { loggedIn: false };

// 고정 문구/라벨
const GREETING = '안녕하세요, 무엇을 도와드릴까요?';
const BTN_LABELS = [
    '고인이 유가족일 시 절차',
    '장례식 절차 / 장례식 예절',
    '온라인 추모 공간이란?',
];

// 상세 콘텐츠 (TODO: 실제 텍스트로 교체)
const CONTENT_1 = `... (첫 번째 상세 텍스트) ...`;
const CONTENT_2 = `... (두 번째 상세 텍스트) ...`;
const CONTENT_3 = `... (세 번째 상세 텍스트) ...`;

// 유틸: 말풍선 추가
function pushBubble(text, who = 'bot') {
    const wrap = document.getElementById('chat-content');
    if (!wrap) return;
    const div = document.createElement('div');
    div.className = `chat-bubble ${who}`;
    div.innerHTML = toHtml(text);   // ← HTML 변환된 결과 넣기
    wrap.appendChild(div);
    wrap.scrollTop = wrap.scrollHeight;
}

// AI 답변 보기 좋게 정리 → HTML 변환 (**, •/-, 1. 지원)
function toHtml(s) {
    if (!s) return '';
    // 1) 안전하게 이스케이프
    let t = String(s)
        .replace(/&/g,'&amp;')
        .replace(/</g,'&lt;')
        .replace(/>/g,'&gt;')
        .replace(/\r/g, '');

    // 2) **bold** → <b>bold</b>
    t = t.replace(/\*\*(.+?)\*\*/g, '<b>$1</b>');

    // 3) 문장 끝나면 줄바꿈(간단 규칙)
    t = t.replace(/(다\.|요\.|함\.)(\s+)/g, '$1\n');

    // 4) 불릿 패턴 (*, -, •)을 통일
    t = t.replace(/^\s*([*\-•])\s+/gm, '• ');

    // 5) 라인 단위 파싱(불릿/번호목록 → <ul><li>)
    const lines = t.split('\n');
    let html = '';
    let inList = false;

    for (const line of lines) {
        const L = line.trim();

        if (L === '') {
            if (inList) { html += '</ul>'; inList = false; }
            html += '<br>';
            continue;
        }

        // • 불릿
        if (/^•\s+/.test(L)) {
            if (!inList) { html += '<ul>'; inList = true; }
            html += '<li>' + L.replace(/^•\s+/, '') + '</li>';
            continue;
        }

        // 1. 번호목록
        if (/^\d+\.\s+/.test(L)) {
            if (!inList) { html += '<ul>'; inList = true; }
            html += '<li>' + L + '</li>';
            continue;
        }

        // 일반 문단
        if (inList) { html += '</ul>'; inList = false; }
        html += '<p>' + L + '</p>';
    }
    if (inList) html += '</ul>';

    return html;
}

// 비로그인 첫 화면(일단 임시 3개 버튼)
function renderGuestMenu() {
    const content = document.getElementById('chat-content');
    if (!content) return;
    content.innerHTML = '';
    pushBubble(GREETING, 'bot');

    const actions = document.createElement('div');
    actions.className = 'chat-actions';

    BTN_LABELS.forEach((label, idx) => {
        const b = document.createElement('button');
        b.type = 'button';
        b.textContent = label;
        b.addEventListener('click', (e) => {
            e.preventDefault();
            if (idx === 0) pushBubble(CONTENT_1, 'bot');
            if (idx === 1) pushBubble(CONTENT_2, 'bot');
            if (idx === 2) pushBubble(CONTENT_3, 'bot');
        });
        actions.appendChild(b);
    });

    content.appendChild(actions);
}

// 로그인 첫 화면(입력창 활성화)
function renderMemberChat() {
    const content = document.getElementById('chat-content');
    const inputArea = document.getElementById('chat-input-area');
    if (!content || !inputArea) return;
    content.innerHTML = '';
    pushBubble(GREETING, 'bot');
    inputArea.classList.remove('hidden');
}

// 무조건 모달 열기 (로그인 여부는 '내용'만 분기)
function openChat(e) {
    try { if (e) e.preventDefault(); } catch(_) {}
    const modal = document.getElementById('chat-modal');
    const overlay = document.getElementById('chat-modal-overlay');
    const inputArea = document.getElementById('chat-input-area');
    if (!modal || !overlay || !inputArea) return;

    modal.classList.remove('hidden');
    overlay.classList.remove('hidden');
    inputArea.classList.add('hidden');

    if (CHAT_STATE.loggedIn) renderMemberChat();
    else renderGuestMenu();
}

function closeChat(e) {
    try { if (e) e.preventDefault(); } catch(_) {}
    const modal = document.getElementById('chat-modal');
    const overlay = document.getElementById('chat-modal-overlay');
    const input = document.getElementById('chat-input');
    const content = document.getElementById('chat-content');
    const inputArea = document.getElementById('chat-input-area');
    if (!modal || !overlay || !content || !inputArea) return;

    modal.classList.add('hidden');
    overlay.classList.add('hidden');
    inputArea.classList.add('hidden');
    if (input) input.value = '';
    content.innerHTML = '';
}

let isSending = false;

// 공통 전송 함수
async function sendMessage() {
    const input = document.getElementById('chat-input');
    const inputArea = document.getElementById('chat-input-area');
    if (!input || !inputArea || inputArea.classList.contains('hidden')) return;

    const text = input.value.trim();
    if (!text || isSending) return;

    isSending = true;

    pushBubble(text, 'me');
    input.value = '';

    const finalQuestion = text;

    const loadingId = '__chat_loading_' + Date.now();
    (function () {
        const wrap = document.getElementById('chat-content');
        if (!wrap) return;
        const d = document.createElement('div');
        d.className = 'chat-bubble bot';
        d.id = loadingId;
        d.textContent = '답변 생성 중...';
        wrap.appendChild(d);
        wrap.scrollTop = wrap.scrollHeight;
    })();

    try {
        const res = await fetch('/api/ai/ask', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ q: finalQuestion })
        });

        const ld = document.getElementById(loadingId);
        if (ld) ld.remove();

        if (res.status === 401 || res.status === 403) {
            pushBubble('로그인이 필요합니다. 로그인 후 다시 시도해주세요.', 'bot');
            inputArea.classList.add('hidden');
            renderGuestMenu();
            return;
        }

        if (!res.ok) {
            const t = await res.text().catch(()=>'');
            pushBubble(`오류가 발생했어요. (${res.status}) ${t || ''}`.trim(), 'bot');
            return;
        }

        const ct = res.headers.get('Content-Type') || '';
        let answerText = '';
        if (ct.includes('application/json')) {
            const data = await res.json();
            answerText = (data && (data.answer || data.message || data.result)) ? String(data.answer || data.message || data.result) : '';
        } else {
            answerText = await res.text();
        }

        pushBubble(answerText || '응답이 비어있어요.', 'bot');

    } catch (err) {
        const ld2 = document.getElementById(loadingId);
        if (ld2) ld2.remove();
        pushBubble('네트워크 오류가 발생했어요. 잠시 후 다시 시도해주세요.', 'bot');
    } finally {
        isSending = false;
    }
}

let isComposing = false;

// 초기 바인딩
document.addEventListener('DOMContentLoaded', () => {
    fetch('/user/auth/status', { credentials: 'include' })
        .then(r => (r.ok ? r.json() : { loggedIn: false }))
        .then(d => { CHAT_STATE.loggedIn = !!d.loggedIn; })
        .catch(() => { CHAT_STATE.loggedIn = false; });

    const fab = document.getElementById('chat-fab');
    const closeBtn = document.getElementById('chat-close-btn');
    const overlay = document.getElementById('chat-modal-overlay');
    const sendBtn = document.getElementById('chat-send-btn');
    const input = document.getElementById('chat-input');

    if (fab) fab.addEventListener('click', openChat);
    if (closeBtn) closeBtn.addEventListener('click', closeChat);
    if (overlay) overlay.addEventListener('click', closeChat);
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeChat(e); });

    if (sendBtn) sendBtn.addEventListener('click', (e) => {
        e.preventDefault();
        sendMessage();
    });

    if (input) {
        input.addEventListener('compositionstart', () => { isComposing = true; });
        input.addEventListener('compositionend',   () => { isComposing = false; });

        input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                if (e.shiftKey) return;
                if (isComposing) return;
                e.preventDefault();
                sendMessage();
            }
        });
    }
});

window.__openChat = function(e){
    try { if (e) e.preventDefault(); } catch(_){}
    openChat(e);
    return false;
};

document.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'chat-fab') {
        e.preventDefault();
        openChat(e);
    }
}, true);