package btrplace.solver.choco;

import btrplace.model.DefaultModel;
import btrplace.model.Mapping;
import btrplace.model.Model;
import btrplace.model.view.ShareableResource;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.SolverException;
import btrplace.solver.choco.chocoUtil.ChocoUtils;
import btrplace.solver.choco.objective.minMTTR.MinMTTR;
import btrplace.test.PremadeElements;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.ElementV;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.integer.IntExp;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests related to the opened issues
 *
 * @author Fabien Hermenier
 */
public class Issues implements PremadeElements {

    /**
     * Another test related to issue #5.
     *
     * @throws SolverException
     */
    @Test
    public void testIssue5a() throws SolverException, ContradictionException {

        ShareableResource resources = new ShareableResource("vcpu", 1);
        resources.set(n1, 2);
        resources.set(n2, 2);

        Mapping map = new MappingBuilder().on(n1, n2).off(n3).run(n1, vm1, vm2).build();

        Model model = new DefaultModel(map);
        model.attach(resources);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .labelVariables()
                .build();

        CPSolver solver = rp.getSolver();
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();

        int NUMBER_OF_NODE = map.getAllNodes().size();

        // Each element is the number of VMs on each node
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_NODE];

        IntDomainVar[] busy = new IntDomainVar[NUMBER_OF_NODE];

        ChocoLogging.setVerbosity(Verbosity.SEARCH);
        //rp.getEnd().setSup(10);
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        for (UUID n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = solver.createBoundIntVar("nVMs", -1, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is -1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{solver.makeConstantIntVar(-1), VMsOnAllNodes[rp.getNode(n)],
                    state, vmsOnInvolvedNodes[i]};
            solver.post(new ElementV(c, 0, solver.getEnvironment()));

            // IF the node is online and hosting VMs -> busy = 1.
            busy[i] = solver.createBooleanVar("busy" + n);
            ChocoUtils.postIfOnlyIf(solver, busy[i], solver.geq(vmsOnInvolvedNodes[i], 1));
            i++;
        }

        // idle is equals the number of vmsOnInvolvedNodes with value 0. (The node without VM)
        IntDomainVar idle = solver.createBoundIntVar("Nidles", 0, NUMBER_OF_NODE);
        solver.post(solver.occurence(vmsOnInvolvedNodes, idle, 0));
        // idle should be less than Amount for MaxSN (0, in this case)
        solver.post(solver.leq(idle, 0));

        // Extract all the state of the involved nodes (all nodes in this case)
        IntDomainVar[] states = new IntDomainVar[NUMBER_OF_NODE];
        int j = 0;
        for (UUID n : map.getAllNodes()) {
            states[j++] = rp.getNodeAction(n).getState();
        }

        // In case the number of VMs is inferior to the number of online nodes, some nodes have to shutdown
        // to satisfy the constraint. This could be express as:
        // The addition of the idle nodes and busy nodes should be equals the number of online nodes.
        IntExp sumStates = (solver.sum(states));
        IntExp sumIB = solver.plus(solver.sum(busy), idle);
        solver.post(solver.eq(sumStates, sumIB));

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    /**
     * Test a suspicious bug in issue #5
     */
    @Test
    public void testIssue5b() throws SolverException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4).build();

        Model model = new DefaultModel(map);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .labelVariables()
                .build();

        IntDomainVar[] nodes_state = rp.getNbRunningVMs();
        IntDomainVar[] nodeVM = new IntDomainVar[map.getAllNodes().size()];

        int i = 0;

        for (UUID n : map.getAllNodes()) {
            nodeVM[i++] = nodes_state[rp.getNode(n)];
        }
        CPSolver solver = rp.getSolver();
        IntDomainVar idle = solver.createBoundIntVar("Nidles", 0, map.getAllNodes().size());

        solver.post(solver.occurence(nodeVM, idle, 0));
        // Amount of maxSpareNode =  1
        solver.post(solver.leq(idle, 1));

        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    /**
     * Test a suspicious bug in issue #5
     */
    @Test
    public void testIssue5c() throws SolverException, ContradictionException {

        Mapping map = new MappingBuilder().on(n1, n2, n3)
                .run(n2, vm1, vm2, vm3, vm4).build();

        Model model = new DefaultModel(map);

        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model)
                .labelVariables()
                .build();

        IntDomainVar[] nodes_state = rp.getNbRunningVMs();
        IntDomainVar[] nodeVM = new IntDomainVar[map.getAllNodes().size()];

        int i = 0;

        for (UUID n : map.getAllNodes()) {
            nodeVM[i++] = nodes_state[rp.getNode(n)];
            //rp.getNodeAction(n).getState().setVal(1);
        }
        CPSolver solver = rp.getSolver();
        IntDomainVar idle = solver.createBoundIntVar("Nidles", 0, map.getAllNodes().size());

        solver.post(solver.occurence(nodeVM, idle, 0));
        solver.post(solver.leq(idle, 1));
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
    }

    @Test
    public void testIssue10() throws SolverException, ContradictionException {
        //ShareableResource resources = new ShareableResource("vcpu", 1);
        //resources.set(n1, 2);
        //resources.set(n2, 2);
        Mapping map = new MappingBuilder().on(n1, n2).off(n3).run(n1, vm1, vm2).build();
        Model model = new DefaultModel(map);
        //model.attach(resources);
        ReconfigurationProblem rp = new DefaultReconfigurationProblemBuilder(model).labelVariables().build();
        CPSolver solver = rp.getSolver();
        rp.getNodeAction(n3).getState().setVal(1);  // n3 goes online
        solver.post(solver.leq(rp.getEnd(), 10));
        int NUMBER_OF_NODE = map.getAllNodes().size();
        // Extract all the state of the involved nodes (all nodes in this case)
        IntDomainVar[] VMsOnAllNodes = rp.getNbRunningVMs();
        // Each element is the number of VMs on each node
        IntDomainVar[] vmsOnInvolvedNodes = new IntDomainVar[NUMBER_OF_NODE];
        IntDomainVar[] idles = new IntDomainVar[NUMBER_OF_NODE];
        int i = 0;
        int maxVMs = rp.getSourceModel().getMapping().getAllVMs().size();
        List<SConstraint> elms = new ArrayList<SConstraint>();
        for (UUID n : map.getAllNodes()) {
            vmsOnInvolvedNodes[i] = solver.createBoundIntVar("nVMs"+n, -1, maxVMs);
            IntDomainVar state = rp.getNodeAction(n).getState();
            // If the node is offline -> the temporary variable is 1, otherwise, it equals the number of VMs on that node
            IntDomainVar[] c = new IntDomainVar[]{solver.makeConstantIntVar(-1), VMsOnAllNodes[rp.getNode(n)],
                    state, vmsOnInvolvedNodes[i]};
            SConstraint elem = new ElementV(c, 0, solver.getEnvironment());
            elms.add(elem);
            solver.post(elem);
            // IF number of VMs on a node is 0 -> Idle
            idles[i] = solver.createBooleanVar("idle" + n);
            ChocoUtils.postIfOnlyIf(solver, idles[i], solver.eq(vmsOnInvolvedNodes[i], 0));
            i++;
        }
        IntExp Sidle = solver.sum(idles);
        // idle should be less than Amount for MaxSN (0, in this case)
        solver.post(solver.eq(Sidle, 0));
        System.err.flush();
        MinMTTR obj = new MinMTTR();
        obj.inject(rp);
        //System.err.println(solver.pretty());
        ChocoLogging.setVerbosity(Verbosity.SEARCH);
        ChocoLogging.setLoggingMaxDepth(100);
        ReconfigurationPlan plan = rp.solve(0, false);
        Assert.assertNotNull(plan);
        System.out.println(plan);
        System.out.println(plan.getResult());

    }
}
