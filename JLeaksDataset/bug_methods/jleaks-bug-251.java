    public Object source( String filename, NameSpace nameSpace )
        throws FileNotFoundException, IOException, EvalError
    {
        File file = pathToFile( filename );
        Interpreter.debug("Sourcing file: ", file);
        Reader sourceIn = new BufferedReader( new FileReader(file) );
        try {
            return eval( sourceIn, nameSpace, filename );
        } finally {
            sourceIn.close();
        }
    }
