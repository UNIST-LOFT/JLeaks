    public void inform(Event event) {
        if (event instanceof ScanUploadEvent) {

            File file = ((ScanUploadEvent)event).getFile();
            try {
                Analysis analysis = new DependencyCheckParser().parse(file);

                QueryManager qm = new QueryManager();
                Project project = qm.createProject(analysis.getProjectInfo().getName());
                Scan scan = qm.createScan(project, new Date(), new Date());

                List<Component> components = new ArrayList<>();
                for (Dependency dependency : analysis.getDependencies()) {
                    Component component = qm.createComponent(
                            dependency.getFileName(),
                            dependency.getFileName(),
                            dependency.getMd5(),
                            dependency.getSha1(),
                            dependency.getDescription(),
                            dependency.getLicense(),
                            null
                    );
                    components.add(component);
                    qm.bind(scan, component);

                    for (Evidence evidence : dependency.getEvidenceCollected()) {
                        qm.createEvidence(component, evidence.getType(), evidence.getConfidenceScore(evidence.getConfidenceType()), evidence
                                .getSource(), evidence.getName(), evidence.getValue());
                    }
                }

                qm.close();
            } catch (ParseException e) {

            }
        }
    }
