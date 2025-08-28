public void execute () throws BuildException {        
    if ( questionsFile == null ) {
        throw new BuildException ("questions file must be provided");
    }
    
    if ( output == null ) {
        throw new BuildException ("output file must be specified");
    }
    
    boolean generateTemplate = !questionsFile.exists();
    
    if (
        !generateTemplate && 
        output.exists() && 
        questionsFile.lastModified() <= output.lastModified() &&
        this.getProject().getProperty ("arch.generate") == null
    ) {
        // nothing needs to be generated. everything is up to date
        return;
    }
    
    
    Document q;
    Source qSource;
    DocumentBuilderFactory factory;
    DocumentBuilder builder;
    try {
        factory = DocumentBuilderFactory.newInstance ();
        factory.setValidating(!generateTemplate && !"true".equals(this.getProject().getProperty ("arch.private.disable.validation.for.test.purposes"))); // NOI18N
        
        builder = factory.newDocumentBuilder();
        builder.setErrorHandler(this);
        builder.setEntityResolver(this);

        if (generateTemplate) {
            InputStream resource = Arch.class.getResourceAsStream("Arch-api-questions.xml");
            try {
                q = builder.parse(resource);
                qSource = new DOMSource (q);
            } finally {
                resource.close();
            }
        } else {
            q = builder.parse (questionsFile);
            qSource = new DOMSource (q);
        }
        
        if (parseException != null) {
            throw parseException;
        }
        
    } catch (SAXParseException ex) {
        log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
        throw new BuildException(questionsFile.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
    } catch (Exception ex) {
        throw new BuildException ("File " + questionsFile + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
    }
    questions = readElements (q, "question");
    
    String questionsVersion;
    {
        NodeList apiQuestions = q.getElementsByTagName("api-questions");
        if (apiQuestions.getLength () != 1) {
            throw new BuildException ("No element api-questions");
        }
        questionsVersion = ((Element)apiQuestions.item (0)).getAttribute ("version");
        if (questionsVersion == null) {
            throw new BuildException ("Element api-questions does not have attribute version");
        }
    }
    
    if (questions.size () == 0) {
        throw new BuildException ("There are no <question> elements in the file!");
    }
    
    if (generateTemplate) {
        log ("Input file " + questionsFile + " does not exist. Generating it with skeleton answers.");
        try {
            SortedSet<String> s = new TreeSet<>(questions.keySet());
            generateTemplateFile(questionsVersion, s);
        } catch (IOException ex) {
            throw new BuildException (ex);
        }
        
        return;
    }
    
    answers = readElements (q, "answer");
    
    
    {
        //System.out.println("doc:\n" + q.getDocumentElement());
        
        // version of answers and version of questions
        NodeList apiAnswers = q.getElementsByTagName("api-answers");
        
        if (apiAnswers.getLength() != 1) {
            throw new BuildException ("No element api-answers");
        }
        
        String answersVersion = ((Element)apiAnswers.item (0)).getAttribute ("question-version");
        
        if (answersVersion == null) {
            throw new BuildException ("Element api-answers does not have attribute question-version");
        }
        
        if (!answersVersion.equals(questionsVersion)) {
            String msg = questionsFile.getAbsolutePath() + ": answers were created for questions version \"" + answersVersion + "\" but current version of questions is \"" + questionsVersion + "\"";
            if ("false".equals (this.getProject().getProperty("arch.warn"))) {
                throw new BuildException (msg);
            } else {
                log (msg, Project.MSG_WARN);
            }
        }
    }
    
    {
        // check all answers have their questions
        SortedSet<String> s = new TreeSet<>(questions.keySet());
        s.removeAll (answers.keySet ());
        if (!s.isEmpty()) {
            if ("true".equals (this.getProject().getProperty ("arch.generate"))) {
                log ("Missing answers to questions: " + s);
                log ("Generating the answers to end of file " + questionsFile);
                try {
                    generateMissingQuestions(questionsVersion, s);
                } catch (IOException ex) {
                    throw new BuildException (ex);
                }
                qSource = new StreamSource (questionsFile);
                try {
                    q = builder.parse(questionsFile);
                } catch (IOException ex) {
                    throw new BuildException(ex);
                } catch (SAXException ex) {
                    throw new BuildException(ex);
                }
            } else {
                log (
                    questionsFile.getAbsolutePath() + ": some questions have not been answered: " + s + "\n" + 
                    "Run with -Darch.generate=true to add missing questions into the end of question file"
                , Project.MSG_WARN);
            }
        }
    }
    
    if (apichanges != null) {
        // read also apichanges and add them to the document
        log("Reading apichanges from " + apichanges);
        Document api;
        try {
            api = builder.parse (apichanges);
        } catch (SAXParseException ex) {
            log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
            throw new BuildException(apichanges.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
        } catch (Exception ex) {
            throw new BuildException ("File " + apichanges + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
        }
        
        NodeList node = api.getElementsByTagName("apichanges");
        if (node.getLength() != 1) {
            throw new BuildException("Expected one element <apichanges/> in " + apichanges + "but was: " + node.getLength());
        }
        Node n = node.item(0);
        Node el = q.getElementsByTagName("api-answers").item(0);
        
        el.appendChild(q.importNode(n, true));
        
        
        qSource = new DOMSource(q);
        qSource.setSystemId(questionsFile.toURI().toString());
    }
    
    if (project != null) {
        // read also project file and apply transformation on defaultanswer tags
        log("Reading project from " + project);
        
        
        Document prj;
        try {
            DocumentBuilderFactory fack = DocumentBuilderFactory.newInstance();
            fack.setNamespaceAware(false);
            prj = fack.newDocumentBuilder().parse (project);
        } catch (SAXParseException ex) {
            log(ex.getSystemId() + ":" + ex.getLineNumber() + ": " + ex.getLocalizedMessage(), Project.MSG_ERR);
            throw new BuildException(project.getAbsolutePath() + " is malformed or invalid", ex, getLocation());
        } catch (Exception ex) {
            throw new BuildException ("File " + project + " cannot be parsed: " + ex.getLocalizedMessage(), ex, getLocation());
        }
        
        // enhance the project document with info about stability and logical name of an API
        // use arch.code-name-base.name and arch.code-name-base.category
        // to modify regular:
        //    <dependency>
        //        <code-name-base>org.openide.util</code-name-base>
        //        <build-prerequisite/>
        //        <compile-dependency/>
        //        <run-dependency>
        //            <specification-version>6.2</specification-version>
        //        </run-dependency>
        //    </dependency>
        // to include additional items like:
        //    <dependency>
        //        <code-name-base>org.openide.util</code-name-base>
        //        <api-name>UtilitiesAPI</api-name>
        //        <api-category>official</api-category>
        //        <build-prerequisite/>
        //        <compile-dependency/>
        //        <run-dependency>
        //            <specification-version>6.2</specification-version>
        //        </run-dependency>
        //    </dependency>
        
        {
            NodeList deps = prj.getElementsByTagName("code-name-base");
            for (int i = 0; i < deps.getLength(); i++) {
                Node name = deps.item(i);
                String api = name.getChildNodes().item(0).getNodeValue();
                String human = this.getProject().getProperty("arch." + api + ".name");
                if (human != null) {
                    if (human.equals("")) {
                        throw new BuildException("Empty name for " + api + " from " + project);
                    }
                    
                    Element e = prj.createElement("api-name");
                    e.appendChild(prj.createTextNode(human));
                    name.getParentNode().insertBefore(e, name);
                }
                String category = this.getProject().getProperty("arch." + api + ".category");
                if (category != null) {
                    if (category.equals("")) {
                        throw new BuildException("Empty category for " + api + " from " + project);
                    }
                    Element e = prj.createElement("api-category");
                    e.appendChild(prj.createTextNode(category));
                    name.getParentNode().insertBefore(e, name);
                }
                
            }
        }
        DOMSource prjSrc = new DOMSource(prj);
        
        NodeList node = prj.getElementsByTagName("project");
        if (node.getLength() != 1) {
            throw new BuildException("Expected one element <project/> in " + project + "but was: " + node.getLength());
        }
        NodeList list= q.getElementsByTagName("answer");
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            String id = n.getAttributes().getNamedItem("id").getNodeValue();
            URL u = Arch.class.getResource("Arch-default-" + id + ".xsl");
            if (u != null) {
                log("Found default answer to " + id + " question", Project.MSG_VERBOSE);
                Node defaultAnswer = findDefaultAnswer(n);
                if (defaultAnswer != null && 
                    "none".equals(defaultAnswer.getAttributes().getNamedItem("generate").getNodeValue())
                ) {
                    log("Skipping answer as there is <defaultanswer generate='none'", Project.MSG_VERBOSE);
                    // ok, this default answer is requested to be skipped
                    continue;
                }
                
                DOMResult res = new DOMResult(q.createElement("p"));
                try {
                    StreamSource defXSL = new StreamSource(u.openStream());
                
                    TransformerFactory fack = TransformerFactory.newInstance();
                    Transformer t = fack.newTransformer(defXSL);
                    t.transform(prjSrc, res);
                } catch (IOException ex) {
                    throw new BuildException (ex);
                } catch (TransformerException ex) {
                    throw new BuildException (ex);
                }
                
                if (defaultAnswer != null) {
                    log("Replacing default answer", Project.MSG_VERBOSE);
                    defaultAnswer.getParentNode().replaceChild(res.getNode(), defaultAnswer);
                } else {
                    log("Adding default answer to the end of previous one", Project.MSG_VERBOSE);
                    Element para = q.createElement("p");
                    para.appendChild(q.createTextNode("The default answer to this question is:"));
                    para.appendChild(q.createComment("If you do not want default answer to be generated you can use <defaultanswer generate='none' /> here"));
                    para.appendChild(q.createElement("br"));
                    para.appendChild(res.getNode());
                    n.appendChild(para);
                }
            }
        }
        
        
        qSource = new DOMSource(q);
        qSource.setSystemId(questionsFile.toURI().toString());
    }
    if (this.getProject().getProperty("javadoc.title") != null) {
        // verify we have the api-answers@module and possibly add it
        NodeList deps = q.getElementsByTagName("api-answers");
        if (deps.getLength() != 1) {
            throw new BuildException("Strange number of api-answers elements: " + deps.getLength());
        }
        
        Node module = deps.item(0).getAttributes().getNamedItem("module");
        if (module == null) {
            Attr attr = q.createAttribute("module");
            deps.item(0).getAttributes().setNamedItem(attr);
            attr.setValue(this.getProject().getProperty("javadoc.title"));
        }
        qSource = new DOMSource(q);
        qSource.setSystemId(questionsFile.toURI().toString());
    }            
    
    
    // apply the transform operation
    try {
        StreamSource ss;
        String file = this.xsl != null ? this.xsl.toString() : getProject().getProperty ("arch.xsl");
        
        if (file != null) {
            log ("Using " + file + " as the XSL stylesheet");
            ss = new StreamSource (file);
        } else {
            ss = new StreamSource (
                getClass ().getResourceAsStream ("Arch.xsl")
            );
        }
        
        log("Transforming " + questionsFile + " into " + output);
        TransformerFactory trans;
        trans = TransformerFactory.newInstance();
        trans.setURIResolver(this);
        Transformer t = trans.newTransformer(ss);
        OutputStream os = new BufferedOutputStream (new FileOutputStream (output));
        StreamResult r = new StreamResult (os);
        if (stylesheet == null) {
            stylesheet = this.getProject ().getProperty ("arch.stylesheet");
        }
        if (stylesheet != null) {
            t.setParameter("arch.stylesheet", stylesheet);
        }
        if (overviewlink != null) {
            t.setParameter("arch.overviewlink", overviewlink);
        }
        if (footer != null) {
            t.setParameter("arch.footer", footer);
        }
        t.setParameter("arch.answers.date", DateFormat.getDateInstance().format(new Date(questionsFile.lastModified())));

        String archTarget = output.toString();
        int slash = archTarget.lastIndexOf(File.separatorChar);
        if (slash > 0) {
            archTarget = archTarget.substring (slash + 1);
        }
        String archPref = getProject ().getProperty ("arch.target");
        if (archPref != null) {
            archTarget = archPref + "/" + archTarget;
        }

        t.setParameter("arch.target", archTarget);
        String when = getProject().getProperty("arch.when");
        if (when != null) {
            t.setParameter("arch.when", when);
        }
        t.transform(qSource, r);
        os.close ();
    } catch (IOException ex) {
        throw new BuildException (ex);
    } catch (TransformerConfigurationException ex) {
        throw new BuildException (ex);
    } catch (TransformerException ex) {
        throw new BuildException (ex);
    }
}