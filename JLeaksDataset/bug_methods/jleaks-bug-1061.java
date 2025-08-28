  public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
    IMethod target = base.getCalleeTarget(caller, site, receiver);
    if (target != null && target.getReference().equals(loadFileFunRef)) {

      Set<String> names = new HashSet<String>();
      SSAInstruction call = caller.getIR().getInstructions()[caller.getIR().getCallInstructionIndices(site).intIterator().next()];
      if (call.getNumberOfUses() > 1) {
        LocalPointerKey fileNameV = new LocalPointerKey(caller, call.getUse(1));
        OrdinalSet<InstanceKey> ptrs = builder.getPointerAnalysis().getPointsToSet(fileNameV);
        for(InstanceKey k : ptrs) {
          if (k instanceof ConstantKey) {
            Object v = ((ConstantKey)k).getValue();
            if (v instanceof String) {
              names.add((String)v);
            }
          }
        }

        if (names.size() == 1) {
          String str = names.iterator().next();
          try {
            JavaScriptLoader cl = (JavaScriptLoader) builder.getClassHierarchy().getLoader(JavaScriptTypes.jsLoader);
            URL url = new URL(builder.getBaseURL(), str);
            if(!loadedFiles.contains(url)) {
              // try to open the input stream for the URL.  if it fails, we'll get an IOException and fall through to default case
              InputStream inputStream = url.openConnection().getInputStream();
              inputStream.close();
              JSCallGraphUtil.loadAdditionalFile(builder.getClassHierarchy() , cl, url);
              loadedFiles.add(url);
              IClass script = builder.getClassHierarchy().lookupClass(TypeReference.findOrCreate(cl.getReference(), "L" + url.getFile()));
              return script.getMethod(JavaScriptMethods.fnSelector);
            }
          } catch (MalformedURLException e1) {
            // do nothing, fall through and return 'target'
          } catch (IOException e) {
            // do nothing, fall through and return 'target'
          } catch (RuntimeException e) {
            // do nothing, fall through and return 'target'
          }
        }
      }
    }

    return target;
  }
