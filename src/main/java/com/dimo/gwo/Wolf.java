package com.dimo.gwo;

import com.dimo.model.DiscoveredPath;
import com.dimo.model.Element;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import com.dimo.objectives.ExternalDivObjective;
import com.dimo.objectives.InternalDivObjective;
import com.dimo.objectives.ModelObjective;
import com.dimo.ocl.Checker;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

public class Wolf {

    private MetaModel metaModel;
    private DiscoveredPath discoveredPath;
    private int modelCount;
    private List<Model> solutionVector;
    private BigDecimal internalDiversityWeight;
    private double externalDiversityWeight;
    private ArrayList<Boolean> objectiveMapper;

//    private BigDecimal fitnessValue;
    private double fitnessValue;

    private ExternalDivObjective externalDivObjective;
    private InternalDivObjective internalDivObjective;


    public Wolf(int modelCount, MetaModel metaModel, DiscoveredPath initialDistribution,
                BigDecimal internalDiversityWeight, double externalDiversityWeight) {
        this.modelCount = modelCount;
        this.metaModel = metaModel;
        this.discoveredPath = initialDistribution;
        solutionVector = new ArrayList<>(modelCount);
        this.internalDiversityWeight = internalDiversityWeight;
        this.externalDiversityWeight = externalDiversityWeight;

        this.objectiveMapper = new ArrayList<>(modelCount);
        for (int i = 0; i < modelCount; i++) {
            objectiveMapper.add(true);
        }

        internalDivObjective = new InternalDivObjective();
        externalDivObjective = new ExternalDivObjective();
//        this.fitnessValue = BigDecimal.valueOf(0);
        this.fitnessValue = 0;
    }

    public boolean generate(int elementCount, String folderName, Checker checker){
        boolean returnValue = true;
        for (int k = 0; k < this.modelCount; k++) {
            if (objectiveMapper.get(k) == false) continue;

            Model model = new Model(discoveredPath.possibleRoots());

            for (int i = 0; i < elementCount; i++) {
                List<Element> possibleNext = model.possibleNextSteps();
                int next = discoveredPath.chooseNext(possibleNext, BigDecimal.ZERO,  BigDecimal.ONE);
                if (next == -1)
                    break;
                discoveredPath.expand(possibleNext.get(next));
                var result = model.addElementForNextStep(possibleNext.get(next), metaModel, checker);
                if (!result) {
                    System.out.println("step number " + i + " was unsuccessful. trying again");
                    i--;
                }
            }
            model.generateResource(this.metaModel, URI.createURI(folderName + UUID.randomUUID() + ".xmi"));

            if (solutionVector.size() < modelCount)
                solutionVector.add(model);
            else
                solutionVector.set(k, model);

            returnValue = returnValue && model
                    .getModelResource()
                    .getContents()
                    .stream()
                    .map(checker::check)
                    .filter(aBoolean -> !aBoolean)
                    .findAny().orElse(true);
        }

        return returnValue;
    }

    public List<Model> getSolution(){
        return this.solutionVector;
    }

    public void setFitnessFunction()
    {
        List<ModelObjective> objectives = solutionVector
                .stream()
                .map(ModelObjective::new)
                .collect(Collectors.toList());
        objectives.forEach(objective -> objective.setInternalDiv(internalDivObjective.score(objective.getModel(), metaModel)));

        //Todo optimize this
        objectives.forEach(objective -> objective.setExternalDiv(
                objectives
                        .stream()
                        .map(m -> externalDivObjective.compare(objective.getModel().getModelResource(), m.getModel().getModelResource()))
                        .reduce(Integer::sum)
                        .orElse(0)
        ));

        BigDecimal internalSum = BigDecimal.valueOf(0);
        //double externalSum = BigDecimal.valueOf(0);
        double externalSum = 0;
        int allRefsCount = metaModel.getAllInstantiableEClasses().stream().map(EClass::getEAllReferences).map(List::size).reduce(Integer::sum).orElse(0);

        for (int i = 0; i < objectives.size(); i++) {
            internalSum = internalSum.add(objectives.get(i).getInternalDiv());
//            externalSum = externalSum.add(BigDecimal.valueOf(objectives.get(i).getExternalDiv() / allRefsCount));
            externalSum = externalSum + objectives.get(i).getExternalDiv() / allRefsCount;
        }
        internalSum = internalSum.divide(BigDecimal.valueOf(objectives.size()), RoundingMode.HALF_UP);
//        externalSum = externalSum.divide(BigDecimal.valueOf(allRefsCount));
        externalSum = externalSum / allRefsCount;

//        fitnessValue = (internalSum.multiply(internalDiversityWeight)).add(externalSum.multiply(externalDiversityWeight));
        fitnessValue = internalSum.multiply(internalDiversityWeight).doubleValue() + (externalSum * externalDiversityWeight);
    }

    public double getFitnessFunction() {
        return this.fitnessValue;
    }

    public void UpdateObjectiveMapperPosition(int index, boolean value){
        objectiveMapper.set(index, value);
    }

    public void ResetObjectiveMapper(){
        Collections.fill(objectiveMapper, Boolean.FALSE);
    }
}
