package com.dimo.model;

import com.dimo.ocl.Checker;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.Resource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class Model {

    private List<Element> model;
    private List<EObject> eObjects;
    private Resource modelResource;
    private List<Element> possibleNext;
    private BigDecimal fintnessValue;

    public Model(List<Element> possibleRoots) {
        this.model = new ArrayList<>();
        eObjects = new ArrayList<>();
        this.possibleNext = new ArrayList<>(possibleRoots);
    }

    public List<Element> getModelElements(){
        return model;
    }

    /**
     * A list of elements that are valid for next step to pick.
     * @return a list of elements
     */
    public List<Element> possibleNextSteps(){
        return Collections.unmodifiableList(possibleNext);
    }

    /**
     * Adds selected step to model
     * @param element that should be added as next step
     * @throws IllegalStateException if element is not in possible next list
     */
    public boolean addElementForNextStep(Element element, MetaModel metaModel, Checker checker) throws IllegalStateException{
        EPackage ePackage = (EPackage) metaModel.getResource().getContents().get(0);
        EFactory eFactory = ePackage.getEFactoryInstance();
        EObject eObject = element.createEObject(checker, eFactory);
        if(eObject == null)
            return false;
        eObjects.add(eObject);
        this.model.add(element);
        this.possibleNext.remove(element);
        if(isRoot(element)){
            processForRoot(element);
        }
        else if(element.getParent().isPointer()){
            processForPointerFather(element);
        }
        else if(!element.getParent().isConcrete()){
            processForAbstractFather(element);
        }
        else{
            processConcrete(element);
        }

        return true;
    }


    private void processConcrete(Element element) {
        int indexInParent = element.getParent().getChildren().indexOf(element);
        List<EReference> eReferences = element.getParent().getEClass().getEAllReferences();
        EReference eReference = eReferences.get(indexInParent);
        if(eReference.isContainer()){
            for(int i=0; i<eReferences.size(); i++){
                if(eReference.isContainer())
                    this.possibleNext.remove(element.getParent().getChildren().get(i));
            }
        }
        this.possibleNext.addAll(this.getAllConcreteChildren(element, element.getParent(),
                element.getParent().getChildren().indexOf(element)));
    }

    private void processForAbstractFather(Element element) {
        int indexInParent;
        Element parent;
        this.possibleNext.removeAll(element.getParent().getChildren());
        if(element.getParent().getParent().isPointer()){
            parent = element.getParent().getParent().getParent();
            indexInParent = parent.getChildren().indexOf(element.getParent().getParent());
            EReference eReference = element
                    .getParent().getParent().getParent()
                    .getEClass().getEAllReferences()
                    .get(indexInParent);
            if(eReference.isContainment()){
                for(int i=0; i< element.getEClass().getEAllReferences().size(); i++){
                    if(element.getEClass().getEAllReferences().get(i).isContainer())
                        this.possibleNext.remove(element.getParent().getParent().getChildren().get(i));
                }
            }
            int indexInDirectParent = element.getParent().getParent().getChildren().indexOf(element.getParent());
            this.possibleNext.addAll(element.getParent().getParent().getChildren().get(indexInDirectParent+1).getChildren());
        }else {
            parent = element.getParent().getParent();
            indexInParent = parent.getChildren().indexOf(element.getParent());
            EReference eReference = element
                    .getParent().getParent()
                    .getEClass().getEAllReferences()
                    .get(indexInParent);
            if(eReference.isContainment()){
                for(int i=0; i< element.getEClass().getEAllReferences().size(); i++){
                    if(element.getEClass().getEAllReferences().get(i).isContainer())
                        this.possibleNext.remove(element.getParent().getParent().getChildren().get(i));
                }
            }
        }
        this.possibleNext.addAll(this.getAllConcreteChildren(element, parent, indexInParent));

    }

    private void processForPointerFather(Element element) {
        int index = element.getParent().getChildren().indexOf(element);
        this.possibleNext.add(element.getParent().getChildren().get(index + 1));
        this.possibleNext.addAll(getAllConcreteChildren(element, element.getParent().getParent(),
                element.getParent().getParent().getChildren().indexOf(element.getParent())));
    }

    private void processForRoot(Element element) {
        this.possibleNext.clear();
        this.possibleNext.addAll(this.getAllConcreteChildren(element, null, -1));
    }

    private List<Element> getAllConcreteChildren(Element element, Element parent, int indexInParent){
        List<Element> children = new ArrayList<>();
        element.getChildren().forEach(child -> {
            EReference eReference = element.getEClass().getEAllReferences().get( element.getChildren().indexOf(child));
            if(!child.isContained() || eReference.isContainment()){
                if(parent != null && parent.getEClass().getEAllReferences().get(indexInParent).isContainment()){
                    if(eReference.isContainer())
                        return;
                }
                if(child.isConcrete()){
                    children.add(child);
                }else{
                    if(child.isPointer()){
                        if(child.getChildren().get(0).isConcrete())
                            children.add(child.getChildren().get(0));
                        else
                            children.addAll(child.getChildren().get(0).getChildren());
                    }
                    else
                        children.addAll(child.getChildren());
                }
            }
        });
        return children;
    }

    private boolean isRoot(Element element){
        return element.getParent() == null ||
                (element.getParent().getParent() == null && !element.getParent().isConcrete());
    }

    /**
     * after all model elements been created, a call to this method is required in order to
     * generate the resource representation of the model.
     * @param metaModel that this model is based on.
     * @param uri for created model to be saved.
     */
    public void generateResource(MetaModel metaModel, URI uri){
        if (this.modelResource != null)
            return;

        Resource model = metaModel.getResource().getResourceSet().createResource(uri);
        List<Element> elements = new ArrayList<>(this.model);
        Map<Integer, EObject> eObjectMap = new HashMap<>();
        Element root = elements.remove(0);
        buildModel(elements, root, model, eObjectMap);
        this.addCrossRefs(eObjectMap, metaModel);
        this.modelResource = model;
    }

    private EObject buildModel(List<Element> elements, Element root, Resource model, Map<Integer, EObject> eObjectMap) {
        int index = this.model.indexOf(root);
        EObject rootObject = eObjects.get(index);
        if (!root.isContained()){
            model.getContents().add(rootObject);
        }
        eObjectMap.put(index, rootObject);
        for (int i = 0; i < root.getChildren().size(); i++){
            Element child = root.getChildren().get(i);
            if (child.isPointer()){
                EList<EObject> children = new BasicEList<>();
                for (int j = 0; j< child.getChildren().size(); j++){
                    if(child.getChildren().get(j).isConcrete()){
                        if(elements.contains(child.getChildren().get(j))){
                            elements.remove(child.getChildren().get(j));
                            EObject childObject = buildModel(elements, child.getChildren().get(j), model, eObjectMap);
                            children.add(childObject);
                        }
                    }else {
                        for(int k = 0; k< child.getChildren().get(j).getChildren().size(); k++)
                            if(elements.contains(child.getChildren().get(j).getChildren().get(k))){
                                elements.remove(child.getChildren().get(j).getChildren().get(k));
                                EObject childObject = buildModel(elements, child.getChildren().get(j).getChildren().get(k), model, eObjectMap);
                                children.add(childObject);
                                break;
                            }
                    }
                }
                rootObject.eSet(root.getEClass().getEAllReferences().get(i), children);
            }
            else if (child.isConcrete())
            {
                if(elements.contains(child)){
                    elements.remove(child);
                    EObject childObject = buildModel(elements, child, model, eObjectMap);
                    rootObject.eSet(root.getEClass().getEAllReferences().get(i), childObject);
                }
            }else {
                for(int k = 0; k< child.getChildren().size(); k++)
                    if(elements.contains(child.getChildren().get(k))){
                        elements.remove(child.getChildren().get(k));
                        EObject childObject = buildModel(elements, child.getChildren().get(k), model, eObjectMap);
                        rootObject.eSet(root.getEClass().getEAllReferences().get(i), childObject);
                        break;
                    }
            }

        }
        return rootObject;
    }

    public Resource getModelResource(){
        if (this.modelResource == null)
            throw new IllegalStateException("please first generate the resource by calling generateResource");
        return this.modelResource;
    }




    public int findNotExistEClasses(HashSet<EClass> eClasses) {
        this.model.forEach(element -> eClasses.remove(element.getEClass()));
        return eClasses.size();
    }


    private void addCrossRefs(Map<Integer, EObject> eObjectMap, MetaModel metaModel) {
        for (int i = 0; i < this.model.size(); i++){
            for (int j =0; j< this.model.get(i).getEClass().getEAllReferences().size(); j++){
                EReference eReference = this.model.get(i).getEClass().getEAllReferences().get(j);
                if (!this.model.get(i).getChildren().get(j).isContained() ||
                        (this.model.get(i).getChildren().get(j).isContained() && !eReference.isContainment())){
                    if(!eReference.isContainer()){
                        if(!eReference.isMany()){
                            EObject crossRef = findOneOfType(metaModel.getPossibleReplacesFor(eReference.getEReferenceType()), eObjectMap.values());
                            if(!eObjectMap.get(i).equals(crossRef))
                                eObjectMap.get(i).eSet(eReference, crossRef);
                        }
                        else {
                                EList<EObject> crossRefs = findRandomCountOfType(metaModel.getPossibleReplacesFor(eReference.getEReferenceType()), eObjectMap.values());
                                eObjectMap.get(i).eSet(eReference, crossRefs);
                        }
                    }
                }
            }
        }
    }

    private EList<EObject> findRandomCountOfType(List<EClass> eClasses, Collection<EObject> eObjects) {
         List<EObject> possibleRefs = eObjects
                .stream()
                .filter(object -> eClasses.contains(object.eClass()))
                .collect(Collectors.toList());
         return new BasicEList<>(possibleRefs
                 .stream()
                 .filter(object -> RandomGenerators.generateRandomBoolean())
                 .collect(Collectors.toList()));
    }

    private EObject findOneOfType(List<EClass> eClasses, Collection<EObject> eObjects) {
        List<EObject> possibleRefs = eObjects
                .stream()
                .filter(object -> eClasses.contains(object.eClass()))
                .collect(Collectors.toList());
        if(possibleRefs.size() == 0)
            return null;
        return possibleRefs.get(RandomGenerators.positiveIntLessThan(possibleRefs.size()));
    }

    public BigDecimal getFintnessValue() {
        return fintnessValue;
    }

    public void setFintnessValue(BigDecimal fintnessValue) {
        this.fintnessValue = fintnessValue;
    }
}
