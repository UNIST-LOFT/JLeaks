    private static void help() {
        InputStream help = SimpleCommandLineValidator.class.getClassLoader().getResourceAsStream(
                "nu/validator/localentities/files/cli-help");
        try {
            System.out.println("");
            for (int b = help.read(); b != -1; b = help.read()) {
                System.out.write(b);
            }
            help.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
