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
import com.stormpath.spring.security.provider.StormpathAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class GroupService {
    @Value("#{ @environment['stormpath.authorized.group.user.href'] }")
    private String userGroupHref;

    @Value("#{ @environment['stormpath.authorized.group.admin.href'] }")
    private String adminGroupHref;

    @Autowired
    StormpathAuthenticationProvider stormpathAuthenticationProvider;

    public boolean isInAdminGroup(Account account) {
        return (account != null && account.isMemberOfGroup(adminGroupHref));
    }

    public boolean isInUserGroup(Account account) {
        return (account != null && account.isMemberOfGroup(userGroupHref));
    }

    public void joinUserGroup(Account account) {
        if (account != null && !isInUserGroup(account)) {
            account.addGroup(userGroupHref);

            // Make Spring Security recognize! Maybe not best practice?
            Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
            authorities.add(new SimpleGrantedAuthority(userGroupHref));
            Authentication authentication = new UsernamePasswordAuthenticationToken(account.getEmail(), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    }

}
