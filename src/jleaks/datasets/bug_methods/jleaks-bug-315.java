                    public void run(InputStream in) throws Exception {
                        final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                        final StringBuilder sb = new StringBuilder();
                        try {
                            boolean first = false;
                            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                                if (first) {
                                    first = false;
                                } else {
                                    sb.append('\n');
                                }
                                sb.append(line);
                            }
                            group.setDescription(sb.toString());
                        } catch (Exception e) {
                            logger.error("Error while reading timeline group description file: " + descriptionFile, e);
                        }

                        reader.close();
                    }