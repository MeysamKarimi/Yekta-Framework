package com.dimo.model;

import com.dimo.model.Element;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class DiscoveredPath {
    private final Map<EClass, Element> roots;
    private final MetaModel metaModel;
    private final BigDecimal defaultImpact = BigDecimal.ONE;

    public DiscoveredPath(MetaModel metaModel) {
        this.metaModel = metaModel;
        this.roots = new HashMap<>();
        List<EClass> eClasses = metaModel.findPossibleRoots();
        eClasses.forEach(eClass -> {
                List<EClass> replaces = metaModel.getPossibleReplacesFor(eClass);
                boolean isContained = this.metaModel.getContainedEClasses().contains(eClass);
                Element element;
                if(replaces.size() == 1)
                    element = Element.createConcreteRoot(this.defaultImpact,  eClass, isContained);
                else
                    element = Element.createAbstractRoot(this.defaultImpact, eClass, isContained, replaces);
                roots.put(eClass, element);
            });
    }


    /**
     * when a new element gets added to a model for the first time, it's open up new paths.
     * this method expand the discovered paths with those new paths.
     * @param element selected element by a model
     */
    public void expand(Element element) {

        // this means that this element is not a new path. so we don't need to do anything
        if (element.getChildren().size() != 0)
            return;

        // if parent is a many(list) then when element is the last element in list,
        // we need to add a new element to list(expand list)
        addedNewSiblingIfNeeded(element);

        // here we add element's children to the discovered path.
        addedChildrenToDiscoveredPath(element);
    }

    private void addedChildrenToDiscoveredPath(Element element) {
        for (int i = 0 ;i < element.getEClass().getEAllReferences().size(); i++){
            EReference eReference = element.getEClass().getEAllReferences().get(i);
            EClass eClass = eReference.getEReferenceType();
            boolean isContained = this.metaModel.getContainedEClasses().contains(eClass);
            List<EClass> replaces = this.metaModel.getPossibleReplacesFor(eClass);
            isContained = isContained || replaces.stream().anyMatch(metaModel.getContainedEClasses()::contains);
            if(!eReference.isMany()){
                Element child = replaces.size() == 1 ?
                    Element.createConcreteChild(this.defaultImpact, eClass, element, isContained):
                    Element.createAbstractChild(this.defaultImpact, eClass, element, isContained, replaces);
                element.getChildren().add(child);
            }else {
                Element pointer = Element.createPointer(element, isContained);
                Element child;
                if(replaces.size() == 1)
                    child = Element.createConcreteChild(this.defaultImpact, eClass, pointer, isContained);
                else
                    child = Element.createAbstractChild(this.defaultImpact,eClass, pointer, isContained, replaces);
                pointer.getChildren().add(child);
                element.getChildren().add(pointer);
            }
        }
    }

    private void addedNewSiblingIfNeeded(Element element) {
        if(element.getParent() != null){
            if(element.getParent().isPointer()){
                int index = element.getParent().getChildren().indexOf(element);
                if(index == element.getParent().getChildren().size() -1){
                    Element sibling= Element.createConcreteChild(this.defaultImpact, element.getEClass(), element.getParent(), element.isContained());
                    element.getParent().getChildren().add(sibling);
            }
        }else if(element.getParent().getParent()!=null && element.getParent().getParent().isPointer()) {
                int index = element.getParent().getParent().getChildren().indexOf(element.getParent());
                if (index == element.getParent().getParent().getChildren().size() - 1) {
                    List<EClass> replaces = element
                            .getParent()
                            .getChildren()
                            .stream()
                            .map(Element::getEClass)
                            .collect(Collectors.toList());
                    Element sibling = Element.createAbstractChild(this.defaultImpact,null, element.getParent().getParent(), element.isContained(), replaces);
                    element.getParent().getParent().getChildren().add(sibling);
                }

            }
    }
    }

    /**
     * choose one of the elements as next based on pheromone info that it contains.
     * @param possibleNext list of possible elements as next for a model
     * @return selected index in provided list
     */
    public int chooseNextOld(List<Element> possibleNext){
        List<BigDecimal> probabilities = new ArrayList<>();

        possibleNext
                .stream()
                .map(Element::getPheromone)
                .forEach(ph -> probabilities.add(BigDecimal.valueOf(Math.random())));
        return rouletteWheel(probabilities);
    }

    public int chooseNext(List<Element> possibleNext, BigDecimal alpha, BigDecimal beta){
        List<BigDecimal> probabilities = new ArrayList<>();
        BigDecimal sumOfExpressions = BigDecimal.ZERO;

        for (Element element : possibleNext) {
            BigDecimal pheromonePowered = pow(element.getPheromone(), alpha);
            BigDecimal randomPowered = pow(BigDecimal.valueOf(Math.random()), beta);
            BigDecimal expression = pheromonePowered.add(randomPowered);
            probabilities.add(expression);
            sumOfExpressions = sumOfExpressions.add(expression);
        }

        for (int i = 0; i < probabilities.size(); i++) {
            BigDecimal probability = probabilities.get(i);
            probability = probability.divide(sumOfExpressions, RoundingMode.HALF_UP);
            probabilities.set(i, probability);
        }
        return rouletteWheel(probabilities);
    }

    private BigDecimal pow(BigDecimal base, BigDecimal exponent) {
        BigDecimal result = BigDecimal.ZERO;
        int signOf2 = exponent.signum();

        // Perform X^(A+B)=X^A*X^B (B = remainder)
        double dn1 = base.doubleValue();
        // Compare the same row of digits according to context
        BigDecimal n2 = exponent.multiply(new BigDecimal(signOf2)); // n2 is now positive
        BigDecimal remainderOf2 = n2.remainder(BigDecimal.ONE);
        BigDecimal n2IntPart = n2.subtract(remainderOf2);
        // Calculate big part of the power using context -
        // bigger range and performance but lower accuracy
        BigDecimal intPow = base.pow(n2IntPart.intValueExact());
        BigDecimal doublePow = new BigDecimal(Math.pow(dn1, remainderOf2.doubleValue()));
        result = intPow.multiply(doublePow);

        // Fix negative power
        if (signOf2 == -1)
            result = BigDecimal.ONE.divide(result, RoundingMode.HALF_UP);
        return result;
    }

    public int rouletteWheel(List<BigDecimal> probabilities){
        BigDecimal rand = BigDecimal.valueOf(Math.random());
        for(int i = 0 ;i < probabilities.size(); i++){
            rand = rand.subtract(probabilities.get(i));
            if(rand.compareTo(BigDecimal.ZERO) < 0)
                return i;
        }
        return probabilities.size() - 1;
    }

    public List<Element> possibleRoots(){
        return roots
                .values()
                .stream()
                .map(element -> {
                    if (element.isConcrete())
                        return List.of(element);
                    else
                        return element.getChildren();})
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public void updatePheromone(Model model, BigDecimal pheromoneIncreaseAmount) {
        model.getModelElements().forEach(element -> element.setPheromone(element.getPheromone().add(pheromoneIncreaseAmount)));
    }

    public void evaporate(BigDecimal rho) {
        this.roots
                .values()
                .forEach(element -> {
                    if(!element.isPointer() && element.isConcrete()){
                        element.setPheromone(element.getPheromone().multiply(BigDecimal.ONE.subtract(rho)));
                    }
                });
    }
}
