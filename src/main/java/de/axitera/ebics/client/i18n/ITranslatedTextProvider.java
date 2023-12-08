package de.axitera.ebics.client.i18n;

import java.util.Locale;

public interface ITranslatedTextProvider {
    String getString(String key, Object ... arguments);
    String getString(String key);
    void reInitWithNewLocale(Locale locale);
}
