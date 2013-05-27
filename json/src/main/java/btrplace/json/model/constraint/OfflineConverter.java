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

package btrplace.json.model.constraint;

import btrplace.json.JSONConverterException;
import btrplace.model.constraint.Offline;
import net.minidev.json.JSONObject;

/**
 * JSON Converter for the constraint {@link btrplace.model.constraint.Online}.
 *
 * @author Fabien Hermenier
 */
public class OfflineConverter extends SatConstraintConverter<Offline> {


    @Override
    public Class<Offline> getSupportedConstraint() {
        return Offline.class;
    }

    @Override
    public String getJSONId() {
        return "offline";
    }


    @Override
    public Offline fromJSON(JSONObject o) throws JSONConverterException {
        checkId(o);
        return new Offline(requiredElements(o, "nodes"));
    }

    @Override
    public JSONObject toJSON(Offline o) {
        JSONObject c = new JSONObject();
        c.put("id", getJSONId());
        c.put("nodes", elementsToJSON(o.getInvolvedNodes()));
        return c;
    }
}
