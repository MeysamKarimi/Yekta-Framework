package com.dimo.objectives;

import com.dimo.model.Model;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ModelObjective {

    private Model model;
    private int externalDiv;
    private BigDecimal internalDiv;

    public ModelObjective(Model model) {
        this.model = model;
    }
}
