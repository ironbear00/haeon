const API_BASE = "http://localhost:8080";
const ME_ENDPOINT = API_BASE + "/user/me";
const LOGOUT_ENDPOINT = API_BASE + "/user/logout";

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
            authStatus.innerHTML = `
                    <span class="user-status-item">${user.name}님</span>
                    <button id="logoutBtn" class="logout-btn">로그아웃</button>
                `;
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
        const user = await fetchMe();
        updateUI(user);
    })();
});