package btrplace.solver.api.cstrSpec.invariant.func;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.invariant.Term;
import btrplace.solver.api.cstrSpec.invariant.type.IntType;
import btrplace.solver.api.cstrSpec.invariant.type.Type;

import java.util.Collection;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class Card extends Function {

    private Term set;

    public Card(List<Term> stack) {
        this.set = stack.get(0);
    }

    @Override
    public Type type() {
        return IntType.getInstance();
    }

    @Override
    public String toString() {
        return new StringBuilder("card(").append(set).append(")").toString();
    }

    @Override
    public Integer eval(Model mo) {
        Collection c = (Collection) set.eval(mo);
        if (c == null) {
            return null;
        }
        return c.size();
    }
}
