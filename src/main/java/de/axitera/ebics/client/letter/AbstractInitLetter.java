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

package de.axitera.ebics.client.letter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.axitera.ebics.client.i18n.ITranslatedTextProvider;
import org.apache.commons.codec.binary.Hex;
import org.kopi.ebics.exception.EbicsException;
import de.axitera.ebics.client.letter.IEbicsInitLetter;
import de.axitera.ebics.client.i18n.GenericTextProvider;


public abstract class AbstractInitLetter implements IEbicsInitLetter {



  private LetterTemplate  letter;
  protected Locale  locale;
  protected final TranslatedLetterText messages;

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  /**
   * Constructs a new initialization letter.
   * @param locale the application locale
   */
  public AbstractInitLetter(Locale locale) {
    this.locale = locale;
    this.messages = new TranslatedLetterText(locale);
  }

  @Override
  public void writeTo(OutputStream output) throws IOException {
    output.write(letter.getLetter());
  }

  /**
   * Builds an initialization letter.
   * @param hostId the host ID
   * @param bankName the bank name
   * @param userId the user ID
   * @param username the user name
   * @param partnerId the partner ID
   * @param version the signature version
   * @param certTitle the certificate title
   * @param certificate the certificate content
   * @param hashTitle the hash title
   * @param hash the hash value
   * @throws IOException
   */
  protected void build(String hostId,
                       String bankName,
                       String userId,
                       String username,
                       String partnerId,
                       String version,
                       String certTitle,
                       byte[] certificate,
                       String hashTitle,
                       byte[] hash)
    throws IOException {
    letter = new LetterTemplate(
      getTitle(),
      hostId,
      bankName,
      userId,
      username,
      partnerId,
      version,
      messages
    );
    letter.build(certTitle, certificate, hashTitle, hash);
  }

  /**
   * Returns the value of the property key.
   *
   * @param key the property key
   * @return the property value
   */
  protected String getString(String key) {
    return messages.getString(key);
  }

  /**
   * Returns the certificate hash
   * @param certificate the certificate
   * @return the certificate hash
   * @throws GeneralSecurityException
   */
  protected byte[] getHash(byte[] certificate) throws GeneralSecurityException {
    String hash256 = new String(
        Hex.encodeHex(MessageDigest.getInstance("SHA-256").digest(certificate), false));
    return format(hash256).getBytes();
  }

    protected byte[] getHash(RSAPublicKey publicKey) throws EbicsException {
        String			modulus;
        String			exponent;
        String			hash;
        byte[]			digest;

        exponent = Hex.encodeHexString(publicKey.getPublicExponent().toByteArray());
        modulus =  Hex.encodeHexString(removeFirstByte(publicKey.getModulus().toByteArray()));
        hash = exponent + " " + modulus;

        if (hash.charAt(0) == '0') {
          hash = hash.substring(1);
        }

        try {
          digest = MessageDigest.getInstance("SHA-256", "BC").digest(hash.getBytes("US-ASCII"));
        } catch (GeneralSecurityException | UnsupportedEncodingException e) {
          throw new EbicsException(e.getMessage());
        }

        return format(new String(Hex.encodeHex(digest, false))).getBytes();
    }

    private static byte[] removeFirstByte(byte[] byteArray) {
        byte[] b = new byte[byteArray.length - 1];
        System.arraycopy(byteArray, 1, b, 0, b.length);
        return b;
    }

  /**
   * Formats a hash 256 input.
   * @param hash256 the hash input
   * @return the formatted hash
   */
  private String format(String hash256) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < hash256.length(); i += 2) {
      buffer.append(hash256.charAt(i));
      buffer.append(hash256.charAt(i + 1));
      buffer.append(' ');
    }
    return buffer.substring(0, 48) + LINE_SEPARATOR + buffer.substring(48) + LINE_SEPARATOR;
  }


}
