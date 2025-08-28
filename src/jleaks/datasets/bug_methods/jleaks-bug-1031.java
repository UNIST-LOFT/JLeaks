				public void actionPerformed(ActionEvent e) {
					JPanel tPanel = new JPanel(new BorderLayout());
					final File conf = new File(configuration.getProfilePath());
					final JTextArea textArea = new JTextArea();
					textArea.setFont(new Font("Courier", Font.PLAIN, 12));
					JScrollPane scrollPane = new JScrollPane(textArea);
					scrollPane.setPreferredSize(new java.awt.Dimension(900, 450));

					try {
						try (FileInputStream fis = new FileInputStream(conf)) {
							BufferedReader in = new BufferedReader(new InputStreamReader(fis));
							String line;
							StringBuilder sb = new StringBuilder();

							while ((line = in.readLine()) != null) {
								sb.append(line);
								sb.append("\n");
							}

							in.close();
							textArea.setText(sb.toString());
						}
					} catch (Exception e1) {
						return;
					}

					tPanel.add(scrollPane, BorderLayout.NORTH);
					Object[] options = {Messages.getString("LooksFrame.9"), Messages.getString("NetworkTab.45")};

					if (JOptionPane.showOptionDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
						tPanel, Messages.getString("NetworkTab.51"),
						JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, options, null) == JOptionPane.OK_OPTION) {
						String text = textArea.getText();

						try {
							try (FileOutputStream fos = new FileOutputStream(conf)) {
								fos.write(text.getBytes());
								fos.flush();
							}
							configuration.reload();
						} catch (Exception e1) {
							JOptionPane.showMessageDialog((JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
								Messages.getString("NetworkTab.52") + e1.toString());
						}
					}
				}
