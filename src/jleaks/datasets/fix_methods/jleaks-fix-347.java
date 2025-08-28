
            public void run() {
                PrintWriter out = null;
                boolean fileOutput = false;

                try {
                    try {
                        out = new PrintWriter(new FileOutputStream(PROFILEFILE));
                        fileOutput = true;
                    } catch (final FileNotFoundException e) {
                        out = Context.getContext().getErr();
                    }

                    dump(out);
                } finally {
                    if (out != null && fileOutput) {
                        out.close();
                    }
                }
            }
