package com.dimo.gwo;

import com.dimo.AlgorithmUI;
import com.dimo.model.DiscoveredPath;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import com.dimo.objectives.ExternalDivObjective;
import com.dimo.objectives.ModelRanker;
import com.dimo.ocl.Checker;
import org.eclipse.emf.ecore.EClass;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GWO {
    private final DiscoveredPath discoveredPath;
    private final MetaModel metaModel;
    private final Checker checker;
    private final ModelRanker modelRanker;
    private BigDecimal internalDiversityWeight;
    private double externalDiversityWeight;
    private List<Model> bestModels = new ArrayList<>();
    private List<Wolf> bestWolves = new ArrayList<>();
    private final String folderName;
    private final int elementCount;
    private final int rounds;
    private final int populationCount;
    private final int modelCount;
    private ExternalDivObjective externalDivObjective;
    private double A;
    private double decrementValueForA;

    public GWO(MetaModel metaModel, DiscoveredPath discoveredPath, int elementCount, int rounds, int populationCount,
               int modelCount, BigDecimal internalDiversityWeight, double externalDiversityWeight) {
        this.discoveredPath = discoveredPath;
        this.metaModel = metaModel;
        this.modelCount = modelCount;
        this.populationCount = populationCount;
        this.rounds = rounds;
        this.elementCount = elementCount;
        this.modelRanker = new ModelRanker(metaModel);
        this.internalDiversityWeight = internalDiversityWeight;
        this.externalDiversityWeight = externalDiversityWeight;
        this.folderName = "models/" + (System.currentTimeMillis() / 1000) % 1000000 + "/";
        this.checker = new Checker(metaModel.getOclConstraints());

        externalDivObjective = new ExternalDivObjective();
        A = 2;
        decrementValueForA = A / rounds;
    }


    public void run() {
        AlgorithmUI algorithmUI = AlgorithmUI.getInstance();

        List<Wolf> wolves = createPopulation();
        IntStream
                .range(0, this.rounds)
                .peek(i -> AlgorithmUI.displayReport("Executing Iteration: " + (i+1), AlgorithmUI.ReportType.INFO))
                .forEach(i -> runOneIteration(wolves));
        //bestWolves.forEach(model -> {
        try {
            var models = bestWolves.get(0).getSolution();
            for (int i = 0; i < models.size(); i++) {
                models.get(i).getModelResource().save(null);
                //model.getModelResource().save(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        });
    }

    private void runOneIteration(List<Wolf> wolves){
        generateModels(wolves);
        List<Wolf> currentPopulation = combineGeneratedWolvesWithBests(wolves); //New wolves + 3 best from last iteration
        List<Wolf> sortedWolves = modelRanker.rankSolutions(currentPopulation);
        selectBests(sortedWolves); ////Save Alpha, Beta and Delta
        updatePositionOfWolves(sortedWolves);
        updateValueOfA();
    }

    private void updateValueOfA() {
        A = A - decrementValueForA;
    }

    private void updatePositionOfWolves(List<Wolf> wolves) {
        var alpha =  wolves.get(0);
        var beta =  wolves.get(1);
        var delta = wolves.get(2);

        for (int i = 3; i < wolves.size(); i++) { //we don't update alpha, beta and delta position
            wolves.get(i).ResetObjectiveMapper();

            for (int j = 0; j < this.modelCount; j++) {
                // Calculate distance of each wolf to Alpha, Beta and Delta
                double symmetricDifferenceToAlpha = calculateSymmetricDifference(wolves.get(i).getSolution().get(j), alpha.getSolution().get(j));
                double symmetricDifferenceToBeta = calculateSymmetricDifference(wolves.get(i).getSolution().get(j), beta.getSolution().get(j));
                double symmetricDifferenceToDelta = calculateSymmetricDifference(wolves.get(i).getSolution().get(j), delta.getSolution().get(j));

                // Get X(t+1) for each wolf
                double nextPositionBasedOnAlpha = calculateNextPosition(wolves.get(i).getFitnessFunction(), symmetricDifferenceToAlpha);
                double nextPositionBasedOnBeta = calculateNextPosition(wolves.get(i).getFitnessFunction(), symmetricDifferenceToBeta);
                double nextPositionBasedOnDelta = calculateNextPosition(wolves.get(i).getFitnessFunction(), symmetricDifferenceToDelta);

                double nextpositionValue = (nextPositionBasedOnAlpha + nextPositionBasedOnBeta + nextPositionBasedOnDelta) / 3;
                double nextPosition = Math.tanh(nextpositionValue);

                // set the vector who should be replaced in next iteration
                if(nextPosition > Math.random())
                    wolves.get(i).UpdateObjectiveMapperPosition(j, true);
            }
        }
    }

    private double calculateNextPosition(double fitnessValue, double  symmetricDifferenceToAlpha) {
        return fitnessValue - (A * symmetricDifferenceToAlpha);
    }

    private double calculateSymmetricDifference(Model source, Model target) {
        int allRefsCount = metaModel.getAllInstantiableEClasses().stream().map(EClass::getEAllReferences).map(List::size).reduce(Integer::sum).orElse(0);
        double distance = externalDivObjective.compare(source.getModelResource(), target.getModelResource())
                / allRefsCount;

        var C = Math.random();
        return Math.abs(C * distance);
    }

    private void selectBests(List<Wolf> combinedWolves) {
        this.bestWolves = combinedWolves.subList(0, 3); // Alpha, Beta and Delta
    }


    private List<Wolf> combineGeneratedWolvesWithBests(List<Wolf> wolves) {
        return Stream
                .concat(bestWolves.stream(), wolves.stream())
                .collect(Collectors.toList());
    }

    private void generateModels(List<Wolf> wolves) {
        wolves.forEach(wolf -> {
            while (!wolf.generate(this.elementCount , this.folderName, checker)){
                System.out.println("wolf number " + wolves.indexOf(wolf) + " was unsuccessful to create models. trying again!");
            }
            wolf.setFitnessFunction();
        });
    }

    private List<Wolf> createPopulation() {
        return IntStream
                .range(0, this.populationCount)
                .mapToObj(i -> new Wolf(this.modelCount, this.metaModel, this.discoveredPath,
                        internalDiversityWeight, externalDiversityWeight))
                .collect(Collectors.toList());
    }
}
