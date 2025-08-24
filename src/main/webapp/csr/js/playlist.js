export default class playlist {

    static auth;
    static songs;

    constructor(authInstance, uuid) {
        this.auth = authInstance;
        this.songs = [];
        this.fetchSongs(uuid);
    }

    fetchSongs(uuid) {
        this.auth.authenticatedFetch(`/api/playlists/${uuid}/songs`, {method: "GET"})
            .then(response => {
                if (!response.ok) {
                    throw new Error("Failed to fetch playlist songs");
                }
                return response.json();
            })
            .then(data => {
                this.songs = Array.isArray(data) ? data : [];
                this.renderSongs();
            })
            .catch(err => {
                console.error("Error fetching playlist songs:", err);
            });
    }

    getSongs(page = 0) {
        //get 5 songs based on page
        const start = page * 5;
        return this.songs.slice(start, start + 5);
    }
}