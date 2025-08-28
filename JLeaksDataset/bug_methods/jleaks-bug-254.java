    public static Set<String> postQuery(String xml)
            throws IOException{



        URL u = new URL(SERVICELOCATION);


        String encodedXML = URLEncoder.encode(xml,"UTF-8");


        InputStream in =  doPOST(u,encodedXML);

        Set<String> pdbIds = new TreeSet<String>();


        BufferedReader rd = new BufferedReader(new InputStreamReader(in));

        String line;
        while ((line = rd.readLine()) != null) {

            pdbIds.add(line);

        }
        rd.close();


        return pdbIds;



    }
