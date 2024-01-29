package com.dimo.objectives;

import com.dimo.gwo.Wolf;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import org.eclipse.emf.ecore.EClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ModelRanker {


    private ExternalDivObjective externalDivObjective;
    private InternalDivObjective internalDivObjective;
    private MetaModel metaModel;

    public ModelRanker(MetaModel metaModel) {
        this.metaModel = metaModel;
        internalDivObjective = new InternalDivObjective();
        externalDivObjective = new ExternalDivObjective();
    }

    // Todo change this impl
    public List<Model> rankModels(List<Model> models) {
        List<ModelObjective> objectives = models
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
        objectives.sort(Comparator.comparing(ModelObjective::getExternalDiv).reversed());
        objectives.sort(Comparator.comparing(ModelObjective::getInternalDiv).reversed());
        System.out.println("Internal Diversity:" + objectives.get(0).getInternalDiv());
        System.out.println("External Diversity:" + objectives.get(0).getExternalDiv());
        //return objectives.stream().map(ModelObjective::getModel).collect(Collectors.toList());

        int maxExternalDiv = getMaxExternalDiv(objectives);
        List<Model> updatedModels = objectives.stream()
                .peek(objective -> {
                    // Update the value of Model based on the properties of objective
                    // For example:
                    Model model = objective.getModel();
//                    int aa = model.getModelResource().getContents().size();
//                    int allRefsCount = metaModel.getAllInstantiableEClasses().stream().map(EClass::getEAllReferences).map(List::size).reduce(Integer::sum).orElse(0);
                    BigDecimal sum = objective.getInternalDiv().add(BigDecimal.valueOf(objective.getExternalDiv()).divide(BigDecimal.valueOf(maxExternalDiv), 5, RoundingMode.UP));
                    model.setFintnessValue(sum.divide(BigDecimal.valueOf(2)));
                    System.out.println("Fitness function: " + sum.divide(BigDecimal.valueOf(2)));
                })
                .map(ModelObjective::getModel)
                .collect(Collectors.toList());

        return updatedModels;

    }

    public List<Wolf> rankSolutions(List<Wolf> wolves) {
        List<Wolf> sortedWolves = wolves.stream()
                .sorted(Comparator.comparing(Wolf::getFitnessFunction).reversed())
                .collect(Collectors.toList());

        System.out.println("fintness value(first):" + sortedWolves.get(0).getFitnessFunction());
        System.out.println("fintness value(last):" + sortedWolves.get(sortedWolves.size() - 1).getFitnessFunction());
        return sortedWolves;
    }

    public static int getMaxExternalDiv(List<ModelObjective> modelObjectives) {
        return modelObjectives.stream()
                .mapToInt(ModelObjective::getExternalDiv)
                .max()
                .orElse(Integer.MIN_VALUE);
    }
}
