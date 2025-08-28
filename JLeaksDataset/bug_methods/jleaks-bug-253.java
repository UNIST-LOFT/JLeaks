		protected
		Searcher(
			boolean		_persistent,
			boolean		_async )

			throws DeviceManagerException
		{
			try{
				int	last_port = COConfigurationManager.getIntParameter( "devices.tivo.net.tcp.port", 0 );

				if ( last_port > 0 ){

					try{
						ServerSocket ss = new ServerSocket( last_port );

						ss.setReuseAddress( true );

						ss.close();

					}catch( Throwable e ){

						last_port = 0;
					}
				}

				twc = plugin_interface.getTracker().createWebContext( last_port, Tracker.PR_HTTP );

				tcp_port = twc.getURLs()[0].getPort();

				COConfigurationManager.setParameter( "devices.tivo.net.tcp.port", tcp_port );

				twc.addPageGenerator(
					new TrackerWebPageGenerator()
					{
						@Override
						public boolean
						generate(
							TrackerWebPageRequest 	request,
							TrackerWebPageResponse 	response )

							throws IOException
						{
							String	id = (String)request.getHeaders().get( "tsn" );

							if ( id == null ){

								id = (String)request.getHeaders().get( "tivo_tcd_id" );
							}

							if ( id != null && is_enabled ){

								persistent = true;

								DeviceTivo tivo = foundTiVo( request.getClientAddress2().getAddress(), id, null, null );

								return( tivo.generate( request, response ));
							}

							return( false );
						}
					});

				control_socket  = new DatagramSocket( null );

				control_socket.setReuseAddress( true );

				try{
					control_socket.setSoTimeout( 60*1000 );

				}catch( Throwable e ){
				}

				InetAddress bind = NetworkAdmin.getSingleton().getSingleHomedServiceBindAddress();

				control_socket.bind( new InetSocketAddress( bind, CONTROL_PORT ));

				timer_event =
					SimpleTimer.addPeriodicEvent(
						"Tivo:Beacon",
						60*1000,
						new TimerEventPerformer()
						{
							@Override
							public void
							perform(
								TimerEvent 	event )
							{
								if ( !( manager_destroyed || search_destroyed )){

									sendBeacon();
								}

									// see if time to auto-shutdown searching

								if ( !persistent ){

									synchronized( DeviceTivoManager.this ){

										if ( SystemTime.getMonotonousTime() - start >= LIFE_MILLIS ){

											log( "Terminating search, no devices found" );

											current_search = null;

											destroy();
										}
									}
								}
							}
						});

				final AESemaphore start_sem = new AESemaphore( "TiVo:CtrlListener" );

				new AEThread2( "TiVo:CtrlListener", true )
				{
					@Override
					public void
					run()
					{
						start_sem.release();

						long	successful_accepts 	= 0;
						long	failed_accepts		= 0;

						while( !( manager_destroyed || search_destroyed )){

							try{
								byte[] buf = new byte[8192];

								DatagramPacket packet = new DatagramPacket(buf, buf.length );

								control_socket.receive( packet );

								successful_accepts++;

								failed_accepts	 = 0;

								if ( receiveBeacon( packet.getAddress(), packet.getData(), packet.getLength())){

									persistent = true;
								}

							}catch( SocketTimeoutException e ){

							}catch( Throwable e ){

								if ( control_socket != null && !search_destroyed && !manager_destroyed ){

									failed_accepts++;

									log( "UDP receive on port " + CONTROL_PORT + " failed", e );
								}

								if (( failed_accepts > 100 && successful_accepts == 0 ) || failed_accepts > 1000 ){

									log( "    too many failures, abandoning" );

									break;
								}
							}
						}
					}
				}.start();

				if ( _async ){

					new DelayedEvent(
						"search:delay",
						5000,
						new AERunnable()
						{
							@Override
							public void
							runSupport()
							{
								sendBeacon();
							}
						});
				}else{

					start_sem.reserve( 5000 );

					sendBeacon();
				}

				log( "Initiated device search" );

			}catch( Throwable e ){

				log( "Failed to initialise search", e );

				destroy();

				throw( new DeviceManagerException( "Creation failed",e ));
			}
		}
