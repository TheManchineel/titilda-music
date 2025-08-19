package org.titilda.music.base.model;

import java.util.Objects;

/**
 * Model class representing a Genre in the music application.
 */
public final class Genre {
    private String name;

    // Default constructor
    public Genre() {}

    // Constructor with name
    public Genre(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(name, genre.name);
    }

}
