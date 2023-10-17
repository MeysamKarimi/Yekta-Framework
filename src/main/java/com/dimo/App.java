package com.dimo;


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

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;



public class App {

    public static void main(String[] args){

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AlgorithmUI();
            }
        });


//        long startTime = System.nanoTime();
//        long startTime2 = System.currentTimeMillis();
//        MetaModel metaModel = new MetaModel(loadMetamodel(), loadOclRules());
//        DiscoveredPath discoveredPath = new DiscoveredPath(metaModel);
//        //GWO gwo = new GWO(metaModel, discoveredPath, 5, 3, 5, 4, new BigDecimal(0.6), 0.4);
//        //GWO gwo = new GWO(metaModel, discoveredPath, 10, 10, 10, 1, new BigDecimal(0.6), 0.4);
//        GWO gwo = new GWO(metaModel, discoveredPath, 10, 10, 10, 500, new BigDecimal(0.6), 0.4);
//        gwo.run();
//        long duration = System.nanoTime() - startTime;
//        long endTime = System.currentTimeMillis();
//        System.out.println("time:" + (duration/1000000) + "ms");
//        System.out.println("Execution Time(MS): " + (endTime - startTime2));
//        System.out.println("Heap: " + ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
//        System.out.println("NonHeap: " + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
    }


    private static Resource loadMetamodel() { // loads the input metamodel
        try {
            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                    .put("ecore", new EcoreResourceFactoryImpl());
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                    .put("xmi", new XMIResourceFactoryImpl());
            Resource myMetaModel = resourceSet
                    //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Families.ecore").getPath()),true);
                    //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Families_Extended.ecore").getPath()),true);
                    .getResource(URI.createFileURI(App.class.getClassLoader().getResource("CPL.ecore").getPath()),true);
                    //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("Grafcet.ecore").getPath()),true);
                    //.getResource(URI.createFileURI(App.class.getClassLoader().getResource("SOOML.ecore").getPath()),true);
            registerPackages(myMetaModel);
            System.out.println("Problem.metaModel is loaded!");
            return myMetaModel;
        } catch (Exception ex) {
            System.out.println("Unable to load the Metamodel");
            throw ex;
        }
    }
    private static Map<String, Constraint> loadOclRules() {
        Map<String, Constraint> oclConstraints;
        OCL ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
        InputStream in = null;
        try {
            in = new FileInputStream(App.class.getClassLoader().getResource("P.ocl").getPath());
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
            System.out.printf("Error in parsing OCL file, The OCL check is ignored! The exception: ", ex.getStackTrace());
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
}



