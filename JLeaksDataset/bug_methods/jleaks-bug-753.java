              public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                  exporterMetrics.addSuccess(numItems);
                  result.succeed();
                  return;
                }

                exporterMetrics.addFailed(numItems);
                int code = response.code();

                String status = extractErrorStatus(response);

                logger.log(
                    Level.WARNING,
                    "Failed to export "
                        + type
                        + "s. Server responded with HTTP status code "
                        + code
                        + ". Error message: "
                        + status);
                result.fail();
              }