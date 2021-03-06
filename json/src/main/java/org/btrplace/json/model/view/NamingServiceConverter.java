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

package org.btrplace.json.model.view;

import net.minidev.json.JSONObject;
import org.btrplace.json.JSONConverterException;
import org.btrplace.model.Element;
import org.btrplace.model.Model;
import org.btrplace.model.Node;
import org.btrplace.model.VM;
import org.btrplace.model.view.NamingService;

import java.util.Map;

import static org.btrplace.json.JSONs.*;

/**
 * Serialize/Un-serialize an {@link org.btrplace.model.view.NamingService}.
 *
 * @author Fabien Hermenier
 */
public class NamingServiceConverter implements ModelViewConverter<NamingService> {

    @Override
    public Class<NamingService> getSupportedView() {
        return NamingService.class;
    }

    @Override
    public String getJSONId() {
        return "ns";
    }

    @Override
    public JSONObject toJSON(NamingService rc) {
        JSONObject container = new JSONObject();
        container.put("id", getJSONId());
        container.put("type", rc.getElementIdentifier());
        JSONObject map = new JSONObject();
        for (Object o : rc.getNamedElements()) {
            Element e = (Element) o;
            map.put(rc.resolve(e), e.id());
        }
        container.put("map", map);
        return container;
    }

    @Override
    public NamingService<? extends Element> fromJSON(Model mo, JSONObject o) throws JSONConverterException {
        String id = requiredString(o, "id");
        if (!id.equals(getJSONId())) {
            return null;
        }

        NamingService ns;
        String type = requiredString(o, "type");
        switch (type) {
            case VM.TYPE:
                ns = NamingService.newVMNS();
                break;
            case Node.TYPE:
                ns = NamingService.newNodeNS();
                break;
            default:
                throw new JSONConverterException("Unsupported type of element '" + type + "'");
        }

        checkKeys(o, "map");
        JSONObject map = (JSONObject) o.get("map");
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String n = e.getKey();
            int v = Integer.parseInt(e.getValue().toString());
            Element el = VM.TYPE.equals(type) ? getVM(mo, v) : getNode(mo, v);
            if (!ns.register(el, n)) {
                throw new JSONConverterException("Duplicated name '" + n + "'");
            }
        }
        return ns;
    }
}
