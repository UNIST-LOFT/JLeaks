public void accept(Session session)
                {
                    // if the MessageReceiver is closed - we no-longer need to create the link
                    if (MessageReceiver.this.getIsClosingOrClosed()) {

                        session.close();
                        return;
                    }

                    final Source source = new Source();
                    source.setAddress(receivePath);

                    final Map<Symbol, UnknownDescribedType> filterMap = MessageReceiver.this.settingsProvider.getFilter(MessageReceiver.this.lastReceivedMessage);
                    if (filterMap != null)
                        source.setFilter(filterMap);
                    
                    final Receiver receiver = session.receiver(TrackingUtil.getLinkName(session));
                    receiver.setSource(source);
                    
                    final Target target = new Target();
                    
                    receiver.setTarget(target);
                    // use explicit settlement via dispositions (not pre-settled)
                    receiver.setSenderSettleMode(SenderSettleMode.UNSETTLED);
                    receiver.setReceiverSettleMode(ReceiverSettleMode.SECOND);
                    
                    final Map<Symbol, Object> linkProperties = MessageReceiver.this.settingsProvider.getProperties();
                    if (linkProperties != null)
                        receiver.setProperties(linkProperties);
                    final Symbol[] desiredCapabilities = MessageReceiver.this.settingsProvider.getDesiredCapabilities();
                    if (desiredCapabilities != null)
                        receiver.setDesiredCapabilities(desiredCapabilities);
                    
                    final ReceiveLinkHandler handler = new ReceiveLinkHandler(MessageReceiver.this);
                    BaseHandler.setHandler(receiver, handler);
                    MessageReceiver.this.underlyingFactory.registerForConnectionError(receiver);

                    receiver.open();

                    MessageReceiver.this.receiveLink = receiver;
                }