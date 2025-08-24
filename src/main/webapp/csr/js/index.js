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
    "/songs": document.getElementById("song")
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

function createReorderButton(playlistId) {
    const reorderBtn = document.createElement("button");
    reorderBtn.className = "btn reorder-btn";
    reorderBtn.type = "button";
    reorderBtn.textContent = "Reorder";
    reorderBtn.addEventListener("click", (e) => {
        e.preventDefault();
        e.stopPropagation();
        openEmptyModal(playlistId);
    });
    return reorderBtn;
}

/**
 * Opens a modal dialog for reordering songs in a playlist.
 * 
 * This function creates a modal overlay with a draggable list of songs from the specified playlist.
 * Users can drag and drop songs to reorder them, and save the new order to the server.
 * 
 * @param {string} playlistId - The UUID of the playlist to reorder songs for
 * @returns {Promise<void>} A promise that resolves when the modal is fully loaded
 * 
 * @example
 * // Open reorder modal for playlist with ID "123e4567-e89b-12d3-a456-426614174000"
 * await openEmptyModal("123e4567-e89b-12d3-a456-426614174000");
 */
async function openEmptyModal(playlistId ) {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay";

    const modal = document.createElement("div");
    modal.className = "modal";
    modal.setAttribute("role", "dialog");
    modal.setAttribute("aria-modal", "true");

    const header = document.createElement("div");
    header.className = "modal-header";



    const title = document.createElement("h3");
    title.textContent = "Reorder";

    const closeBtn = document.createElement("button");
    closeBtn.className = "modal-close";
    closeBtn.type = "button";
    closeBtn.setAttribute("aria-label", "Close");
    closeBtn.textContent = "×";

    const closeModal = () => {
        // unlock scrolling and remove modal
        document.body.style.overflow = "";
        overlay.remove();
    };

    closeBtn.addEventListener("click", closeModal);
    overlay.addEventListener("click", (e) => {
        if (e.target === overlay) closeModal();
    });

    const body = document.createElement("div");
    body.className = "modal-body";
    // Intentionally empty for now

    header.appendChild(title);
    header.appendChild(closeBtn);
    modal.appendChild(header);
    modal.appendChild(body);
    overlay.appendChild(modal);
    document.body.appendChild(overlay);
    // Lock scrolling
    document.body.style.overflow = "hidden";

    // Load playlist object and aggregate all songs for in-function use
    try {
        const playlist = new Playlist(auth, playlistId);
        await playlist.load();
        const allSongs = playlist.getAllSongs();
        const playlistName = playlist.getName();
        if (playlistName) {
            title.textContent = "Reorder " + playlistName;
        }

        // Build draggable list inside the modal body
        const list = document.createElement("ul");
        list.className = "dnd-list";

        allSongs.forEach(song => {
            const li = document.createElement("li");
            li.className = "dnd-item";
            li.draggable = true;
            li.dataset.songId = song.id;
            li.textContent = `${song.title || "Unknown title"} — ${song.artist || "Unknown artist"}`;

            li.addEventListener("dragstart", (ev) => {
                li.classList.add("dragging");
                if (ev.dataTransfer) {
                    ev.dataTransfer.effectAllowed = "move";
                    ev.dataTransfer.setData("text/plain", song.id);
                }
            });

            li.addEventListener("dragend", () => {
                li.classList.remove("dragging");
            });

            list.appendChild(li);
        });


        /**
         * Helper function to find the best insertion point for a dragged element.
         * 
         * This is the trickiest part of the drag-and-drop logic. It determines where to insert
         * the dragged item based on the mouse cursor position relative to other items.
         * 
         * The algorithm works by:
         * 1. Finding all non-dragging elements (excluding the one being dragged)
         * 2. For each element, calculating the vertical offset from cursor to element center
         * 3. Finding the element with the smallest negative offset (closest above cursor)
         * 4. Returning that element as the insertion point
         * 
         * If no element is found (cursor is at the very top), returns null (insert at beginning)
         * If cursor is below all elements, returns null (append to end)
         * 
         * @param {HTMLElement} container - The container element holding the draggable items
         * @param {number} y - The vertical mouse position (clientY) during the drag event
         * @returns {HTMLElement|null} The element after which to drop, or null for beginning/end
         */
        const getDragAfterElement = (container, y) => {
            // Get all items except the one being dragged
            const elements = [...container.querySelectorAll('.dnd-item:not(.dragging)')];
            
            // Find the element closest to the cursor position
            return elements.reduce((closest, child) => {
                const box = child.getBoundingClientRect();
                // Calculate offset from cursor to center of this element
                // Negative offset means cursor is above element center
                const offset = y - box.top - box.height / 2;
                
                // We want the element with the smallest negative offset (closest above cursor)
                // but still negative (cursor is above its center)
                if (offset < 0 && offset > closest.offset) {
                    return { offset, element: child };
                } else {
                    return closest;
                }
            }, { offset: Number.NEGATIVE_INFINITY, element: null }).element;
        };

        // Handle real-time reordering during drag
        list.addEventListener("dragover", (e) => {
            e.preventDefault(); // Required to allow drops
            const dragging = list.querySelector('.dnd-item.dragging');
            if (!dragging) return;
            
            // Find the best insertion point based on cursor position
            const afterElement = getDragAfterElement(list, e.clientY);
            
            // Move the dragging element to the new position
            if (afterElement == null) {
                // Cursor is at the very top or bottom - append to end
                list.appendChild(dragging);
            } else {
                // Insert before the element we found
                list.insertBefore(dragging, afterElement);
            }
        });

        // Track the current order of songs for saving
        let currentOrder = allSongs.map(s => s.id); // Initialize with original order

        // Update our order tracking whenever a drop completes
        list.addEventListener("drop", (e) => {
            e.preventDefault();
            // Read the new order from the DOM and update our tracking variable
            // This ensures currentOrder always reflects the actual visual order
            currentOrder = [...list.querySelectorAll('.dnd-item')].map(el => el.dataset.songId);
        });

        // Add save button to persist the new order
        const saveBtn = document.createElement("button");
        saveBtn.className = "btn save-btn";
        saveBtn.type = "button";
        saveBtn.textContent = "Save Changes";
        saveBtn.addEventListener("click", async () => {
            try {
                // Send the current order to the server
                const response = await auth.authenticatedFetch(`/api/playlists/${playlistId}/song-order`, {
                    method: "PUT",
                    headers: {"Content-Type": "application/json"},
                    body: JSON.stringify(currentOrder) // Send array of song IDs in new order
                });
                if (!response.ok) {
                    throw new Error("Failed to save song order");
                }
                console.log("Song order saved successfully");
                closeModal(); // Close modal on successful save
            } catch (err) {
                console.error("Error saving song order:", err);
                // Note: We could add user-facing error handling here
            }
        });

        body.appendChild(list);
        body.appendChild(saveBtn);
    } catch (err) {
        console.error("Failed to load playlist for reorder modal:", err);
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
                li.appendChild(createReorderButton(item.id));
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
    
    a.href = "#";
    a.addEventListener("click", (e) => {
        e.preventDefault();
        navigate("/songs/" + song.id);
    });

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


function initSong(songId) {
    if (!songId || !isValidUUID(songId)) {
        navigate("/404");
        return;
    }
    auth.authenticatedFetch(`/api/songs/${songId}`, {method: "GET"})
        .then(response => response.json())
        .then(song => {
            document.querySelectorAll(".player-song-title").forEach(el => el.textContent = song.title);
            document.querySelectorAll(".player-song-artist").forEach(el => el.textContent = song.artist);
            document.querySelectorAll(".player-album-title-with-year").forEach(el => el.textContent = `${song.album} (${song.releaseYear})`)
            document.querySelectorAll(".player-song-genre").forEach(el => el.textContent = song.genre);
            const audioSource = document.querySelector(".player-song-audio");
            audioSource.type = song.audioMimeType;
            auth.authenticatedBlobFetch(song.audioUrl).then(blob => {
                audioSource.src = URL.createObjectURL(blob);
                audioSource.parentElement.load();
            });
            auth.authenticatedBlobFetch(song.artworkUrl).then(blob => {
                document.querySelector(".player-song-artwork").src = URL.createObjectURL(blob);
            });
            document.querySelector(".home-link").addEventListener("click", () => {
                navigate("/home");
            })
        })
}


// TODO: refactor this, by creating a Route class and using it to map both the template and the init function
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
        if (pathComponents[0] === "songs") {
            initSong(pathComponents[1]);
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