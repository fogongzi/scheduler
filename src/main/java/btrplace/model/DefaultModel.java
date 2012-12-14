/*
 * Copyright (c) 2012 University of Nice Sophia-Antipolis
 *
 * This file is part of btrplace.
 *
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

package btrplace.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation for {@link Model}.
 *
 * @author Fabien Hermenier
 */
public class DefaultModel implements Model, Cloneable {

    private Mapping cfg;

    private Map<String, ShareableResource> resources;

    private Attributes attrs;

    /**
     * Make a new instance using a particular mapping.
     *
     * @param m the mapping to use
     */
    public DefaultModel(Mapping m) {
        this.cfg = m;
        this.resources = new HashMap<String, ShareableResource>();
        attrs = new DefaultAttributes();
    }


    @Override
    public ShareableResource getResource(String id) {
        return this.resources.get(id);
    }

    @Override
    public boolean attach(ShareableResource rc) {
        if (this.resources.containsKey(rc.getIdentifier())) {
            return false;
        }
        this.resources.put(rc.getIdentifier(), rc);
        return true;
    }

    @Override
    public Collection<ShareableResource> getResources() {
        return this.resources.values();
    }

    @Override
    public Mapping getMapping() {
        return this.cfg;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Model that = (Model) o;

        if (!cfg.equals(that.getMapping())) {
            return false;
        }

        if (!attrs.equals(that.getAttributes())) {
            return false;
        }
        Collection<ShareableResource> thatRrcs = that.getResources();
        return resources.values().containsAll(thatRrcs) && resources.size() == thatRrcs.size();
    }

    @Override
    public int hashCode() {
        int result = cfg.hashCode();
        result = 31 * result + resources.hashCode();
        result = 31 * result + attrs.hashCode();
        return result;
    }

    @Override
    public boolean detach(ShareableResource rc) {
        return resources.remove(rc.getIdentifier()) != null;
    }

    @Override
    public void clearResources() {
        this.resources.clear();
    }

    @Override
    public Attributes getAttributes() {
        return attrs;
    }

    @Override
    public void setAttributes(Attributes a) {
        attrs = a;
    }

    @Override
    public Model clone() {
        Model m = new DefaultModel(cfg.clone());
        for (ShareableResource rc : resources.values()) {
            m.attach(rc.clone());
        }
        m.setAttributes(this.getAttributes().clone());
        return m;
    }
}
