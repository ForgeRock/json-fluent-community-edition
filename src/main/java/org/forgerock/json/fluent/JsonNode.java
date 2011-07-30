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
 * Copyright © 2010–2011 ApexIdentity Inc. All rights reserved.
 * Portions Copyrighted 2011 ForgeRock AS.
 */

package org.forgerock.json.fluent;

// Java Standard Edition
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Represents a node in a JSON object model structure.
 *
 * @author Paul C. Bryan
 */
public class JsonNode implements Iterable<JsonNode> {

    /** Transformers to apply to the node, and are inherited by its children. */
    private final ArrayList<JsonTransformer> transformers;

    /** The pointer to the node within the object model structure. */
    private final JsonPointer pointer;

    /** The value being wrapped by the node. */
    private Object value;

    /**
     * Constructs a JSON node with given value, pointer and transformers.
     * <p>
     * Transformers are applied to the node upon construction, in the order specified in the
     * list. Transformers are inherited by and applied to the node's children.
     *
     * @param value the value being wrapped.
     * @param pointer the pointer to assign this node in a JSON object model structure.
     * @param transformers a list of transformers to apply to node values.
     * @throws JsonNodeException if a transformer failed during node initialization.
     */
    public JsonNode(Object value, JsonPointer pointer, List<JsonTransformer> transformers) throws JsonNodeException {
        if (pointer == null) {
            pointer = new JsonPointer();
        }
        if (transformers == null) {
            transformers = Collections.emptyList();
        }
        this.value = value;
        this.pointer = pointer;
        this.transformers = new ArrayList<JsonTransformer>(transformers);
        for (JsonTransformer transformer : transformers) { // apply transformers in specified order
            this.value = transformer.transform(this);
        }
    }

    /**
     * Constructs a JSON node with given value and pointer.
     *
     * @param value the value being wrapped.
     * @param pointer the pointer to assign this node in a JSON object model structure.
     */
    public JsonNode(Object value, JsonPointer pointer) {
        this(value, pointer, null);
    }

    /**
     * Constructs a root JSON node with a given value.
     */
    public JsonNode(Object value) {
        this(value, null);
    }

    /**
     * Returns the value being wrapped by the node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value being wrapped by the node. This does not modify the parent node
     * in any way; use the {@link #put(String, Object)} method to do so.
     *
     * @param value the value to set.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Returns the pointer of the node in the JSON object model structure.
     */
    public JsonPointer getPointer() {
        return pointer;
    }

    /**
     * Returns the node's list of transformers. The list modifiable; child nodes shall
     * inherit the modified list.
     */
    public List<JsonTransformer> getTransformers() {
        return transformers;
    }

    /**
     * Throws a {@code JsonNodeException} if the node value is {@code null}.
     *
     * @throws JsonNodeException if the node value is {@code null}.
     * @return the node.
     */
    public JsonNode required() throws JsonNodeException {
        if (value == null) {
            throw new JsonNodeException(this, "expecting a value");
        }
        return this;
    }

    /**
     * Called to enforce that the node value is of a particular type. A value of
     * {@code null} is allowed.
     *
     * @param type the class that the underlying value must have.
     * @return the node.
     * @throws JsonNodeException if the value is not the specified type.
     */
    public JsonNode expect(Class type) throws JsonNodeException {
        if (value != null && !type.isInstance(value)) {
            throw new JsonNodeException(this, "expecting " + type.getName());
        }
        return this;
    }

    /**
     * Returns {@code true} if the node value is a {@link Map}.
     */
    public boolean isMap() {
        return (value != null && value instanceof Map);
    }

    /**
     * Returns the node value as a {@code Map} object. If the node value is {@code null}, this
     * method returns {@code null}.
     *
     * @return the map value, or {@code null} if no value.
     * @throws JsonNodeException if the node value is not a {@code Map}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap() throws JsonNodeException {
        return (value == null ? null : ((Map)expect(Map.class).value));
    }

    /**
     * Returns {@code true} if the node value is a {@link List}.
     */
    public boolean isList() {
        return (value != null && value instanceof List);
    }

    /**
     * Returns the node value as a {@link List} object. If the node value is {@code null},
     * this method returns {@code null}.
     *
     * @return the list value, or {@code} null if no value.
     * @throws JsonNodeException if the node value is not a list.
     */
    @SuppressWarnings("unchecked")
    public List<Object> asList() throws JsonNodeException {
        return (value == null ? null : ((List)expect(List.class).value));
    }

    /**
     * Returns {@code true} if the node value is a {@link String}.
     */
    public boolean isString() {
        return (value != null && value instanceof String);
    }

    /**
     * Returns the node value as a {@code String} object. If the node value is {@code null},
     * this method returns {@code null}.
     * 
     * @return the string value.
     * @throws JsonNodeException if the node value is not a string.
     */
    public String asString() throws JsonNodeException {
        return (value == null ? null : ((String)expect(String.class).value));
    }

    /**
     * Returns {@code true} if the node value is a {@link Number}.
     */
    public boolean isNumber() {
        return (value != null && value instanceof Number);
    }

    /**
     * Returns the node value as a {@code Number} object. If the node value is {@code null},
     * this method returns {@code null}.
     *
     * @return the numeric value.
     * @throws JsonNodeException if the node value is not a number.
     */
    public Number asNumber() throws JsonNodeException {
        return (value == null ? null : (Number)expect(Number.class).value);
    }

    /**
     * Returns the node value as an {@link Integer} object. This may involve rounding or
     * truncation. If the node value is {@code null}, this method returns {@code null}.
     *
     * @return the integer value.
     * @throws JsonNodeException if the node value is not a number.
     */
    public Integer asInteger() throws JsonNodeException {
        return (value == null ? null : Integer.valueOf(asNumber().intValue()));
    }

    /**
     * Returns the node value as a {@link Double} object. This may involve rounding.
     * If the node value is {@code null}, this method returns {@code null}.
     *
     * @return the double-precision floating point value.
     * @throws JsonNodeException if the node value is not a number.
     */
    public Double asDouble() throws JsonNodeException {
        return (value == null ? null : Double.valueOf(asNumber().doubleValue()));
    }

    /**
     * Returns the node value as a {@link Long} object. This may involve rounding or
     * truncation. If the node value is {@code null}, this method returns {@code null}.
     *
     * @return the long integer value.
     * @throws JsonNodeException if the node value is not a number.
     */
    public Long asLong() throws JsonNodeException {
        return (value == null ? null : Long.valueOf(asNumber().longValue()));
    }

    /**
     * Returns {@code true} if the node value is a {@link Boolean}.
     */
    public boolean isBoolean() {
        return (value != null && value instanceof Boolean);
    }

    /**
     * Returns the node value as a {@link Boolean} object. If the value is {@code null},
     * this method returns {@code null}.
     *
     * @return the boolean value.
     * @throws JsonNodeException if the node value is not a boolean type.
     */
    public Boolean asBoolean() throws JsonNodeException {
        return (value == null ? null : (Boolean)expect(Boolean.class).value);
    }

    /**
     * Returns {@code true} if the value is {@code null}.
     */
    public boolean isNull() {
        return (value == null);
    }

    /**
     * Returns the node string value as an enum constant of the specified enum type.
     * If the node value is {@code null}, this method returns {@code null}.
     *
     * @param type the enum type to match constants with the value.
     * @return the enum constant represented by the string value.
     * @throws JsonNodeException if the node value does not match any of the enum's constants.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    public <T extends Enum<T>> T asEnum(Class<T> type) throws JsonNodeException {
        if (type == null) {
            throw new NullPointerException();
        }
        try {
            return (value == null ? null : Enum.valueOf(type, asString().toUpperCase()));
        } catch (IllegalArgumentException iae) {
            StringBuilder sb = new StringBuilder("Expecting one of:");
            for (Object constant : type.getEnumConstants()) {
                sb.append(constant.toString()).append(' ');
            }
            throw new JsonNodeException(this, sb.toString());
        }
    }

    /**
     * Returns the node string value as a JSON pointer. If the node value is {@code null},
     * this method returns {@code null}.
     *
     * @return the JSON pointer represented by the node value string.
     * @throws JsonNodeException if the node value is not a valid JSON pointer.
     */
    public JsonPointer asPointer() throws JsonNodeException {
        try {
            return (value == null ? null : new JsonPointer(asString()));
        } catch (JsonException je) {
            throw (je instanceof JsonNodeException ? je : new JsonNodeException(this, je));
        }
    }

    /**
     * Defaults the node value to the specified value if it is currently {@code null}.
     *
     * @param value the string to default to.
     * @return this node or a new node with the default value.
     */
    public JsonNode defaultTo(Object value) {
        return (this.value != null ? this : new JsonNode(value, pointer, transformers));
    }

    /**
     * Returns the key as an array index value. If the string does not represent a valid
     * index value, then {@code -1} is returned.
     *
     * @param key the string key to be converted into an array index value.
     * @return the converted index value, or {@code -1} if invalid.
     */
    private static int toIndex(String key) {
        int index;
        try {
            index = Integer.parseInt(key);
        } catch (NumberFormatException nfe) {
            index = -1;
        }
        return (index < 0 ? -1 : index);
    }

    /**
     * Returns {@code true} if the specified child node is defined within this node.
     *
     * @param key the key of the child node to check for.
     * @return {@code true} if this node contains the specified child.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public boolean isDefined(String key) {
        if (isMap()) {
            return ((Map)value).containsKey(key);
        } else if (isList()) {
            int index = toIndex(key);
            return (index >= 0 && index < ((List)value).size());
        } else {
            return false;
        }
    }

    /**
     * Returns the specified child node. If no such child node exists, then a node with a
     * {@code null} value is returned.
     *
     * @param key property or element identifying the child node to return.
     * @return the child node, or a node with {@code null} value.
     * @throws JsonNodeException if a transformer failed during node initialization.
     */
    public JsonNode get(String key) throws JsonNodeException {
        Object result = null;
        if (isMap()) {
            result = ((Map)value).get(key);
        } else if (isList()) {
            List list = (List)value;
            int index = toIndex(key);
            if (index >= 0 && index < list.size()) {
                result = list.get(index);
            }
        }
        return new JsonNode(result, pointer.child(key), transformers);
    }

    /**
     * Returns the specified child node. If this node value is not a {@link List} or if no
     * such child exists, then a node with a {@code null} value is returned.
     *
     * @param index element identifying the child node to return.
     * @return the child node, or a node with {@code null} value.
     * @throws IndexOutOfBoundsException if {@code index < 0}.
     * @throws JsonNodeException if a transformer failed during node initialization.
     */
    public JsonNode get(int index) throws JsonNodeException {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }
        Object child = null;
        if (isList()) {
            List list = (List)value;
            if (index < list.size()) {
                child = list.get(index);
            }
        }
        return new JsonNode(child, pointer.child(index), transformers);
    }
     
    /**
     * Returns the specified child node. If the specified child does not exist, then
     * {@code null} is returned.
     *
     * @param pointer the JSON pointer identifying the child node to return.
     * @return the child node, or {@code null} if no such child exists.
     * @throws JsonNodeException if a transformer failed during node initialization.
     */
    public JsonNode get(JsonPointer pointer) throws JsonNodeException {
        JsonNode node = this;
        for (String token : pointer) {
            JsonNode child = node.get(token);
            if (child.isNull() && !node.isDefined(token)) {
                return null; // undefined node yields a null value, not an empty node
            }
            node = child;
        }
        return node;
    }

    /**
     * Sets the value of the specified child node.
     *
     * @param key property or element identifying the child node to put.
     * @param value the value to assign to the child node.
     * @throws JsonNodeException if the specified child node is invalid.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public void put(String key, Object value) throws JsonNodeException {
        if (key == null) {
            throw new NullPointerException();
        }
        if (isMap()) {
            asMap().put(key, value);
        } else if (isList()) {
            put(toIndex(key), value);
        } else {
            throw new JsonNodeException(this, "expecting Map or List");
        }
    }

    /**
     * Sets the value of the specified child node.
     *
     * @param index element identifying the child node to put.
     * @param value the value to assign to the child node.
     * @throws JsonNodeException if the specified child node is invalid.
     */
    public void put(int index, Object value) throws JsonNodeException {
        if (!isList()) {
            throw new JsonNodeException(this, "expecting List");
        }
        List<Object> list = asList();
        if (index < 0 || index > list.size()) {
            throw new JsonNodeException(this, "index out of range");
        } else if (index == list.size()) { // appending to end of list
            list.add(value);
        } else { // replacing existing element
            list.set(index, value);
        }
    }

    /**
     * Removes the specified child node.
     *
     * @param key property or element identifying the child node to remove.
     */
    public void remove(String key) {
        if (isMap()) {
            ((Map)value).remove(key);
        } else if (isList()) {
            remove(toIndex(key));
        }
    }

    /**
     * Removes the specified child node.
     *
     * @param index element identifying the child node to remove.
     */
    public void remove(int index) {
        if (index >= 0 && isList()) {
            List list = (List)value;
            if (index < list.size()) {
                list.remove(index);
            }
        }
    }

    /**
     * TODO: Description.
     *
     * @param key TODO.
     * @param value TODO.
     * @throws JsonNodeException TODO.
     */
    public void add(String key, Object value) throws JsonNodeException {
        if (isMap()) {
            if (isDefined(key)) {
                throw new JsonNodeException(this, key + " already exists");
            }
            asMap().put(key, value);
        } else if (isList()) {
            add(toIndex(key), value);
        } else {
            throw new JsonNodeException(this, "expecting Map or List");
        }
    }

    /**
     * TODO: Description.
     *
     * @param index TODO.
     * @param value TODO.
     * @throws JsonNodeException TODO.
     */
    public void add(int index, Object value) throws JsonNodeException {
        if (!isList()) {
            throw new JsonNodeException(this, "expecting List");
        }
        List<Object> list = asList();
        if (index < 0 || index > list.size()) {
            throw new JsonNodeException(this, "index out of range");
        } else {
            list.add(index, value);
        }
    }

    /**
     * Removes all child nodes from this node, if it has any.
     */
    public void clear() throws JsonNodeException {
        if (isMap()) {
            asMap().clear();
        } else if (isList()) {
            asList().clear();
        }
    }

    /**
     * Returns the number of child nodes that this node contains.
     */
    public int size() {
        if (isMap()) {
            return ((Map)value).size();
        } else if (isList()) {
            return ((List)value).size();
        } else {
            return 0;
        }
    }

    /**
     * Returns the set of keys for this node's children. If there are no child nodes, this
     * method returns an empty set.
     */
    public Set<String> keys() {
        Set<String> result;
        if (isMap()) {
            result = new HashSet<String>();
            for (Object key : ((Map)value).keySet()) {
                if (key instanceof String) {
                    result.add((String)key); // only expose string keys in map
                }
            }
        } else if (isList() && size() > 0) {
            result = new RangeSet(0, size() - 1);
        } else {
            result = Collections.emptySet();
        }
        return result;
    }

    /**
     * Returns an iterator the child nodes this node contains. If this node is a {@code Map},
     * then the order of the resulting child nodes is undefined.
     * <p>
     * Note: calls to the {@code next()} method may throw the runtime {@link JsonNodeException}
     * if any transformers fail.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<JsonNode> iterator() {
        if (isList()) { // optimize for list
            return new Iterator<JsonNode>() {
                int cursor = 0;
                Iterator<Object> i = ((List)value).iterator();
                @Override public boolean hasNext() {
                    return i.hasNext();
                }
                @Override public JsonNode next() {
                    Object element = i.next();
                    return new JsonNode(element, pointer.child(cursor++), transformers);
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return new Iterator<JsonNode>() {
                Iterator<String> i = keys().iterator();
                @Override public boolean hasNext() {
                    return i.hasNext();
                }
                @Override public JsonNode next() {
                    return get(i.next());
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Performs deep copy of {@code Map} and {@code List} objects.
     *
     * @param value the value to be recursively copied.
     * @return the copied value.
     */
    @SuppressWarnings("unchecked")
    private static Object copy(Object value) {
        Object result = value; // default: shallow copy of value
        if (value instanceof Map) {
            Map map = ((Map)value);
            HashMap copy = new HashMap(map.size());
            for (Object key : map.keySet()) {
                if (key instanceof String) { // non-string keys are ignored
                    copy.put(key, copy(map.get(key))); // recursive
                }
            }
            result = copy;
        } else if (value instanceof List) {
            List list = ((List)value);
            ArrayList copy = new ArrayList(list.size());
            for (Object element : list) {
                copy.add(copy(element)); // recursive
            }
            result = copy;
        }
        return result;
    }

    /**
     * Returns a deep copy of this node.
     */
    public JsonNode copy() {
        JsonNode node = new JsonNode(copy(value), pointer); // do not re-transform value
        node.transformers.addAll(transformers);
        return node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isNull()) {
            sb.append("null");
        } else if (isMap()) {
            sb.append("{ ");
            Map<String, Object> map = asMap();
            for (Iterator<String> i = map.keySet().iterator(); i.hasNext();) {
                String key = i.next();
                sb.append('"').append(key).append("\": ");
                sb.append(new JsonNode(map.get(key)).toString()); // recursive
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" }");
        } else if (isList()) {
            sb.append("[ ");
            for (Iterator<Object> i = asList().iterator(); i.hasNext();) {
                sb.append(new JsonNode(i.next()).toString()); // recursive
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" ]");
        } else if (isString()) {
            sb.append('"').append(value).append('"');
        } else {
            sb.append(value);
        }
        return sb.toString();
    }
}
