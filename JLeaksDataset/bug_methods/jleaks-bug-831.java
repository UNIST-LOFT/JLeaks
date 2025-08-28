  private ObservableOnSubscribe<Object> receiveLoop() {
    return emitter -> {
      Message message;
      try {
        while ( ( message = consumer.receive( receiverTimeout ) ) != null ) {
          streamStep.logDebug( message.toString() );
          emitter.onNext( ofNullable( message.getBody( Object.class ) ) ); //wrap message - RX doesn't allow nulls
        }
      } catch ( JMSRuntimeException jmsException ) {
        emitter.onError( jmsException );
      }
      if ( !closed.get() ) {
        streamStep.logBasic( getString( PKG, "JmsStreamSource.HitReceiveTimeout" ) );
      }
      emitter.onComplete();
    };
  }
