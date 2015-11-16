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
package com.stormpath.tutorial.service;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.tutorial.model.Book;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.0.RC5
 */
@Service
public class BookService {
    @PreAuthorize("hasRole(@roles.AUTHORIZED_USER)")
    public void newBook(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        Map<String, Object> bookMap = new HashMap<String, Object>();
        bookMap.put("author", book.getAuthor());
        bookMap.put("title", book.getTitle());
        bookMap.put("url", book.getUrl());

        if (accountCustomData != null) {
            ArrayList<Map<String, Object>> books = (ArrayList<Map<String, Object>>) accountCustomData.get("books");
            if (books == null) {
                books = new ArrayList<Map<String, Object>>();
            } else if (exists(books, book)) { // unique key is title + author
                return;
            }
            books.add(bookMap);
            accountCustomData.put("books", books);
            accountCustomData.save();
        }

        if (groupCustomData != null) {
            bookMap.put("votes", 1L);
            ArrayList<Map<String, Object>> books = (ArrayList<Map<String, Object>>) groupCustomData.get("books");
            if (books == null) {
                books = new ArrayList<Map<String, Object>>();
            }
            books.add(bookMap);
            groupCustomData.put("books", books);
            groupCustomData.save();
        }
    }

    // ew
    private boolean exists(ArrayList<Map<String, Object>> books, Book book) {
        for (Map<String, Object> bookMap : books) {
            String title = ((String)bookMap.get("title")).toLowerCase();
            String author = ((String)bookMap.get("author")).toLowerCase();
            // unique key is title and author
            if (title.equals(book.getTitle().toLowerCase()) && author.equals(book.getAuthor().toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}