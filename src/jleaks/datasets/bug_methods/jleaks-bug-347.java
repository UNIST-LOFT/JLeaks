            public void run() {
                OutputStream out;

                try {
                    out = new FileOutputStream(PROFILEFILE);
                } catch (final FileNotFoundException e) {
                    out = new PrintStream(System.err); //TODO abstraction break- why is this hard coded to System.err when everything else uses the context
                }

                try (PrintStream stream = new PrintStream(out)) {
                    int index = 0;
                    for (final ProfilingLinkerCallSite callSite : profileCallSites) {
                       stream.println("" + (index++) + '\t' +
                                      callSite.getDescriptor().getName() + '\t' +
                                      callSite.totalTime + '\t' +
                                      callSite.hitCount);
                    }
                }
            }
