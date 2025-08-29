const API_BASE = "http://localhost:8080";
const ME_ENDPOINT = API_BASE + "/user/me";
const LOGOUT_ENDPOINT = API_BASE + "/user/logout";

let currentUser = null;

document.addEventListener("DOMContentLoaded", function() {
    const authStatus = document.getElementById("authStatus");

    async function fetchMe() {
        try {
            const res = await fetch(ME_ENDPOINT, { credentials: "include" });
            if (res.ok) {
                return await res.json();
            }
            return null;
        } catch (err) {
            console.error("Failed to fetch user info:", err);
            return null;
        }
    }

    function updateUI(user) {
        if (!authStatus) return;
        authStatus.innerHTML = "";

        if (user) {
            // // 🔹 [수정된 부분 시작] : 사람 아이콘(마이페이지 이동) 추가
            // authStatus.innerHTML = `
            //     <a href="/mypage" style="text-decoration: none; margin-right: 8px;">
            //         <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
            //              stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
            //              class="feather feather-user">
            //             <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
            //             <circle cx="12" cy="7" r="4"></circle>
            //         </svg>
            //     </a>
            //     <span class="user-status-item">${user.name}님</span>
            //     <button id="logoutBtn" class="logout-btn">로그아웃</button>
            // `;
            // // 🔹 [수정된 부분 끝]

            document.getElementById("logoutBtn").addEventListener("click", async () => {
                await fetch(LOGOUT_ENDPOINT, { method: "POST", credentials: "include" });
                window.location.reload();
            });
        } else {
            authStatus.innerHTML = `
                <a class="user-status-btn" href="/user/login">로그인</a>
                <a class="user-status-btn" href="/user/signup">회원가입</a>
            `;
        }
    }

    (async () => {
        currentUser = await fetchMe();
        updateUI(currentUser);
    })();
});
