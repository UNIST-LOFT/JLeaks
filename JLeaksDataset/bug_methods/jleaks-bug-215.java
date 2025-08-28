    private ArrayList<WritableComparable> readPartitions(FileSystem fs, Path p, JobConf job) 
    	throws IOException 
    {
    	SequenceFile.Reader reader = new SequenceFile.Reader(fs, p, job);
    	ArrayList<WritableComparable> parts = new ArrayList<WritableComparable>();
    	try 
    	{
			//WritableComparable key = keyClass.newInstance();
    		DoubleWritable key = new DoubleWritable();
			NullWritable value = NullWritable.get();
			while (reader.next(key, value)) {
				parts.add(key);
				//key=keyClass.newInstance();
				key = new DoubleWritable();
			}
		} 
    	catch (Exception e) {
			throw new RuntimeException(e);
		} 
		
		reader.close();
		return parts;
    }
