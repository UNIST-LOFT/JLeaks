public static boolean isKali() 
{
    if (onKali == null) {
        onKali = Boolean.FALSE;
        File osReleaseFile = new File("/etc/os-release");
        if (isLinux() && !isDailyBuild() && osReleaseFile.exists()) {
            // Ignore the fact we're on Kali if this is a daily build - they will only have been installed manually
            try (InputStream in = Files.newInputStream(osReleaseFile.toPath())) {
                Properties osProps = new Properties();
                osProps.load(in);
                String osLikeValue = osProps.getProperty("ID");
                if (osLikeValue != null) {
                    String[] oSLikes = osLikeValue.split(" ");
                    for (String osLike : oSLikes) {
                        if (osLike.toLowerCase().equals("kali")) {
                            onKali = Boolean.TRUE;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }
    }
    return onKali;
}