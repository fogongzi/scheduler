package btrplace.solver.api.cstrSpec.invariant.type;

import btrplace.model.Model;
import btrplace.model.Node;
import btrplace.solver.api.cstrSpec.invariant.Constant;

import java.util.Set;

/**
 * @author Fabien Hermenier
 */
public class NodeType extends Atomic {

    private static NodeType instance = new NodeType();


    private NodeType() {
    }

    public static NodeType getInstance() {
        return instance;
    }

    @Override
    public String toString() {
        return label();
    }

    @Override
    public boolean match(String n) {
        return false;
    }

    @Override
    public String label() {
        return "node";
    }

    @Override
    public Set<Node> domain(Model mo) {
        return mo.getMapping().getAllNodes();
    }

    @Override
    public Constant newValue(String n) {
        throw new UnsupportedOperationException();
    }
}
