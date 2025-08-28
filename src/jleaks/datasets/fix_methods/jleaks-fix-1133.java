 private void readUiExtensions(final Map<UiExtensionType, List<String>> uiExtensions, final UiExtensionType uiExtensionType, final JarFile jarFile, final JarEntry jarEntry) throws IOException {
        if (jarEntry == null) {
            return;
        }

        // get an input stream for the nifi-processor configuration file
        try (BufferedReader in = new BufferedReader(new InputStreamReader(jarFile.getInputStream(jarEntry)))) {

            // read in each configured type
            String rawComponentType;
            while ((rawComponentType = in.readLine()) != null) {
                // extract the component type
                final String componentType = extractComponentType(rawComponentType);
                if (componentType != null) {
                    List<String> extensions = uiExtensions.get(uiExtensionType);

                    // if there are currently no extensions for this type create it
                    if (extensions == null) {
                        extensions = new ArrayList<>();
                        uiExtensions.put(uiExtensionType, extensions);
                    }

                    // add the specified type
                    extensions.add(componentType);
                }
            }
        }
    }