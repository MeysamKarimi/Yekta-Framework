package com.dimo.model;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;

import java.util.List;

public class RandomGenerators {
    public static EList<String> generateRandomStrings(){
        EList<String> strings = new BasicEList<>();
        int count = (int)(Math.random() * 10);
        for(int i =0 ; i< count ; i++){
            strings.add(generateRandomString());
        }
        return strings;
    }
    public static String generateRandomString(){
        return NameGenerator.randomName();
    }

    public static List<Integer> generateRandomIntegers(){
        EList<Integer> integers = new BasicEList<>();
        int count = (int)(Math.random() * 1000);
        for(int i =0 ; i< count ; i++){
            integers.add(generateRandomInt());
        }
        return integers;
    }

    public static int generateRandomInt(){
        if(generateRandomBoolean())
            return (int)(Math.random() * Integer.MAX_VALUE);
        else
            return (int)(Math.random() * Integer.MIN_VALUE);
    }

    public static double generateRandomDouble(){
        if(generateRandomBoolean())
            return Math.random() * Double.MAX_VALUE;
        else
            return Math.random() * Double.MIN_VALUE;
    }

    public static int positiveIntLessThan(int x){
        return (int)(Math.random() * x);
    }

    public static boolean generateRandomBoolean(){
        return Math.random() >= 0.5;
    }
}
