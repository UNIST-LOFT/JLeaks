public void removeTaskLogFile(Integer processInstanceId)
{
    LogClientService logClient = null;
    try {
        logClient = new LogClientService();
        List<TaskInstance> taskInstanceList = findValidTaskListByProcessId(processInstanceId);
        if (CollectionUtils.isEmpty(taskInstanceList)) {
            return;
        }
        for (TaskInstance taskInstance : taskInstanceList) {
            String taskLogPath = taskInstance.getLogPath();
            if (StringUtils.isEmpty(taskInstance.getHost())) {
                continue;
            }
            int port = Constants.RPC_PORT;
            String ip = "";
            try {
                ip = Host.of(taskInstance.getHost()).getIp();
            } catch (Exception e) {
                // compatible old version
                ip = taskInstance.getHost();
            }
            // remove task log from loggerserver
            logClient.removeTaskLog(ip, port, taskLogPath);
        }
    } finally {
        if (logClient != null) {
            logClient.close();
        }
    }
}