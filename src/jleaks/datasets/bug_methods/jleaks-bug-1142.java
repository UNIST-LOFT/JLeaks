        private void stopExecutors(IgniteLogger log) {
            boolean interrupted = Thread.interrupted();

            try {
                stopExecutors0(log);
            }
            finally {
                if (interrupted)
                    Thread.currentThread().interrupt();
            }
        }

        /**
         * Stops executor services if they has been started.
         *
         * @param log Grid logger.
         */
        private void stopExecutors0(IgniteLogger log) {
            assert log != null;

            U.shutdownNow(getClass(), execSvc, log);

            execSvc = null;

            U.shutdownNow(getClass(), sysExecSvc, log);

            sysExecSvc = null;

            U.shutdownNow(getClass(), qryExecSvc, log);

            qryExecSvc = null;

            U.shutdownNow(getClass(), schemaExecSvc, log);

            schemaExecSvc = null;

            U.shutdownNow(getClass(), stripedExecSvc, log);

            stripedExecSvc = null;

            U.shutdownNow(getClass(), mgmtExecSvc, log);

            mgmtExecSvc = null;

            U.shutdownNow(getClass(), p2pExecSvc, log);

            p2pExecSvc = null;

            U.shutdownNow(getClass(), dataStreamerExecSvc, log);

            dataStreamerExecSvc = null;

            U.shutdownNow(getClass(), igfsExecSvc, log);

            igfsExecSvc = null;

            if (restExecSvc != null)
                U.shutdownNow(getClass(), restExecSvc, log);

            restExecSvc = null;

            U.shutdownNow(getClass(), utilityCacheExecSvc, log);

            utilityCacheExecSvc = null;

            U.shutdownNow(getClass(), affExecSvc, log);

            affExecSvc = null;

            U.shutdownNow(getClass(), idxExecSvc, log);

            idxExecSvc = null;

            U.shutdownNow(getClass(), callbackExecSvc, log);

            callbackExecSvc = null;

            if (!F.isEmpty(customExecSvcs)) {
                for (ThreadPoolExecutor exec : customExecSvcs.values())
                    U.shutdownNow(getClass(), exec, log);

                customExecSvcs = null;
            }
        }
