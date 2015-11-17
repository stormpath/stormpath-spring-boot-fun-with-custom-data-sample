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

import com.stormpath.sdk.directory.CustomData;
import com.stormpath.tutorial.model.Book;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookService {
    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void newBook(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        if (accountCustomData != null) {
            List<Book> books = getBooksFromCustomData(accountCustomData);
            if (books == null) {
                books = new ArrayList<Book>();
            } else if (books.contains(book)) {
                return;
            }

            books.add(book);
            accountCustomData.put("books", books);
            accountCustomData.save();
        }

        if (groupCustomData != null) {
            book.setVotes(1);
            List<Book> books = getBooksFromCustomData(groupCustomData);
            if (books == null) {
                books = new ArrayList<Book>();
            }
            books.add(book);
            groupCustomData.put("books", books);
            groupCustomData.save();
        }
    }

    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void upvote(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        if (accountCustomData != null) {
            List<Book> books = getBooksFromCustomData(accountCustomData);
            if (books == null) {
                books = new ArrayList<Book>();
            } else if (books.contains(book)) {
                return;
            }
            books.add(book);
            accountCustomData.put("books", books);
            accountCustomData.save();
        }

        if (groupCustomData != null) {
            List<Book> books = getBooksFromCustomData(groupCustomData);
            int foundIndex = books.indexOf(book);
            if (foundIndex >= 0) {
                Book foundBook = books.get(foundIndex);
                foundBook.setVotes(foundBook.getVotes()+1);
                groupCustomData.put("books", books);
                groupCustomData.save();
            }
        }
    }

    public List<Book> getBooksFromCustomData(CustomData customData) {
        List<Book> books = new ArrayList<Book>();

        if (customData == null || customData.get("books") == null) {
            return new ArrayList<Book>();
        }

        List<Map<String, Object>> booksList = (List<Map<String, Object>>) customData.get("books");
        if (booksList != null) {
            for (Map<String, Object> bookMap: booksList) {
                Book book = new Book();
                book.setAuthor((String) bookMap.get("author"));
                book.setTitle((String) bookMap.get("title"));
                book.setUrl((String) bookMap.get("url"));
                // only group custom data has vote count
                if (bookMap.get("votes") != null) {
                    book.setVotes((Integer)bookMap.get("votes"));
                }
                books.add(book);
            }
        }
        return books;
    }
}