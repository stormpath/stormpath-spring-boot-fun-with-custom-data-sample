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
import com.stormpath.sdk.account.AccountCriteria;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.tutorial.model.Book;
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

    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void newBook(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        List<Book> myBooks = getBooksFromCustomData(accountCustomData);
        List<Book> groupBooks = getBooksFromCustomData(groupCustomData);

        // shouldn't add the same book; key is case insensitive combo of author and title
        if (myBooks.contains(book)) { return; }

        // save Group CustomData
        book.setVotes(1);
        groupBooks.add(book);
        groupCustomData.put("books", groupBooks);
        groupCustomData.save();

        // save Account CustomData
        // votes are ignored at the account level, but let's keep it 0
        book.setVotes(0);
        myBooks.add(book);
        accountCustomData.put("books", myBooks);
        accountCustomData.save();
    }

    @PreAuthorize("hasRole(@roles.GROUP_USER)")
    public void upvote(CustomData accountCustomData, CustomData groupCustomData, Book book) {
        List<Book> myBooks = getBooksFromCustomData(accountCustomData);
        List<Book> groupBooks = getBooksFromCustomData(groupCustomData);
        int foundIndex;

        // shouldn't get here if the book doesn't exist or if they've already upvoted
        if (
            groupBooks == null ||
            (foundIndex = groupBooks.indexOf(book)) < 0 ||
            myBooks.contains(book)
        ) {
            return;
        }

        // save to Group CustomData
        Book foundBook = groupBooks.get(foundIndex);
        foundBook.setVotes(foundBook.getVotes()+1);
        groupCustomData.put("books", groupBooks);
        groupCustomData.save();

        // save to Account CustomData
        // votes are ignored at the account level, but let's keep it 0
        foundBook.setVotes(0);
        myBooks.add(foundBook);
        accountCustomData.put("books", myBooks);
        accountCustomData.save();
    }

    @PreAuthorize("hasRole(@roles.GROUP_ADMIN)")
    public void rebuildBookData(Application application, CustomData groupCustomData) {
        List<Book> books = new ArrayList<Book>();

        AccountCriteria criteria = Accounts.criteria().withCustomData().withGroups();
        AccountList accountList = application.getAccounts(criteria);
        for (Account account : accountList) {
            List<Book> accountBooks = getBooksFromCustomData(account.getCustomData());
            for (Book book : accountBooks) {
                int index;
                if ((index = books.indexOf(book)) >= 0) {
                    Book foundBook = books.get(index);
                    foundBook.setVotes(foundBook.getVotes()+1);
                } else {
                    book.setVotes(1);
                    books.add(book);
                }
            }
        }
        groupCustomData.put("books", books);
        groupCustomData.save();
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

                book.setOwner((String) bookMap.get("owner"));
                book.setVotes((Integer) bookMap.get("votes"));
                books.add(book);
            }
        }
        return new ArrayList<Book>(books);
    }

}