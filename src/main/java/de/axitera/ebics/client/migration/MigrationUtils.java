package de.axitera.ebics.client.migration;

import de.axitera.ebics.client.EbicsClient;
import org.kopi.ebics.client.User;
import org.kopi.ebics.interfaces.PasswordCallback;

import java.net.URL;

public class MigrationUtils {


    private User createUser(EbicsClient.ConfigProperties properties, PasswordCallback pwdHandler)
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
        return createUser(new URL(bankUrl), bankName, hostId, partnerId, userId, userName, userEmail,
                userCountry, userOrg, useCertificates, saveCertificates, pwdHandler);
    }



}
