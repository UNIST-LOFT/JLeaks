public void enhanceThisClass(ApplicationClass applicationClass) throws Exception 
{
    if (isScala(applicationClass)) {
        return;
    }
    CtClass ctClass = makeClass(applicationClass);
    if (!ctClass.subtypeOf(classPool.get(ControllersEnhancer.ControllerSupport.class.getName()))) {
        return;
    }
    boolean needsContinuations = shouldEnhance(ctClass);
    if (!needsContinuations) {
        return;
    }
    // To be able to runtime detect if a class is enhanced for Continuations,
    // we add the interface EnhancedForContinuations to the class
    CtClass enhancedForContinuationsInterface;
    try {
        InputStream in = getClass().getClassLoader().getResourceAsStream("play/classloading/enhancers/EnhancedForContinuations.class");
        try {
            enhancedForContinuationsInterface = classPool.makeClass(in);
        } finally {
            in.close();
        }
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
    ctClass.addInterface(enhancedForContinuationsInterface);
    // Apply continuations
    applicationClass.enhancedByteCode = new AsmClassTransformer().transform(ctClass.toBytecode());
    ctClass.defrost();
    enhancedForContinuationsInterface.defrost();
}