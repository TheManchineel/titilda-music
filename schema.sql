CREATE TABLE IF NOT EXISTS Users (
    username VARCHAR(64) PRIMARY KEY CHECK (LENGTH(username) > 0),
    password_hash TEXT,
    full_name TEXT NOT NULL CHECK (LENGTH(full_name) > 0),
    last_session_invalidation timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Genres (
    name TEXT PRIMARY KEY CHECK (LENGTH(name) > 0)
);

CREATE TABLE IF NOT EXISTS Songs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title TEXT NOT NULL CHECK (LENGTH(title) > 0),
    album TEXT,
    artist TEXT NOT NULL CHECK (LENGTH(artist) > 0),
    artwork TEXT DEFAULT NULL,
    audio_file TEXT NOT NULL,
    audio_mime_type TEXT NOT NULL CHECK (audio_mime_type IN ('audio/mpeg', 'audio/wav', 'audio/ogg', 'audio/flac')),
    release_year INTEGER CHECK (release_year > 0),
    genre TEXT REFERENCES Genres(name) ON DELETE SET NULL ON UPDATE CASCADE,
    owner VARCHAR(64) REFERENCES Users(username) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS Playlists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name TEXT NOT NULL CHECK(LENGTH(name) > 0),
    owner VARCHAR(64) REFERENCES Users(username) ON DELETE CASCADE ON UPDATE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_manually_sorted BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS PlaylistSongs (
    id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    playlist_id UUID REFERENCES Playlists(id) ON DELETE CASCADE ON UPDATE CASCADE,
    song_id UUID REFERENCES Songs(id) ON DELETE CASCADE ON UPDATE CASCADE,
    position INTEGER NOT NULL CHECK (position >= 0),
    UNIQUE (playlist_id, position)
);

INSERT INTO Genres (name) VALUES
('Pop'),
('Rock'),
('Hip-Hop'),
('Jazz'),
('Classical'),
('Electronic'),
('Country'),
('Reggae'),
('Blues'),
('Folk');