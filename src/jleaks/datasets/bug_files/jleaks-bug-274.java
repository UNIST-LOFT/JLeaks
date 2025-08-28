/*
 * Copyright 2012-2013 Continuuity,Inc. All Rights Reserved.
 */
package com.continuuity.metrics.query;

import com.continuuity.api.data.StatusCode;
import com.continuuity.common.conf.Constants;
import com.continuuity.common.http.core.AbstractHttpHandler;
import com.continuuity.common.http.core.InternalHttpResponse;
import com.continuuity.common.utils.ImmutablePair;
import com.continuuity.data2.OperationException;
import com.continuuity.passport.PassportConstants;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

import java.io.InputStreamReader;
import java.net.URI;

/**
 * Base metrics handler that can validate metrics path for existance of elements like streams, datasets, and programs.
 */
public abstract class BaseMetricsHandler extends AbstractHttpHandler {

  protected MetricsRequest parseAndValidate(HttpRequest request, URI requestURI)
    throws MetricsPathException, OperationException {
    ImmutablePair<MetricsRequest, MetricsRequestContext> pair = MetricsRequestParser.parseRequestAndContext(requestURI);
    validatePathElements(request, pair.getSecond());
    return pair.getFirst();
  }

  /**
   * Checks whether the elements (datasets, streams, and programs) in the context exist, throwing
   * a {@link MetricsPathException} with relevant error message if there is an element that does not exist.
   *
   * @param metricsRequestContext context containing elements whose existance we need to check.
   * @throws com.continuuity.data2.OperationException
   * @throws MetricsPathException
   */
  protected void validatePathElements(HttpRequest request, MetricsRequestContext metricsRequestContext)
    throws OperationException, MetricsPathException {
    String apiKey = request.getHeader(PassportConstants.CONTINUUITY_API_KEY_HEADER);
    // check for existance of elements in the path
    String dataName = metricsRequestContext.getTag();
    if (dataName != null) {
      if (metricsRequestContext.getTagType().equals("stream")) {
        if (!streamExists(dataName, apiKey)) {
          throw new MetricsPathException("stream " + dataName + " not found");
        }
      } else if (metricsRequestContext.getTagType().equals("dataset")) {
        if (!datasetExists(dataName, apiKey)) {
          throw new MetricsPathException("dataset " + dataName + " not found");
        }
      }
    }
    String appId = metricsRequestContext.getAppId();
    if (appId != null) {
      MetricsRequestParser.ProgramType programType = metricsRequestContext.getProgramType();
      String programId = metricsRequestContext.getProgramId();
      String componentId = metricsRequestContext.getComponentId();

      // if there was a flowlet in the context
      if (componentId != null && programType == MetricsRequestParser.ProgramType.FLOWS) {
        if (!flowletExists(appId, programId, componentId, apiKey)) {
          String msg = String.format("application %s with flow %s and flowlet %s not found.",
                                     appId, programId, componentId);
          throw new MetricsPathException(msg);
        }
      } else if (!programExists(programType, appId, programId, apiKey)) {
        String programMsg = (programId == null || programType == null) ? "" :
          " with " + programType.name().toLowerCase() + " " + programId;
        String msg = "application " + appId + programMsg + " not found.";
        throw new MetricsPathException(msg);
      }
    }
  }

  /**
   * Check if the given stream exists.
   *
   * @param streamName name of the stream to check for.
   * @param apiKey api key required for authentication.
   * @return true if the stream exists, false if it does not.
   * @throws OperationException if there was some problem looking up the stream.
   */
  private boolean streamExists(String streamName, String apiKey) throws OperationException {
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, "/streams/" + streamName);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
      return true;
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
      return false;
    } else {
      String msg = String.format("got a %d while checking if stream %s exists", response.getStatusCode(), streamName);
      throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
  }

  /**
   * Check if the given dataset exists.
   *
   * @param datasetName name of the dataset to check for.
   * @param apiKey api key required for authentication.
   * @return true if the dataset exists, false if it does not.
   * @throws OperationException if there was some problem looking up the dataset.
   */
  private boolean datasetExists(String datasetName, String apiKey) throws OperationException {
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, "/datasets/" + datasetName);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
      return true;
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
      return false;
    } else {
      String msg = String.format("got a %d while checking if dataset %s exists", response.getStatusCode(), datasetName);
      throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
  }

  /**
   * Check if the app, flow, and flowlet exists.
   *
   * @param appId name of the application.
   * @param flowId name of the flow.
   * @param flowletId name of the flowlet.
   * @param apiKey api key required for authentication.
   * @return true if the app, flow, and flowlet exist, false if not.
   * @throws OperationException if there was some problem looking up the flowlet.
   */
  // TODO: add an app-fabric thrift endpoint and a corresponding gateway endpoint for getting a flowlet spec
  private boolean flowletExists(String appId, String flowId, String flowletId, String apiKey)
    throws OperationException {
    String path = String.format("/apps/%s/flows/%s", appId, flowId);
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, path);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
      JsonObject flowSpec = new Gson().fromJson(new InputStreamReader(response.getInputStream()), JsonObject.class);
      if (flowSpec != null && flowSpec.has("flowlets")) {
        JsonObject flowlets = flowSpec.getAsJsonObject("flowlets");
        return flowlets.has(flowletId);
      }
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
      return false;
    } else {
      String msg = String.format("got a %d while checking if flowlet %s from flow %s and app %s exists",
                                 response.getStatusCode(), flowletId, flowId, appId);
      throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
    return false;
  }

  /**
   * Check if the given type of program exists.
   *
   * @param type type of program, expected to be a flow, mapreduce or procedure.  If null, existence of just the
   *             application will be checked.
   * @param appId name of the application.
   * @param programId name of the program.  If null, existence of just the application will be checked.
   * @param apiKey api key required for authentication.
   * @return true if the program exists, false if not.
   * @throws OperationException
   */
  private boolean programExists(MetricsRequestParser.ProgramType type, String appId, String programId, String apiKey)
    throws OperationException {
    Preconditions.checkNotNull(appId, "must specify an app name to check existance for");

    String path;
    if (type == null || programId == null) {
      path = String.format("/apps/%s", appId);
    } else {
      path = String.format("/apps/%s/%s/%s", appId, type.name().toLowerCase(), programId);
    }
    HttpRequest request = createRequest(HttpMethod.GET, apiKey, path);
    InternalHttpResponse response = sendInternalRequest(request);
    // OK means it exists, NOT_FOUND means it doesn't, and anything else means there was some problem.
    if (response.getStatusCode() == HttpResponseStatus.OK.getCode()) {
      return true;
    } else if (response.getStatusCode() == HttpResponseStatus.NOT_FOUND.getCode()) {
      return false;
    } else {
      String programMsg = (programId == null || type == null) ? "" :
        " with " + type.name().toLowerCase() + " " + programId;
      String msg = "application " + appId + programMsg + " not found.";
      throw new OperationException(StatusCode.INTERNAL_ERROR, msg);
    }
  }

  private HttpRequest createRequest(HttpMethod method, String apiKey, String path) {
    String uri = Constants.Gateway.GATEWAY_VERSION + path;
    HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, method, uri);
    if (apiKey != null) {
      request.setHeader(PassportConstants.CONTINUUITY_API_KEY_HEADER, apiKey);
    }
    return request;
  }
}
