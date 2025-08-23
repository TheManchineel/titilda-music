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
    "/signup": document.getElementById("signup"),
    "/home": document.getElementById("home"),
    "/playlist": document.getElementById("playlist"),
};

function initLogin() {
    const loginForm = document.getElementById("login-form");
    if (!loginForm) return;
    loginForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const username = loginForm.username.value;
        const password = loginForm.password.value;
        try {
            await auth.login(username, password);
            updateNavVisibility();
            navigate("/home");
        } catch (err) {
            reportErrorToForm(loginForm, err);
        }
    });
    document.getElementById("signup-link").addEventListener("click", () => {
        navigate("/signup");
    });
}

function reportErrorToForm(form, err) {
    let errorDiv = form.getElementsByClassName("error-message")[0]
    errorDiv.classList.remove("hidden");
    errorDiv.getElementsByClassName("error-text")[0].innerText = err.message;
}

function initSignup() {
    const signupForm = document.getElementById("signup-form");
    if (!signupForm) return;
    signupForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const username = signupForm.username.value;
        const password = signupForm.password.value;
        const fullName = signupForm.fullName.value;
        try {
            await auth.signup(username, password, fullName);
            updateNavVisibility();
            navigate("/home");
        } catch (err) {
            reportErrorToForm(signupForm, err);
        }
    });
    document.getElementById("login-link").addEventListener("click", () => {
        navigate("/login");
    });
}

function initHome() {
    const fullNameEl = document.getElementById("full-name");
    if (!fullNameEl) return;
    fullNameEl.textContent = auth.getFullName() || "User";
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
        if(path === "/signup"){
            initSignup();
        }
        if(path[1] === "home"){
            initHome();
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