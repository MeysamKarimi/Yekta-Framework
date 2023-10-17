package com.dimo.aco;

import com.dimo.model.DiscoveredPath;
import com.dimo.model.Element;
import com.dimo.model.MetaModel;
import com.dimo.model.Model;
import com.dimo.ocl.Checker;
import org.eclipse.emf.common.util.URI;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Ant {

    private MetaModel metaModel;
    private DiscoveredPath discoveredPath;
    private Model model;


    public Ant(MetaModel metaModel, DiscoveredPath initialDistribution) {
        this.metaModel = metaModel;
        this.discoveredPath = initialDistribution;
    }

    public boolean generate(int elementCount, String folderName, Checker checker, BigDecimal alpha, BigDecimal beta){
         this.model = new Model(discoveredPath.possibleRoots());

        for (int  i =0 ; i< elementCount ; i++){
            List<Element> possibleNext = model.possibleNextSteps();
            int next = discoveredPath.chooseNext(possibleNext, alpha, beta);
            if (next == -1)
                break;
            discoveredPath.expand(possibleNext.get(next));
            var result = model.addElementForNextStep(possibleNext.get(next), metaModel, checker);
            if(!result){
                System.out.println("step number " + i + " was unsuccessful. trying again");
                i--;
            }
        }
        model.generateResource(this.metaModel, URI.createURI(folderName + UUID.randomUUID() + ".xmi"));
        return model
                .getModelResource()
                .getContents()
                .stream()
                .map(checker::check)
                .filter(aBoolean -> !aBoolean)
                .findAny().orElse(true);
    }

    public Model getModel(){
        return this.model;
    }
}
