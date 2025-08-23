export default class Auth {

    static TOKEN_KEY = "titilda_music_access_token";

    /**
     * Constructor
     * - Get the token from the local storage
     * - Use the authenticatedFetch method to make requests
     */
    constructor() {
        this.token = localStorage.getItem(Auth.TOKEN_KEY);
    }

    /**
     * Set the token in the local storage 
     * - Use the login method to set the token
     * @param {string} token which is the token to set
     * @returns void
     */
    setToken(token) {
        this.token = token;
        if (token) {
            localStorage.setItem(Auth.TOKEN_KEY, token);
        } else {
            localStorage.removeItem(Auth.TOKEN_KEY);
        }
    }

    /**
     * Check if the user is logged in
     * @returns true if the user is logged in, false otherwise
     */
    isLoggedIn() {
        console.log("Checking if user is logged in. Token:", this.token);
        return this.token !== null;
    }

    /**
     * Get the authentication header
     * @returns the authentication header
     */
    getAuthHeader() {
        if (this.token) {
            return { Authorization: "Bearer " + this.token };
        }
        return {};
    }

    /**
     * Login to the application
     * @param {*} username which is the username to login with
     * @param {*} password which is the password to login with
     * @throws Error if the login fails
     */
    async login(username, password) {
        const response = await fetch("/api/auth/login", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password }),
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error("Login failed");
        }
        const data = await response.json();
        if (data.token_type === "Bearer" && data.access_token) {
            this.setToken(data.access_token);
        } else {
            throw new Error("Login failed");
        }
    }

    /**
     * Logout from the current device
     * - Redirects to the login page
     * @throws Error if the logout fails
     */
    logout() {
        this.token = null;
        window.location.href = "/login";
    }

    /**
     * Logout from all devices
     * - Redirects to the login page
     * @throws Error if the logout from all devices fails
     */
    async logoutFromAllDevices() {
        const response = await fetch("/api/sessions", {
            method: "DELETE",
            headers: this.getAuthHeader(),
        });
        if (!response.ok) {
            throw new Error("Failed to logout from all devices");
        }
        this.logout();
    }

    /**
     * Make a fetch call with the authentication header
     * @param {*} url the url to fetch
     * @param {*} options the options to pass to the fetch call
     * @returns the response from the fetch call
     */
    authenticatedFetch(url, options = {}) {
        const headers = {
            ...(options.headers || {}),
            ...this.getAuthHeader(),
        };
        return fetch(url, { ...options, headers });
    }

}