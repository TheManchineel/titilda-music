import Auth from "./auth.js";

const auth = new Auth();

function updateNavVisibility() {
    const navEl = document.getElementById("nav");
    if (!navEl) return;
    if (auth.isLoggedIn()) {
        navEl.classList.remove("hidden");
    } else {
        navEl.classList.add("hidden");
    }
}

const routes = {
    "/login": document.getElementById("login"),
    "/home": document.getElementById("home"),
    "/playlist": document.getElementById("playlist"),
};

function initLogin() {
    const loginForm = document.getElementById("login-form");
    if (!loginForm) return;
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const username = document.getElementById("username").value;
        const password = document.getElementById("password").value;
        try {
            await auth.login(username, password);
            updateNavVisibility();
            navigate("/home");
        } catch (err) {
            alert("Login failed: " + err.message);
        }
    });
}

function navigate(path) {
    window.history.pushState({}, path, window.location.origin + path);
    path = path.split("/");
    console.log(path);
    const template = routes["/" + (path[1] || "")];
    const app = document.getElementById("app");
    app.innerHTML = "";
    if (template) {
        app.appendChild(template.content.cloneNode(true));
        if(path[1] === "login"){
            initLogin();
        }
    } else {
        app.innerHTML = "<h2>404</h2><p>Page not found.</p>";
    }
    updateNavVisibility();
}


document.querySelectorAll("nav a").forEach(link => {
    link.addEventListener("click", e => {
        e.preventDefault(); // stop full reload
        const path = e.target.getAttribute("data-route");
        if(!auth.isLoggedIn()){
            navigate("/login");
            return;
        }
        if (path === "/logout") {
            auth.logout();
            return;
        }
        if (path) navigate(path);
    });
});

window.addEventListener("popstate", () => {
    navigate(window.location.pathname);
});

const initialPath = window.location.pathname;

updateNavVisibility();

if(auth.isLoggedIn()){
    navigate(routes[initialPath] ? initialPath : "/home");
}else{
    navigate("/login");
}