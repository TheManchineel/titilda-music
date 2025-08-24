import Auth from "./auth.js";
import Playlist from "./playlist.js";

const auth = new Auth();

function updateNavVisibility() {
    const navEl = document.getElementById("nav");
    const usernameEl = document.getElementById("nav-username");
    if (!navEl) return;
    if (auth.isLoggedIn()) {
        navEl.classList.remove("hidden");
        usernameEl.textContent = auth.getUsername() || "User";
    } else {
        navEl.classList.add("hidden");
    }
}

const routes = {
    "/login": document.getElementById("login"),
    "/signup": document.getElementById("signup"),
    "/home": document.getElementById("home"),
    "/playlists": document.getElementById("playlist"),
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

async function createPlaylist(playlistName, selectedSongs) {
    const response = await auth.authenticatedFetch("/api/playlists", {
        method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify({
            name: playlistName, songs: selectedSongs
        })
    });
    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.error || "Playlist creation failed");
    }
    const newPlaylistUrl = await response.json().then(data => `/playlists/${data.id}`).catch(() => "/home");
    navigate(newPlaylistUrl);
}

async function addSongsToPlaylist(playlistId, selectedSongs) {
    const response = await auth.authenticatedFetch(`/api/playlists/${playlistId}/songs`, {
        method: "POST", headers: {"Content-Type": "application/json"}, body: JSON.stringify(selectedSongs)
    });
    if (!response.ok) {
        const err = await response.json().catch(() => ({}));
        throw new Error(err.error || "Playlist update failed");
    }
    navigate(`/playlists/${playlistId}`);
}

function createSongSelectionForm(existingId = null) {
    const title = existingId ? "Add songs to playlist" : "Create a new playlist";

    const selectionFormTemplate = document.getElementById("song-selection-form");
    const selectionFormSection = selectionFormTemplate.content.cloneNode(true).firstElementChild;
    const selectionForm = selectionFormSection.querySelector("form");
    selectionFormSection.getElementsByClassName("form-title")[0].textContent = title;

    const songSourceUrl = `/api/songs${existingId ? "?excludePlaylist=" + existingId : ""}`;
    const playlistUpdateCallback = existingId ? addSongsToPlaylist : createPlaylist;

    const allSongs = auth.authenticatedFetch(songSourceUrl, {method: "GET"}).then(response => response.json());

    if (existingId) selectionForm.getElementsByClassName("playlist-name-section")[0].remove();

    allSongs.then(songs => {
        if (songs.length === 0) {
            selectionForm.getElementsByClassName("no-songs-available-section")[0].classList.remove("hidden");
        } else {
            const checkboxesGroup = selectionForm.getElementsByClassName("song-select-main-form-group");
            const selectableEntryTemplate = document.getElementById("song-selectable-entry");
            // populate checkboxes:
            songs.forEach(song => {
                const checkboxEntry = selectableEntryTemplate.content.cloneNode(true).firstElementChild;
                checkboxEntry.querySelector("input").value = song.id;
                checkboxEntry.getElementsByClassName("song-display-name").item(0).textContent = song.title + " — " + song.artist;
                checkboxesGroup.item(0).appendChild(checkboxEntry);
            })
        }
    });

    selectionForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        const selectedSongs = [];
        const checkboxes = selectionForm.querySelectorAll("input[type='checkbox']");
        checkboxes.forEach(checkbox => {
            if (checkbox.checked) {
                selectedSongs.push(checkbox.value);
            }
        });

        let playlistName;

        if (!existingId) {
            playlistName = selectionForm.playlistName.value;
            if (playlistName === "") {
                reportErrorToForm(selectionForm, new Error("Playlist name cannot be empty"));
                return;
            }
        }

        const playlistKey = existingId || playlistName;

        try {
            await playlistUpdateCallback(playlistKey, selectedSongs);
        } catch (err) {
            reportErrorToForm(selectionForm, err);
        }
    })

    return selectionFormSection;
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
                a.href = "#";
                a.addEventListener("click", (e) => {
                    e.preventDefault();
                    navigate("/playlists/" + item.id);
                });

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
        .then(genres => {
            genres.forEach(genre => {
                const option = document.createElement("option");
                option.value = genre;
                option.textContent = genre;
                createSongForm.genre.appendChild(option);
            })
        });

    createSongForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        try {
            await createSongFromForm(createSongForm);
            navigate("/home");
        } catch (err) {
            reportErrorToForm(createSongForm, err);
        }
    });
    const selectionFormSection = createSongSelectionForm();

    const leftSection = document.getElementById("left-section");
    leftSection.appendChild(selectionFormSection);
}

function isValidUUID(uuid) {
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
    return uuidRegex.test(uuid);
}

function createSongElement(playlist, song) {
    const td = document.createElement("td");
    const a = document.createElement("a");
    a.className = "song-cell";
    //todo: add link to song page

    const span1 = document.createElement("span");
    span1.className = "song-title";
    span1.textContent = song.title || "Unknown title";

    const span2 = document.createElement("span");
    span2.className = "song-artist";
    span2.textContent = song.artist || "Unknown artist";

    const div = document.createElement("div");
    div.className = "song-cover";

    const img = document.createElement("img");
    img.alt = song.title || "Song cover";
    img.src = playlist.getArtworkUrl(song.id);

    div.appendChild(img);
    a.appendChild(div);
    a.appendChild(span1);
    a.appendChild(span2);
    td.appendChild(a);

    return td;
}

function renderPlaylistContent(playlist, page) {
    const noSongsEl = document.getElementById("no-songs-message");

    if (playlist.getSongs().length === 0) {
        noSongsEl.classList.remove("hidden");
        noSongsEl.textContent = "No songs in this playlist.";
        return;
    } else {
        noSongsEl.classList.add("hidden");
    }

    const songsForCurrentPage = playlist.getSongs(page);
    const songRowEl = document.getElementById("song-table-row");
    const frag = document.createDocumentFragment();

    songsForCurrentPage.forEach(async song => {
        const songCell = createSongElement(playlist, song);
        frag.appendChild(songCell);
    });

    songRowEl.replaceChildren(frag);
}


function setupPagination(playlist, page) {
    const prevBtn = document.getElementById("left-button");
    const nextBtn = document.getElementById("right-button");

    if (prevBtn) {
        prevBtn.style.visibility = page <= 0 ? "hidden" : "visible";
        prevBtn.onclick = () => {
            if (page > 0) {
                updatePlaylistPage(playlist, page - 1);
            }
        };
    }

    if (nextBtn) {
        const totalPages = playlist.getNumberOfPages();
        nextBtn.style.visibility = page >= totalPages - 1 ? "hidden" : "visible";
        nextBtn.onclick = () => {
            if (page < totalPages - 1) {
                updatePlaylistPage(playlist, page + 1);
            }
        };
    }
}

function updatePlaylistPage(playlist, newPage) {
    setupPagination(playlist, newPage);
    renderPlaylistContent(playlist, newPage);
}

function renderPlaylistMetadata(playlist) {
    const playlistTitleEl = document.getElementById("playlist-name");
    if (playlistTitleEl) {
        playlistTitleEl.textContent = playlist.getName() || "Playlist";
    }

    const metaSpan = document.querySelector(".meta span");
    if (metaSpan) {
        const createdAt = playlist.getCreatedAt() ? new Date(playlist.getCreatedAt()) : null;
        let createdAtStr = "";
        if (createdAt && !isNaN(createdAt)) {
            const day = createdAt.getDate();
            const month = createdAt.toLocaleString("en-US", {month: "short"});
            const year = createdAt.getFullYear();
            const hour = createdAt.getHours().toString().padStart(2, "0");
            const min = createdAt.getMinutes().toString().padStart(2, "0");
            createdAtStr = `${day} ${month} ${year}, ${hour}:${min}`;
        }
        metaSpan.textContent = `Created at: ${createdAtStr}`;
    }
}

function initPlaylist(playlistId, page) {
    if (!playlistId || !isValidUUID(playlistId)) {
        navigate("/404");
        return;
    }

    page = parseInt(page);
    if (isNaN(page) || page < 0) page = 0;

    const playlist = new Playlist(auth, playlistId);
    playlist.load()
        .then(() => {
            if (!playlist.getName()) {
                navigate("/404");
                return;
            }

            setupPagination(playlist, page);
            renderPlaylistMetadata(playlist);
            renderPlaylistContent(playlist, page);

            const addSongsToPlaylistForm = createSongSelectionForm(playlistId);
            document.getElementById("add-song-form").appendChild(addSongsToPlaylistForm)
        })
        .catch(err => {
            console.error("Error loading playlist:", err);
            navigate("/404");
        });
}

function navigate(path) {
    if (path === "/") {
        path = "/home";
    }

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
        if (pathComponents[0] === "playlists") {
            initPlaylist(pathComponents[1], pathComponents[2] || null);
        }
    } else {
        // 404
        const notFoundTemplate = document.getElementById("404");
        app.appendChild(notFoundTemplate.content.cloneNode(true));
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
    navigate(initialPath);
} else {
    navigate("/login");
}