const routes = {
    "/login": document.getElementById("login"),
    "/home": document.getElementById("home"),
    "/playlist": document.getElementById("playlist"),
};

function navigate(path) {
    window.history.pushState({}, path, window.location.origin + path);
    const template = routes[path];
    const app = document.getElementById("app");
    app.innerHTML = "";
    if (template) {
        app.appendChild(template.content.cloneNode(true));
    } else {
        app.innerHTML = "<h2>404</h2><p>Page not found.</p>";
    }
}

document.querySelectorAll("nav a").forEach(link => {
    link.addEventListener("click", e => {
        e.preventDefault(); // stop full reload
        const path = e.target.getAttribute("data-route");
        navigate(path);
    });
});

window.addEventListener("popstate", () => {
    navigate(window.location.pathname);
});

const initialPath = window.location.pathname;
navigate(routes[initialPath] ? initialPath : "/home");