    private boolean writeCommands(List<String> commands) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            //Write the opening to the list of commands
            writer.write(VARNAME);
            //Write all the commands to the list
            for (String command: commands){
                writer.write(command);
                writer.newLine();
            }
            writer.write("\"");
            writer.newLine();
            //Write function to generate the commands
            writer.write(BASH_FUNCTION);
            writer.newLine();
            //write function to tell bash what function the autocompletion is for
            writer.write(COMPLETE_CALL);
            writer.newLine();
            //Add directory of payara5/glassfish/bin to the path
            writer.write(ADD_PATH);
            writer.write(serverContext.getInstallRoot().getPath() + File.separator + "bin");
            //flush the buffer
            writer.flush();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(GenerateBashAutoCompletionCommand.class.getName()).log(Level.WARNING, "Unable to write to file at " + filePath, ex);
        }
        return false;

    }