package de.axitera.ebics.client;

import de.axitera.ebics.client.repository.IConfigRepository;
import de.axitera.ebics.client.repository.IUserRepository;

public class EbicsClient15 {

    final IConfigRepository configRepository;
    final IUserRepository userRepository;

    /**
     * Construct an EbicsClient that will read Configuration from given Repository
     * UserRepository will be created from ConfigOptions.
     * @param configRepository
     */
    public EbicsClient15(IConfigRepository configRepository) {
        //TODO: create user repository
        this.configRepository = configRepository;
        this.userRepository = null;
    }

    /**
     * Construct an EbicsClient that will read Configuration from given Repository
     * Explicit UserRepository will be used.
     * @param configRepository
     * @param userRepository
     */
    public EbicsClient15(IConfigRepository configRepository, IUserRepository userRepository) {
        this.configRepository = configRepository;
        this.userRepository = userRepository;
    }



}
