	public void parse(InputFile inputFile, Format ext, int type, boolean thumbOnly, boolean resume, RendererConfiguration renderer) {
		int i = 0;

		while (isParsing()) {
			if (i == 5) {
				mediaparsed = true;
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }

			i++;
		}

		if (isMediaparsed()) {
			return;
		}

		if (inputFile != null) {
			File file = inputFile.getFile();
			if (file != null) {
				size = file.length();
			} else {
				size = inputFile.getSize();
			}

			ProcessWrapperImpl pw = null;
			boolean ffmpeg_parsing = true;

			if (type == Format.AUDIO || ext instanceof AudioAsVideo) {
				ffmpeg_parsing = false;
				DLNAMediaAudio audio = new DLNAMediaAudio();

				if (file != null) {
					try {
						AudioFile af = AudioFileIO.read(file);
						AudioHeader ah = af.getAudioHeader();

						if (ah != null && !thumbOnly) {
							int length = ah.getTrackLength();
							int rate = ah.getSampleRateAsNumber();

							if (ah.getEncodingType().toLowerCase().contains("flac 24")) {
								audio.setBitsperSample(24);
							}

							audio.setSampleFrequency("" + rate);
							durationSec = (double) length;
							bitrate = (int) ah.getBitRateAsNumber();
							audio.getAudioProperties().setNumberOfChannels(2);

							if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("mono")) {
								audio.getAudioProperties().setNumberOfChannels(1);
							} else if (ah.getChannels() != null && ah.getChannels().toLowerCase().contains("stereo")) {
								audio.getAudioProperties().setNumberOfChannels(2);
							} else if (ah.getChannels() != null) {
								audio.getAudioProperties().setNumberOfChannels(Integer.parseInt(ah.getChannels()));
							}

							audio.setCodecA(ah.getEncodingType().toLowerCase());

							if (audio.getCodecA().contains("(windows media")) {
								audio.setCodecA(audio.getCodecA().substring(0, audio.getCodecA().indexOf("(windows media")).trim());
							}
						}

						Tag t = af.getTag();

						if (t != null) {
							if (t.getArtworkList().size() > 0) {
								thumb = t.getArtworkList().get(0).getBinaryData();
							} else {
								if (configuration.getAudioThumbnailMethod() > 0) {
									thumb =
										CoverUtil.get().getThumbnailFromArtistAlbum(
											configuration.getAudioThumbnailMethod() == 1 ?
												CoverUtil.AUDIO_AMAZON :
												CoverUtil.AUDIO_DISCOGS,
											audio.getArtist(), audio.getAlbum()
										);
								}
							}

							if (!thumbOnly) {
								audio.setAlbum(t.getFirst(FieldKey.ALBUM));
								audio.setArtist(t.getFirst(FieldKey.ARTIST));
								audio.setSongname(t.getFirst(FieldKey.TITLE));
								String y = t.getFirst(FieldKey.YEAR);

								try {
									if (y.length() > 4) {
										y = y.substring(0, 4);
									}
									audio.setYear(Integer.parseInt(((y != null && y.length() > 0) ? y : "0")));
									y = t.getFirst(FieldKey.TRACK);
									audio.setTrack(Integer.parseInt(((y != null && y.length() > 0) ? y : "1")));
									audio.setGenre(t.getFirst(FieldKey.GENRE));
								} catch (NumberFormatException | KeyNotFoundException e) {
									LOGGER.debug("Error parsing unimportant metadata: " + e.getMessage());
								}
							}

							int thumbnailWidth = renderer.isSquareAudioThumbnails() ? renderer.getThumbnailHeight() : renderer.getThumbnailWidth();
							int thumbnailHeight = renderer.getThumbnailHeight();

							// Make sure the image fits in the renderer's bounds
							boolean isWatchedThumbnail = configuration.getFullyPlayedAction() == FullyPlayedAction.MARK && MediaMonitor.isFullyPlayed(file.getAbsolutePath());
							thumb = UMSUtils.scaleImage(thumb, thumbnailWidth, thumbnailHeight, isWatchedThumbnail);

							BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumb));
							if (image != null && isWatchedThumbnail) {
								Graphics2D g = image.createGraphics();
								g.setPaint(THUMBNAIL_OVERLAY_BACKGROUND_COLOR);
								g.fillRect(0, 0, thumbnailWidth, thumbnailHeight);
								g.setColor(THUMBNAIL_OVERLAY_TEXT_COLOR);

								/**
								 * TODO: Include and use a custom font
								 */
								if (thumbnailFontSizeAudio > 0) {
									g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeAudio));
									g.drawString(THUMBNAIL_TEXT_AUDIO, thumbnailTextHorizontalPositionAudio, thumbnailTextVerticalPositionAudio);
								} else {
									thumbnailFontSizeAudio = thumbnailWidth / 6;
									g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeAudio));
									FontMetrics fm = g.getFontMetrics();
									Rectangle2D textsize = fm.getStringBounds(THUMBNAIL_TEXT_AUDIO, g);
									int textWidth = (int) textsize.getWidth();
									thumbnailTextHorizontalPositionAudio = (thumbnailWidth - textWidth) / 2;

									// Use a smaller font size if there isn't enough room
									if (textWidth > thumbnailWidth) {
										for (int divider = 7; divider < 99; divider++) {
											thumbnailFontSizeAudio = thumbnailWidth / divider;
											g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeAudio));
											fm = g.getFontMetrics();
											textsize = fm.getStringBounds(THUMBNAIL_TEXT_AUDIO, g);
											textWidth = (int) textsize.getWidth();
											if (textWidth <= (thumbnailWidth * 0.9)) {
												thumbnailTextHorizontalPositionAudio = (thumbnailWidth - textWidth) / 2;
												break;
											}
										}
									}

									thumbnailTextVerticalPositionAudio = (int) (thumbnailHeight - textsize.getHeight()) / 2 + fm.getAscent();
									g.drawString(THUMBNAIL_TEXT_AUDIO, thumbnailTextHorizontalPositionAudio, thumbnailTextVerticalPositionAudio);
								}
							}
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							ImageIO.write(image, "jpeg", out);
							thumb = out.toByteArray();
						}
					} catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException | NumberFormatException | KeyNotFoundException e) {
						LOGGER.debug("Error parsing audio file: {} - {}", e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "");
						ffmpeg_parsing = false;
					}

					if (audio.getSongname() != null && audio.getSongname().length() > 0) {
						if (renderer != null && renderer.isPrependTrackNumbers() && audio.getTrack() > 0) {
							audio.setSongname(audio.getTrack() + ": " + audio.getSongname());
						}
					} else {
						audio.setSongname(file.getName());
					}

					if (!ffmpeg_parsing) {
						audioTracks.add(audio);
					}
				}
			}

			if (type == Format.IMAGE && file != null) {
				try {
					ffmpeg_parsing = false;
					ImageInfo info = Sanselan.getImageInfo(file);
					width = info.getWidth();
					height = info.getHeight();
					bitsPerPixel = info.getBitsPerPixel();
					String formatName = info.getFormatName();

					if (formatName.startsWith("JPEG")) {
						codecV = "jpg";
						IImageMetadata meta = Sanselan.getMetadata(file);

						if (meta != null && meta instanceof JpegImageMetadata) {
							JpegImageMetadata jpegmeta = (JpegImageMetadata) meta;
							TiffField tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_MODEL);

							if (tf != null) {
								model = tf.getStringValue().trim();
							}

							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_EXPOSURE_TIME);
							if (tf != null) {
								exposure = (int) (1000 * tf.getDoubleValue());
							}

							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ORIENTATION);
							if (tf != null) {
								orientation = tf.getIntValue();
							}

							tf = jpegmeta.findEXIFValue(TiffConstants.EXIF_TAG_ISO);
							if (tf != null) {
								// Galaxy Nexus jpg pictures may contain multiple values, take the first
								int[] isoValues = tf.getIntArrayValue();
								iso = isoValues[0];
							}
						}
					} else if (formatName.startsWith("PNG")) {
						codecV = "png";
					} else if (formatName.startsWith("GIF")) {
						codecV = "gif";
					} else if (formatName.startsWith("TIF")) {
						codecV = "tiff";
					}

					container = codecV;
					imageCount++;
				} catch (ImageReadException | IOException e) {
					LOGGER.info("Error parsing image ({}) with Sanselan, switching to FFmpeg.", file.getAbsolutePath());
				}
				if (configuration.getImageThumbnailsEnabled() && gen_thumb) {
					LOGGER.trace("Creating (temporary) thumbnail: {}", file.getName());

					// Create the thumbnail image using the Thumbnailator library
					try {
						int thumbnailWidth = renderer.isSquareImageThumbnails() ? renderer.getThumbnailHeight() : renderer.getThumbnailWidth();
						int thumbnailHeight = renderer.getThumbnailHeight();

						// Make sure the image fits in the renderer's bounds
						boolean isWatchedThumbnail = configuration.getFullyPlayedAction() == FullyPlayedAction.MARK && MediaMonitor.isFullyPlayed(file.getAbsolutePath());
						thumb = UMSUtils.scaleImage(Files.readAllBytes(file.toPath()), thumbnailWidth, thumbnailHeight, isWatchedThumbnail);

						BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumb));
						if (image != null && isWatchedThumbnail) {
							Graphics2D g = image.createGraphics();
							g.setPaint(THUMBNAIL_OVERLAY_BACKGROUND_COLOR);
							g.fillRect(0, 0, thumbnailWidth, thumbnailHeight);
							g.setColor(THUMBNAIL_OVERLAY_TEXT_COLOR);

							/**
							 * TODO: Include and use a custom font
							 */
							if (thumbnailFontSizeImage > 0) {
								g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeImage));
								g.drawString(THUMBNAIL_TEXT_IMAGE, thumbnailTextHorizontalPositionImage, thumbnailTextVerticalPositionImage);
							} else {
								thumbnailFontSizeImage = thumbnailWidth / 6;
								g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeImage));
								FontMetrics fm = g.getFontMetrics();
								Rectangle2D textsize = fm.getStringBounds(THUMBNAIL_TEXT_IMAGE, g);
								int textWidth = (int) textsize.getWidth();
								thumbnailTextHorizontalPositionImage = (thumbnailWidth - textWidth) / 2;

								// Use a smaller font size if there isn't enough room
								if (textWidth > thumbnailWidth) {
									for (int divider = 7; divider < 99; divider++) {
										thumbnailFontSizeImage = thumbnailWidth / divider;
										g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeImage));
										fm = g.getFontMetrics();
										textsize = fm.getStringBounds(THUMBNAIL_TEXT_IMAGE, g);
										textWidth = (int) textsize.getWidth();
										if (textWidth <= (thumbnailWidth * 0.9)) {
											thumbnailTextHorizontalPositionImage = (thumbnailWidth - textWidth) / 2;
											break;
										}
									}
								}

								thumbnailTextVerticalPositionImage = (int) (thumbnailHeight - textsize.getHeight()) / 2 + fm.getAscent();
								g.drawString(THUMBNAIL_TEXT_IMAGE, thumbnailTextHorizontalPositionImage, thumbnailTextVerticalPositionImage);
							}
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							ImageIO.write(image, "jpeg", out);
							thumb = out.toByteArray();
						}
					} catch (IOException | IllegalArgumentException | IllegalStateException e) {
						LOGGER.debug("Error generating thumbnail for: " + file.getName());
						LOGGER.debug("The full error was: " + e);
					}
				}
			}

			if (ffmpeg_parsing) {
				if (!thumbOnly || !configuration.isUseMplayerForVideoThumbs()) {
					pw = getFFmpegThumbnail(inputFile, resume, renderer);
				}

				boolean dvrms = false;
				String input = "-";

				if (file != null) {
					input = ProcessUtil.getShortFileNameIfWideChars(file.getAbsolutePath());
					dvrms = file.getAbsolutePath().toLowerCase().endsWith("dvr-ms");
				}

				if (pw != null && !ffmpeg_failure && !thumbOnly) {
					parseFFmpegInfo(pw.getResults(), input);
				}

				if (
					!thumbOnly &&
					container != null &&
					file != null &&
					container.equals("mpegts") &&
					isH264() &&
					getDurationInSeconds() == 0
				) {
					// Parse the duration
					try {
						int length = MpegUtil.getDurationFromMpeg(file);
						if (length > 0) {
							durationSec = (double) length;
						}
					} catch (IOException e) {
						LOGGER.trace("Error retrieving length: " + e.getMessage());
					}
				}

				if (configuration.isUseMplayerForVideoThumbs() && type == Format.VIDEO && !dvrms) {
					try {
						getMplayerThumbnail(inputFile, resume);
						String frameName = "" + inputFile.hashCode();
						frameName = configuration.getTempFolder() + "/mplayer_thumbs/" + frameName + "00000001/00000001.jpg";
						frameName = frameName.replace(',', '_');
						File jpg = new File(frameName);

						if (jpg.exists()) {
							try (InputStream is = new FileInputStream(jpg)) {
								int sz = is.available();

								if (sz > 0) {
									thumb = new byte[sz];
									is.read(thumb);
								}
							}

							if (!jpg.delete()) {
								jpg.deleteOnExit();
							}

							// Try and retry
							if (!jpg.getParentFile().delete() && !jpg.getParentFile().delete()) {
								LOGGER.debug("Failed to delete \"" + jpg.getParentFile().getAbsolutePath() + "\"");
							}
						}
					} catch (IOException e) {
						LOGGER.debug("Caught exception", e);
					}
				}

				if (type == Format.VIDEO && pw != null && thumb == null) {
					InputStream is;
					try {
						int sz = 0;
						is = pw.getInputStream(0);
						if (is != null) {
							sz = is.available();
							if (sz > 0) {
								thumb = new byte[sz];
								is.read(thumb);
							}
							is.close();
						}

						if (sz > 0) {
							BufferedImage image = ImageIO.read(new ByteArrayInputStream(thumb));
							if (image != null && configuration.getFullyPlayedAction() == FullyPlayedAction.MARK && file != null && MediaMonitor.isFullyPlayed(file.getAbsolutePath())) {
								int thumbnailWidth = renderer.getThumbnailWidth();

								Graphics2D g = image.createGraphics();
								g.setPaint(THUMBNAIL_OVERLAY_BACKGROUND_COLOR);
								g.fillRect(0, 0, thumbnailWidth, thumbnailWidth);
								g.setColor(THUMBNAIL_OVERLAY_TEXT_COLOR);

								/**
								 * TODO: Include and use a custom font
								 */
								if (thumbnailFontSizeVideo > 0) {
									g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeVideo));
									g.drawString(THUMBNAIL_TEXT_VIDEO, thumbnailTextHorizontalPositionVideo, thumbnailTextVerticalPositionVideo);
								} else {
									thumbnailFontSizeVideo = thumbnailWidth / 6;
									g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeVideo));
									FontMetrics fm = g.getFontMetrics();
									Rectangle2D textsize = fm.getStringBounds(THUMBNAIL_TEXT_VIDEO, g);
									int textWidth = (int) textsize.getWidth();
									thumbnailTextHorizontalPositionVideo = (thumbnailWidth - textWidth) / 2;

									// Use a smaller font size if there isn't enough room
									if (textWidth > thumbnailWidth) {
										for (int divider = 7; divider < 99; divider++) {
											thumbnailFontSizeVideo = thumbnailWidth / divider;
											g.setFont(new Font("Arial", Font.PLAIN, thumbnailFontSizeVideo));
											fm = g.getFontMetrics();
											textsize = fm.getStringBounds(THUMBNAIL_TEXT_VIDEO, g);
											textWidth = (int) textsize.getWidth();
											if (textWidth <= (thumbnailWidth * 0.9)) {
												thumbnailTextHorizontalPositionVideo = (thumbnailWidth - textWidth) / 2;
												break;
											}
										}
									}

									thumbnailTextVerticalPositionVideo = (int) (renderer.getThumbnailHeight() - textsize.getHeight()) / 2 + fm.getAscent();
									g.drawString(THUMBNAIL_TEXT_VIDEO, thumbnailTextHorizontalPositionVideo, thumbnailTextVerticalPositionVideo);
								}
							}
							ByteArrayOutputStream out = new ByteArrayOutputStream();
							ImageIO.write(image, "jpeg", out);
							thumb = out.toByteArray();
						}
					} catch (IOException e) {
						LOGGER.debug("Error while decoding thumbnail: " + e.getMessage());
					}
				}
			}

			finalize(type, inputFile);
			mediaparsed = true;
		}
	}
