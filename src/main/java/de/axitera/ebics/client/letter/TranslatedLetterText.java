package de.axitera.ebics.client.letter;

import de.axitera.ebics.client.i18n.GenericTextProvider;
import de.axitera.ebics.client.i18n.ITranslatedTextProvider;

import java.util.Locale;

public class TranslatedLetterText extends GenericTextProvider implements ITranslatedTextProvider {

    protected static final String RESOURCE_BUNDLE_NAME = "org.kopi.ebics.letter.messages";

    public TranslatedLetterText(Locale locale) {
        super(RESOURCE_BUNDLE_NAME, locale);
    }

}
