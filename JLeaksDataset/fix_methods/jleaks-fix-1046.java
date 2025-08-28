private static void help() 
{
    try (InputStream help = SimpleCommandLineValidator.class.getClassLoader().getResourceAsStream("nu/validator/localentities/files/cli-help")) {
        System.out.println("");
        for (int b = help.read(); b != -1; b = help.read()) {
            System.out.write(b);
        }
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}