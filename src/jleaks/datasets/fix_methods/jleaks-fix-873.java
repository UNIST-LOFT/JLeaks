private String generateFromTemplate(final CharSequence[] namespaces, final String templateName){
    final String jarFile = "golang/templates/" + templateName + ".go";
    try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(jarFile)) {
        final Scanner scanner = new Scanner(new BufferedInputStream(inputStream)).useDelimiter("\\A");
        if (!scanner.hasNext()) {
            return "";
        }
        return String.format(scanner.next(), namespacesToPackageName(namespaces));
    }
}