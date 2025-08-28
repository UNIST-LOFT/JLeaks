        public void doPollingLog(StaplerRequest req, StaplerResponse rsp) throws IOException {
            rsp.setContentType("text/plain;charset=UTF-8");
            // Prevent jelly from flushing stream so Content-Length header can be added afterwards
            FlushProofOutputStream out = new FlushProofOutputStream(rsp.getCompressedOutputStream(req));
            getPollingLogText().writeLogTo(0, out);
            out.close();
        }
