package com.dimo;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.dimo.aco.ACO;
import com.dimo.model.DiscoveredPath;
import com.dimo.gwo.GWO;
import com.dimo.model.MetaModel;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.OCLInput;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;

public class AlgorithmUI {
    private JFrame frame;
    private JPanel mainPanel;
    private JPanel parametersPanel;
    private JComboBox<String> algorithmComboBox;
    private FileChooserButton metamodelButton;
    private FileChooserButton oclButton;
    private JFormattedTextField elementCountField;
    private JFormattedTextField iterationsField;
    private JFormattedTextField populationSizeField;
    private JFormattedTextField modelCountField;
    private JTextField alphaTextField;
    private JTextField betaTextField;
    private static JTextPane reportTextArea;

    private static File metamodelFile = null;
    static File oclFile = null;
    Integer elementCount = 10;
    Integer iterations = 1;
    Integer populationSize = 10;
    Integer modelCount = 10;
    BigDecimal alpha = BigDecimal.ONE;
    BigDecimal beta = BigDecimal.ONE;

    private static AlgorithmUI instance;
    private boolean isPrerequisitesMet = true;

    public static AlgorithmUI getInstance() {
        return instance;
    }

    public AlgorithmUI() {
        instance = this;

        // Initialize the UI components
        frame = new JFrame("Yekta Framework");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        algorithmComboBox = new JComboBox<>();
        algorithmComboBox.addItem("ACO");
        algorithmComboBox.addItem("GWO");
        algorithmComboBox.addItem("RANDOM");

        JPanel algorithmPanel = new JPanel();
        algorithmPanel.add(new JLabel("Algorithm:"));
        algorithmPanel.add(algorithmComboBox);

        // Update GridBagConstraints for algorithm panel
        GridBagConstraints gbcAlgorithmPanel = new GridBagConstraints();
        gbcAlgorithmPanel.gridx = 0;
        gbcAlgorithmPanel.gridy = 0;
        gbcAlgorithmPanel.gridwidth = 2;
        gbcAlgorithmPanel.anchor = GridBagConstraints.NORTHWEST;
        gbcAlgorithmPanel.insets = new Insets(5, 5, 5, 5);

        // Add algorithm panel to main panel
        mainPanel.add(algorithmPanel, BorderLayout.NORTH);

        parametersPanel = new JPanel();
        parametersPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        metamodelButton = new FileChooserButton("Choose Metamodel");
        oclButton = new FileChooserButton("Choose OCL");

        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        NumberFormatter formatter = new NumberFormatter(integerFormat);
        formatter.setValueClass(Integer.class);
        formatter.setAllowsInvalid(false);
        formatter.setMinimum(0);

        elementCountField = new JFormattedTextField(formatter);
        iterationsField = new JFormattedTextField(formatter);
        populationSizeField = new JFormattedTextField(formatter);
        modelCountField = new JFormattedTextField(formatter);

        alphaTextField = new JTextField();
        betaTextField = new JTextField();

        reportTextArea =  new JTextPane();
        reportTextArea.setPreferredSize(new Dimension(10, 300));
        reportTextArea.setEditable(false);

        JButton runButton = new JButton("Run Algorithm");
        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reportTextArea.setText("");
                isPrerequisitesMet = true;

                String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();
                displayReport("Running algorithm: " + selectedAlgorithm, ReportType.INFO);

                LoadCommonVariables(selectedAlgorithm);

                if(selectedAlgorithm.equals("ACO"))
                {
                    try {
                        BigDecimal input = new BigDecimal(alphaTextField.getText());
                        if (input.compareTo(BigDecimal.ZERO) > 0 && input.compareTo(BigDecimal.ONE) <= 0) {
                            alpha = new BigDecimal( alphaTextField.getText());
                        } else {
                            // Input is out of range, handle the error accordingly
//                            JOptionPane.showMessageDialog(null, "Please enter a value between 0 and 1.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                            JOptionPane.showMessageDialog(null, "Alpha value must be in range of zero and one. Default value (1.0) is selected", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            displayReport("Please enter a decimal value bigger than 0 and less than or equal to 1 for Alpha!", ReportType.ERROR);
                            displayReport("Alpha is set to 1.0", ReportType.INFO);
                            // we go with default
                            alpha = new BigDecimal(1.0);
                        }
                    } catch (NumberFormatException ex) {
                        // Input is not a valid BigDecimal, handle the error accordingly
//                        JOptionPane.showMessageDialog(null, "Alpha value must be in range of zero and one. Default value (1.0) is selected", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        displayReport("Alpha value must be bigger than 0 and less than or equal to 1. Default value (1.0) is selected", ReportType.INFO);
                        displayReport("Alpha is set to 1.0", ReportType.INFO);
                        // we go with default
                        alpha = new BigDecimal(1.0);

                    }
                    try {
                        BigDecimal input = new BigDecimal(betaTextField.getText());
                        if (input.compareTo(BigDecimal.ZERO) >= 0 && input.compareTo(BigDecimal.ONE) <= 0) {
                            beta = new BigDecimal(betaTextField.getText());
                        } else {
                            // Input is out of range, handle the error accordingly
//                            JOptionPane.showMessageDialog(null, "Please enter a decimal value between 0 and 1.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//                            JOptionPane.showMessageDialog(null, "Beta value must be in range of zero and one. Default value (1.0) is selected", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                            displayReport("Please enter a decimal value between 0 and 1 for Beta!", ReportType.ERROR);
                            displayReport("Beta is set to 1.0", ReportType.INFO);
                            beta = new BigDecimal(1.0);
                        }
                    } catch (NumberFormatException ex) {
                        // Input is not a valid BigDecimal, handle the error accordingly
                        //JOptionPane.showMessageDialog(null, "Please enter a valid decimal number.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        //JOptionPane.showMessageDialog(null, "Beta value must be in range of zero and one. Default value (1.0) is selected", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                        displayReport("Beta value must be in range of zero and one. Default value (1.0) is selected", ReportType.INFO);
                        displayReport("Beta is set to 1.0", ReportType.INFO);
                        // we go with default
                        beta = new BigDecimal(1.0);
                    }
                    if(isPrerequisitesMet)
                        RunACO(selectedAlgorithm);
                    else
                        displayReport("Prerequisites requirements are not met. Make sure inputs are provided and try again!", ReportType.ERROR);
                }
                else if(selectedAlgorithm.equals("GWO"))
                {
                    if(isPrerequisitesMet)
                        RunGWO();
                    else
                        displayReport("Prerequisites requirements are not met. Make sure inputs are provided and try again!", ReportType.ERROR);
                }
                else if(selectedAlgorithm.equals("RANDOM"))
                {
                    alpha = new BigDecimal(0.0);
                    beta = new BigDecimal(1.0);
                    iterations = 1;
                    populationSize = modelCount;

                    if(isPrerequisitesMet)
                        RunACO(selectedAlgorithm);
                    else
                        displayReport("Prerequisites requirements are not met. Make sure inputs are provided and try again!", ReportType.ERROR);
                }
                else
                {
                    //return erro
                }
            }
        });

        // Add components to the main panel
        mainPanel.add(parametersPanel, BorderLayout.CENTER);
        mainPanel.add(runButton, BorderLayout.SOUTH);

        // Register a listener for algorithm selection changes
        algorithmComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateParametersPanel();
            }
        });

        // Set initial parameters panel based on the selected algorithm
        updateParametersPanel();

        // Add the main panel to the frame
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private void RunACO(String selectedAlgorithm) {
        long startTime = System.nanoTime();
        long startTime2 = System.currentTimeMillis();
        MetaModel metaModel = new MetaModel(loadMetamodel(), loadOclRules());
        DiscoveredPath discoveredPath = new DiscoveredPath(metaModel);
        ACO aco = new ACO(metaModel, discoveredPath, elementCount, iterations, populationSize, modelCount, BigDecimal.ONE, new BigDecimal("0.01"), alpha, beta, selectedAlgorithm);
        aco.run();
        long duration = System.nanoTime() - startTime;
        long endTime = System.currentTimeMillis();

        displayReport("Algorithm has been successfully created " + modelCount + " models!", ReportType.INFO);
        displayReport("Execution Time(MS): " + (endTime - startTime2), ReportType.INFO);
        displayReport("Heap: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), ReportType.INFO);
        displayReport("NonHeap: " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), ReportType.INFO);
    }

    private void RunGWO() {
        long startTime = System.nanoTime();
        long startTime2 = System.currentTimeMillis();
        MetaModel metaModel = new MetaModel(loadMetamodel(), loadOclRules());
        DiscoveredPath discoveredPath = new DiscoveredPath(metaModel);
        //GWO gwo = new GWO(metaModel, discoveredPath, 5, 3, 5, 4, new BigDecimal(0.6), 0.4);
        //GWO gwo = new GWO(metaModel, discoveredPath, 10, 10, 10, 1, new BigDecimal(0.6), 0.4);
//        GWO gwo = new GWO(metaModel, discoveredPath, 10, 10, 10, 500, new BigDecimal(0.6), 0.4);
        GWO gwo = new GWO(metaModel, discoveredPath, elementCount, iterations, populationSize, modelCount, new BigDecimal(0.6), 0.4);
        gwo.run();
        long duration = System.nanoTime() - startTime;
        long endTime = System.currentTimeMillis();

        displayReport("Algorithm has been successfully created " + modelCount + " models!", ReportType.INFO);
        displayReport("Execution Time(MS): " + (endTime - startTime2), ReportType.INFO);
        displayReport("Heap: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage(), ReportType.INFO);
        displayReport("NonHeap: " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage(), ReportType.INFO);
    }

    private static Resource loadMetamodel() { // loads the input metamodel
        try {
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                    .put("ecore", new EcoreResourceFactoryImpl());
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                    .put("xmi", new XMIResourceFactoryImpl());
            Resource myMetaModel = resourceSet
                    .getResource(URI.createFileURI(metamodelFile.getPath()),true);
            //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Families.ecore").getPath()),true);
            //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Families_Extended.ecore").getPath()),true);
//                    .getResource(URI.createFileURI(App.class.getClassLoader().getResource("CPL.ecore").getPath()),true);
            //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Grafcet.ecore").getPath()),true);
            //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("SOOML.ecore").getPath()),true);
            registerPackages(myMetaModel);
//            System.out.println("Problem.metaModel is loaded!");
            displayReport("Problem.metaModel is loaded!", ReportType.INFO);

            return myMetaModel;
        } catch (Exception ex) {
            displayReport("Program terminated! Unable to load the Metamodel! Exception has been occurred: " + ex.getMessage(), ReportType.ERROR);
            throw ex;
        }
    }
    private static Map<String, Constraint> loadOclRules() {
        Map<String, Constraint> oclConstraints;
        OCL ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
        InputStream in = null;
        try {
            in = new FileInputStream(oclFile.getPath());
//            in = new FileInputStream(App.class.getClassLoader().getResource("P.ocl").getPath());
            Map<String, Constraint> constraintMap = new HashMap<>();
            // parse the contents as an OCL document
            OCLInput document = new OCLInput(in);
            List<Constraint> constraints = ocl.parse(document);
            for (Constraint next : constraints) {
                constraintMap.put(next.getName(), next);
            }
            in.close();
            oclConstraints = constraintMap;
        } catch (Exception ex) {
            displayReport("The OCL check is ignored! The exception: " + ex.toString(), ReportType.INFO);
            oclConstraints = Map.of();
        }
        return oclConstraints;
    }
    private static void registerPackages(Resource resource){
        EObject eObject = resource.getContents().get(0);
        if(eObject instanceof EPackage)
        {
            EPackage p = (EPackage) eObject;
            EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
        }
    }

    private void LoadCommonVariables(String selectedAlgorithm) {
        metamodelFile = metamodelButton.getSelectedFile();
        if(metamodelFile == null) {
            isPrerequisitesMet = false;
            displayReport("Source Metamodel is mandatory!", ReportType.ERROR);
        }
        oclFile = oclButton.getSelectedFile();
        try {
            modelCount = (Integer) modelCountField.getValue();
            if(modelCount == null || modelCount == 0) {
                isPrerequisitesMet = false;
                displayReport("Number of models to be created is mandatory!", ReportType.ERROR);
            }

            elementCount = (Integer) elementCountField.getValue();
            if(elementCount == null || elementCount == 0) {
                displayReport("Number of elements in each model is mandatory!", ReportType.ERROR);
                isPrerequisitesMet = false;
            }
            if (!selectedAlgorithm.equals("RANDOM")) {
                iterations = (Integer) iterationsField.getValue();
                if(iterations == null || iterations == 0) {
                    isPrerequisitesMet = false;
                    displayReport("Number of running iterations of the algorithm is mandatory!", ReportType.ERROR);
                }

                populationSize = (Integer) populationSizeField.getValue();
                if(populationSize == null || populationSize == 0 || modelCount > populationSize && selectedAlgorithm != "RANDOM") {
                    isPrerequisitesMet = false;
                    displayReport("pupulation size is mandatory and must be bigger or equal to Model Count!", ReportType.ERROR);
                }
            }
        } catch (Exception e) {
            isPrerequisitesMet = false;
            displayReport("Exception occurred: " + e.getMessage(), ReportType.ERROR);
        }
    }

    public enum ReportType
    {
        ERROR,
        INFO
    }

    public static void displayReport(String message, ReportType reportType) {
        Color textColor;

        switch (reportType) {
            case ERROR:
                // Set the color to red for error messages
                textColor = Color.RED;
                message = "Error: " + message;
                break;
            case INFO:
                // Set the color to dark blue for info messages
                textColor = Color.blue;
                break;
            default:
                // Use default text color for other message types
                textColor = reportTextArea.getForeground();
                break;
        }

        Style style = reportTextArea.addStyle("", null);
        StyleConstants.setForeground(style, textColor);
        StyledDocument doc = reportTextArea.getStyledDocument();

        try {
            doc.insertString(doc.getLength(), message + "\n", style);
        }
        catch (Exception ex)
        {}
    }

    private void updateParametersPanel() {
        parametersPanel.removeAll();
        String selectedAlgorithm = (String) algorithmComboBox.getSelectedItem();

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.gridy = GridBagConstraints.RELATIVE;
        gbcLabel.weightx = 0;
        gbcLabel.anchor = GridBagConstraints.EAST;
        gbcLabel.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints gbcComponent = new GridBagConstraints();
        gbcComponent.gridx = 1;
        gbcComponent.gridy = GridBagConstraints.RELATIVE;
        gbcComponent.weightx = 1;
        gbcComponent.fill = GridBagConstraints.HORIZONTAL;
        gbcComponent.insets = new Insets(5, 5, 5, 5);

        parametersPanel.add(new JLabel("Metamodel:"), gbcLabel);
        parametersPanel.add(metamodelButton, gbcComponent);

        if(!selectedAlgorithm.equals("RANDOM")) {
            parametersPanel.add(new JLabel("OCL:"), gbcLabel);
            parametersPanel.add(oclButton, gbcComponent);
        }
        parametersPanel.add(new JLabel("Model Count:"), gbcLabel);
        parametersPanel.add(modelCountField, gbcComponent);

        parametersPanel.add(new JLabel("Element Count:"), gbcLabel);
        parametersPanel.add(elementCountField, gbcComponent);

        if(!selectedAlgorithm.equals("RANDOM")) {
            parametersPanel.add(new JLabel("Number of Iterations:"), gbcLabel);
            parametersPanel.add(iterationsField, gbcComponent);

            parametersPanel.add(new JLabel("Population Size:"), gbcLabel);
            parametersPanel.add(populationSizeField, gbcComponent);
        }


        if (selectedAlgorithm.equals("ACO")) {
            parametersPanel.add(new JLabel("Alpha:"), gbcLabel);
            parametersPanel.add(alphaTextField, gbcComponent);

            parametersPanel.add(new JLabel("Beta:"), gbcLabel);
            parametersPanel.add(betaTextField, gbcComponent);
        }

        parametersPanel.add(new JLabel("Output:"), gbcLabel);
        parametersPanel.add(new JScrollPane(reportTextArea), gbcComponent);

        parametersPanel.revalidate();
        parametersPanel.repaint();
    }
}

class FileChooserButton extends JPanel {
    private JTextField textField;
    private JButton browseButton;
    private JFileChooser fileChooser;

    public FileChooserButton(String buttonText) {
        setLayout(new BorderLayout());

        textField = new JTextField();
        textField.setEditable(false);

        browseButton = new JButton(buttonText);
        fileChooser = new JFileChooser();

        browseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileChooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    textField.setText(file.getAbsolutePath());
                }
            }
        });

        add(textField, BorderLayout.CENTER);
        add(browseButton, BorderLayout.EAST);
    }

    public File getSelectedFile() {
        String filePath = textField.getText();
        if (filePath.isEmpty()) {
            return null;
        } else {
            return new File(filePath);
        }
    }
}
