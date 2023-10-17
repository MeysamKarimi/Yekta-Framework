package com.dimo.objectives;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.HashSet;
import java.util.Set;

public class ExternalDivObjective {

    public int compare(Resource first, Resource second) {
        return externalDiv(first, second);
    }

    private int externalDiv(Resource r1, Resource r2){
        int min = r1.getContents().size();
        if(min > r2.getContents().size())
            min = r2.getContents().size();
        int div = 0;
        Set<EObject> r1Visited = new HashSet<>();
        Set<EObject> r2Visited = new HashSet<>();

        for (int i =0; i< min; i++)
            div += div(r1.getContents().get(i), r2.getContents().get(i), r1Visited, r2Visited);
        if(r1.getContents().size() > min)
            for (int i = min; i < r1.getContents().size(); i++)
                div += countChildren(r1.getContents().get(i), r1Visited);
        else if(r2.getContents().size() > min)
            for (int i = min; i < r2.getContents().size(); i++)
                div += countChildren(r2.getContents().get(i), r2Visited);
        return div;
    }
    private int div(EObject e1, EObject e2, Set<EObject> e1Visited, Set<EObject> e2Visited){
        int dif = 0;
        if((e1 == null && e2 == null) || (e1Visited.contains(e1) && e2Visited.contains(e2)))  {
            return 0;
        }
        if(e1 != null && e2 != null && e1.eClass().equals(e2.eClass())){
            e1Visited.add(e1);
            e2Visited.add(e2);
            for (int i = 0; i < e1.eClass().getEReferences().size(); i++){
                EReference eReference = e1.eClass().getEReferences().get(i);
                if(eReference.isMany()){
                    EList<EObject> el1 = (EList<EObject>) e1.eGet(eReference);
                    EList<EObject> el2 = (EList<EObject>) e2.eGet(eReference);
                    int minSize = Math.min(el1.size(), el2.size());
                    for(int j = 0; j< minSize ; j++){
                        dif += div(el1.get(j), el2.get(j), e1Visited, e2Visited);
                    }
                    if (el1.size() > minSize){
                        for(int j = minSize; j< el1.size() ; j++)
                            dif += countChildren(el1.get(j), e1Visited);
                    }

                    else if (el2.size() > minSize){
                        for(int j = minSize; j< el2.size() ; j++)
                            dif += countChildren(el2.get(j), e2Visited);
                    }
                }else {
                    dif += div((EObject) e1.eGet(eReference), (EObject) e2.eGet(eReference), e1Visited, e2Visited);
                }
            }
            return dif;
        }
        dif += countChildren(e1, e1Visited);
        dif += countChildren(e2, e2Visited);
        return dif;
    }

    private int countChildren(EObject e, Set<EObject> visited) {
        if(e == null || visited.contains(e))
            return 0;
        visited.add(e);
        int count = 1;
        for (int i = 0; i < e.eClass().getEReferences().size(); i++){
            EReference eReference = e.eClass().getEReferences().get(i);
            if(eReference.isMany()){
                EList<EObject> el = (EList<EObject>) e.eGet(eReference);
                for(int j = 0; j< el.size() ; j++){
                    count += countChildren(el.get(j), visited);
                }
            }
            else{
                count += countChildren((EObject) e.eGet(eReference), visited);
            }
        }
        return count;
    }
}
