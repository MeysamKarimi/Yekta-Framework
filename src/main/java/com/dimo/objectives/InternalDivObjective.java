package com.dimo.objectives;

import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.ETypedElement;
import org.eclipse.emf.ecore.resource.Resource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class InternalDivObjective {

    private static final int refCo = 1;
    private static final int aSizeCo = 3;

    public BigDecimal score(Model model, MetaModel metaModel) {
        //return this.internalDiv(model, metaModel.getAllInstantiableEClasses())
        //        .add(this.refFraction(model, metaModel.getAllInstantiableEClasses()).multiply(new BigDecimal(refCo)))
        //        .add(this.aSizeFraction(model, metaModel.getAllInstantiableEClasses()).multiply(new BigDecimal(aSizeCo)));

        BigDecimal rawValue = this.internalDiv(model, metaModel.getAllInstantiableEClasses())
                .add(this.refFraction(model, metaModel.getAllInstantiableEClasses()).multiply(new BigDecimal(refCo)));
        return rawValue.divide(new BigDecimal(2), 3, RoundingMode.UP);
    }

    private BigDecimal internalDiv(Model model, Set<EClass> instantiableEClasses){
        int notExistEClassesCount = model.findNotExistEClasses(new HashSet<>(instantiableEClasses));
        return new BigDecimal(instantiableEClasses.size() - notExistEClassesCount).divide(new BigDecimal(instantiableEClasses.size()), 3, RoundingMode.UP);
    }

    private BigDecimal refFraction(Model model, Set<EClass> instantiableEClasses){
        int allRefsCount = instantiableEClasses.stream().map(EClass::getEAllReferences).map(List::size).reduce(Integer::sum).orElse(0);
        int existingRefsCount = instantiableEClasses
                .stream()
                .map(eClass -> {
                    int refCount = 0;
                    List<EObject> eObjects = findAllEObjectOfType(model.getModelResource(), eClass);
                    for (int i = 0 ;i < eClass.getEAllReferences().size(); i++){
                        for (EObject eObject : eObjects) {
                            if (eObject.eIsSet(eClass.getEStructuralFeature(i))) {
                                refCount++;
                                break;
                            }
                        }
                    }
                    return refCount;
                })
                .reduce(Integer::sum)
                .orElse(0);
        return new BigDecimal(existingRefsCount).divide(new BigDecimal(allRefsCount), 3, RoundingMode.UP);
    }

    private BigDecimal aSizeFraction(Model model, Set<EClass> instantiableEClasses){
        int aSize = 1;
        TreeIterator<EObject> allContents = model.getModelResource().getAllContents();
        while (allContents.hasNext())
        {
            EObject eObject = allContents.next();
            aSize += eObject
                    .eClass()
                    .getEAllReferences()
                    .stream()
                    .filter(ETypedElement::isMany)
                    .filter(eReference -> ((EList<?>) eObject.eGet(eReference)).size() > 1)
                    .map(eReference -> ((EList<?>) eObject.eGet(eReference)).size() - 1)
                    .mapToInt(integer -> integer)
                    .sum();
        }
        return new BigDecimal(1).divide(new BigDecimal(aSize), 3, RoundingMode.UP);
    }

    private List<EObject> findAllEObjectOfType(Resource modelResource, EClass eClass) {
        TreeIterator<EObject> allContents = modelResource.getAllContents();
        List<EObject> eObjects = new ArrayList<>();
        while (allContents.hasNext())
        {
            EObject eObject = allContents.next();
            if(eObject.eClass().equals(eClass))
                eObjects.add(eObject);
        }
        return eObjects;
    }


}
