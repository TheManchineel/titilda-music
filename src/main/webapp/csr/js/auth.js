export default class Auth {

    static TOKEN_KEY = "titilda_music_access_token";
    static FULL_NAME_KEY = "titilda_music_full_name";
    static USERNAME_KEY = "titilda_music_username";

    /**
     * Constructor
     * - Get the token from the local storage
     * - Use the authenticatedFetch method to make requests
     */
    constructor() {
        this.token = localStorage.getItem(Auth.TOKEN_KEY);
        this.fullName = localStorage.getItem(Auth.FULL_NAME_KEY);
        this.username = localStorage.getItem(Auth.USERNAME_KEY);
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
        console.log("Full Name:", this.fullName);
        console.log("Username:", this.username);
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
            throw new Error(err.error || "Login failed");
        }
        const data = await response.json();
        if (data.token_type.toLowerCase() === "bearer" && data.access_token) {
            this.setToken(data.access_token);
        } else {
            throw new Error("Login failed (invalid token type or access token)");
        }
    }

    async signup(username, password, fullName) {
        const response = await fetch("/api/auth/signup", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ username, password, fullName }),
        });
        if (!response.ok) {
            const err = await response.json().catch(() => ({}));
            throw new Error(err.error || "Signup failed");
        }
        const data = await response.json()
        if (data.token_type.toLowerCase() === "bearer" && data.access_token) {
            this.setToken(data.access_token);
        } else {
            throw new Error("Signup failed (invalid token type or access token)");
        }

        const userInfoResponse = await this.authenticatedFetch("/api/me", { method: "GET" });
        if (userInfoResponse.ok) {
            const userInfo = await userInfoResponse.json();
            console.log(userInfoResponse.status, userInfo);
            this.fullName = userInfo.fullName;
            this.username = userInfo.username;
            if (this.fullName) {
                localStorage.setItem(Auth.FULL_NAME_KEY, this.fullName);
            }
            if (this.username) {
                localStorage.setItem(Auth.USERNAME_KEY, this.username);
            }
        } else {
            const errorBody = await userInfoResponse.text().catch(() => "");
            console.log(userInfoResponse.status, errorBody);
        }

    }

    /**
     * Logout from the current device
     * - Redirects to the login page
     * @throws Error if the logout fails
     */
    logout() {
        this.setToken(null);
        this.fullName = null;
        this.username = null;
        localStorage.removeItem(Auth.FULL_NAME_KEY);
        localStorage.removeItem(Auth.USERNAME_KEY);
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

    /**
     * Get the full name of the logged in user
     * @returns {String |null}
     */
    getFullName() {
        return this.fullName;
    }

    /**
     * Get the username of the logged in user
     * @returns {String |null}
     */
    getusername() {
        return this.username;
    }

    /**
     * Preferred-casing alias for username getter
     * @returns {String | null}
     */
    getUsername() {
        return this.username;
    }

}