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

package org.btrplace.json.model.constraint;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.JSONs;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Offline;

import static org.btrplace.json.JSONs.requiredBoolean;
import static org.btrplace.json.JSONs.requiredNode;
/**
 * JSON Converter for the constraint {@link org.btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OfflineConverter implements ConstraintConverter<Offline> {


    @Override
    public Class<Offline> getSupportedConstraint() {
        return Offline.class;
    }

    @Override
    public String getJSONId() {
        return "offline";
    }


    @Override
    public Offline fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Offline(requiredNode(mo, o, "node"), requiredBoolean(o, "continuous"));
    }

    @Override
    public JSONObject toJSON(Offline o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("node", JSONs.elementToJSON(o.getInvolvedNodes().iterator().next()));
        c.put("continuous", o.isContinuous());
        return c;
    }
}
