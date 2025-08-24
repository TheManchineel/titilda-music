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
    console.log("Setting up login...")
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

/**
 * Fetch playlist items from the server
 * @returns {Promise<Array>} a promise that resolves to an array of playlist items
 */
function getPlaylistItems() {
    return auth.authenticatedFetch("/api/playlists", {method: "GET"})
        .then(response => {
            if (!response.ok) {
                throw new Error("Failed to fetch playlist");
            }
            return response.json();
        })
        .then(data => {
            return Array.isArray(data) ? data : [];
        });
}

async function createSongFromForm(form) {
    const formData = new FormData(form);

    const response = await auth.authenticatedFetch("/api/songs", {method: "POST", body: formData})
    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.error || "Song creation failed");
    }
}

function initHome() {
    const fullNameEl = document.getElementById("full-name");
    if (!fullNameEl) return;
    fullNameEl.textContent = auth.getFullName() || "User";

    // Fetch and display playlist items
    getPlaylistItems()
        .then(items => {
            const playlistEl = document.querySelector(".playlist-list");
            if (!playlistEl) return;
            const emptyEl = document.querySelector(".playlist-empty");
            const notEmptyEl = document.querySelector(".playlist-header");
            if (items.length === 0) {
                if (emptyEl) emptyEl.classList.remove("hidden");
                if (notEmptyEl) notEmptyEl.classList.add("hidden");
                playlistEl.replaceChildren();
                return;
            } else {
                if (emptyEl) {
                    emptyEl.classList.add("hidden");
                }
                if (notEmptyEl) {
                    notEmptyEl.classList.remove("hidden");
                }
            }

            const frag = document.createDocumentFragment();

            items.forEach(item => {
                const li = document.createElement("li");
                const a = document.createElement("a");
                a.className = "playlist-btn";

                const nameSpan = document.createElement("span");
                nameSpan.className = "playlist-name";
                nameSpan.textContent = item.name || "Playlist name";

                const dateSpan = document.createElement("span");
                dateSpan.className = "playlist-date";
                if (item.createdAt) {
                    const d = new Date(item.createdAt);
                    if (!isNaN(d)) {
                        const day = d.getDate();
                        const month = d.toLocaleString("en-US", {month: "short"});
                        const year = d.getFullYear();
                        const hour = d.getHours().toString().padStart(2, "0");
                        const min = d.getMinutes().toString().padStart(2, "0");
                        dateSpan.textContent = `${day} ${month} ${year}, ${hour}:${min}`;
                    }
                }

                a.appendChild(nameSpan);
                a.appendChild(dateSpan);
                li.appendChild(a);
                frag.appendChild(li);
            });

            // Clears existing children and inserts new ones without using innerHTML
            playlistEl.replaceChildren(frag);
        })
        .catch(error => {
            console.error("Error fetching playlist items:", error);
        });

    const createSongForm = document.getElementById("create-song-form");
    if (!createSongForm) return;

    auth.authenticatedFetch("/api/genres", {method: "GET"})
        .then(response => response.json())
        .then(genres => {genres.forEach(
            genre => {
                const option = document.createElement("option");
                option.value = genre;
                option.textContent = genre;
                createSongForm.genre.appendChild(option);
            }
        )});

    createSongForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            await createSongFromForm(createSongForm);
            navigate("/home");
        } catch (err) {
            reportErrorToForm(createSongForm, err);
        }
    })
}

function navigate(path) {
    window.history.pushState({}, path, window.location.origin + path);
    const pathComponents = path.split("/");
    pathComponents.shift();
    const template = routes["/" + (pathComponents[0] || "")];
    const app = document.getElementById("app");
    app.innerHTML = "";
    if (template) {
        app.appendChild(template.content.cloneNode(true));
        if (pathComponents[0] === "login") {
            initLogin();
        }
        if (pathComponents[0] === "signup") {
            initSignup();
        }
        if (pathComponents[0] === "home") {
            initHome();
        }
    } else {
        app.innerHTML = "<h2>404</h2><p>Page not found.</p>";
    }
    updateNavVisibility();
}


document.querySelectorAll("nav a").forEach(link => {
    link.addEventListener("click", async e => {
        e.preventDefault(); // stop full reload
        // IMPORTANT: currentTarget is the element that the eventListener is attached to, not the target of the click itself. Won't work otherwise!!!
        const path = e.currentTarget.getAttribute("data-route");
        if (!auth.isLoggedIn()) {
            navigate("/login");
            return;
        }
        console.log(path);
        switch (path) {
            case "/logout": {
                auth.logout();
                return;
            }
            case "/logout-everywhere": {
                await auth.logoutFromAllDevices();
                return;
            }
            default: {
                if (path) {
                    navigate(path);
                    return;
                }
            }
        }
    });
});

window.addEventListener("popstate", () => {
    navigate(window.location.pathname);
});

const initialPath = window.location.pathname;

updateNavVisibility();

if (auth.isLoggedIn()) {
    navigate(routes[initialPath] ? initialPath : "/home");
} else {
    console.log("Going to login...");
    navigate("/login");
}