  public InputSplit[] getSplits(JobConf jobConf, int numSplits) throws IOException {

    RecordScannable recordScannable = DatasetAccessor.getRecordScannable(jobConf);

    Job job = new Job(jobConf);
    JobContext jobContext = ShimLoader.getHadoopShims().newJobContext(job);
    // TODO: figure out the significance of table paths - REACTOR-277
    Path[] tablePaths = FileInputFormat.getInputPaths(jobContext);

    List<Split> dsSplits = recordScannable.getSplits();
    recordScannable.close();

    InputSplit[] inputSplits = new InputSplit[dsSplits.size()];
    for (int i = 0; i < dsSplits.size(); i++) {
      inputSplits[i] = new DatasetInputSplit(dsSplits.get(i), tablePaths[0]);
    }
    return inputSplits;
  }
