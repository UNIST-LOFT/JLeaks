    public static List<InetAddress> filterReachable(Collection<InetAddress> addrs) {
        final int reachTimeout = 2000;

        if (addrs.isEmpty())
            return Collections.emptyList();

        if (addrs.size() == 1) {
            InetAddress addr = F.first(addrs);

            if (reachable(addr, reachTimeout))
                return Collections.singletonList(addr);

            return Collections.emptyList();
        }

        final List<InetAddress> res = new ArrayList<>(addrs.size());

        Collection<Future<?>> futs = new ArrayList<>(addrs.size());

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(10, addrs.size()));

        for (final InetAddress addr : addrs) {
            futs.add(executor.submit(new Runnable() {
                @Override public void run() {
                    if (reachable(addr, reachTimeout)) {
                        synchronized (res) {
                            res.add(addr);
                        }
                    }
                }
            }));
        }

        for (Future<?> fut : futs) {
            try {
                fut.get();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();

                throw new IgniteException("Thread has been interrupted.", e);
            }
            catch (ExecutionException e) {
                throw new IgniteException(e);
            }
        }

        executor.shutdown();

        return res;
    }
