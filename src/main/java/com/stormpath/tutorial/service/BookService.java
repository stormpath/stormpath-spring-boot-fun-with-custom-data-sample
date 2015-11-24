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
import com.stormpath.tutorial.model.BookDatum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class BookService {
    @Value("#{ @environment['stormpath.authorized.group.user'] }")
    private String userGroupHref;

    @Value("#{ @environment['stormpath.authorized.group.admin'] }")
    private String adminGroupHref;


    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void newBook(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        List<Book> books = getBooksFromCustomData(accountCustomData);

        // shouldn't add the same book; keys of case insensitive combo of author and title
        if (books.contains(book)) { return; }

        books.add(book);
        accountCustomData.put("books", books);
        accountCustomData.save();

        books = getBooksFromCustomData(groupCustomData);
        book.setVotes(1);
        books.add(book);
        groupCustomData.put("books", books);
        groupCustomData.save();
    }

    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void upvote(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        List<Book> books = getBooksFromCustomData(accountCustomData);

        // shouldn't get here if they've already upvoted
        if (books.contains(book)) { return; }

        books.add(book);
        accountCustomData.put("books", books);
        accountCustomData.save();

        books = getBooksFromCustomData(groupCustomData);
        int foundIndex = books.indexOf(book);
        if (foundIndex >= 0) {
            Book foundBook = books.get(foundIndex);
            foundBook.setVotes(foundBook.getVotes()+1);
            groupCustomData.put("books", books);
            groupCustomData.save();
        }
    }

    public List<BookDatum> getBookData(Account account, List<Book> allBooks, List<Book> myBooks) {
        List<BookDatum> bookData = new ArrayList<BookDatum>();
        if (allBooks != null) {
            for (Book book : allBooks) {
                BookDatum bookDatum = new BookDatum();
                bookDatum.setBook(book);
                if (
                    account != null && account.isMemberOfGroup(userGroupHref) &&
                    myBooks != null && !myBooks.contains(book)
                ) {
                    bookDatum.setCanUpVote(true);
                } else {
                    bookDatum.setCanUpVote(false);
                }
                bookData.add(bookDatum);
            }
        }
        return bookData;
    }

    public List<Book> getBooksFromCustomData(CustomData customData) {
        if (customData == null || customData.get("books") == null) {
            return new ArrayList<Book>();
        }

        // order by votes, descending then title, ascending
        Set<Book> books = new TreeSet<Book>(new Comparator<Book>() {
            public int compare(Book b1, Book b2) {
                if (b1.getVotes() == b2.getVotes()) {
                    return b1.getTitle().compareTo(b2.getTitle());
                } else {
                    return b2.getVotes() - b1.getVotes();
                }
            }
        });

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
        return new ArrayList<Book>(books);
    }
}