package de.axitera.ebics.client.letter;

import de.axitera.ebics.client.i18n.GenericTextProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The <code>Letter</code> object is the common template
 * for all initialization letter.
 *
 * @author Hachani
 *
 */
class LetterTemplate {

    private final TranslatedLetterText messages;
    private static final String			LINE_SEPARATOR = System.getProperty("line.separator");


    /**
     * Constructs new <code>Letter</code> template
     * @param title the letter title
     * @param hostId the host ID
     * @param bankName the bank name
     * @param userId the user ID
     * @param partnerId the partner ID
     * @param version the signature version
     */
    public LetterTemplate(String title,
                  String hostId,
                  String bankName,
                  String userId,
                  String username,
                  String partnerId,
                  String version,
                          TranslatedLetterText messages)
    {
        this.title = title;
        this.hostId = hostId;
        this.bankName = bankName;
        this.userId = userId;
        this.username = username;
        this.partnerId = partnerId;
        this.version = version;
        this.messages = messages;
    }

    /**
     * Builds the letter content.
     * @param certTitle the certificate title
     * @param certificate the certificate content
     * @param hashTitle the hash title
     * @param hash the hash content
     * @throws IOException
     */
    public void build(String certTitle,
                      byte[] certificate,
                      String hashTitle,
                      byte[] hash)
            throws IOException
    {
        out = new ByteArrayOutputStream();
        writer = new PrintWriter(out, true);
        buildTitle();
        buildHeader();
        if (certificate != null)
            buildCertificate(certTitle, certificate);
        buildHash(hashTitle, hash);
        buildFooter();
        writer.close();
        out.flush();
        out.close();
    }

    /**
     * Builds the letter title.
     * @throws IOException
     */
    public void buildTitle() throws IOException {
        emit(title);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
    }

    /**
     * Builds the letter header
     * @throws IOException
     */
    public void buildHeader() throws IOException {
        emit(messages.getString("Letter.date"));
        appendSpacer();
        emit(formatDate(new Date()));
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.time"));
        appendSpacer();
        emit(formatTime(new Date()));
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.hostId"));
        appendSpacer();
        emit(hostId);
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.bank"));
        appendSpacer();
        emit(bankName);
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.userId"));
        appendSpacer();
        emit(userId);
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.username"));
        appendSpacer();
        emit(username);
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.partnerId"));
        appendSpacer();
        emit(partnerId);
        emit(LINE_SEPARATOR);
        emit(messages.getString("Letter.version"));
        appendSpacer();
        emit(version);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
    }

    /**
     * Writes the certificate core.
     * @param title the title
     * @param cert the certificate core
     * @throws IOException
     */
    public void buildCertificate(String title, byte[] cert)
            throws IOException
    {
        emit(title);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit("-----BEGIN CERTIFICATE-----" + LINE_SEPARATOR);
        emit(new String(cert));
        emit("-----END CERTIFICATE-----" + LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
    }

    /**
     * Builds the hash section.
     * @param title the title
     * @param hash the hash value
     * @throws IOException
     */
    public void buildHash(String title, byte[] hash)
            throws IOException
    {
        emit(title);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(new String(hash));
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
        emit(LINE_SEPARATOR);
    }

    /**
     * Builds the footer section
     * @throws IOException
     */
    public void buildFooter() throws IOException {
        emit(messages.getString("Letter.date"));
        emit("                                  ");
        emit(messages.getString("Letter.signature"));
    }

    /**
     * Appends a spacer
     * @throws IOException
     */
    public void appendSpacer() throws IOException {
        emit("        ");
    }

    /**
     * Emits a text to the writer
     * @param text the text to print
     * @throws IOException
     */
    public void emit(String text) throws IOException {
        writer.write(text);
    }

    /**
     * Formats the input date
     * @param date the input date
     * @return the formatted date
     */
    public String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                messages.getString("Letter.dateFormat"));
        return formatter.format(date);
    }

    /**
     * Formats the input time
     * @param time the input time
     * @return the formatted time
     */
    public String formatTime(Date time) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                messages.getString("Letter.timeFormat"));
        return formatter.format(time);
    }

    /**
     * Returns the letter content
     * @return
     */
    public byte[] getLetter() {
        return out.toByteArray();
    }

    // --------------------------------------------------------------------
    // DATA MEMBERS
    // --------------------------------------------------------------------

    private ByteArrayOutputStream	out;
    private Writer writer;
    private final String		title;
    private final String		hostId;
    private final String		bankName;
    private final String		userId;
    private final String		username;
    private final String		partnerId;
    private final String		version;
}