package com.prollpa.security;


import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LdapUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//    	 if (username == null || !username.equals("validUser")) { // Replace with real LDAP validation logic
//             throw new UsernameNotFoundException("please enter valid username -> you entered wrong " + username);
//         }
        // Implement LDAP user retrieval logic here

        return User.builder()
                .username(username)
                .password("{noop}password") // No password encoding for now
                .roles("USER")
                .build();
    }
    
}

