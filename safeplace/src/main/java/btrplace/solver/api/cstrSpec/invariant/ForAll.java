package btrplace.solver.api.cstrSpec.invariant;

import btrplace.model.Model;
import btrplace.solver.api.cstrSpec.generator.AllTuplesGenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabien Hermenier
 */
public class ForAll implements Proposition {

    private List<UserVariable> vars;

    private Term from;

    private Proposition prop;

    public ForAll(List<UserVariable> vars, Proposition p) {
        this.vars = vars;
        this.from = vars.get(0).getBackend();
        prop = p;
    }

    @Override
    public Proposition not() {
        return new Exists(vars, prop.not());
        //throw new UnsupportedOperationException();
    }

    @Override
    public Boolean evaluate(Model m) {
        boolean ret = true;
        List<List<Object>> values = new ArrayList<>(vars.size());
        for (int i = 0; i < vars.size(); i++) {
            values.add(new ArrayList<>((Collection<Object>) from.eval(m)));
        }
        AllTuplesGenerator<Object> tg = new AllTuplesGenerator<>(Object.class, values);
        for (Object[] tuple : tg) {
            for (int i = 0; i < tuple.length; i++) {
                vars.get(i).set(tuple[i]);
            }
            Boolean r = prop.evaluate(m);
            if (r == null) {
                return null;
            }
            ret &= r;
        }
        for (Var v : vars) {
            v.unset();
        }
        return ret;
    }

    public String toString() {
        StringBuilder b = new StringBuilder("!(");
        Iterator<UserVariable> ite = vars.iterator();
        while (ite.hasNext()) {
            Var v = ite.next();
            if (ite.hasNext()) {
                b.append(v.label());
                b.append(",");
            } else {
                b.append(v.pretty());
            }
        }
        return b.append(") ").append(prop).toString();
    }
}
