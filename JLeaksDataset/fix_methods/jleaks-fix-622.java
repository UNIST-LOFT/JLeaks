 
        public SourceCache(AnalyzeTask originalTask) {
            this.originalTask = originalTask;
            List<Path> sources = findSources();
            if (sources.iterator().hasNext()) {
                StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
                try {
                    fm.setLocationFromPaths(StandardLocation.SOURCE_PATH, sources);
                } catch (IOException ex) {
                    proc.debug(ex, "SourceCodeAnalysisImpl.SourceCache.<init>(...)");
                    try {
                        fm.close();
                    } catch (IOException closeEx) {
                        proc.debug(closeEx, "SourceCodeAnalysisImpl.SourceCache.close()");
                    }
                    fm = null;
                }
                this.fm = fm;
            } else {
                //don't waste time if there are no sources
                this.fm = null;
            }
        }