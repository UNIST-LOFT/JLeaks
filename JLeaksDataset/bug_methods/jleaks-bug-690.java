                public void onNext(DeframedMessage unframed) {
                    final ByteBuf buf = unframed.buf();
                    // Compression not supported.
                    assert buf != null;
                    responseFuture.complete(HttpResponse.of(msg.headers(), HttpData.wrap(buf), msg.trailers()));
                }
