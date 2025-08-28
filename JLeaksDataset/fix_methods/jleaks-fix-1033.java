public void testOnFile() 
{
    String[] vocbs = { "x", "DIGITs" };
    String fpath1 = "/Users/bowu/Research/testdata/tmp/data.txt";
    String fpath2 = "/Users/bowu/Research/testdata/tmp/labels.txt";
    try (BufferedReader br1 = new BufferedReader(new FileReader(fpath1));
        BufferedReader br2 = new BufferedReader(new FileReader(fpath2))) {
        String line = "";
        ArrayList<String> data = new ArrayList<String>();
        ArrayList<String> labels = new ArrayList<String>();
        while ((line = br1.readLine()) != null) {
            if (line.trim().length() > 0) {
                data.add(line);
            }
        }
        while ((line = br2.readLine()) != null) {
            if (line.trim().length() > 0) {
                labels.add(line);
            }
        }
        RecordFeatureSet rfs = new RecordFeatureSet();
        rfs.addVocabulary(vocbs);
        this.rf = rfs;
        for (int i = 0; i < data.size(); i++) {
            addTrainingData(data.get(i), "c" + labels.get(i));
        }
        learnClassifer();
        String[] test = { "4 x 9\"", "H: 58 x  W: 25\"", "15\" x 18\"", "14.75\" H x 11\" W", "Framed at 21.75\" H x 24.25\" W", "49.5\" x 9\"" };
        for (int i = 0; i < test.length; i++) {
            System.out.println(String.format("%s, %s ", test[i], getLabel(test[i])));
        }
        selfVerify();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
}