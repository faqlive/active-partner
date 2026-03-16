package org.example.activepartner.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class I18nService {
    private static final String BASE_NAME = "org.example.activepartner.i18n.messages";

    private Locale locale;

    public I18nService() {
        this.locale = AppLanguage.SPANISH.getLocale();
    }

    public synchronized void setLanguageCode(String code) {
        this.locale = AppLanguage.fromCode(code).getLocale();
    }

    public synchronized String getLanguageCode() {
        return locale.getLanguage();
    }

    public synchronized ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME, locale);
    }

    public synchronized String text(String key, Object... args) {
        String pattern = getBundle().getString(key);
        if (args == null || args.length == 0) {
            return pattern;
        }
        return new MessageFormat(pattern, locale).format(args);
    }
}
