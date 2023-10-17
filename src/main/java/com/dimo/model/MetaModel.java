package com.dimo.model;

import lombok.Getter;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ocl.ecore.Constraint;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class MetaModel {

    private final Set<EClass> containedEClasses;
    private final Map<EClass, Set<EClass>> possibleReplaces;
    private final Map<EClass, List<EReference>> eClassEReferences;
    private final Resource resource;

    private final Map<String, Constraint> oclConstraints;


    public MetaModel(Resource metaModel, Map<String, Constraint> oclConstraints) {
        this.oclConstraints = oclConstraints;
        this.resource = metaModel;
        this.containedEClasses = new HashSet<>();
        this.possibleReplaces = new HashMap<>();
        this.eClassEReferences = new HashMap<>();
        findPossibleReplaces();
        findEClassesERefs();
        findContained();
    }

    private void findPossibleReplaces(){
        this.resource
                .getContents()
                .get(0)
                .eContents()
                .stream()
                .filter(eObject -> !(eObject instanceof EEnum))
                .map(eObject -> (EClass)eObject)
                .forEach(eClass -> {
                   if(!(eClass.isAbstract() || eClass.isInterface()))
                           this.possibleReplaces.put(eClass, new HashSet<>(List.of(eClass)));
                   else
                       this.possibleReplaces.put(eClass, new HashSet<>());

                });
        this.possibleReplaces
                .forEach((key, values) -> values.addAll(this.findDirectSubClasses(key)));
        this.possibleReplaces
                .forEach((key, values) -> values.addAll(this.findInDirectSubClasses(key)));
    }

    public List<EClass> getPossibleReplacesFor(EClass eClass){
        return new ArrayList<>(this.possibleReplaces.get(eClass));
    }

    private Set<EClass> findInDirectSubClasses(EClass key) {
        Set<EClass> subs = this
                .possibleReplaces
                .get(key)
                .stream()
                .filter(eClass -> !eClass.equals(key))
                .map(this.possibleReplaces::get)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
        subs.addAll(this
                .possibleReplaces
                .get(key)
                .stream()
                .filter(eClass -> !eClass.equals(key))
                .map(this::findInDirectSubClasses)
                .flatMap(Set::stream)
                .collect(Collectors.toList()));
        return subs;
    }

    public Collection<? extends EClass> findDirectSubClasses(EClass superClass) {
        return this.resource
                .getContents()
                .get(0)
                .eContents()
                .stream()
                .filter(eObject -> !(eObject instanceof EEnum))
                .map(eObject -> (EClass)eObject)
                .map(eClass -> {
                    if(!(eClass.isAbstract() || eClass.isInterface())){
                        return eClass;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .filter(superClass::isSuperTypeOf)
                .collect(Collectors.toList());
    }

    private void findContained(){
        List<EClass> searched = new ArrayList<>();
        resource.getContents().get(0).eContents().forEach(object -> {
            if(object instanceof EClass){
                if (searched.contains((EClass) object))
                    return;
                searched.add((EClass) object);
                searchForContainedEClasses((EClass) object, searched);
            }
        });
    }


    private void searchForContainedEClasses(EClass root, List<EClass> searched){
        root.getEAllReferences().forEach(eReference -> {
            if (eReference.isContainment()){
                this.containedEClasses.addAll(this.getPossibleReplacesFor(eReference.getEReferenceType()));
                this.containedEClasses.add(eReference.getEReferenceType());
            }
            if (searched.contains(eReference.getEReferenceType()))
                return;
            searched.add(eReference.getEReferenceType());
            searchForContainedEClasses(eReference.getEReferenceType(), searched);
        });
    }

    // only those classes could be root that through them all other classes(paths) are accessible.
    public List<EClass> findPossibleRoots(){
        // first gets count of all instantiable eClasses.
        // then it counts all eClasses that are accessible by a given eClass.
        // if two counts are the same, then that given eClass can be root.
        int elementCount = this.eClassCount();
        return this.possibleReplaces
                .values()
                .stream()
                .flatMap(Set::stream)
                .distinct()
                .filter(eClass -> !this.containedEClasses.contains(eClass))
                .filter(eClass -> reachableElementsCountFrom(eClass) == elementCount)
                .collect(Collectors.toList());
    }

    private int eClassCount() {
        return (int)this.possibleReplaces
                .values()
                .stream()
                .flatMap(Set::stream)
                .filter(this::hasRef)
                .distinct()
                .count();
    }

    private boolean hasRef(EClass eClass) {
        return this.resource
                .getContents()
                .get(0)
                .eContents()
                .stream()
                .filter(eObject -> eObject instanceof EClass)
                .map(eObject -> ((EClass)eObject))
                .flatMap(ec -> ec.getEAllReferences().stream())
                .anyMatch(eReference -> eReference.getEReferenceType().equals(eClass));
    }

    private int reachableElementsCountFrom(EClass eClass) {
        Set<EClass> reachable = new HashSet<>();
        reachableElementsFrom(eClass, reachable);
        return (int) reachable.stream().filter(this::hasRef).count();
    }

    private void reachableElementsFrom(EClass eClass, Set<EClass> reachable){
        reachable.add(eClass);
        eClass.getEAllReferences().forEach(eReference -> {
            this.possibleReplaces
                    .get(eReference.getEReferenceType())
                    .stream()
                    .filter(ec -> !reachable.contains(ec))
                    .filter(ec -> !this.getContainedEClasses().contains(ec) || eReference.isContainment())
                    .forEach(ec -> reachableElementsFrom(ec, reachable));
        });
    }

    public Set<EClass> getAllInstantiableEClasses() {
        return this
                .eClassEReferences
                .keySet();
    }

    private void findEClassesERefs(){
        this
            .possibleReplaces
            .values()
            .stream()
            .flatMap(Set::stream)
            .forEach(eClass -> {
                this.eClassEReferences.put(eClass, eClass.getEAllReferences());
            });
    }
}
