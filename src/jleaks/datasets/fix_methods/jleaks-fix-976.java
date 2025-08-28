private static List<RealClass> readRealClassesFromJarFile(File jarFile){
    if (!jarFile.exists()) {
        throw new FileNotFoundException("Attempted to load jar file at: " + jarFile + " but it does not exist.");
    }
    try (ZipFile zipFile = new ZipFile(jarFile)) {
        return readJarFile(zipFile);
    } catch (IOException e) {
        throw new JarFileParsingException("Error extracting jar data.", e);
    } catch (RealClassCreationException e) {
        throw new JarFileParsingException("Error extracting jar data.", e);
    }
}