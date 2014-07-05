/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tangram.spring.security;

import java.util.Collection;
import java.util.HashSet;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Simple UserDetailsService accepting any user.
 *
 * Returns just the passed over username in the userdetails.
 */
public class AnyNameUserService implements UserDetailsService {


    /**
     * Return user details instance for username.
     *
     * @param name
     * @return user instance valid for any condition containing just the name
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Collection<GrantedAuthority> grants = new HashSet<>();
        return new User(name, "", true, true, true, true, grants);
    } // loadUserByUsername()

} // AnyNameUserService
