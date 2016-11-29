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
package com.stormpath.tutorial.controller;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.client.Client;
import com.stormpath.sdk.directory.CustomData;
import com.stormpath.sdk.servlet.account.AccountResolver;
import com.stormpath.sdk.servlet.application.ApplicationResolver;
import com.stormpath.sdk.servlet.client.ClientResolver;
import com.stormpath.tutorial.model.Book;
import com.stormpath.tutorial.service.BookService;
import com.stormpath.tutorial.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class BooksController {

    @Value("#{ @environment['stormpath.authorized.group.user.href'] }")
    private String userGroupHref;

    @Value("#{ @environment['stormpath.web.msg.rebuilt_book_list'] ?: 'You successfully rebuilt the book list' }")
    private String rebuiltBookListMsg;


    @Autowired
    private BookService bookService;

    @Autowired
    private GroupService groupService;

    @RequestMapping("/")
    String home(HttpServletRequest req, Model model) {
        Account account = AccountResolver.INSTANCE.getAccount(req);

        model.addAttribute("status", req.getParameter("status"));

        boolean isInAdminGroup = groupService.isInAdminGroup(account);
        boolean isInUserGroup = groupService.isInUserGroup(account);
        String groups;
        if (isInUserGroup || isInAdminGroup) {
            if (isInUserGroup && isInAdminGroup) {
                groups = "user, admin";
            } else {
                groups = (isInUserGroup) ? "user" : "admin";
            }
        } else {
            groups = "NONE";
        }
        model.addAttribute("isInAdminGroup", isInAdminGroup);
        model.addAttribute("isInUserGroup", isInUserGroup);
        model.addAttribute("groups", groups);

        model.addAttribute("book", new Book());
        model.addAttribute("allBooks", getBooksFromGroupCustomData(req));
        model.addAttribute("myBooks", getBooksFromAccountCustomData(req));

        return "home";
    }

    @RequestMapping(value="/new_book", method= RequestMethod.POST)
    String newBook(HttpServletRequest req, @ModelAttribute Book book) {
        // server sets the owner; not left up to browser data
        book.setOwner(AccountResolver.INSTANCE.getAccount(req).getFullName());
        bookService.newBook(getAccountCustomData(req), getGroupCustomData(req), book);

        return "redirect:/";
    }

    @RequestMapping(value="/upvote", method=RequestMethod.POST)
    String upvote(HttpServletRequest req, @ModelAttribute Book book) {
        bookService.upvote(getAccountCustomData(req), getGroupCustomData(req), book);

        return "redirect:/";
    }

    @RequestMapping("/join_user_group")
    String joinGroup(HttpServletRequest req) {
        Account account = AccountResolver.INSTANCE.getAccount(req);

        groupService.joinUserGroup(account);

        return "redirect:/";
    }

    @RequestMapping("/rebuild_book_list")
    String rebuildBookList(HttpServletRequest req, Model model) {
        bookService.rebuildBookData(
            ApplicationResolver.INSTANCE.getApplication(req),
            getGroupCustomData(req)
        );

        model.addAttribute("msg", rebuiltBookListMsg);
        return home(req, model);
    }

    private List<Book> getBooksFromGroupCustomData(HttpServletRequest req) {
        CustomData customData = getGroupCustomData(req);
        return bookService.getBooksFromCustomData(customData);
    }

    private List<Book> getBooksFromAccountCustomData(HttpServletRequest req) {
        CustomData customData = getAccountCustomData(req);
        return bookService.getBooksFromCustomData(customData);
    }

    private CustomData getGroupCustomData(HttpServletRequest req) {
        Client client = ClientResolver.INSTANCE.getClient(req);
        return client.getResource(userGroupHref + "/customData", CustomData.class);
    }

    private CustomData getAccountCustomData(HttpServletRequest req) {
        Account account = AccountResolver.INSTANCE.getAccount(req);
        if (account == null) { return null; }
        return account.getCustomData();
    }
}