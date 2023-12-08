package de.axitera.ebics.client.migration;

import de.axitera.ebics.client.EbicsClient;
import org.kopi.ebics.client.User;
import org.kopi.ebics.interfaces.PasswordCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class MigrationUtils {


    public static class LegacyConfigProperties {
        Properties properties = new Properties();

        public LegacyConfigProperties(File file) throws IOException {
            properties.load(new FileInputStream(file));
        }

        public String get(String key) {
            String value = properties.getProperty(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("property not set or empty: " + key);
            }
            return value.trim();
        }
    }

    private User createUser(LegacyConfigProperties properties, PasswordCallback pwdHandler)
            throws Exception {
        String userId = properties.get("userId");
        String partnerId = properties.get("partnerId");
        String bankUrl = properties.get("bank.url");
        String bankName = properties.get("bank.name");
        String hostId = properties.get("hostId");
        String userName = properties.get("user.name");
        String userEmail = properties.get("user.email");
        String userCountry = properties.get("user.country");
        String userOrg = properties.get("user.org");
        boolean useCertificates = false;
        boolean saveCertificates = true;
        //TODO: call client, create a user.
        //return createUser(new URL(bankUrl), bankName, hostId, partnerId, userId, userName, userEmail,
         //       userCountry, userOrg, useCertificates, saveCertificates, pwdHandler);

        return null;
    }



}
