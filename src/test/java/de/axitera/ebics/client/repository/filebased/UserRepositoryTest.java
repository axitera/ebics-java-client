package de.axitera.ebics.client.repository.filebased;


import org.junit.jupiter.api.Test;
import org.kopi.ebics.client.User;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class UserRepositoryTest {

    @Test
    void testUserRepository() throws GeneralSecurityException, IOException {
        File folder = new File(System.getProperty("user.dir")+"/ebics-tests/users");
        if(!folder.exists()){
            folder.mkdirs();
        }

        UserRepository userRepository = new UserRepository(folder);

        User user = new User(null,"testId","testName","testEmail",null);

    }

}
