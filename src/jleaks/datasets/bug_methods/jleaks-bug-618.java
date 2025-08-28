    public int write(AudioInputStream stream, AudioFileFormat.Type fileType, File out) throws IOException {
        Objects.requireNonNull(stream);
        Objects.requireNonNull(fileType);
        Objects.requireNonNull(out);

        // throws IllegalArgumentException if not supported
        AiffFileFormat aiffFileFormat = (AiffFileFormat)getAudioFileFormat(fileType, stream);

        // first write the file without worrying about length fields
        FileOutputStream fos = new FileOutputStream( out );     // throws IOException
        BufferedOutputStream bos = new BufferedOutputStream( fos, bisBufferSize );
        int bytesWritten = writeAiffFile(stream, aiffFileFormat, bos );
        bos.close();

        // now, if length fields were not specified, calculate them,
        // open as a random access file, write the appropriate fields,
        // close again....
        if( aiffFileFormat.getByteLength()== AudioSystem.NOT_SPECIFIED ) {

            // $$kk: 10.22.99: jan: please either implement this or throw an exception!
            // $$fb: 2001-07-13: done. Fixes Bug 4479981
            int channels = aiffFileFormat.getFormat().getChannels();
            int sampleSize = aiffFileFormat.getFormat().getSampleSizeInBits();
            int ssndBlockSize = channels * ((sampleSize + 7) / 8);

            int aiffLength=bytesWritten;
            int ssndChunkSize=aiffLength-aiffFileFormat.getHeaderSize()+16;
            long dataSize=ssndChunkSize-16;
            //TODO possibly incorrect round
            int numFrames = (int) (dataSize / ssndBlockSize);

            RandomAccessFile raf=new RandomAccessFile(out, "rw");
            // skip FORM magic
            raf.skipBytes(4);
            raf.writeInt(aiffLength-8);
            // skip aiff2 magic, fver chunk, comm magic, comm size, channel count,
            raf.skipBytes(4+aiffFileFormat.getFverChunkSize()+4+4+2);
            // write frame count
            raf.writeInt(numFrames);
            // skip sample size, samplerate, SSND magic
            raf.skipBytes(2+10+4);
            raf.writeInt(ssndChunkSize-8);
            // that's all
            raf.close();
        }
    }
