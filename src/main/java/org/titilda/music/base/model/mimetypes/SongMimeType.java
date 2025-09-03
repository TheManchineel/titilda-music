package org.titilda.music.base.model.mimetypes;

public enum SongMimeType {
    MP3("audio/mpeg", "mp3"),
    WAV("audio/wav", "wav"),
    OGG("audio/ogg", "ogg"),
    FLAC("audio/flac", "flac");

    private final String mimeType;
    private final String extension;

    SongMimeType(String mimeType, String extension) {
        this.mimeType = mimeType;
        this.extension = extension;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getExtension() {
        return extension;
    }

    public static SongMimeType fromMimeType(String mimeType) throws IllegalArgumentException {
        for (SongMimeType songMimeType : SongMimeType.values()) {
            if (songMimeType.getMimeType().equals(mimeType)) {
                return songMimeType;
            }
        }
        throw new IllegalArgumentException("Unsupported type " + mimeType);
    }

    public static SongMimeType fromExtension(String extension) throws IllegalArgumentException {
        for (SongMimeType songMimeType : SongMimeType.values()) {
            if (songMimeType.getExtension().equals(extension)) {
                return songMimeType;
            }
        }
        throw new IllegalArgumentException("Unsupported type " + extension);
    }
}
