package de.axitera.ebics.client.repository;

import org.kopi.ebics.interfaces.Configuration;

public interface IConfigRepository {

    void put(Configuration configuration);

    Configuration get();
}
