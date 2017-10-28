package org.thirdstreet.blogger.security;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.ScanFilter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.thirdstreet.aws.DynamoDBUtils;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jbramlet on 7/10/17.
 */
public class AwsUserDetailsService implements UserDetailsService {

    private static final String kTableName = "users";

    private final Map<String, UserDetails> users = new ConcurrentHashMap<>();
    private final Map<String, CurrentUser> currentUsers = new ConcurrentHashMap<>();

    /**
     * Constructor.
     */
    public AwsUserDetailsService() {
    }

    @PostConstruct
    public void init() {
        DynamoDBUtils.scanTable(kTableName, new ScanFilter("status").eq("active"), this::toUserDetails)
                .forEach(u -> users.put(u.getUsername(), u));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.get(username);
    }

    public CurrentUser getCurrentUser(String userName) {
        return currentUsers.get(userName);
    }

    private UserDetails toUserDetails(final Item item) {
        CurrentUser cUser = new CurrentUser(item.getString("firstName"), item.getString("lastName"),
                item.getString("email"), item.getString("password"),
                AuthorityUtils.createAuthorityList("USER"));
        currentUsers.put(cUser.getUsername(), cUser);

        return new User(cUser.getUsername(), cUser.getPassword(), cUser.getAuthorities());
    }
}
