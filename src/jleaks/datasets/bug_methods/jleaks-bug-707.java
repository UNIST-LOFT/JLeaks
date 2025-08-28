    public void subscribe(Subscriber<? super T> actual) {
        Span span;
        boolean finishOnClose;
        if (spanBuilder != null) {
            span = spanBuilder.start();
            finishOnClose = true;
        } else {
            span = parentSpan;
            finishOnClose = false;
        }
        if (span != null) {
            try (Scope ignored = tracer.scopeManager().activate(span)) {
                publisher.subscribe(new Subscriber<T>() {
                    boolean finished = false;
                    @Override
                    public void onSubscribe(Subscription s) {
                        try (Scope ignored = tracer.scopeManager().activate(span)) {
                            TracingPublisher.this.doOnSubscribe(span);
                            actual.onSubscribe(s);
                        }
                    }

                    @Override
                    public void onNext(T object) {
                        boolean closedAfterNext = isSingle && finishOnClose;
                        Scope ignored = tracer.scopeManager().activate(span);
                        try {
                            if (object instanceof MutableHttpResponse) {
                                MutableHttpResponse response = (MutableHttpResponse) object;
                                Optional<?> body = response.getBody();
                                if (body.isPresent()) {
                                    Object o = body.get();
                                    if (Publishers.isConvertibleToPublisher(o)) {
                                        Class<?> type = o.getClass();
                                        Publisher<?> resultPublisher = Publishers.convertPublisher(o, Publisher.class);
                                        Publisher<?> scopedPublisher = new ScopePropagationPublisher(resultPublisher, tracer, span);
                                        response.body(Publishers.convertPublisher(scopedPublisher, type));
                                    }
                                }

                            }
                            TracingPublisher.this.doOnNext(object, span);
                            actual.onNext(object);
                            if (isSingle) {
                                finished = true;
                                TracingPublisher.this.doOnFinish(span);
                            }
                        } finally {
                            if (closedAfterNext) {
                                span.finish();
                                ignored.close();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Scope ignored = tracer.scopeManager().activate(span);
                        try {
                            TracingPublisher.this.onError(t, span);
                            actual.onError(t);
                            finished = true;
                        } finally {
                            if (finishOnClose) {
                                span.finish();
                                ignored.close();
                            }
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (!finished) {
                            Scope ignored = tracer.scopeManager().activate(span);
                            try {
                                actual.onComplete();
                                TracingPublisher.this.doOnFinish(span);
                            } finally {
                                if (finishOnClose) {
                                    span.finish();
                                    ignored.close();
                                }
                            }

                        } else {
                            actual.onComplete();
                        }
                    }
                });
            }
        } else {
            publisher.subscribe(actual);
        }
    }
