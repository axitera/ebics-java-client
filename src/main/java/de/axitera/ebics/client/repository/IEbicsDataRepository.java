package de.axitera.ebics.client.repository;

import org.kopi.ebics.client.Bank;

import java.util.List;

public interface IEbicsDataRepository<T> {
    void put(T data);

    T getById(String id);

    List<T> getAll();

    List<String>getAllIds();
}
