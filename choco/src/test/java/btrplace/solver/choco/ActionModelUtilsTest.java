/*
 * Copyright (c) 2013 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package btrplace.solver.choco;

import btrplace.model.DefaultModel;
import btrplace.model.Model;
import btrplace.model.VM;
import btrplace.plan.ReconfigurationPlan;
import btrplace.solver.choco.actionModel.ActionModelUtils;
import btrplace.solver.choco.actionModel.ActionModelVisitor;
import btrplace.solver.choco.actionModel.VMActionModel;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.variables.IntVar;
import solver.variables.VF;

import java.util.List;


/**
 * Unit tests for {@link btrplace.solver.choco.actionModel.ActionModelUtils}.
 *
 * @author Fabien Hermenier
 */
public class ActionModelUtilsTest {

    private VMActionModel[] makeActions() {
        VMActionModel[] as = new VMActionModel[10];
        Solver s = new Solver();
        for (int i = 0; i < as.length; i++) {
            as[i] = new MockActionModel(s, i);

        }
        return as;
    }

    @Test
    public void testGetDSlices() {
        List<Slice> cs = ActionModelUtils.getDSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("dS"));
            Assert.assertTrue(s.getHoster().getValue() < ns.getHoster().getValue());
        }
    }

    @Test
    public void testGetCSlices() {
        List<Slice> cs = ActionModelUtils.getCSlices(makeActions());
        Assert.assertEquals(5, cs.size());
        for (int i = 0; i < cs.size() - 1; i++) {
            Slice s = cs.get(i);
            Slice ns = cs.get(i + 1);
            Assert.assertTrue(s.getHoster().getName().startsWith("cS"));
            Assert.assertTrue(s.getHoster().getValue() < ns.getHoster().getValue());
        }
    }

    @Test
    public void testGetStarts() {
        IntVar[] sts = ActionModelUtils.getStarts(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("start"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    @Test
    public void testGetEnds() {
        IntVar[] sts = ActionModelUtils.getEnds(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("end"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    @Test
    public void testGetDurations() {
        IntVar[] sts = ActionModelUtils.getDurations(makeActions());
        Assert.assertEquals(10, sts.length);
        for (int i = 0; i < sts.length - 1; i++) {
            IntVar s = sts[i];
            IntVar ns = sts[i + 1];
            Assert.assertTrue(s.getName().startsWith("duration"));
            Assert.assertTrue(s.getValue() < ns.getValue());
        }
    }

    public static class MockActionModel implements VMActionModel {

        private IntVar st, ed, d, h, c, state;

        private Slice cSlice, dSlice;

        public MockActionModel(Solver s, int nb) {
            Model mo = new DefaultModel();
            st = VF.bounded("start" + nb, nb, nb + 1, s);
            ed = VF.bounded("end" + nb, nb, nb + 1, s);
            d = VF.bounded("duration" + nb, nb, nb + 1, s);
            h = VF.bounded("hoster" + nb, nb, nb + 1, s);
            c = VF.bounded("cost" + nb, nb, nb + 1, s);
            state = VF.bounded("state" + nb, nb, nb + 1, s);
            if (nb % 2 == 0) {
                cSlice = new Slice(mo.newVM(),
                        VF.bounded("cS" + nb + "-st", nb, nb + 1, s),
                        VF.bounded("cS" + nb + "-ed", nb, nb + 1, s),
                        VF.bounded("cS" + nb + "-d", nb, nb + 1, s),
                        VF.bounded("cS" + nb + "-h", nb, nb + 1, s));
            } else {
                dSlice = new Slice(mo.newVM(),
                        VF.bounded("dS" + nb + "-st", nb, nb + 1, s),
                        VF.bounded("dS" + nb + "-ed", nb, nb + 1, s),
                        VF.bounded("dS" + nb + "-d", nb, nb + 1, s),
                        VF.bounded("dS" + nb + "-h", nb, nb + 1, s));
            }
        }

        @Override
        public IntVar getStart() {
            return st;
        }

        @Override
        public VM getVM() {
            return null;
        }

        @Override
        public IntVar getEnd() {
            return ed;
        }

        @Override
        public IntVar getDuration() {
            return d;
        }

        @Override
        public Slice getCSlice() {
            return cSlice;
        }

        @Override
        public Slice getDSlice() {
            return dSlice;
        }

        @Override
        public boolean insertActions(ReconfigurationPlan plan) {
            return true;
        }

        @Override
        public IntVar getState() {
            return state;
        }

        @Override
        public void visit(ActionModelVisitor v) {
            throw new UnsupportedOperationException();
        }
    }
}
