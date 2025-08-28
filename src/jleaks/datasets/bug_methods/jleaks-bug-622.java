        public SourceCache(AnalyzeTask originalTask) {
            this.originalTask = originalTask;
            List<Path> sources = findSources();
            if (sources.iterator().hasNext()) {
                StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
                try {
                    fm.setLocationFromPaths(StandardLocation.SOURCE_PATH, sources);
                } catch (IOException ex) {
                    proc.debug(ex, "SourceCodeAnalysisImpl.SourceCache.<init>(...)");
                    fm = null;
                }
                this.fm = fm;
            } else {
                //don't waste time if there are no sources
                this.fm = null;
            }
        }
