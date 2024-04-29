package de.axitera.ebics.client.repository.filebased;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.axitera.ebics.client.repository.IUserRepository;
import org.kopi.ebics.client.User;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserRepository extends AbstractFileBasedRepo<User> implements IUserRepository {


    protected UserRepository(File folder) {
        super(folder);
    }

    @Override
    Class<User> getClassOfT() {
        return User.class;
    }

    @Override
    String getUniqueFileNameForData(User data) {
        return data.getUserId();
    }




}
