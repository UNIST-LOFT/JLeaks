public CenterPicker(final String mapName) {
  super("Center Picker");
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  File file = null;
  if (mapFolderLocation != null && mapFolderLocation.exists()) {
    file = new File(mapFolderLocation, "polygons.txt");
  }
  if (file == null || !file.exists()) {
    file = new File(new File(mapName).getParent() + File.separator + "polygons.txt");
  }
  if (file.exists() && JOptionPane.showConfirmDialog(new JPanel(),
      "A polygons.txt file was found in the map's folder, do you want to use the file to supply the territories "
          + "names?",
      "File Suggestion", 1) == 0) {
    try {
      polygons = PointFileReaderWriter.readOneToManyPolygons(new FileInputStream(file.getPath()));
    } catch (final IOException ex1) {
      System.out.println("Something wrong with your Polygons file: " + ex1);
      ex1.printStackTrace();
    }
  } else {
    try {
      final String polyPath = new FileOpen("Select A Polygon File", mapFolderLocation, ".txt").getPathString();
      if (polyPath != null) {
        polygons = PointFileReaderWriter.readOneToManyPolygons(new FileInputStream(polyPath));
      }
    } catch (final IOException ex1) {
      System.out.println("Something wrong with your Polygons file: " + ex1);
      ex1.printStackTrace();
    }
  }
  createImage(mapName);
  final JPanel imagePanel = createMainPanel();
  /*
   * Add a mouse listener to show
   * X : Y coordinates on the lower
   * left corner of the screen.
   */
  imagePanel.addMouseMotionListener(new MouseMotionAdapter() {
    @Override
    public void mouseMoved(final MouseEvent e) {
      locationLabel.setText("x:" + e.getX() + " y:" + e.getY());
    }
  });
  // Add a mouse listener to monitor for right mouse button being clicked.
  imagePanel.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(final MouseEvent e) {
      mouseEvent(e.getPoint(), SwingUtilities.isRightMouseButton(e));
    }
  });
  // set up the image panel size dimensions ...etc
  imagePanel.setMinimumSize(new Dimension(image.getWidth(this), image.getHeight(this)));
  imagePanel.setPreferredSize(new Dimension(image.getWidth(this), image.getHeight(this)));
  imagePanel.setMaximumSize(new Dimension(image.getWidth(this), image.getHeight(this)));
  // set up the layout manager
  this.getContentPane().setLayout(new BorderLayout());
  this.getContentPane().add(new JScrollPane(imagePanel), BorderLayout.CENTER);
  this.getContentPane().add(locationLabel, BorderLayout.SOUTH);
  // set up the actions
  final Action openAction = SwingAction.of("Load Centers", e -> loadCenters());
  openAction.putValue(Action.SHORT_DESCRIPTION, "Load An Existing Center Points File");
  final Action saveAction = SwingAction.of("Save Centers", e -> saveCenters());
  saveAction.putValue(Action.SHORT_DESCRIPTION, "Save The Center Points To File");
  final Action exitAction = SwingAction.of("Exit", e -> System.exit(0));
  exitAction.putValue(Action.SHORT_DESCRIPTION, "Exit The Program");
  // set up the menu items
  final JMenuItem openItem = new JMenuItem(openAction);
  openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
  final JMenuItem saveItem = new JMenuItem(saveAction);
  saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
  final JMenuItem exitItem = new JMenuItem(exitAction);
  // set up the menu bar
  final JMenuBar menuBar = new JMenuBar();
  setJMenuBar(menuBar);
  final JMenu fileMenu = new JMenu("File");
  fileMenu.setMnemonic('F');
  fileMenu.add(openItem);
  fileMenu.add(saveItem);
  fileMenu.addSeparator();
  fileMenu.add(exitItem);
  menuBar.add(fileMenu);
} // end constructor