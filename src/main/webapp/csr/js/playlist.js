export default class Playlist {
    #auth;
    #uuid;
    #name;
    #createdAt;
    #songs;
    #numberOfPages;
    #loadPromise;

    constructor(authInstance, uuid) {
        this.#auth = authInstance;
        this.#songs = [];
        this.#uuid = uuid;
        this.#numberOfPages = 0;
        this.#loadPromise = this.#fetchSongs(uuid);
    }

    // Return a promise that resolves when all data is loaded
    async load() {
        const result = await this.#loadPromise;
        return result;
    }

    #fetchSongs(uuid) {
        console.log(" #fetchSongs called with UUID:", uuid);

        // Create promises for both API calls
        const metadataPromise = this.#auth.authenticatedFetch(`/api/playlists/${uuid}`, {method: "GET"})
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch playlist info");
                }
                return response.json();
            })
            .then(data => {
                console.log("🎵 Metadata data received:", data);
                this.#uuid = data.id || this.#uuid;
                this.#name = data.name;
                this.#createdAt = data.createdAt;
                return data;
            })
            .catch(err => {
                throw err;
            });

        const songsPromise = this.#auth.authenticatedFetch(`/api/playlists/${uuid}/songs`, {method: "GET"})
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch playlist songs");
                }
                return response.json();
            })
            .then(data => {
                this.#songs = Array.isArray(data) ? data : [];
                this.#numberOfPages = Math.ceil(this.#songs.length / 5);
                return data;
            })
            .catch(err => {
                throw err;
            });

        // Return a promise that resolves when both are complete
        return Promise.all([metadataPromise, songsPromise]);
    }

    /**
     * Gets songs for the specified page
     * - A page contains 5 songs
     * @param {number} page which is the page number (0-indexed)
     * @returns {Array} an array of songs for the specified page
     */
    getSongs(page = 0) {
        if (page < 0) {
            page = 0;
        }
        if (page >= this.#numberOfPages) {
            page = this.#numberOfPages - 1;
        }
        const start = page * 5;
        return this.#songs.slice(start, start + 5);
    }

    getUUID() {
        return this.#uuid;
    }

    getName() {
        return this.#name;
    }

    getCreatedAt() {
        return this.#createdAt;
    }

    getNumberOfPages() {
        return this.#numberOfPages;
    }
}