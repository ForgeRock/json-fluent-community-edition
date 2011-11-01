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

    /** Internet media type for JSON Patch format. */
    public static final String MEDIA_TYPE = "application/json-patch";

    /**
     * TODO: Description.
     * <p>
     * Note: If an unexpected (non-JSON) type is encountered, this method returns
     * {@code true}, triggering a change in the resulting patch. 
     */
    private static boolean differentTypes(JsonValue v1, JsonValue v2) {
        if (v1.isNull() && v2.isNull()) { // both values are null
            return false;
        } else if (v1.isMap() && v2.isMap()) {
            return false;
        } else if (v1.isList() && v2.isList()) {
            return false;
        } else if (v1.isString() && v2.isString()) {
            return false;
        } else if (v1.isNumber() && v2.isNumber()) {
            return false;
        } else if (v1.isBoolean() && v2.isBoolean()) {
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
    private static HashMap<String, Object> op(String op, JsonPointer pointer, JsonValue value) {
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
     * @param v1 TODO.
     * @param v2 TODO.
     * @throws NullPointerException if either of {@code v1} or {@code v2} are {@code null}.
     */
    public static JsonValue diff(JsonValue v1, JsonValue v2) {
        ArrayList<Object> result = new ArrayList<Object>();
        if (differentTypes(v1, v2)) { // different types cause a replace
            result.add(op("replace", v1.getPointer(), v2));
        } else if (v1.isMap()) {
            for (String key : v1.keys()) {
                if (v2.isDefined(key)) { // v2 also has the property
                    JsonValue diff = diff(v1.get(key), v2.get(key)); // recursively compare properties
                    if (diff.size() > 0) {
                        result.addAll(diff.asList()); // add diff results
                    }
                } else { // property is missing in v2
                    result.add(op("remove", v1.getPointer().child(key), null));
                }
            }
            for (String key : v2.keys()) {
                if (!v1.isDefined(key)) { // property is in v2, not in v1
                    result.add(op("add", v1.getPointer().child(key), v2.get(key)));
                }
            }
        } else if (v1.isList()) {
            boolean replace = false;
            if (v1.size() != v2.size()) {
                replace = true;
            } else {
                Iterator<JsonValue> i1 = v1.iterator();
                Iterator<JsonValue> i2 = v2.iterator();
                while (i1.hasNext() && i2.hasNext()) {
                    if (diff(i1.next(), i2.next()).size() > 0) { // recursively compare elements
                        replace = true;
                        break;
                    }
                }
            }
            if (replace) { // replace list entirely
                result.add(op("replace", v1.getPointer(), v2));
            }
        } else if (!v1.isNull() && !v1.getValue().equals(v2.getValue())) { // simple value comparison
            result.add(op("replace", v1.getPointer(), v2));
        }
        return new JsonValue(result);
    }

    /**
     * Returns the operation value.
     *
     * @param op the patch operation containing the value.
     * @return the value specified in the operation.
     * @throws JsonValueException if a value is not provided.
     */
    private static Object opValue(JsonValue op) throws JsonValueException {
        Object value = op.get("value").getValue();
        if (value == null && !op.isDefined("value")) { // allow explicit null value
            throw new JsonValueException(op, "expecting value member");
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
     * @throws JsonValueException TODO.
     */
    private static JsonValue parentValue(JsonPointer pointer, JsonValue target) throws JsonException {
        JsonValue result = null;
        JsonPointer parent = pointer.parent();
        if (parent != null) {
            result = target.get(parent);
            if (result == null) {
                throw new JsonException("parent value not found");
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
     * @throws JsonValueException TODO.
     */
    public static void patch(JsonValue target, JsonValue diff) throws JsonValueException {
        for (JsonValue op : diff.required().expect(List.class)) {
            JsonPointer pointer;
            if ((pointer = op.get("replace").asPointer()) != null) {
                JsonValue parent = parentValue(pointer, target);
                if (parent != null) { // replacing a child
                    String leaf = pointer.leaf();
                    if (!parent.isDefined(leaf)) {
                        throw new JsonValueException(op, "value to replace not found");
                    }
                    parent.put(leaf, opValue(op));
                } else { // replacing the root value itself
                    target.setValue(opValue(op));
                }
            } else if ((pointer = op.get("add").asPointer()) != null) {
                JsonValue parent = parentValue(pointer, target);
                if (parent == null) {
                    throw new JsonValueException(op, "cannot add root value");
                }
                try {
                    parent.add(pointer.leaf(), opValue(op));
                } catch (JsonException je) {
                    throw new JsonValueException(op, je);
                }
            } else if ((pointer = op.get("remove").asPointer()) != null) {
                JsonValue parent = parentValue(pointer, target);
                String leaf = pointer.leaf();
                if (parent == null) {
                    throw new JsonValueException(op, "cannot remove root value");
                }
                if (!parent.isDefined(leaf)) {
                    throw new JsonValueException(op, "value to remove not found");
                }
                try {
                    parent.remove(leaf);
                } catch (JsonException je) {
                    throw new JsonValueException(op, je);
                }
            } else {
                throw new JsonValueException(op, "expecting add, remove or replace member");
            }
        }
    }
}
