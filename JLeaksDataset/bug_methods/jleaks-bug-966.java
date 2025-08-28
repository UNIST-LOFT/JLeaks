    private static String getFormattedLicense(@NonNull final Context context,
                                              @NonNull final License license) {
        final StringBuilder licenseContent = new StringBuilder();
        final String webViewData;
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    context.getAssets().open(license.getFilename()), StandardCharsets.UTF_8));
            String str;
            while ((str = in.readLine()) != null) {
                licenseContent.append(str);
            }
            in.close();

            // split the HTML file and insert the stylesheet into the HEAD of the file
            webViewData = licenseContent.toString().replace("</head>",
                    "<style>" + getLicenseStylesheet(context) + "</style></head>");
        } catch (final IOException e) {
            throw new IllegalArgumentException(
                    "Could not get license file: " + license.getFilename(), e);
        }
        return webViewData;
    }
