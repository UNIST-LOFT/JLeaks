 
    public static int remotePost(String[] args) throws Exception {
        String projectName = args[0];

        String home = getHudsonHome();
        if(!home.endsWith("/"))     home = home + '/';  // make sure it ends with '/'

        // check for authentication info
        String auth = new URL(home).getUserInfo();
        if(auth != null) auth = "Basic " + new Base64Encoder().encode(auth.getBytes("UTF-8"));

        {// check if the home is set correctly
            HttpURLConnection con = open(new URL(home));
            if (auth != null) con.setRequestProperty("Authorization", auth);
            con.connect();
            if(con.getResponseCode()!=200
            || con.getHeaderField("X-Hudson")==null) {
                System.err.println(home+" is not Hudson ("+con.getResponseMessage()+")");
                return -1;
            }
        }

        String projectNameEnc = URLEncoder.encode(projectName,"UTF-8").replaceAll("\\+","%20");

        {// check if the job name is correct
            HttpURLConnection con = open(new URL(home+"job/"+projectNameEnc+"/acceptBuildResult"));
            if (auth != null) con.setRequestProperty("Authorization", auth);
            con.connect();
            if(con.getResponseCode()!=200) {
                System.err.println(projectName+" is not a valid job name on "+home+" ("+con.getResponseMessage()+")");
                return -1;
            }
        }

        // get a crumb to pass the csrf check
        String crumbField = null, crumbValue = null;
        try {
            HttpURLConnection con = open(new URL(home +
                    "crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)'"));
            if (auth != null) con.setRequestProperty("Authorization", auth);
            String line = IOUtils.readFirstLine(con.getInputStream(),"UTF-8");
            String[] components = line.split(":");
            if (components.length == 2) {
                crumbField = components[0];
                crumbValue = components[1];
            }
        } catch (IOException e) {
            // presumably this Hudson doesn't use CSRF protection
        }

        // write the output to a temporary file first.
        File tmpFile = File.createTempFile("hudson","log");
        try {
            FileOutputStream os = new FileOutputStream(tmpFile);

            Writer w = new OutputStreamWriter(os,"UTF-8");
            int ret;
            try {
                w.write("<?xml version='1.0' encoding='UTF-8'?>");
                w.write("<run><log encoding='hexBinary' content-encoding='"+Charset.defaultCharset().name()+"'>");
                w.flush();

                // run the command
                long start = System.currentTimeMillis();

                List<String> cmd = new ArrayList<String>();
                for( int i=1; i<args.length; i++ )
                    cmd.add(args[i]);
                Proc proc = new Proc.LocalProc(cmd.toArray(new String[0]),(String[])null,System.in,
                    new DualOutputStream(System.out,new EncodingStream(os)));

                ret = proc.join();

                w.write("</log><result>"+ret+"</result><duration>"+(System.currentTimeMillis()-start)+"</duration></run>");
            }
            finally {
                try {
                    w.close();
                }
                catch (IOException ioe) {
                    // swallow exception
                }
            }

            String location = home+"job/"+projectNameEnc+"/postBuildResult";
            while(true) {
                try {
                    // start a remote connection
                    HttpURLConnection con = open(new URL(location));
                    if (auth != null) con.setRequestProperty("Authorization", auth);
                    if (crumbField != null && crumbValue != null) {
                        con.setRequestProperty(crumbField, crumbValue);
                    }
                    con.setDoOutput(true);
                    // this tells HttpURLConnection not to buffer the whole thing
                    con.setFixedLengthStreamingMode((int)tmpFile.length());
                    con.connect();
                    // send the data
                    FileInputStream in = new FileInputStream(tmpFile);
                    try {
                        Util.copyStream(in,con.getOutputStream());
                    }
                    finally {
                        try {
                            in.close();
                        }
                        catch (IOException ioe) {
                            // swallow exception
                        }
                    }

                    if(con.getResponseCode()!=200) {
                        Util.copyStream(con.getErrorStream(),System.err);
                    }

                    return ret;
                } catch (HttpRetryException e) {
                    if(e.getLocation()!=null) {
                        // retry with the new location
                        location = e.getLocation();
                        continue;
                    }
                    // otherwise failed for reasons beyond us.
                    throw e;
                }
            }
        } finally {
            tmpFile.delete();
        }
    }