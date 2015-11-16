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
import com.stormpath.sdk.servlet.client.ClientResolver;
import com.stormpath.tutorial.model.Book;
import com.stormpath.tutorial.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * @since 1.0.RC5
 */
@Controller
public class BooksController {

    @Value("#{ @environment['stormpath.authorized.group.user'] }")
    private String userGroupHref;

    @Autowired
    private BookService bookService;

    @RequestMapping("/")
    String home(HttpServletRequest req, Model model) {
        model.addAttribute("status", req.getParameter("status"));
        model.addAttribute("book", new Book());

        CustomData customData = getGroupCustomData(req);
        if (customData != null) {
            model.addAttribute("books", customData.get("books"));
        }

        return "home";
    }

    @RequestMapping(value="/new_book", method= RequestMethod.POST)
    String newBook(HttpServletRequest req, @ModelAttribute Book book) {
        bookService.newBook(getAccountCustomData(req), getGroupCustomData(req), book);

        return "redirect:/";
    }

    @RequestMapping("/restricted")
    String restricted(HttpServletRequest req, Model model) {
//        String msg = bookService.sayHello(
//            AccountResolver.INSTANCE.getAccount(req)
//        );
//        model.addAttribute("msg", msg);
        return "restricted";
    }

    private CustomData getGroupCustomData(HttpServletRequest req) {
        Client client = ClientResolver.INSTANCE.getClient(req);
        return client.getResource(userGroupHref + "/customData", CustomData.class);
    }

    private CustomData getAccountCustomData(HttpServletRequest req) {
        Client client = ClientResolver.INSTANCE.getClient(req);
        Account account = AccountResolver.INSTANCE.getAccount(req);
        return client.getResource(account.getCustomData().getHref(), CustomData.class);
    }
}