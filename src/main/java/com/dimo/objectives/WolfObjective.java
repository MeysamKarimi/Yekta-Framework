package com.dimo.objectives;

        import com.dimo.gwo.Wolf;
        import com.dimo.model.Model;
        import lombok.Builder;
        import lombok.Getter;
        import lombok.Setter;

        import java.math.BigDecimal;

@Getter
@Setter
public class WolfObjective {

    private Wolf wolf;
    private BigDecimal fitnessValue;

    public WolfObjective(Wolf wolf) {
        this.wolf = wolf;
    }
}