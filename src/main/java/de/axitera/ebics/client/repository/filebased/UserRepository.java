package de.axitera.ebics.client.repository.filebased;

import de.axitera.ebics.client.repository.IUserRepository;
import org.kopi.ebics.client.User;

import java.util.List;

public class UserRepository implements IUserRepository {

    @Override
    public void put(User user) {

    }

    @Override
    public User getById(String id) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return null;
    }

    @Override
    public List<String> getAllIds() {
        return null;
    }

}
