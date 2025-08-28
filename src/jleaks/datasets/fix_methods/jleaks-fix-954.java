private void writeDppResult(DppResult dppResult) throws Exception 
{
    String outputPath = etlJobConfig.getOutputPath();
    String resultFilePath = outputPath + "/" + DPP_RESULT_FILE;
    URI uri = new URI(outputPath);
    Path filePath = new Path(resultFilePath);
    try (FileSystem fs = FileSystem.get(uri, serializableHadoopConf.value());
        FSDataOutputStream outputStream = fs.create(filePath)) {
        Gson gson = new Gson();
        outputStream.write(gson.toJson(dppResult).getBytes());
        outputStream.write('\n');
    }
}