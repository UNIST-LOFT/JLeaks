	protected ContentCache
	loadRelatedContent()
	{
		boolean	fire_event = false;

		try{
			synchronized( rcm_lock ){

				last_config_access = SystemTime.getMonotonousTime();

				ContentCache cc = content_cache==null?null:content_cache.get();

				if ( cc == null ){

					if ( TRACE ){
						System.out.println( "rcm: load new" );
					}

					fire_event = true;

					cc = new ContentCache();

					content_cache = new WeakReference<>(cc);

					try{
						int	new_total_unread = 0;

						if ( FileUtil.resilientConfigFileExists( CONFIG_FILE )){

							Map map = FileUtil.readResilientConfigFile( CONFIG_FILE );

							Map<String,DownloadInfo>						related_content			= cc.related_content;
							ByteArrayHashMapEx<ArrayList<DownloadInfo>>		related_content_map		= cc.related_content_map;

							Map<String,String>	rcm_map;

							byte[]	data = (byte[])map.get( "d" );

							if ( data != null ){

								try{
									map = BDecoder.decode(new BufferedInputStream( new GZIPInputStream( new ByteArrayInputStream( CryptoManagerFactory.getSingleton().deobfuscate( data )))));

								}catch( Throwable e ){

										// can get here is config's been deleted

									map = new HashMap();
								}
							}

							rcm_map = (Map<String,String>)map.get( "rcm" );

							Object	rc_map_stuff 	= map.get( "rc" );

							if ( rc_map_stuff != null && rcm_map != null ){

								Map<Integer,DownloadInfo> id_map = new HashMap<>();

								if ( rc_map_stuff instanceof Map ){

										// migration from when it was a Map with non-ascii key issues

									Map<String,Map<String,Object>>	rc_map 	= (Map<String,Map<String,Object>>)rc_map_stuff;

									for ( Map.Entry<String,Map<String,Object>> entry: rc_map.entrySet()){

										try{

											String	key = entry.getKey();

											Map<String,Object>	info_map = entry.getValue();

											DownloadInfo info = deserialiseDI( info_map, cc );

											if ( info.isUnread()){

												new_total_unread++;
											}

											related_content.put( key, info );

											int	id = ((Long)info_map.get( "_i" )).intValue();

											id_map.put( id, info );

										}catch( Throwable e ){

											Debug.out( e );
										}
									}
								}else{

									List<Map<String,Object>>	rc_map_list 	= (List<Map<String,Object>>)rc_map_stuff;

									for ( Map<String,Object> info_map: rc_map_list ){

										try{

											String	key = new String((byte[])info_map.get( "_k" ), "UTF-8" );

											DownloadInfo info = deserialiseDI( info_map, cc );

											if ( info.isUnread()){

												new_total_unread++;
											}

											related_content.put( key, info );

											int	id = ((Long)info_map.get( "_i" )).intValue();

											id_map.put( id, info );

										}catch( Throwable e ){

											Debug.out( e );
										}
									}
								}

								if ( rcm_map.size() != 0 && id_map.size() != 0 ){

									for ( String key: rcm_map.keySet()){

										try{
											byte[]	hash = Base32.decode( key );

											int[]	ids = MapUtils.importIntArray( rcm_map, key );

											if ( ids == null || ids.length == 0 ){

												// Debug.out( "Inconsistent - no ids" );

											}else{

												ArrayList<DownloadInfo>	di_list = new ArrayList<>(ids.length);

												for ( int id: ids ){

													DownloadInfo di = id_map.get( id );

													if ( di == null ){

														// Debug.out( "Inconsistent: id " + id + " missing" );

													}else{

															// we don't currently remember all originators, just one that works

														di.setRelatedToHash( hash );

														di_list.add( di );
													}
												}

												if ( di_list.size() > 0 ){

													related_content_map.put( hash, di_list );
												}
											}
										}catch( Throwable e ){

											Debug.out( e );
										}
									}
								}

								Iterator<DownloadInfo> it = related_content.values().iterator();

								while( it.hasNext()){

									DownloadInfo di = it.next();

									if ( di.getRelatedToHash() == null ){

										// Debug.out( "Inconsistent: info not referenced" );

										if ( di.isUnread()){

											new_total_unread--;
										}

										it.remove();
									}
								}

								popuplateSecondaryLookups( cc );
							}
						}

						if ( total_unread.get() != new_total_unread ){

							// Debug.out( "total_unread - inconsistent (" + total_unread + "/" + new_total_unread + ")" );

							total_unread.set( new_total_unread );

							COConfigurationManager.setParameter( CONFIG_TOTAL_UNREAD, new_total_unread );
						}
					}catch( Throwable e ){

						Debug.out( e );
					}

					enforceMaxResults( cc, false );
				}

				content_cache_ref = cc;

				return( cc );
			}
		}finally{

			if ( fire_event ){

				contentChanged( false );
			}
		}
	}

	protected void
	saveRelatedContent(
		int	tick_count )
	{
		synchronized( rcm_lock ){

			COConfigurationManager.setParameter( CONFIG_TOTAL_UNREAD, total_unread.get());

			long	now = SystemTime.getMonotonousTime();

			ContentCache cc = content_cache==null?null:content_cache.get();

			if ( !content_dirty ){

				if ( cc != null  ){

					if ( now - last_config_access > CONFIG_DISCARD_MILLIS ){

						if ( content_cache_ref != null ){

							content_discard_ticks = 0;
						}

						if ( TRACE ){
							System.out.println( "rcm: discard: tick count=" + content_discard_ticks++ );
						}

						content_cache_ref	= null;
					}
				}else{

					if ( TRACE ){
						System.out.println( "rcm: discarded" );
					}
				}

				return;
			}

			if ( tick_count % CONFIG_SAVE_TICKS != 0 ){

				return;
			}

			last_config_access = now;

			content_dirty	= false;

			if ( cc == null ){

				// Debug.out( "RCM: cache inconsistent" );

			}else{

				if ( persist ){

					if ( TRACE ){
						System.out.println( "rcm: save" );
					}

					Map<String,DownloadInfo>						related_content			= cc.related_content;
					ByteArrayHashMapEx<ArrayList<DownloadInfo>>		related_content_map		= cc.related_content_map;

					if ( related_content.size() == 0 ){

						FileUtil.deleteResilientConfigFile( CONFIG_FILE );

					}else{

						Map<String,Object>	map = new HashMap<>();

						Set<Map.Entry<String,DownloadInfo>> rcs = related_content.entrySet();

						List<Map<String,Object>> rc_map_list = new ArrayList<>(rcs.size());

						map.put( "rc", rc_map_list );

						int		id = 0;

						Map<DownloadInfo,Integer>	info_map = new HashMap<>();

						for ( Map.Entry<String,DownloadInfo> entry: rcs ){

							DownloadInfo	info = entry.getValue();

							Map<String,Object> di_map = serialiseDI( info, cc );

							if ( di_map != null ){

								info_map.put( info, id );

								di_map.put( "_i", new Long( id ));
								di_map.put( "_k", entry.getKey());

								rc_map_list.add( di_map );

								id++;
							}
						}

						Map<String,Object> rcm_map = new HashMap<>();

						map.put( "rcm", rcm_map );

						for ( byte[] hash: related_content_map.keys()){

							List<DownloadInfo> dis = related_content_map.get( hash );

							int[] ids = new int[dis.size()];

							int	pos = 0;

							for ( DownloadInfo di: dis ){

								Integer	index = info_map.get( di );

								if ( index == null ){

									// Debug.out( "inconsistent: info missing for " + di );

									break;

								}else{

									ids[pos++] = index;
								}
							}

							if ( pos == ids.length ){

								MapUtils.exportIntArrayAsByteArray( rcm_map, Base32.encode( hash), ids );
							}
						}

						if ( true ){

							ByteArrayOutputStream baos = new ByteArrayOutputStream( 100*1024 );

							try{
								GZIPOutputStream gos = new GZIPOutputStream( baos );

								gos.write( BEncoder.encode( map ));

								gos.close();

							}catch( Throwable e ){

								Debug.out( e );
							}

							map.clear();

							map.put( "d", CryptoManagerFactory.getSingleton().obfuscate( baos.toByteArray()));
						}

						FileUtil.writeResilientConfigFile( CONFIG_FILE, map );
					}
				}else{

					deleteRelatedContent();
				}

				for ( RelatedContentSearcher searcher: searchers ){

					searcher.updateKeyBloom( cc );
				}
			}
		}
	}
