package com.dimo.model;

import com.dimo.ocl.Checker;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.emf.ecore.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Element {
    private BigDecimal pheromone;
    private boolean pointer;
    private boolean concrete;
    private EClass eClass;
    private Element parent;
    private boolean contained;
    private List<Element> children;


    public static Element createConcreteRoot(BigDecimal pheromone, EClass eClass, boolean contained){
        return new Element(pheromone, false, true, eClass, null, contained, new ArrayList<>());
    }

    public static Element createAbstractRoot(BigDecimal pheromone, EClass eClass, boolean contained, List<EClass> possibleReplaces){
       return createAbstractChild(pheromone, eClass, null, contained, possibleReplaces);
    }

    public static Element createPointer(Element parent, boolean contained){
        return new Element(null, true, false, null, parent, contained, new ArrayList<>());
    }

    public static Element createConcreteChild(BigDecimal pheromone, EClass eClass, Element parent, boolean contained){
        return new Element(pheromone, false, true, eClass, parent, contained, new ArrayList<>());
    }

    public static Element createAbstractChild(BigDecimal pheromone, EClass eClass, Element parent, boolean contained, List<EClass> possibleReplaces){
        Element element = new Element(null, false, false, eClass,parent, contained, new ArrayList<>());
        List<Element> children = possibleReplaces
                .stream()
                .map(child -> createConcreteChild(pheromone, child, element, contained))
                .collect(Collectors.toList());
        element.getChildren().addAll(children);
        return element;
    }

    public Element(BigDecimal pheromone, boolean pointer, boolean concrete, EClass eClass, Element parent, boolean contained, List<Element> children) {
        this.pheromone = pheromone;
        this.pointer = pointer;
        this.concrete = concrete;
        this.eClass = eClass;
        this.parent = parent;
        this.contained = contained;
        this.children = children;
    }

    /**
     * Creates an EObject for this Element's EClass.
     * it also randomly set All attributes and enumeration of generated object.
     * @param eFactory that should be used to create object
     * @return the generated EObject
     */
    public EObject createEObject(Checker oclChecker, EFactory eFactory){
        EObject eObject = eFactory.create(this.eClass);
        addRandomAttributes(eObject, this.eClass);
        var constraints = oclChecker.findConstraints(this.eClass);
        for (var constraint : constraints){
            int counter = 0;
            while (!oclChecker.check(eObject, constraint))
            {
                if(counter >= 10)
                    return null;
                var a = oclChecker.constrainedAttribute(eClass, constraint).get();

                //Todo replace with smart picker
                addValueForAttribute(eObject, a);
                counter++;
            }
        }
            return eObject;
    }
    private void addRandomAttributes(EObject object, EClass eClass) {
        // loop thorough all attributes of generated eObject to set them
        eClass.getEAttributes().forEach(eAttribute -> {
            addValueForAttribute(object, eAttribute);
        });
        eClass.getESuperTypes().forEach(ec -> {
            addRandomAttributes(object, ec);
        });
    }

    private void addValueForAttribute(EObject object, EAttribute eAttribute) {
        var classifierId = eAttribute.getEAttributeType().getClassifierID();

        // if attribute is many then it should set an EList
        if(eAttribute.isMany()){
            if(classifierId == EcorePackage.ESTRING)
                object.eSet(eAttribute, RandomGenerators.generateRandomStrings());
            else if(classifierId == EcorePackage.EINT)
                object.eSet(eAttribute, RandomGenerators.generateRandomIntegers());
            else {
                // just to notify that an attribute type has not been handled
                throw new IllegalStateException();
            }
        }
        else {
            if(classifierId == EcorePackage.ESTRING)
                object.eSet(eAttribute, RandomGenerators.generateRandomString());
            else if(classifierId == EcorePackage.EBOOLEAN)
                object.eSet(eAttribute, RandomGenerators.generateRandomBoolean());
            else if(classifierId == EcorePackage.EINT)
                object.eSet(eAttribute, RandomGenerators.generateRandomInt());
            else if(classifierId == EcorePackage.EDOUBLE)
                object.eSet(eAttribute, RandomGenerators.generateRandomDouble());
            else if(eAttribute.getEAttributeType() instanceof EEnum){
                EEnum eEnum = (EEnum) eAttribute.getEAttributeType();
                int index = RandomGenerators.positiveIntLessThan(eEnum.getELiterals().size());
                EEnumLiteral literal = eEnum.getEEnumLiteral(index);
                object.eSet(eAttribute, literal);
            }else {
                throw new IllegalStateException();
            }
        }
    }
}
