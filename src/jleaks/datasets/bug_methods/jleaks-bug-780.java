        void stopLoop() {
            try {
                interrupt();
                final FileOutputStream fis = new FileOutputStream(fifoInFile);
                fis.write(66);
                fis.flush();
                fis.close();
                join(8000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
