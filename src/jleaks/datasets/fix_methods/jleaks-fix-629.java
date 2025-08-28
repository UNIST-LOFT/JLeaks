
    public StyleSheet getStyleSheet() {
        AppContext appContext = AppContext.getAppContext();
        StyleSheet defaultStyles = (StyleSheet) appContext.get(DEFAULT_STYLES_KEY);

        if (defaultStyles == null) {
            defaultStyles = new StyleSheet();
            appContext.put(DEFAULT_STYLES_KEY, defaultStyles);
            try (InputStream is = HTMLEditorKit.getResourceAsStream(DEFAULT_CSS);
                 InputStreamReader isr = new InputStreamReader(is, ISO_8859_1);
                 Reader r = new BufferedReader(isr))
            {
                defaultStyles.loadRules(r, null);
            } catch (Throwable e) {
                // on error we simply have no styles... the html
                // will look mighty wrong but still function.
            }
        }
        return defaultStyles;
    }