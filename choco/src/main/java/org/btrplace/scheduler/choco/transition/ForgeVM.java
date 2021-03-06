/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.transition;

import org.btrplace.model.VM;
import org.btrplace.model.VMState;
import org.btrplace.plan.ReconfigurationPlan;
import org.btrplace.scheduler.SchedulerException;
import org.btrplace.scheduler.choco.ReconfigurationProblem;
import org.btrplace.scheduler.choco.Slice;
import org.btrplace.scheduler.choco.extensions.TaskMonitor;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;


/**
 * Model a transition that will forge a VM to put it into the ready state.
 * <p>
 * The VM must have an attribute (provided by {@link org.btrplace.model.Model#getAttributes()}
 * {@code template} that indicate the template identifier to use to build the VM image.
 * <p>
 * An estimation of the action duration must be provided through a
 * {@link org.btrplace.scheduler.choco.duration.ActionDurationEvaluator} accessible from
 * {@link org.btrplace.scheduler.choco.ReconfigurationProblem#getDurationEvaluators()} with the key {@code ForgeVM.class}
 * <p>
 * If the reconfiguration problem has a solution, a {@link org.btrplace.plan.event.ForgeVM} action
 * will inserted into the resulting reconfiguration plan.
 *
 * @author Fabien Hermenier
 */
public class ForgeVM implements VMTransition {

    /**
     * The prefix to use for the variables
     */
    public static final String VAR_PREFIX = "forge";


    private VM vm;

    private IntVar duration;

    private BoolVar state;

    private String template;

    private IntVar start;

    private IntVar end;
    /**
     * Make a new model.
     *
     * @param rp the RP to use as a basis.
     * @param e  the VM managed by the action
     * @throws org.btrplace.scheduler.SchedulerException if an error occurred
     */
    public ForgeVM(ReconfigurationProblem rp, VM e) throws SchedulerException {
        int d = rp.getDurationEvaluators().evaluate(rp.getSourceModel(), org.btrplace.plan.event.ForgeVM.class, e);
        template = rp.getSourceModel().getAttributes().get(e, "template", "");
        if ("".equals(template)) {
            throw new SchedulerException(rp.getSourceModel(), "Unable to forge the VM '" + e + "'. The required attribute 'template' is missing from the model");
        }
        Solver s = rp.getSolver();
        duration = VariableFactory.fixed(d, s);
        state = VariableFactory.zero(s);
        vm = e;

        /*
         * We don't make any "real" d-slice cause it may impacts the TaskScheduler
         * so the hosting variable is set to -1 to be sure the VM is not hosted on a node
         */

        start = rp.makeUnboundedDuration(VAR_PREFIX, "(", e, ").start");
        end = rp.makeUnboundedDuration(VAR_PREFIX, "(", e, ").stop");
        TaskMonitor.build(start, duration, end);
        s.post(IntConstraintFactory.arithm(duration, ">=", d));
        s.post(IntConstraintFactory.arithm(end, "<=", rp.getEnd()));
    }

    @Override
    public boolean insertActions(Solution s, ReconfigurationPlan plan) {
        org.btrplace.plan.event.ForgeVM a = new org.btrplace.plan.event.ForgeVM(vm, s.getIntVal(getStart()), s.getIntVal(getEnd()));
        return plan.add(a);
    }

    @Override
    public VM getVM() {
        return vm;
    }

    @Override
    public IntVar getStart() {
        return start;
    }

    @Override
    public IntVar getEnd() {
        return end;
    }

    @Override
    public IntVar getDuration() {
        return duration;
    }

    @Override
    public Slice getCSlice() {
        return null;
    }

    @Override
    public Slice getDSlice() {
        return null;
    }

    @Override
    public BoolVar getState() {
        return state;
    }

    @Override
    public boolean isManaged() {
        return true;
    }

    @Override
    public VMState getSourceState() {
        return VMState.INIT;
    }

    @Override
    public VMState getFutureState() {
        return VMState.READY;
    }

    /**
     * Get the template to use to build the VM.
     *
     * @return the template identifier
     */
    public String getTemplate() {
        return template;
    }

    /**
     * The builder devoted to a init->ready transition.
     */
    public static class Builder extends VMTransitionBuilder {

        /**
         * New builder
         */
        public Builder() {
            super("forge", VMState.INIT, VMState.READY);
        }

        @Override
        public VMTransition build(ReconfigurationProblem r, VM v) throws SchedulerException {
            return new ForgeVM(r, v);
        }
    }
}
