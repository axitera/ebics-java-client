/*
 * Copyright (c) 1990-2012 kopiLeft Development SARL, Bizerte, Tunisia
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1 as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */

package de.axitera.ebics.client;

import de.axitera.ebics.client.letter.IEbicsInitLetter;
import de.axitera.ebics.client.logging.IEbicsLogger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.kopi.ebics.client.*;
import org.kopi.ebics.exception.EbicsException;
import org.kopi.ebics.exception.NoDownloadDataAvailableException;
import org.kopi.ebics.interfaces.*;
import org.kopi.ebics.io.IOUtils;
import de.axitera.ebics.client.i18n.GenericTextProvider;
import org.kopi.ebics.schema.h003.OrderAttributeType;
import org.kopi.ebics.session.EbicsSession;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;
import org.kopi.ebics.utils.Constants;

import java.io.*;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * The ebics client application. Performs necessary tasks to contact the ebics
 * bank server like sending the INI, HIA and HPB requests for keys retrieval and
 * also performs the files transfer including uploads and downloads.
 *
 */
public class EbicsClient {

    private final Configuration configuration;
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Partner> partners = new HashMap<>();
    private final Map<String, Bank> banks = new HashMap<>();
    private final GenericTextProvider logMessages;

    static {
        org.apache.xml.security.Init.init();
        java.security.Security.addProvider(new BouncyCastleProvider());
    }

    private final IEbicsLogger logger;
    private final LetterManager letterManager;
    private final TraceManager traceManager;

    /**
     * Constructs a new ebics client application
     *
     * @param configuration the application configuration
     * @param letterManager
     * @param traceManager
     */
    public EbicsClient(Configuration configuration, IEbicsLogger logger, LetterManager letterManager, TraceManager traceManager) {
        this.configuration = configuration;
        this.logMessages = new GenericTextProvider(Constants.APPLICATION_BUNDLE_NAME, configuration.getLocale());
        this.logger= logger;
        this.letterManager = letterManager;
        this.traceManager = traceManager;
        logger.info(logMessages.getString("init.configuration"));
        configuration.init();
    }

    private EbicsSession createSession(User user, Product product) {
        EbicsSession session = new EbicsSession(user, configuration);
        session.setProduct(product);
        return session;
    }

    /**
     * Creates the user necessary directories
     *
     * @param user
     *            the concerned user
     */
    public void createUserDirectories(EbicsUser user) {
        logger.info(
            logMessages.getString("user.create.directories", user.getUserId()));
        IOUtils.createDirectories(configuration.getUserDirectory(user));
        IOUtils.createDirectories(configuration.getTransferTraceDirectory(user));
        IOUtils.createDirectories(configuration.getKeystoreDirectory(user));
        IOUtils.createDirectories(configuration.getLettersDirectory(user));
    }

    /**
     * Creates a new EBICS bank with the data you should have obtained from the
     * bank.
     *
     * @param url
     *            the bank URL
     * @param name
     *            the bank name
     * @param hostId
     *            the bank host ID
     * @param useCertificate
     *            does the bank use certificates ?
     * @return the created ebics bank
     */
    private Bank createBank(URL url, String name, String hostId, boolean useCertificate) {
        Bank bank = new Bank(url, name, hostId, useCertificate);
        banks.put(hostId, bank);
        return bank;
    }

    /**
     * Creates a new ebics partner
     *
     * @param bank
     *            the bank
     * @param partnerId
     *            the partner IDqr
     */
    private Partner createPartner(EbicsBank bank, String partnerId) {
        Partner partner = new Partner(bank, partnerId);
        partners.put(partnerId, partner);
        return partner;
    }

    /**
     * Creates a new ebics user and generates its certificates.
     *
     * @param url
     *            the bank url
     * @param bankName
     *            the bank name
     * @param hostId
     *            the bank host ID
     * @param partnerId
     *            the partner ID
     * @param userId
     *            UserId as obtained from the bank.
     * @param name
     *            the user name,
     * @param email
     *            the user email
     * @param country
     *            the user country
     * @param organization
     *            the user organization or company
     * @param useCertificates
     *            does the bank use certificates ?
     * @param saveCertificates
     *            save generated certificates?
     * @param passwordCallback
     *            a callback-handler that supplies us with the password. This
     *            parameter can be null, in this case no password is used.
     * @return
     * @throws Exception
     */
    public User createUser(URL url, String bankName, String hostId, String partnerId,
        String userId, String name, String email, String country, String organization,
        boolean useCertificates, boolean saveCertificates, PasswordCallback passwordCallback)
        throws Exception {
        logger.info(logMessages.getString("user.create.info", userId));

        Bank bank = createBank(url, bankName, hostId, useCertificates);
        Partner partner = createPartner(bank, partnerId);
        try {
            User user = new User(partner, userId, name, email, country, organization,
                passwordCallback);
            createUserDirectories(user);
            if (saveCertificates) {
                user.saveUserCertificates(configuration.getKeystoreDirectory(user));
            }
            configuration.getSerializationManager().serialize(bank);
            configuration.getSerializationManager().serialize(partner);
            configuration.getSerializationManager().serialize(user);
            createLetters(user, useCertificates);
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);

            logger.info(logMessages.getString("user.create.success", userId));
            return user;
        } catch (Exception e) {
            logger.error(logMessages.getString("user.create.error"), e);
            throw e;
        }
    }

    public void createLetters(EbicsUser user, boolean useCertificates)
        throws GeneralSecurityException, IOException, EbicsException {
        user.getPartner().getBank().setUseCertificate(useCertificates);
       
        List<IEbicsInitLetter> letters = Arrays.asList(letterManager.createA005Letter(user),
            letterManager.createE002Letter(user), letterManager.createX002Letter(user));

        File directory = new File(configuration.getLettersDirectory(user));
        for (IEbicsInitLetter letter : letters) {
            try (FileOutputStream out = new FileOutputStream(new File(directory, letter.getName()))) {
                letter.writeTo(out);
            }
        }
    }

    /**
     * Loads a user knowing its ID
     *
     * @throws Exception
     */
    public User loadUser(String hostId, String partnerId, String userId,
        PasswordCallback passwordCallback) throws Exception {
        logger.info(logMessages.getString("user.load.info", userId));

        try {
            Bank bank;
            Partner partner;
            User user;
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                hostId)) {
                bank = (Bank) input.readObject();
            }
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                "partner-" + partnerId)) {
                partner = new Partner(bank, input);
            }
            try (ObjectInputStream input = configuration.getSerializationManager().deserialize(
                "user-" + userId)) {
                user = new User(partner, input, passwordCallback);
            }
            users.put(userId, user);
            partners.put(partner.getPartnerId(), partner);
            banks.put(bank.getHostId(), bank);
            logger.info(logMessages.getString("user.load.success", userId));
            return user;
        } catch (Exception e) {
            logger.error(logMessages.getString("user.load.error"), e);
            throw e;
        }
    }

    /**
     * Sends an INI request to the ebics bank server
     *
     * @param user the user
     * @param product the application product
     * @throws Exception
     */
    public void sendINIRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        logger.info(logMessages.getString("ini.request.send", userId));
        if (user.isInitialized()) {
            logger.info(logMessages.getString("user.already.initialized", userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session, traceManager);
        traceManager.setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            keyManager.sendINI(null);
            user.setInitialized(true);
            logger.info(logMessages.getString("ini.send.success", userId));
        } catch (Exception e) {
            logger.error(logMessages.getString("ini.send.error", userId), e);
            throw e;
        }
    }

    /**
     * Sends a HIA request to the ebics server.
     *
     * @param user
     *            the user ID.
     * @param product
     *            the application product.
     * @throws Exception
     */
    public void sendHIARequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        logger.info(logMessages.getString("hia.request.send", userId));
        if (user.isInitializedHIA()) {
            logger
                .info(logMessages.getString("user.already.hia.initialized", userId));
            return;
        }
        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session, traceManager);
        traceManager.setTraceDirectory(
            configuration.getTransferTraceDirectory(user));
        try {
            keyManager.sendHIA(null);
            user.setInitializedHIA(true);
        } catch (Exception e) {
            logger.error(logMessages.getString("hia.send.error", userId), e);
            throw e;
        }
        logger.info(logMessages.getString("hia.send.success", userId));
    }

    /**
     * Sends a HPB request to the ebics server.
     */
    public void sendHPBRequest(User user, Product product) throws Exception {
        String userId = user.getUserId();
        logger.info(logMessages.getString("hpb.request.send", userId));

        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session, traceManager);

        traceManager.setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            keyManager.sendHPB();
            logger.info(logMessages.getString("hpb.send.success", userId));
        } catch (Exception e) {
            logger.error(logMessages.getString("hpb.send.error", userId), e);
            throw e;
        }
    }

    /**
     * Sends the SPR order to the bank.
     *
     * @param user
     *            the user ID
     * @param product
     *            the session product
     * @throws Exception
     */
    public void revokeSubscriber(User user, Product product) throws Exception {
        String userId = user.getUserId();

        logger.info(logMessages.getString("spr.request.send", userId));

        EbicsSession session = createSession(user, product);
        KeyManagement keyManager = new KeyManagement(session, traceManager);

        traceManager.setTraceDirectory(
            configuration.getTransferTraceDirectory(user));

        try {
            keyManager.lockAccess();
        } catch (Exception e) {
            logger.error(logMessages.getString("spr.send.error", userId), e);
            throw e;
        }

        logger.info(logMessages.getString("spr.send.success", userId));
    }

    /**
     * Sends a file to the ebics bank server
     * @throws Exception
     */
    public void sendFile(File file, User user, Product product, EbicsOrderType orderType) throws Exception {
        EbicsSession session = createSession(user, product);
        OrderAttributeType.Enum orderAttribute = OrderAttributeType.OZHNN;

        traceManager.setTraceDirectory(configuration.getTransferTraceDirectory(user));

        FileTransfer transferManager = new FileTransfer(session,traceManager,logger);
        
        //  swiss pain.001 file with XE2 command
        if (orderType==OrderType.XE2) {
        	session.addSessionParam("FORMAT", "pain.001.001.03.ch.02");
        }



        try {
            transferManager.sendFile(IOUtils.getFileContent(file), orderType, orderAttribute);
        } catch (IOException | EbicsException e) {
            logger
                .error(logMessages.getString("upload.file.error", file.getAbsolutePath()), e);
            throw e;
        }
    }


    public void fetchFile(File file, User user, Product product, EbicsOrderType orderType,
        boolean isTest, Date start, Date end) throws IOException, EbicsException {

        EbicsSession session = createSession(user, product);
        session.addSessionParam("FORMAT", "pain.xxx.cfonb160.dct");
        if (isTest) {
            session.addSessionParam("TEST", "true");
        }
        traceManager.setTraceDirectory(configuration.getTransferTraceDirectory(user));

        FileTransfer transferManager = new FileTransfer(session,traceManager,logger);

        try {
            transferManager.fetchFile(orderType, start, end, file);
        } catch (NoDownloadDataAvailableException e) {
            // don't log this exception as an error, caller can decide how to handle
            throw e;
        } catch (Exception e) {
            logger.error(logMessages.getString("download.file.error"), e);
            throw e;
        }
    }


    /**
     * Performs buffers save before quitting the client application.
     */
    public void quit() {
        //TODO: this should not be necessary anymore with repository
        try {
            for (User user : users.values()) {
                if (user.needsSave()) {
                    logger
                        .info(logMessages.getString("app.quit.users", user.getUserId()));
                    configuration.getSerializationManager().serialize(user);
                }
            }

            for (Partner partner : partners.values()) {
                if (partner.needsSave()) {
                    logger
                        .info(logMessages.getString("app.quit.partners", partner.getPartnerId()));
                    configuration.getSerializationManager().serialize(partner);
                }
            }

            for (Bank bank : banks.values()) {
                if (bank.needsSave()) {
                    logger
                        .info(logMessages.getString("app.quit.banks", bank.getHostId()));
                    configuration.getSerializationManager().serialize(bank);
                }
            }
        } catch (EbicsException e) {
            logger.info(logMessages.getString("app.quit.error"));
        }

        clearTraces();
    }

    public void clearTraces() {
        logger.info(logMessages.getString("app.cache.clear"));
        traceManager.clear();
    }







}
