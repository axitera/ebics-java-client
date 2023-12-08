package de.axitera.ebics.client.cli;

import de.axitera.ebics.client.EbicsClient;
import org.apache.commons.cli.*;
import org.kopi.ebics.client.User;
import org.kopi.ebics.interfaces.EbicsOrderType;
import org.kopi.ebics.session.DefaultConfiguration;
import org.kopi.ebics.session.OrderType;
import org.kopi.ebics.session.Product;


import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CommandLineProcessor {

    static final List<? extends EbicsOrderType> sendFileOrders = Arrays.asList(
            OrderType.XKD, OrderType.FUL, OrderType.XCT,
            OrderType.XE2, OrderType.CCT
    );

    static final List<? extends EbicsOrderType> fetchFileOrders = Arrays.asList(
            OrderType.STA, OrderType.VMK,
            OrderType.C52, OrderType.C53, OrderType.C54,
            OrderType.ZDF, OrderType.ZB6, OrderType.PTK,
            OrderType.HAC, OrderType.Z01,OrderType.Z53, OrderType.Z54
    );


    public static void main(String[] args) throws Exception {
        Options options = new Options();
        addOption(options, OrderType.INI, "Send INI request");
        addOption(options, OrderType.HIA, "Send HIA request");
        addOption(options, OrderType.HPB, "Send HPB request");
        options.addOption(null, "letters", false, "Create INI Letters");
        options.addOption(null, "create", false, "Create and initialize EBICS user");
        addOption(options, OrderType.STA,"Fetch STA file (MT940 file)");
        addOption(options, OrderType.VMK, "Fetch VMK file (MT942 file)");
        addOption(options, OrderType.C52, "Fetch camt.052 file");
        addOption(options, OrderType.C53, "Fetch camt.053 file");
        addOption(options, OrderType.C54, "Fetch camt.054 file");
        addOption(options, OrderType.ZDF, "Fetch ZDF file (zip file with documents)");
        addOption(options, OrderType.ZB6, "Fetch ZB6 file");
        addOption(options, OrderType.PTK, "Fetch client protocol file (TXT)");
        addOption(options, OrderType.HAC, "Fetch client protocol file (XML)");
        addOption(options, OrderType.Z01, "Fetch Z01 file");

        addOption(options, OrderType.XKD, "Send payment order file (DTA format)");
        addOption(options, OrderType.FUL, "Send payment order file (any format)");
        addOption(options, OrderType.XCT, "Send XCT file (any format)");
        addOption(options, OrderType.XE2, "Send XE2 file (any format)");
        addOption(options, OrderType.CCT, "Send CCT file (any format)");

        addOption(options, OrderType.Z53, "Fetch Z53 file (Swiss Kontoauszug)");
        addOption(options, OrderType.Z54, "Fetch Z54 file (Swiss Sammler)");


        options.addOption(null, "skip_order", true, "Skip a number of order ids");

        options.addOption("o", "output", true, "output file");
        options.addOption("i", "input", true, "input file");


        CommandLine cmd = parseArguments(options, args);

        File defaultRootDir = new File(System.getProperty("user.home") + File.separator + "ebics"
                + File.separator + "client");
        File ebicsClientProperties = new File(defaultRootDir, "ebics.txt");
        EbicsClient client = createEbicsClient(defaultRootDir, ebicsClientProperties);

        /*
        if (cmd.hasOption("create")) {
            client.createDefaultUser();
        } else {
            client.loadDefaultUser();
        }
        */
        //FIXME: load user and product.
        User user = null;
        Product product = null;

        if (cmd.hasOption("letters")) {
            client.createLetters(user, false);
        }

        if (hasOption(cmd, OrderType.INI)) {
            client.sendINIRequest(user, product);
        }
        if (hasOption(cmd, OrderType.HIA)) {
            client.sendHIARequest(user, product);
        }
        if (hasOption(cmd, OrderType.HPB)) {
            client.sendHPBRequest(user, product);
        }

        String outputFileValue = cmd.getOptionValue("o");
        String inputFileValue = cmd.getOptionValue("i");


        for (EbicsOrderType type : fetchFileOrders) {
            if (hasOption(cmd, type)) {
                client.fetchFile(getOutputFile(outputFileValue), user,
                        product, type, false, null, null);
                break;
            }
        }

        for (EbicsOrderType type : sendFileOrders) {
            if (hasOption(cmd, type)) {
                client.sendFile(new File(inputFileValue), user,
                        product, type);
                break;
            }
        }

        if (cmd.hasOption("skip_order")) {
            int count = Integer.parseInt(cmd.getOptionValue("skip_order"));
            while(count-- > 0) {
                user.getPartner().nextOrderId();
            }
        }
        client.quit();
    }



    private static void addOption(Options options, EbicsOrderType type, String description) {
        options.addOption(null, type.getCode().toLowerCase(), false, description);
    }

    private static boolean hasOption(CommandLine cmd, EbicsOrderType type) {
        return cmd.hasOption(type.getCode().toLowerCase());
    }

    private static CommandLine parseArguments(Options options, String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        options.addOption(null, "help", false, "Print this help text");
        CommandLine line = parser.parse(options, args);
        if (line.hasOption("help")) {
            HelpFormatter formatter = new HelpFormatter();
            System.out.println();
            formatter.printHelp(EbicsClient.class.getSimpleName(), options);
            System.out.println();
            System.exit(0);
        }
        return line;
    }

    public static EbicsClient createEbicsClient(File rootDir, File configFile) throws IOException {
        //TODO: create file based repos then init
/*
        EbicsClient.ConfigProperties properties = new EbicsClient.ConfigProperties(configFile);
        final String country = properties.get("countryCode").toUpperCase();
        final String language = properties.get("languageCode").toLowerCase();
        final String productName = properties.get("productName");
*/
        /*
        final Locale locale = new Locale(language, country);

        DefaultConfiguration configuration = new DefaultConfiguration(
                rootDir.getAbsolutePath() ){

            @Override
            public Locale getLocale() {
                return locale;
            }
        };
*/
   //     EbicsClient client = new EbicsClient(configuration);

   //     Product product = new Product(productName, language, null);
        //fixme WHAT TO DO ABOUT PRODUICT

   //     return client;
        throw new RuntimeException("NOT IMPLEMENTED");
    }

    private static File getOutputFile(String outputFileName) {
        if (outputFileName == null || outputFileName.isEmpty()) {
            throw new IllegalArgumentException("outputFileName not set");
        }
        File file = new File(outputFileName);
        if (file.exists()) {
            throw new IllegalArgumentException("file already exists " + file);
        }
        return file;
    }


}
