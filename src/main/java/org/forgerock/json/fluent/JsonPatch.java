/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.fluent;

// Java Standard Edition
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class JsonPatch {

    /** Media type for JSON Patch format. */
    public static final String MEDIA_TYPE = "application/patch+json";

    /**
     * TODO: Description.
     * <p>
     * Note: If an unexpected (non-JSON) type is encountered, this method returns
     * {@code true}, triggering a change in the resulting patch. 
     */
    private static boolean differentTypes(JsonNode n1, JsonNode n2) {
        if (n1.isNull() && n2.isNull()) { // both values are null
            return false;
        } else if (n1.isMap() && n2.isMap()) {
            return false;
        } else if (n1.isList() && n2.isList()) {
            return false;
        } else if (n1.isString() && n2.isString()) {
            return false;
        } else if (n1.isNumber() && n2.isNumber()) {
            return false;
        } else if (n1.isBoolean() && n2.isBoolean()) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * TODO: Description.
     *
     * @param op TODO.
     * @param pointer TODO.
     * @param value TODO.
     * @return TODO.
     */
    private static HashMap<String, Object> op(String op, JsonPointer pointer, JsonNode value) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put(op, pointer.toString());
        if (value != null) {
            result.put("value", value.copy().getValue());
        }
        return result;
    }

    /**
     * TODO: Description.
     *
     * @param n1 TODO.
     * @param n2 TODO.
     * @throws NullPointerException if either of {@code n1} or {@code n2} are {@code null}.
     */
    public static JsonNode diff(JsonNode n1, JsonNode n2) {
        ArrayList<Object> result = new ArrayList<Object>();
        if (differentTypes(n1, n2)) { // different types cause a replace
            result.add(op("replace", n1.getPointer(), n2));
        } else if (n1.isMap()) {
            for (String key : n1.keys()) {
                if (n2.isDefined(key)) { // n2 also has the property
                    JsonNode diff = diff(n1.get(key), n2.get(key)); // recursively compare properties
                    if (diff.size() > 0) {
                        result.addAll(diff.asList()); // add diff results
                    }
                } else { // property is missing in n2
                    result.add(op("remove", n1.getPointer().child(key), null));
                }
            }
            for (String key : n2.keys()) {
                if (!n1.isDefined(key)) { // property is in n2, not in n1
                    result.add(op("add", n1.getPointer().child(key), n2.get(key)));
                }
            }
        } else if (n1.isList()) {
            boolean replace = false;
            if (n1.size() != n2.size()) {
                replace = true;
            } else {
                Iterator<JsonNode> i1 = n1.iterator();
                Iterator<JsonNode> i2 = n2.iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    if (diff(i1.next(), i2.next()).size() > 0) { // recursively compare elements
                        replace = true;
                        break;
                    }
                }
            }
            if (replace) { // replace list entirely
                result.add(op("replace", n1.getPointer(), n2));
            }
        } else if (!n1.isNull() && !n1.getValue().equals(n2.getValue())) { // simple value comparison
            result.add(op("replace", n1.getPointer(), n2));
        }
        return new JsonNode(result);
    }

    /**
     * Returns the operation value.
     *
     * @param op the patch operation containing the value.
     * @return the value specified in the operation.
     * @throws JsonNodeException if a value is not provided.
     */
    private static Object opValue(JsonNode op) throws JsonNodeException {
        Object value = op.get("value").getValue();
        if (value == null && !op.isDefined("value")) { // allow explicit null value
            throw new JsonNodeException(op, "expecting value property");
        }
        return value;
    }

    /**
     * TODO: Description.
     *
     * @param op TODO.
     * @param pointer TODO.
     * @param target TODO.
     * @return TODO.
     * @throws JsonNodeException TODO.
     */
    private static JsonNode parentNode(JsonPointer pointer, JsonNode target) throws JsonException {
        JsonNode result = null;
        JsonPointer parent = pointer.parent();
        if (parent != null) {
            result = target.get(parent);
            if (result == null) {
                throw new JsonException("parent node not found");
            }
        }
        return result;
    }

    /**
     * TODO: Description.
     * <p>
     * In event of failure, this method does not revert the changes applied up to the point
     * of failure. If this is incompatible with your requirements, perform the patch against
     * a copy of your object.
     *
     * @param target TODO.
     * @param diff TODO.
     * @throws JsonNodeException TODO.
     */
    public static void patch(JsonNode target, JsonNode diff) throws JsonNodeException {
        for (JsonNode op : diff.required().expect(List.class)) {
            JsonPointer pointer;
            if ((pointer = op.get("replace").asPointer()) != null) {
                JsonNode parent = parentNode(pointer, target);
                if (parent != null) { // replacing a child
                    String leaf = pointer.leaf();
                    if (!parent.isDefined(leaf)) {
                        throw new JsonNodeException(op, "node not found");
                    }
                    parent.put(leaf, opValue(op));
                } else { // replacing the root node itself
                    target.setValue(opValue(op));
                }
            } else if ((pointer = op.get("add").asPointer()) != null) {
                JsonNode parent = parentNode(pointer, target);
                if (parent == null) {
                    throw new JsonNodeException(op, "cannot add root node");
                }
                try {
                    parent.add(pointer.leaf(), opValue(op));
                } catch (JsonException je) {
                    throw new JsonNodeException(op, je);
                }
            } else if ((pointer = op.get("remove").asPointer()) != null) {
                JsonNode parent = parentNode(pointer, target);
                String leaf = pointer.leaf();
                if (parent == null) {
                    throw new JsonNodeException(op, "cannot remove root node");
                }
                if (!parent.isDefined(leaf)) {
                    throw new JsonNodeException(op, "node not found");
                }
                try {
                    parent.remove(leaf);
                } catch (JsonException je) {
                    throw new JsonNodeException(op, je);
                }
            } else {
                throw new JsonNodeException(op, "expecting add, remove or replace property");
            }
        }
    }
}
