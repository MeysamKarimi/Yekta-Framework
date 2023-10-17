package com.dimo.aco;

import com.dimo.AlgorithmUI;
import com.dimo.model.DiscoveredPath;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import com.dimo.objectives.ModelRanker;
import com.dimo.ocl.Checker;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ACO {
    private final DiscoveredPath discoveredPath;
    private final MetaModel metaModel;
    private final Checker checker;
    private final ModelRanker modelRanker;
    private List<Model> bestModels = new ArrayList<>();
    private final String folderName;
    private final int elementCount;
    private final int rounds;
    private final int populationCount;
    private final int modelCount;

    private final BigDecimal alpha;
    private final BigDecimal beta;

    private final BigDecimal q;
    private final BigDecimal rho;

    private final String selectedAlgorithm;


    public ACO(MetaModel metaModel, DiscoveredPath discoveredPath, int elementCount, int rounds, int populationCount, int modelCount, BigDecimal q, BigDecimal tau, BigDecimal alpha, BigDecimal beta, String selectedAlgorithm) {
        this.discoveredPath = discoveredPath;
        this.metaModel = metaModel;
        this.modelCount = modelCount;
        this.populationCount = populationCount;
        this.rounds = rounds;
        this.elementCount = elementCount;
        this.q = q;
        this.rho = tau;
        this.alpha = alpha;
        this.beta = beta;
        this.modelRanker = new ModelRanker(metaModel);
        this.folderName = "models/" + (System.currentTimeMillis() / 1000) % 1000000 + "/";
        this.checker = new Checker(metaModel.getOclConstraints());
        this.selectedAlgorithm = selectedAlgorithm;
    }



    public void run(){
        AlgorithmUI algorithmUI = AlgorithmUI.getInstance();

        List<Ant> ants = createPopulation();
        IntStream
                .range(0, this.rounds)
                .peek(i -> AlgorithmUI.displayReport("Executing Iteration: " + (i+1), AlgorithmUI.ReportType.INFO))
                .forEach(i -> runOneIteration(ants));
        bestModels.forEach(model -> {
            try {
                model.getModelResource().save(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void runOneIteration(List<Ant> ants){
        generateModels(ants);
        List<Model> models = combineGeneratedModelWithBests(ants);
        if(selectedAlgorithm == "ACO") {
            models = modelRanker.rankModels(models);
            for (int i = 0; i < models.size(); i++) {
//            discoveredPath.updatePheromone(models.get(i), q.divide(new BigDecimal( i + 1), 5, RoundingMode.UP));
                discoveredPath.updatePheromone(models.get(i), models.get(i).getFintnessValue());
            }
            discoveredPath.evaporate(this.rho);
        }
        selectBests(models);
    }

    private void selectBests(List<Model> combinedModels) {
        this.bestModels = combinedModels.subList(0, this.modelCount);
    }


    private List<Model> combineGeneratedModelWithBests(List<Ant> ants) {
        return Stream
                .concat(bestModels.stream(), ants.stream().map(Ant::getModel))
                .collect(Collectors.toList());
    }

    private void generateModels(List<Ant> ants) {
        ants.forEach(ant -> {
            while (!ant.generate(this.elementCount , this.folderName, checker, alpha, beta)){
                System.out.println("ant number " + ants.indexOf(ant) + " was unsuccessful to create an model. trying again!");
            }
        });
    }

    private List<Ant> createPopulation() {
        return IntStream
                .range(0, this.populationCount)
                .mapToObj(i -> new Ant(this.metaModel, this.discoveredPath))
                .collect(Collectors.toList());
    }
}
