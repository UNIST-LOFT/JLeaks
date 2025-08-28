    private void cmdExecuteActionPerformed(java.awt.event.ActionEvent evt) {                                               
    	try {
    		if (selectedSource == null){
    			JOptionPane.showMessageDialog(this, "Please select data source first", "Error", JOptionPane.ERROR_MESSAGE);
    		}
    		else {
				OBDAProgessMonitor progMonitor = new OBDAProgessMonitor("Executing query...");
				CountDownLatch latch = new CountDownLatch(1);
				ExecuteSQLQueryAction action = new ExecuteSQLQueryAction(latch);
				progMonitor.addProgressListener(action);
				progMonitor.start();
				action.run();
				latch.await();
				progMonitor.stop();
				ResultSet set = action.getResult();
				if(set != null){
					IncrementalResultSetTableModel model = new IncrementalResultSetTableModel(set);
					tblQueryResult.setModel(model);
				}
    		}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			log.error("Error while executing query.", e);
		}

    }                                             
