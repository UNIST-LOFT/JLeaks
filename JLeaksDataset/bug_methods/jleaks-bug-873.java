    private String generateFromTemplate(final CharSequence[] namespaces, final String templateName)
        throws IOException
    {
        final String jarFile = "golang/templates/" + templateName + ".go";
        final InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jarFile);
        final Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        final String template = s.hasNext() ? s.next() : "";
        inputStream.close();

        return String.format(template, namespacesToPackageName(namespaces));
    }
