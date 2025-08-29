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
    div.textContent = text;
    wrap.appendChild(div);
    wrap.scrollTop = wrap.scrollHeight;
}

// 비로그인 첫 화면(3개 버튼)
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
    inputArea.classList.add('hidden'); // 기본은 숨김 (회원일 때만 보임)

    if (CHAT_STATE.loggedIn) renderMemberChat();
    else renderGuestMenu();

    // 디버깅 로그(필요시)
    // console.log('[chat] openChat called, loggedIn=', CHAT_STATE.loggedIn);
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

// 초기 바인딩
document.addEventListener('DOMContentLoaded', () => {
    // 로그인 상태 조회 (실패해도 비로그인으로 처리)
    fetch('/user/auth/status', { credentials: 'include' })
        .then(r => (r.ok ? r.json() : { loggedIn: false }))
        .then(d => { CHAT_STATE.loggedIn = !!d.loggedIn; })
        .catch(() => { CHAT_STATE.loggedIn = false; });

    const fab = document.getElementById('chat-fab');
    const closeBtn = document.getElementById('chat-close-btn');
    const overlay = document.getElementById('chat-modal-overlay');
    const sendBtn = document.getElementById('chat-send-btn');

    if (fab) fab.addEventListener('click', openChat);
    if (closeBtn) closeBtn.addEventListener('click', closeChat);
    if (overlay) overlay.addEventListener('click', closeChat);
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape') closeChat(e); });

    if (sendBtn) sendBtn.addEventListener('click', (e) => {
        e.preventDefault();
        const input = document.getElementById('chat-input');
        if (!input) return;
        const text = input.value.trim();
        if (!text) return;
        pushBubble(text, 'me');
        // TODO: 실제 API 연동
        input.value = '';
    });
});

// 인라인 onclick이 호출하는 보루 핸들러
window.__openChat = function(e){
    try { if (e) e.preventDefault(); } catch(_){}
    openChat(e);
    return false;
};

// 캡처 단계 위임(다른 스크립트가 버블링에서 막아도 동작)
document.addEventListener('click', (e) => {
    if (e.target && e.target.id === 'chat-fab') {
        e.preventDefault();
        openChat(e);
    }
}, true);