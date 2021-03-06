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

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.json.model.constraint.migration.MinMTTRMigConverter;
import org.btrplace.model.Model;
import org.btrplace.model.constraint.Constraint;
import org.btrplace.model.constraint.SatConstraint;

import java.util.*;

import static org.btrplace.json.JSONs.checkKeys;

/**
 * Extensible converter for {@link org.btrplace.model.constraint.Constraint}.
 *
 * @author Fabien Hermenier
 */
public class ConstraintsConverter {

    private Map<Class<? extends Constraint>, ConstraintConverter<? extends Constraint>> java2json;
    private Map<String, ConstraintConverter<? extends Constraint>> json2java;

    /**
     * Make a new empty converter.
     */
    public ConstraintsConverter() {
        java2json = new HashMap<>();
        json2java = new HashMap<>();
    }

    /**
     * Make a new {@code ConstraintsConverter} and fulfill it
     * using a default converter for each supported constraint.
     *
     * @return a fulfilled converter.
     */
    public static ConstraintsConverter newBundle() {
        //The default converters
        ConstraintsConverter c = new ConstraintsConverter();
        c.register(new AmongConverter());
        c.register(new BanConverter());
        c.register(new ResourceCapacityConverter());
        c.register(new RunningCapacityConverter());
        c.register(new FenceConverter());
        c.register(new GatherConverter());
        c.register(new KilledConverter());
        c.register(new LonelyConverter());
        c.register(new OfflineConverter());
        c.register(new OnlineConverter());
        c.register(new OverbookConverter());
        c.register(new PreserveConverter());
        c.register(new QuarantineConverter());
        c.register(new ReadyConverter());
        c.register(new RootConverter());
        c.register(new RunningConverter());
        c.register(new SeqConverter());
        c.register(new SleepingConverter());
        c.register(new SplitAmongConverter());
        c.register(new SplitConverter());
        c.register(new SpreadConverter());
        c.register(new MaxOnlineConverter());
        c.register(new MinMTTRConverter());
        c.register(new MinMTTRMigConverter());
        return c;
    }

    /**
     * Register a converter for a specific constraint.
     *
     * @param c the converter to register
     */
    public void register(ConstraintConverter<? extends Constraint> c) {
        java2json.put(c.getSupportedConstraint(), c);
        json2java.put(c.getJSONId(), c);

    }

    /**
     * Get the Java constraints that are supported by the converter.
     *
     * @return a set of classes derived from {@link Constraint} that may be empty
     */
    public Set<Class<? extends Constraint>> getSupportedJavaConstraints() {
        return java2json.keySet();
    }

    /**
     * Get the JSON constraints that are supported by the converter.
     *
     * @return a set of constraints identifier that may be empty
     */
    public Set<String> getSupportedJSONConstraints() {
        return json2java.keySet();
    }

    /**
     * Convert a json-encoded constraint.
     *
     * @param mo the model to rely on
     * @param in the constraint to decode
     * @return the resulting constraint
     * @throws JSONConverterException if the conversion failed
     */
    public Constraint fromJSON(Model mo, JSONObject in) throws JSONConverterException {
        checkKeys(in, "id");
        Object id = in.get("id");
        ConstraintConverter<? extends Constraint> c = json2java.get(id.toString());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint having id '" + id + "'");
        }
        return c.fromJSON(mo, in);
    }

    /**
     * Serialise a constraint.
     * @param o the constraint
     * @return the resulting encoded constraint
     * @throws JSONConverterException if the conversion failed
     */
    public JSONObject toJSON(Constraint o) throws JSONConverterException {
        ConstraintConverter c = java2json.get(o.getClass());
        if (c == null) {
            throw new JSONConverterException("No converter available for a constraint with the '" + o.getClass() + "' className");
        }
        return c.toJSON(o);
    }

    /**
     * Convert a list of json-encoded sat-constraints.
     * @param mo the model to rely on
     * @param in the constraints to decode
     * @return the constraint list. Might be empty
     * @throws JSONConverterException if the conversion failed
     */
    public List<SatConstraint> listFromJSON(Model mo, JSONArray in) throws JSONConverterException {
        List<SatConstraint> l = new ArrayList<>(in.size());
        for (Object o : in) {
            if (!(o instanceof JSONObject)) {
                throw new JSONConverterException("Expected an array of JSONObject but got an array of " + o.getClass().getName());
            }
            l.add((SatConstraint) fromJSON(mo, (JSONObject) o));
        }
        return l;
    }

    /**
     * Serialise a list of sat-constraints.
     * @param e the list to serialise
     * @return the resulting encoded list
     * @throws JSONConverterException if the conversion failed
     */
    public JSONArray toJSON(Collection<SatConstraint> e) throws JSONConverterException {
        JSONArray arr = new JSONArray();
        for (SatConstraint cstr : e) {
            arr.add(toJSON(cstr));
        }
        return arr;
    }
}
