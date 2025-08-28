    public void execute(GlobalEvalOptions options) throws EvalTaskFailedException {

        if(!options.isForce() && lastModified() >= source.lastModified()) {
            logger.debug("Crossfold {} up to date", this);
            return;
        }    
        
        DAOFactory factory = source.getDAOFactory();
        DataAccessObject dao = factory.create();
        Long2IntMap splits = splitUsers(dao);
        dao.close();
        Holdout mode = this.getHoldout();
        DataAccessObject daoSnap = factory.snapshot();
        logger.debug("Preparing data source {}", getName());
        logger.debug("Writing train test files...");
        createTTFiles(daoSnap, mode, splits);
        daoSnap.close();

    }
