public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext, Throwable t) 
{
    Object wrapperObj = requestContext.getProperty(SpanWrapper.PROPERTY_NAME);
    if (!(wrapperObj instanceof SpanWrapper)) {
        return;
    }
    SpanWrapper wrapper = (SpanWrapper) wrapperObj;
    Tags.HTTP_STATUS.set(wrapper.get(), responseContext.getStatus());
    if (t != null) {
        FilterUtil.addExceptionLogs(wrapper.get(), t);
    }
}