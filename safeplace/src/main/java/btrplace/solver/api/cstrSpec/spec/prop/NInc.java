package btrplace.solver.api.cstrSpec.spec.prop;

import btrplace.solver.api.cstrSpec.spec.term.Term;
import btrplace.solver.api.cstrSpec.verification.spec.SpecModel;

import java.util.Collection;

/**
 * @author Fabien Hermenier
 */
public class NInc extends AtomicProp {

    public NInc(Term a, Term b) {
        super(a, b, "/<:");
    }

    @Override
    public AtomicProp not() {
        return new Inc(a, b);
    }

    @Override
    public Boolean eval(SpecModel m) {
        Collection cA = (Collection) a.eval(m);
        Collection cB = (Collection) b.eval(m);
        if (cB == null) {
            return null;
        }
        return !cB.containsAll(cA);
    }
}
