package org.kopi.ebics.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import de.axitera.ebics.client.i18n.GenericTextProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GenericTextProviderTest {

    @Test
    void getString() {
        // fallback to english
        GenericTextProvider messages = new GenericTextProvider("org.kopi.ebics.letter.messages", Locale.CHINA);
        assertEquals("dd.MM.yyyy", messages.getString("Letter.dateFormat"));
    }

    @Test
    void throwWhenUnknownBundle() {
        assertThrows(RuntimeException.class, new Executable() {
            @Override
            public void execute() {
                new GenericTextProvider("org.kopi.ebics.letter.messages_unknown");
            }
        });
    }
}