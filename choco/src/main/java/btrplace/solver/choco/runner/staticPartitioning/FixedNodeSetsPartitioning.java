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

package btrplace.solver.choco.runner.staticPartitioning;

import btrplace.model.*;
import btrplace.model.constraint.SatConstraint;
import btrplace.solver.SolverException;
import btrplace.solver.choco.ChocoReconfigurationAlgorithmParams;
import btrplace.solver.choco.runner.staticPartitioning.splitter.ConstraintSplitterMapper;

import java.util.*;

/**
 * @author Fabien Hermenier
 */
public class FixedNodeSetsPartitioning extends StaticPartitioning {

    private Collection<Set<Node>> partitions;

    private ConstraintSplitterMapper cstrMapper;

    public FixedNodeSetsPartitioning(Collection<Set<Node>> parts) {

        if (!isDisjoint(parts)) {
            throw new IllegalArgumentException("The constraint expects disjoint sets of nodes");

        }
        partitions = parts;
        cstrMapper = ConstraintSplitterMapper.newBundle();
    }

    public ConstraintSplitterMapper getSplitterMapper() {
        return cstrMapper;
    }

    public void setSplitterMapper(ConstraintSplitterMapper cstrMapper) {
        this.cstrMapper = cstrMapper;
    }

    public Collection<Set<Node>> getPartitions() {
        return partitions;
    }

    public boolean setPartitions(Collection<Set<Node>> parts) {
        if (!isDisjoint(parts)) {
            return false;
        }
        partitions = parts;
        return true;
    }

    @Override
    public List<Instance> split(ChocoReconfigurationAlgorithmParams ps, Instance i) throws SolverException {
        Model mo = i.getModel();
        Mapping map = mo.getMapping();

        List<Instance> parts = new ArrayList<>(partitions.size());
        for (Set<Node> s : partitions) {
            Model partModel = new DefaultModel();
            //Book every node and VMs id, we will re-use them in practice
            for (Node n : i.getModel().getNodes()) {
                partModel.newNode(n.id());
            }
            for (VM vm : i.getModel().getVMs()) {
                partModel.newVM(vm.id());
            }

            parts.add(new Instance(partModel, new HashSet<SatConstraint>(), i.getOptimizationConstraint()));
            for (Node n : s) {
                if (map.getOfflineNodes().contains(n)) {
                    partModel.getMapping().addOfflineNode(n);
                } else {
                    partModel.getMapping().addOnlineNode(n);
                    for (VM v : map.getRunningVMs(n)) {
                        partModel.getMapping().addRunningVM(v, n);
                    }
                    for (VM v : map.getSleepingVMs(n)) {
                        partModel.getMapping().addRunningVM(v, n);
                    }
                }
            }
        }
        for (SatConstraint cstr : i.getConstraints()) {
            cstrMapper.split(cstr, i, parts);
        }
        //TODO: deal with ready VMs to run
        return parts;
    }

    private static boolean isDisjoint(Collection<Set<Node>> p) {
        int cnt = 0;
        Set<Node> all = new HashSet<>();
        for (Collection<Node> s : p) {
            cnt += s.size();
            all.addAll(s);
            if (cnt != all.size()) {
                return false;
            }
        }
        return true;
    }
}