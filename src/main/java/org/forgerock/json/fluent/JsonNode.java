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
import java.util.Collections;
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

    /** The value being wrapped by the node. */
    protected final Object value;

    /** The path of the node in the object model structure. */
    protected final JsonPath path;

    /**
     * Constructs a root JSON node with a given value. A root node has a path of {@code "$"}.
     */
    public JsonNode(Object value) {
        this.value = value;
        this.path = JsonPath.ROOT;
    }

    /**
     * Constructs a JSON node with given value and path.
     *
     * @param value the value being wrapped.
     * @param path the path to assign this node in a JSON object model structure.
     */
    public JsonNode(Object value, JsonPath path) {
        this.value = value;
        this.path = path;
    }

    /**
     * Returns the value being wrapped by the node.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the concrete path of the node in the JSON object model structure.
     */
    public JsonPath getPath() {
        return path;
    }

    /**
     * Throws a {@code JsonNodeException} if this value is {@code null}.
     *
     * @throws JsonNodeException if this value is {@code null}.
     * @return this value.
     */
    public JsonNode required() throws JsonNodeException {
        if (value == null) {
            throw new JsonNodeException(this, "expecting a value");
        }
        return this;
    }

    /**
     * Called to enforce that the value is a particular type. A value of {@code null} is
     * allowed.
     *
     * @param type the class that the underlying value must have.
     * @return this value.
     * @throws JsonNodeException if the value is not the specified type.
     */
    public JsonNode expect(Class type) throws JsonNodeException {
        if (value != null && !type.isInstance(value)) {
            throw new JsonNodeException(this, "expecting " + type.getName());
        }
        return this;
    }

    /**
     * Returns {@code true} if the value is a {@link Map}.
     */
    public boolean isMap() {
        return (value != null && value instanceof Map);
    }

    /**
     * Returns the value as a {@code Map}. If the value is {@code null}, this method returns
     * {@code null}.
     *
     * @return the map value, or {@code null} if no value.
     * @throws JsonNodeException if the value is not a {@code Map}.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap() throws JsonNodeException {
        return (value == null ? null : ((Map)expect(Map.class).value));
    }

    /**
     * Returns {@code true} if the value is a {@link List}.
     */
    public boolean isList() {
        return (value != null && value instanceof List);
    }

    /**
     * Returns the value as a {@link List}. If the value is {@code null}, this method returns
     * {@code null}.
     *
     * @return the list value, or {@code} null if no value.
     * @throws JsonNodeException if the value is not a list.
     */
    @SuppressWarnings("unchecked")
    public List<Object> asList() throws JsonNodeException {
        return (value == null ? null : ((List)expect(List.class).value));
    }

    /**
     * Returns {@code true} if the value is a {@link String}.
     */
    public boolean isString() {
        return (value != null && value instanceof String);
    }

    /**
     * Returns the value as a string. If the value is {@code null}, this method returns
     * {@code null}.
     * 
     * @return the string value.
     * @throws JsonNodeException if the value is not a string.
     */
    public String asString() throws JsonNodeException {
        return (value == null ? null : ((String)expect(String.class).value));
    }

    /**
     * Returns {@code true} if the value is a {@link Number}.
     */
    public boolean isNumber() {
        return (value != null && value instanceof Number);
    }

    /**
     * Returns the value as a number. If the value is {@code null}, this method returns
     * {@code null}.
     *
     * @return the numeric value.
     * @throws JsonNodeException if the value is not a number.
     */
    public Number asNumber() throws JsonNodeException {
        return (value == null ? null : (Number)expect(Number.class).value);
    }

    /**
     * Returns the value as an integer. This may involve rounding or truncation. If the
     * value is {@code null}, this method throws a {@code JsonNodeException}. To avoid this,
     * use the {@code asNumber} method.
     *
     * @return the integer value.
     * @throws JsonNodeException if the value is {@code null} or is not a number.
     */
    public int asInteger() throws JsonNodeException {
        return required().asNumber().intValue();
    }

    /**
     * Returns the value as a double-precision floating point number. This may involve
     * rounding. If the value is {@code null}, this method throws a {@code JsonNodeException}.
     * To avoid this, use the {@code asNumber} method.
     *
     * @return the double-precision floating point value.
     * @throws JsonNodeException if the value is {@code null} or is not a number.
     */
    public double asDouble() throws JsonNodeException {
        return required().asNumber().doubleValue();
    }

    /**
     * Returns the value as a long integer. This may involve rounding or truncation. If the
     * value is {@code null}, this method throws a {@code JsonNodeException}. To avoid this,
     * use the {@code asNumber} method.
     *
     * @return the long integer value.
     * @throws JsonNodeException if the value is {@code null} or is not a number.
     */
    public long asLong() throws JsonNodeException {
        return required().asNumber().longValue();
    }

    /**
     * Returns {@code true} if the value is a {@link Boolean}.
     */
    public boolean isBoolean() {
        return (value != null && value instanceof Boolean);
    }

    /**
     * Returns the value as a Boolean object. If the value is {@code null}, this method
     * returns {@code null}.
     *
     * @return the boolean value.
     * @throws JsonNodeException if the value is not a boolean type.
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
     * Returns the object model string value as the enum constant of the specified enum type.
     * If the value is {@code null}, this method returns {@code null}.
     *
     * @param type the enum type to match constants with the value.
     * @return the enum constant represented by the string value.
     * @throws JsonNodeException if the value does not match any of the enum's constants.
     */
    public <T extends Enum<T>> T asEnum(Class<T> type) throws JsonNodeException {
        if (type == null) {
            throw new NullPointerException();
        }
        try {
            return (value == null ? null : Enum.valueOf(type, asString().toUpperCase()));
        }
        catch (IllegalArgumentException iae) {
            StringBuilder sb = new StringBuilder("Expecting one of:");
            for (Object constant : type.getEnumConstants()) {
                sb.append(constant.toString()).append(' ');
            }
            throw new JsonNodeException(this, sb.toString());
        }
    }

    /**
     * Defaults the value to the specified string if the value is currently {@code null}.
     *
     * @param defaultValue the string to default to.
     * @return this value or the new default value.
     */
    public JsonNode defaultTo(String defaultValue) {
        return (value != null ? this : new JsonNode(defaultValue, path));
    }

    /**
     * Defaults the value to the specified number if the value is currently {@code null}.
     *
     * @param defaultValue the number to default to.
     * @return this value or the new default value.
     */
    public JsonNode defaultTo(Number defaultValue) {
        return (value != null ? this : new JsonNode(defaultValue, path));
    }

    /**
     * Defaults the value to the specified integer if the value is currently {@code null}.
     *
     * @param defaultValue the integer to default to.
     * @return this value or the new default value.
     */      
    public JsonNode defaultTo(int defaultValue) {
        return (value != null ? this : new JsonNode(Integer.valueOf(defaultValue), path));
    }

    /**
     * Defaults the value to the specified long integer if the value is currently {@code null}.
     *
     * @param defaultValue the long integer to default to.
     * @return this value or the new default value.
     */      
    public JsonNode defaultTo(long defaultValue) {
        return (value != null ? this : new JsonNode(Long.valueOf(defaultValue), path));
    }

    /**
     * Defaults the value to the specified double-precision floating point number if the
     * value is currently {@code null}.
     *
     * @param defaultValue the double-precision floating point number to default to.
     * @return this value or the new default value.
     */ 
    public JsonNode defaultTo(double defaultValue) {
        return (value != null ? this : new JsonNode(Double.valueOf(defaultValue), path));
    }


    /**
     * Defaults the value to the specified boolean value if value is currently {@code null}.
     *
     * @param defaultValue the boolean value to default to.
     * @return this value or the new default value.
     */
    public JsonNode defaultTo(boolean defaultValue) {
        return (value != null ? this : new JsonNode(Boolean.valueOf(defaultValue), path));
    }

    /**
     * Returns {@code true} if the specified child node is defined within this node.
     *
     * @param key the key of the child node to check for.
     * @return {@code true} if this node contains the specified child.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public boolean isDefined(Object key) {
        if (key instanceof String) {
            return (isMap() && ((Map)value).containsKey((String)key));
        }
        else if (key instanceof Integer) {
            return (isList() && ((List)value).size() > ((Integer)key).intValue());
        }
        else {
            return false; // unknown key type
        }
    }

    /**
     * Returns the child node that has the specified key, or a node containing {@code null}
     * if no such child exists.
     *
     * @param key the key of the child node to get.
     * @return a node containing the child value or {@code null}.
     * @throws JsonNodeException if this node type is incompatible with the specified key.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public JsonNode get(Object key) throws JsonNodeException {
        if (key instanceof String) {
            return new JsonNode(required().asMap().get((String)key), new JsonPath(path, (String)key));
        }
        else if (key instanceof Integer) {
            List list = required().asList();
            Object o = null;
            int index = ((Integer)key).intValue();
            if (list.size() > index) {
                o = list.get(index);
            }
            return new JsonNode(o, new JsonPath(path, (Integer)key));
        }
        else {
            throw new JsonNodeException(this, "unknown key type");
        }
    }

    /**
     * Puts a child node with the specified key and value. If the child already exists, it
     * is replaced.
     *
     * @param key the key of the child node to put.
     * @param value the value to assign to the child node.
     * @throws JsonNodeException if this node type is incompatible with the specified key.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public void put(Object key, Object value) throws JsonNodeException {
        if (key instanceof String) {
            required().asMap().put((String)key, value);
        }
        else if (key instanceof Integer) {
            List<Object> list = required().asList();
            int index = ((Integer)key).intValue();
            if (index > list.size()) {
                throw new JsonNodeException(this, "cannot sparsely allocate list element");
            }
            else if (index == list.size()) { // appending to end of list
                list.add(value);
            }
            else { // replacing existing element
                list.set(index, value);
            }
        }
        else {
            throw new JsonNodeException(this, "unknown key type");
        }
    }

    /**
     * Removes the child node with the specified key. If the child does not exist, this method
     * has no effect.
     *
     * @param key the key of the map entry to remove.
     * @throws JsonNodeException if this node type is incompatible with the specified key.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public void remove(Object key) throws JsonNodeException {
        if (key instanceof String) {
            required().asMap().remove(key);
        }
        else if (key instanceof Integer) {
            int index = ((Integer)key).intValue();
            List list = required().asList();
            if (list.size() > index) {
                list.remove(index);
            }
        }
        else {
            throw new JsonNodeException(this, "unknown key type");
        }
    }

    /**
     * Returns the number of child nodes that this node contains.
     */
    public int size() {
        if (isMap()) {
            return ((Map)value).size();
        }
        else if (isList()) {
            return ((List)value).size();
        }
        else {
            return 0;
        }
    }

    /**
     * Returns the set of keys for this node's children. If there are no child nodes, this
     * method returns an empty set.
     */
    public Set<? extends Object> keys() {
        if (isMap()) {
            HashSet<Object> set = new HashSet<Object>();
            for (Object key : ((Map)value).keySet()) {
                if (key instanceof String) {
                    set.add(key); // only expose string keys in map
                }
            }
            return set;
        }
        else if (isList()) {
            return new RangeSet(0, ((List)value).size() - 1);
        }
        else {
            return Collections.emptySet();
        }
    }

    /**
     * Returns an iterator the child nodes this node contains.
     */
    @Override
    public Iterator<JsonNode> iterator() {
        return new Iterator<JsonNode>() {
            Iterator<? extends Object> keys = keys().iterator();
            Object key = null; // last key retrieved
            @Override public boolean hasNext() {
                return keys.hasNext();
            }
            @Override public JsonNode next() {
                try {
                    key = keys.next();
                    return get(key);
                }
                catch (JsonNodeException jne) { // shouldn't happen
                    throw new IllegalStateException(jne);
                }
            }
            @Override public void remove() {
                if (key == null) {
                    throw new IllegalStateException();
                }
                else {
                    try {
                        JsonNode.this.remove(key);
                        key = null; // throw exception if called without next()
                    }
                    catch (JsonNodeException jne) {
                        throw new UnsupportedOperationException(jne);
                    }
                }
            }
        };
    }
}
