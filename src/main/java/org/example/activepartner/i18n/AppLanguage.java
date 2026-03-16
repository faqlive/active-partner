package org.example.activepartner.i18n;

import java.util.Locale;

public enum AppLanguage {
    SPANISH("es", "Espanol", new Locale("es")),
    ENGLISH("en", "English", Locale.ENGLISH);

    private final String code;
    private final String label;
    private final Locale locale;

    AppLanguage(String code, String label, Locale locale) {
        this.code = code;
        this.label = label;
        this.locale = locale;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Locale getLocale() {
        return locale;
    }

    public static AppLanguage fromCode(String code) {
        if (code == null) {
            return SPANISH;
        }
        for (AppLanguage language : values()) {
            if (language.code.equalsIgnoreCase(code.trim())) {
                return language;
            }
        }
        return SPANISH;
    }
}
