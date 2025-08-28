public void buildAnalysisScope() throws IOException 
{
    scope = makeSourceAnalysisScope();
    if (getExclusionsFile() != null) {
        try (final InputStream is = new File(getExclusionsFile()).exists() ? new FileInputStream(getExclusionsFile()) : FileProvider.class.getClassLoader().getResourceAsStream(getExclusionsFile())) {
            scope.setExclusions(new FileOfClasses(is));
        }
    }
    for (Module M : this.systemEntries) {
        scope.addToScope(scope.getPrimordialLoader(), M);
    }
    // add user stuff
    addApplicationModulesToScope();
}