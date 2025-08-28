    public StyleSheet getStyleSheet() {
        AppContext appContext = AppContext.getAppContext();
        StyleSheet defaultStyles = (StyleSheet) appContext.get(DEFAULT_STYLES_KEY);

        if (defaultStyles == null) {
            defaultStyles = new StyleSheet();
            appContext.put(DEFAULT_STYLES_KEY, defaultStyles);
            try {
                InputStream is = HTMLEditorKit.getResourceAsStream(DEFAULT_CSS);
                Reader r = new BufferedReader(
                        new InputStreamReader(is, ISO_8859_1));
                defaultStyles.loadRules(r, null);
                r.close();
            } catch (Throwable e) {
                // on error we simply have no styles... the html
                // will look mighty wrong but still function.
            }
        }
        return defaultStyles;
    }
