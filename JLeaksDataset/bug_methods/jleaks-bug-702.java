    public static void writeToBinaryFile(int[] pitchMarks, String filename) throws IOException 
    {
        DataOutputStream d = new DataOutputStream(new FileOutputStream(new File(filename))); 
        
        d.writeInt(pitchMarks.length);
        
        for (int i=0; i<pitchMarks.length; i++)
            d.writeInt(pitchMarks[i]);
        
        d.close();
    }
