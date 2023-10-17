package com.dimo.ocl;

import lombok.RequiredArgsConstructor;
import org.eclipse.emf.ecore.*;
import org.eclipse.ocl.OCL;
import org.eclipse.ocl.Query;
import org.eclipse.ocl.ecore.Constraint;
import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
import org.eclipse.ocl.helper.OCLHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class Checker {

    private final Map<String, Constraint> oclConstraints;
    private final OCL ocl = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);

    public List<Constraint> findConstraints(EClass eClass){
        return oclConstraints
                .values()
                .stream()
                .filter(constraint -> constrainedAttribute(eClass, constraint).isPresent())
                .collect(Collectors.toList());
    }

    public Optional<EAttribute> constrainedAttribute(EClass eClass, Constraint constraint) {
        var constrainedEClass = (EClass) constraint.getConstrainedElements().get(0);
        if(!eClass.equals(constrainedEClass))
            return Optional.empty();
        var cName = constraint.getSpecification().getBodyExpression().eContents().get(1).eContents().get(1).toString().split("\\.")[1];
        return constrainedEClass
                .getEAllAttributes()
                .stream()
                .filter(eAttribute -> eAttribute.getName().equals(cName))
                .findAny();
    }

    public boolean check(EObject root){

        for (String name : oclConstraints.keySet()){
            var result = check(root, oclConstraints.get(name));
            if (!result)
            {
                System.out.println("constraint with name " + name + " violated");
                return false;
            }
        }
        for (EObject eObject : root.eContents()){
            var result = check(eObject);
            if (!result)
            {
                return false;
            }
        }
        return true;
    }
    public boolean check(EObject eObject, Constraint constraint){
        OCLHelper helper = ocl.createOCLHelper();
        helper.setContext(eObject.eClass());
        Query query = ocl.createQuery(constraint);
        if (eObject.eClass().equals(constraint.getConstrainedElements().get(0)) && !query.check(eObject)){
            return false;
        }
        return true;
    }
}
