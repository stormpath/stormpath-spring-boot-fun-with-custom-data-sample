/*
 * Copyright 2015 Stormpath, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stormpath.tutorial.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Book {
    String author;
    String title;
    String url;

    String owner;
    int votes;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    @JsonIgnore
    public String getAuthorLowerCase() {
        if (author != null) {
            return author.toLowerCase();
        }
        return null;
    }

    @JsonIgnore
    public String getTitleLowerCase() {
        if (title != null) {
            return title.toLowerCase();
        }
        return null;
    }

    @Override
    public boolean equals(Object otherBookObj) {
        if (otherBookObj == null || !(otherBookObj instanceof Book)) {
            return false;
        }
        Book otherBook = (Book)otherBookObj;

        // author & title all nulls are equal
        if (this.author == null && otherBook.getAuthor() == null && this.title == null && otherBook.getTitle() == null) {
            return true;
        }

        // author & title matching, case insensitive defines equality
        if (
            this.author != null && this.getAuthorLowerCase().equals(otherBook.getAuthorLowerCase()) &&
            this.title != null && this.getTitleLowerCase().equals(otherBook.getTitleLowerCase())
        ) {
            return true;
        }
        return false;
    }
}
