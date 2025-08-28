public BpmnModel convertToBpmnModel(InputStreamProvider inputStreamProvider, boolean validateSchema, boolean enableSafeBpmnXml, String encoding) 
{
    BpmnModel model = new BpmnModel();
    model.setStartEventFormTypes(startEventFormTypes);
    model.setUserTaskFormTypes(userTaskFormTypes);
    try {
        Process activeProcess = null;
        List<SubProcess> activeSubProcessList = new ArrayList<SubProcess>();
        while (xtr.hasNext()) {
            try {
                xtr.next();
            } catch (Exception e) {
                LOGGER.debug("Error reading XML document", e);
                throw new XMLException("Error reading XML", e);
            }
            if (xtr.isEndElement() && (ELEMENT_SUBPROCESS.equals(xtr.getLocalName()) || ELEMENT_TRANSACTION.equals(xtr.getLocalName()) || ELEMENT_ADHOC_SUBPROCESS.equals(xtr.getLocalName()))) {
                activeSubProcessList.remove(activeSubProcessList.size() - 1);
            }
            if (!xtr.isStartElement()) {
                continue;
            }
            if (ELEMENT_DEFINITIONS.equals(xtr.getLocalName())) {
                definitionsParser.parse(xtr, model);
            } else if (ELEMENT_RESOURCE.equals(xtr.getLocalName())) {
                resourceParser.parse(xtr, model);
            } else if (ELEMENT_SIGNAL.equals(xtr.getLocalName())) {
                signalParser.parse(xtr, model);
            } else if (ELEMENT_MESSAGE.equals(xtr.getLocalName())) {
                messageParser.parse(xtr, model);
            } else if (ELEMENT_ERROR.equals(xtr.getLocalName())) {
                if (StringUtils.isNotEmpty(xtr.getAttributeValue(null, ATTRIBUTE_ID))) {
                    model.addError(xtr.getAttributeValue(null, ATTRIBUTE_ID), xtr.getAttributeValue(null, ATTRIBUTE_ERROR_CODE));
                }
            } else if (ELEMENT_IMPORT.equals(xtr.getLocalName())) {
                importParser.parse(xtr, model);
            } else if (ELEMENT_ITEM_DEFINITION.equals(xtr.getLocalName())) {
                itemDefinitionParser.parse(xtr, model);
            } else if (ELEMENT_DATA_STORE.equals(xtr.getLocalName())) {
                dataStoreParser.parse(xtr, model);
            } else if (ELEMENT_INTERFACE.equals(xtr.getLocalName())) {
                interfaceParser.parse(xtr, model);
            } else if (ELEMENT_IOSPECIFICATION.equals(xtr.getLocalName())) {
                ioSpecificationParser.parseChildElement(xtr, activeProcess, model);
            } else if (ELEMENT_PARTICIPANT.equals(xtr.getLocalName())) {
                participantParser.parse(xtr, model);
            } else if (ELEMENT_MESSAGE_FLOW.equals(xtr.getLocalName())) {
                messageFlowParser.parse(xtr, model);
            } else if (ELEMENT_PROCESS.equals(xtr.getLocalName())) {
                Process process = processParser.parse(xtr, model);
                if (process != null) {
                    activeProcess = process;
                }
            } else if (ELEMENT_POTENTIAL_STARTER.equals(xtr.getLocalName())) {
                potentialStarterParser.parse(xtr, activeProcess);
            } else if (ELEMENT_LANE.equals(xtr.getLocalName())) {
                laneParser.parse(xtr, activeProcess, model);
            } else if (ELEMENT_DOCUMENTATION.equals(xtr.getLocalName())) {
                BaseElement parentElement = null;
                if (!activeSubProcessList.isEmpty()) {
                    parentElement = activeSubProcessList.get(activeSubProcessList.size() - 1);
                } else if (activeProcess != null) {
                    parentElement = activeProcess;
                }
                documentationParser.parseChildElement(xtr, parentElement, model);
            } else if (activeProcess == null && ELEMENT_TEXT_ANNOTATION.equals(xtr.getLocalName())) {
                String elementId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
                TextAnnotation textAnnotation = (TextAnnotation) new TextAnnotationXMLConverter().convertXMLToElement(xtr, model);
                textAnnotation.setId(elementId);
                model.getGlobalArtifacts().add(textAnnotation);
            } else if (activeProcess == null && ELEMENT_ASSOCIATION.equals(xtr.getLocalName())) {
                String elementId = xtr.getAttributeValue(null, ATTRIBUTE_ID);
                Association association = (Association) new AssociationXMLConverter().convertXMLToElement(xtr, model);
                association.setId(elementId);
                model.getGlobalArtifacts().add(association);
            } else if (ELEMENT_EXTENSIONS.equals(xtr.getLocalName())) {
                extensionElementsParser.parse(xtr, activeSubProcessList, activeProcess, model);
            } else if (ELEMENT_SUBPROCESS.equals(xtr.getLocalName()) || ELEMENT_TRANSACTION.equals(xtr.getLocalName()) || ELEMENT_ADHOC_SUBPROCESS.equals(xtr.getLocalName())) {
                subProcessParser.parse(xtr, activeSubProcessList, activeProcess);
            } else if (ELEMENT_COMPLETION_CONDITION.equals(xtr.getLocalName())) {
                if (!activeSubProcessList.isEmpty()) {
                    SubProcess subProcess = activeSubProcessList.get(activeSubProcessList.size() - 1);
                    if (subProcess instanceof AdhocSubProcess) {
                        AdhocSubProcess adhocSubProcess = (AdhocSubProcess) subProcess;
                        adhocSubProcess.setCompletionCondition(xtr.getElementText());
                    }
                }
            } else if (ELEMENT_DI_SHAPE.equals(xtr.getLocalName())) {
                bpmnShapeParser.parse(xtr, model);
            } else if (ELEMENT_DI_EDGE.equals(xtr.getLocalName())) {
                bpmnEdgeParser.parse(xtr, model);
            } else {
                if (!activeSubProcessList.isEmpty() && ELEMENT_MULTIINSTANCE.equalsIgnoreCase(xtr.getLocalName())) {
                    multiInstanceParser.parseChildElement(xtr, activeSubProcessList.get(activeSubProcessList.size() - 1), model);
                } else if (convertersToBpmnMap.containsKey(xtr.getLocalName())) {
                    if (activeProcess != null) {
                        BaseBpmnXMLConverter converter = convertersToBpmnMap.get(xtr.getLocalName());
                        converter.convertToBpmnModel(xtr, model, activeProcess, activeSubProcessList);
                    }
                }
            }
        }
        for (Process process : model.getProcesses()) {
            for (Pool pool : model.getPools()) {
                if (process.getId().equals(pool.getProcessRef())) {
                    pool.setExecutable(process.isExecutable());
                }
            }
            processFlowElements(process.getFlowElements(), process);
        }
    } catch (XMLException e) {
        throw e;
    } catch (Exception e) {
        LOGGER.error("Error processing BPMN document", e);
        throw new XMLException("Error processing BPMN document", e);
    }
    return model;
}