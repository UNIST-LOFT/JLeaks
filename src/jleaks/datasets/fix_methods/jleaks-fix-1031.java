
				public void actionPerformed(ActionEvent e) {
					if (PMS.get().installWin32Service()) {
						LOGGER.info(Messages.getString("PMS.41"));
						JOptionPane.showMessageDialog(
							(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
							Messages.getString("NetworkTab.11") +
							Messages.getString("NetworkTab.12"),
							Messages.getString("Dialog.Information"),
							JOptionPane.INFORMATION_MESSAGE
						);
					} else {
						JOptionPane.showMessageDialog(
							(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
							Messages.getString("NetworkTab.14"),
							Messages.getString("Dialog.Error"),
							JOptionPane.ERROR_MESSAGE
						);
					}
				}