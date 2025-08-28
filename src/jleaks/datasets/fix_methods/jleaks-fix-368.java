	public static void processUploadableImport(String url, Collection owningCollection, Collection[] collections, Context context) throws Exception
	{
		final EPerson eperson = context.getCurrentUser();
		final Collection[] otherCollections = collections;
		final Collection theOwningCollection = owningCollection;
		final String zipurl = url;

/*		Thread go = new Thread()
		{
			public void run()
			{
				Context context = null;
*/
				String importDir = null;
				
				try {
					
					// create a new dspace context
	//				context = new Context();
	//				context.setCurrentUser(eperson);
	//				context.setIgnoreAuthorization(true);
					
					InputStream is = new URL(zipurl).openStream();

					importDir = ConfigurationManager.getProperty("org.dspace.app.batchitemimport.work.dir") + File.separator + "batchuploads" + File.separator + context.getCurrentUser().getID() + File.separator + (new GregorianCalendar()).getTimeInMillis();
					File importDirFile = new File(importDir);
					if (!importDirFile.exists()){
						boolean success = importDirFile.mkdirs();
						if (!success) {
							log.info("Cannot create batch import directory!");
							throw new Exception("Cannot create batch import directory!");
						}
					}

					String dataZipPath = importDirFile + File.separator + "data.zip";
					String dataZipDir = importDirFile + File.separator + "data_unzipped" + File.separator;
					
					OutputStream os = new FileOutputStream(dataZipPath);

					byte[] b = new byte[2048];
					int length;

					while ((length = is.read(b)) != -1) {
						os.write(b, 0, length);
					}

					is.close();
					os.close();
					
					
					
					ZipFile zf = new ZipFile(dataZipPath);
                    ZipEntry entry;
                    Enumeration<? extends ZipEntry> entries = zf.entries();
                    while (entries.hasMoreElements())
                    {
                        entry = entries.nextElement();
                        if (entry.isDirectory())
                        {
                            if (!new File(dataZipDir + entry.getName()).mkdir())
                            {
                                log.error("Unable to create contents directory");
                            }
                        }
                        else
                        {
                            System.out.println("Extracting file: " + entry.getName());
                            int index = entry.getName().lastIndexOf('/');
                            if (index == -1)
                            {
                                // Was it created on Windows instead?
                                index = entry.getName().lastIndexOf('\\');
                            }
                            if (index > 0)
                            {
                                File dir = new File(dataZipDir + entry.getName().substring(0, index));
                                if (!dir.mkdirs())
                                {
                                    log.error("Unable to create directory");
                                }
                            }
                            byte[] buffer = new byte[1024];
                            int len;
                            InputStream in = zf.getInputStream(entry);
                            BufferedOutputStream out = new BufferedOutputStream(
                                new FileOutputStream(dataZipDir + entry.getName()));
                            while((len = in.read(buffer)) >= 0)
                            {
                                out.write(buffer, 0, len);
                            }
                            in.close();
                            out.close();
                        }
                    }
                    zf.close();
                    
					
					String sourcePath = dataZipDir;
					String mapFilePath = importDirFile + File.separator + "mapfile";
					
					
					ItemImport myloader = new ItemImport();
					
					Collection[] finalCollections = null;
					if (theOwningCollection != null){
						finalCollections = new Collection[otherCollections.length + 1];
						finalCollections[0] = theOwningCollection;
						for (int i=0; i<otherCollections.length; i++){
							finalCollections[i+1] = otherCollections[i];
						}
					}
					
					myloader.addItems(context, finalCollections, sourcePath, mapFilePath, template);
					
					// email message letting user know the file is ready for
                    // download
                    emailSuccessMessage(context, eperson, mapFilePath);
                    
					context.complete();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					// abort all operations
	                if (mapOut != null)
	                {
	                    mapOut.close();
	                }

	                mapOut = null;
	                
					//Delete file
					if (importDir != null){
						//FileDeleteStrategy.FORCE.delete(new File(importDir));
					}
					
					try
                    {
                        emailErrorMessage(eperson, e.getMessage());
                        throw new Exception(e.getMessage());
                    }
                    catch (Exception e2)
                    {
                        // wont throw here
                    }
				}
				
				finally
                {
                    // close the mapfile writer
                    if (mapOut != null)
                    {
                        mapOut.close();
                    }

                    // Make sure the database connection gets closed in all conditions.
                	try {
						context.complete();
					} catch (SQLException sqle) {
						context.abort();
					}
                }
/*			}

		};

		go.isDaemon();
		go.start();*/
	}
