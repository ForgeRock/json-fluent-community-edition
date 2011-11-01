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
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a value in a JSON structure.
 *
 * @author Paul C. Bryan
 */
public class JsonValue implements Iterable<JsonValue> {

    /** Transformers to apply to the JSON value; are inherited by its children. */
    private final ArrayList<JsonTransformer> transformers = new ArrayList<JsonTransformer>();

    /** The pointer to the value within a JSON structure. */
    private final JsonPointer pointer;

    /** The raw value being wrapped by this JSON value. */
    private Object value;

    /**
     * Constructs a JSON value object with given value, pointer and transformers.
     * <p>
     * Transformations are applied to the value, by the transformers in the order specified
     * in the list. Transformers are inherited by the value's children and are applied upon
     * construction of such child {@code JsonValue} objects.
     *
     * @param value the value to wrap with the JSON value object.
     * @param pointer the pointer to the value in a JSON structure.
     * @param transformers a list of transformers to apply to values.
     * @throws JsonException if a transformer failed during value initialization.
     */
    public JsonValue(Object value, JsonPointer pointer, Collection<? extends JsonTransformer> transformers) throws JsonException {
        if (pointer == null) {
            pointer = new JsonPointer();
        }
        this.value = value;
        this.pointer = pointer;
        if (transformers != null) {
            this.transformers.addAll(transformers);
        }
        transform(); // apply transformations
    }

    /**
     * Constructs a JSON value object with a given value and pointer.
     *
     * @param value the value to wrap with the JSON value object.
     * @param pointer the pointer to the value in a JSON structure.
     */
    public JsonValue(Object value, JsonPointer pointer) {
        this(value, pointer, null);
    }

    /**
     * Constructs a JSON value object with a given value.
     * <p>
     * If the provided value is an instance of {@code JsonValue}, then this constructor makes
     * a shallow copy of its members.
     *
     * @param value the value to wrap with the JSON value object.
     */
    public JsonValue(Object value) {
        if (value != null && value instanceof JsonValue) { // make shallow copy
            JsonValue jv = (JsonValue)value;
            this.value = jv.value;
            this.pointer = jv.pointer;
            this.transformers.addAll(jv.transformers);
        } else {
            this.value = value;
            pointer = new JsonPointer();
        }
    }

    /**
     * Applies all of the transformations to the value. As each transformer is applied,
     * all transformations are applied again recursively. This process terminates when the
     * result of applying transformations no longer affects the value.
     *
     * @throws JsonException if a transformer failed to apply a transformation.
     */
    private void transform() throws JsonException {
        for (JsonTransformer transformer : transformers) { 
            JsonValue jv = new JsonValue(this.value, this.pointer); // do not inherit transformers
            transformer.transform(jv);
            if ((this.value == null && jv.value != null) || (this.value != null && !this.value.equals(jv.value))) {
                this.value = jv.value; // a transformation occurred; use the new value
                transform(); // recursively iterate through all transformers using new value
                break; // recursive call handled remaining transformations in list
            }
        }
    }

    /**
     * Returns the value being wrapped by the JSON value object.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value being wrapped by the JSON value object. This does not modify the parent
     * value in any way; use the {@link #put(String, Object)} method to do so.
     *
     * @param value the value to set.
     */
    public void setValue(Object value) {
        this.value = value;

    }
    /**
     * Returns the pointer of the JSON value in the JSON structure.
     */
    public JsonPointer getPointer() {
        return pointer;
    }

    /**
     * Returns the JSON value's list of transformers. The list is modifiable; child values
     * shall inherit the modified list.
     */
    public List<JsonTransformer> getTransformers() {
        return transformers;
    }

    /**
     * Adds and applies a transformer to the value. This results in applying all other
     * transformations to the value.
     *
     * @param transformer the transformer to add.
     * @return this JSON value.
     * @throws JsonException if a transformer failed to apply a transformation.
     */
    public JsonValue addTransformer(JsonTransformer transformer) throws JsonException {
        transformers.add(transformer);
        transform();
        return this;
    }

    /**
     * Adds and applies a collection of transformers to the value. This results in applying
     * all other transformations to the value
     *
     * @param transformers the transformers to add.
     * @return this JSON value.
     * @throws JsonException if a transformer failed to apply a transformation.
     */
    public JsonValue addTransformers(Collection<? extends JsonTransformer> transformers) {
        this.transformers.addAll(transformers);
        transform();
        return this;
    }

    /**
     * Throws a {@code JsonValueException} if the JSON value is {@code null}.
     *
     * @throws JsonValueException if the JSON value is {@code null}.
     * @return this JSON value.
     */
    public JsonValue required() throws JsonValueException {
        if (value == null) {
            throw new JsonValueException(this, "expecting a value");
        }
        return this;
    }

    /**
     * Called to enforce that the JSON value is of a particular type. A value of {@code null}
     * is allowed.
     *
     * @param type the class that the underlying value must have.
     * @return this JSON value.
     * @throws JsonValueException if the value is not the specified type.
     */
    public JsonValue expect(Class type) throws JsonValueException {
        if (value != null && !type.isInstance(value)) {
            throw new JsonValueException(this, "expecting " + type.getName());
        }
        return this;
    }

    /**
     * Returns {@code true} if the JSON value is a {@link Map}.
     */
    public boolean isMap() {
        return (value != null && value instanceof Map);
    }

    /**
     * Returns the JSON value as a {@code Map} object. If the JSON value is {@code null}, this
     * method returns {@code null}.
     *
     * @return the map value, or {@code null} if no value.
     * @throws JsonValueException if the JSON value is not a {@code Map} or contains non-{@code String} keys.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> asMap() throws JsonValueException {
        Map result = (value == null ? null : ((Map)expect(Map.class).value));
        if (result != null) {
            for (Object key : result.keySet()) {
                if (key == null || !(key instanceof String)) {
                    throw new JsonValueException(this, "non-string key encountered in map");
                }
            }
        }
        return result; 
    }

    /**
     * Returns the JSON value as a {@link Map} containing objects of the specified type. If
     * the value is {@code null}, this method returns {@code null}. If any of the entries
     * in the map are not {@code null} and not of the specified type,
     * {@code JsonValueException} is thrown.
     *
     * @param type the type of object that all entries in the map are expected to be.
     * @return the map value, or {@code} null if no value.
     * @throws JsonValueException if the JSON value is not a {@code Map} or contains an unexpected type.
     */
    @SuppressWarnings("unchecked")
    public <V> Map<String, V> asMap(Class<V> type) throws JsonValueException {
        Map<String, Object> map = asMap();
        if (map != null) {
            for (String key : map.keySet()) {
                Object value = map.get(key);
                if (value != null && !type.isInstance(value)) {
                    throw new JsonValueException(this, "expecting " + type.getName() + " entries");
                }
            }
        }
        return (Map)map;
    }

    /**
     * Returns {@code true} if the JSON value is a {@link List}.
     */
    public boolean isList() {
        return (value != null && value instanceof List);
    }

    /**
     * Returns the JSON value as a {@link List} object. If the JSON value is {@code null},
     * this method returns {@code null}.
     *
     * @return the list value, or {@code} null if no value.
     * @throws JsonValueException if the JSON value is not a {@code List}.
     */
    @SuppressWarnings("unchecked")
    public List<Object> asList() throws JsonValueException {
        return (value == null ? null : ((List)expect(List.class).value));
    }

    /**
     * Returns the JSON value as a {@link List} containing objects of the specified type. If
     * the value is {@code null}, this method returns {@code null}. If any of the elements
     * of the list are not {@code null} and not of the specified type,
     * {@code JsonValueException} is thrown.
     *
     * @param type the type of object that all elements are expected to be.
     * @return the list value, or {@code} null if no value.
     * @throws JsonVaueException if the JSON value is not a {@code List} or contains an unexpected type.
     */
    @SuppressWarnings("unchecked")
    public <E> List<E> asList(Class<E> type) throws JsonValueException {
        List list = asList(); // expects a list type
        if (list != null) {
            for (Object value : list) {
                if (value != null && !type.isInstance(value)) {
                    throw new JsonValueException(this, "expecting " + type.getName() + " elements");
                }
            }
        }
        return list;
    }

    /**
     * Returns {@code true} if the JSON value is a {@link String}.
     */
    public boolean isString() {
        return (value != null && value instanceof String);
    }

    /**
     * Returns the JSON value as a {@code String} object. If the JSON value is {@code null},
     * this method returns {@code null}.
     * 
     * @return the string value.
     * @throws JsonValueException if the JSON value is not a string.
     */
    public String asString() throws JsonValueException {
        return (value == null ? null : ((String)expect(String.class).value));
    }

    /**
     * Returns {@code true} if the JSON value is a {@link Number}.
     */
    public boolean isNumber() {
        return (value != null && value instanceof Number);
    }

    /**
     * Returns the JSON value as a {@code Number} object. If the JSON value is {@code null},
     * this method returns {@code null}.
     *
     * @return the numeric value.
     * @throws JsonValueException if the JSON value is not a number.
     */
    public Number asNumber() throws JsonValueException {
        return (value == null ? null : (Number)expect(Number.class).value);
    }

    /**
     * Returns the JSON value as an {@link Integer} object. This may involve rounding or
     * truncation. If the JSON value is {@code null}, this method returns {@code null}.
     *
     * @return the integer value.
     * @throws JsonValueException if the JSON value is not a number.
     */
    public Integer asInteger() throws JsonValueException {
        return (value == null ? null : Integer.valueOf(asNumber().intValue()));
    }

    /**
     * Returns the JSON value as a {@link Double} object. This may involve rounding.
     * If the JSON value is {@code null}, this method returns {@code null}.
     *
     * @return the double-precision floating point value.
     * @throws JsonValueException if the JSON value is not a number.
     */
    public Double asDouble() throws JsonValueException {
        return (value == null ? null : Double.valueOf(asNumber().doubleValue()));
    }

    /**
     * Returns the JSON value as a {@link Long} object. This may involve rounding or
     * truncation. If the JSON value is {@code null}, this method returns {@code null}.
     *
     * @return the long integer value.
     * @throws JsonValueException if the JSON value is not a number.
     */
    public Long asLong() throws JsonValueException {
        return (value == null ? null : Long.valueOf(asNumber().longValue()));
    }

    /**
     * Returns {@code true} if the JSON value is a {@link Boolean}.
     */
    public boolean isBoolean() {
        return (value != null && value instanceof Boolean);
    }

    /**
     * Returns the JSON value as a {@link Boolean} object. If the value is {@code null},
     * this method returns {@code null}.
     *
     * @return the boolean value.
     * @throws JsonValueException if the JSON value is not a boolean type.
     */
    public Boolean asBoolean() throws JsonValueException {
        return (value == null ? null : (Boolean)expect(Boolean.class).value);
    }

    /**
     * Returns {@code true} if the value is {@code null}.
     */
    public boolean isNull() {
        return (value == null);
    }

    /**
     * Returns the JSON string value as an enum constant of the specified enum type.
     * If the JSON value is {@code null}, this method returns {@code null}.
     *
     * @param type the enum type to match constants with the value.
     * @return the enum constant represented by the string value.
     * @throws JsonValueException if the JSON value does not match any of the enum's constants.
     * @throws NullPointerException if {@code type} is {@code null}.
     */
    public <T extends Enum<T>> T asEnum(Class<T> type) throws JsonValueException {
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
            throw new JsonValueException(this, sb.toString());
        }
    }

    /**
     * Returns the JSON string value as a {@code File} object. If the JSON value is
     * {@code null}, this method returns {@code null}.
     *
     * @return a file represented by the string value.
     * @throws JsonValueException if the JSON value is not a string.
     */
    public File asFile() throws JsonValueException {
        String s = asString();
        return (s != null ? new File(s) : null);
    }

    /**
     * Returns the JSON string value as a character set used for byte encoding/decoding.
     * If the JSON value is {@code null}, this method returns {@code null}.
     *
     * @return the character set represented by the string value.
     * @throws JsonValueException if the JSON value is not a string or the character set specified is invalid.
     */
    public Charset asCharset() throws JsonValueException {
        try {
            return (value == null ? null : Charset.forName(asString()));
        } catch (IllegalCharsetNameException icne) {
            throw new JsonValueException(this, icne);
        } catch (UnsupportedCharsetException uce) {
            throw new JsonValueException(this, uce);
        }
    }

    /**
     * Returns the JSON string value as a regular expression pattern. If the JSON value is
     * {@code null}, this method returns {@code null}.
     *
     * @return the compiled regular expression pattern.
     * @throws JsonValueException if the pattern is not a string or the value is not a valid regular expression pattern.
     */
    public Pattern asPattern() throws JsonValueException {
        try {
            return (value == null ? null : Pattern.compile(asString()));
        } catch (PatternSyntaxException pse) {
            throw new JsonValueException(this, pse);
        }
    }

    /**
     * Returns the JSON string value as a uniform resource identifier. If the JSON value is
     * {@code null}, this method returns {@code null}.
     *
     * @return the URI represented by the string value.
     * @throws JsonValueException if the given string violates URI syntax.
     */
    public URI asURI() throws JsonValueException {
        try {
            return (value == null ? null : new URI(asString()));
        } catch (URISyntaxException use) {
            throw new JsonValueException(this, use);
        }
    }


    /**
     * Returns the JSON string value as a JSON pointer. If the JSON value is {@code null},
     * this method returns {@code null}.
     *
     * @return the JSON pointer represented by the JSON value string.
     * @throws JsonValueException if the JSON value is not a string or valid JSON pointer.
     */
    public JsonPointer asPointer() throws JsonValueException {
        try {
            return (value == null ? null : new JsonPointer(asString()));
        } catch (JsonException je) {
            throw (je instanceof JsonValueException ? je : new JsonValueException(this, je));
        }
    }

    /**
     * Defaults the JSON value to the specified value if it is currently {@code null}.
     *
     * @param value the string to default to.
     * @return this JSON value or a new JSON value containing the default value.
     */
    public JsonValue defaultTo(Object value) {
        return (this.value != null ? this : new JsonValue(value, pointer, transformers));
    }

    /**
     * Returns the key as an list index value. If the string does not represent a valid
     * list index value, then {@code -1} is returned.
     *
     * @param key the string key to be converted into an list index value.
     * @return the converted index value, or {@code -1} if invalid.
     */
    private static int toIndex(String key) {
        int result;
        try {
            result = Integer.parseInt(key);
        } catch (NumberFormatException nfe) {
            result = -1;
        }
        return (result < 0 ? -1 : result);
    }

    /**
     * Returns {@code true} if the specified child value is defined within this JSON value.
     *
     * @param key the {@code Map} key or {@code List} index of the child value to seek.
     * @return {@code true} if this JSON value contains the specified child.
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
     * Returns the specified child JSON value. If no such child value exists, then a JSON value
     * containing {@code null} is returned.
     *
     * @param key the {@code Map} key or {@code List} index identifying the child value to return.
     * @return the child value, or a JSON value containing {@code null}.
     * @throws JsonException if a transformer failed to transform the child value.
     */
    public JsonValue get(String key) throws JsonException {
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
        return new JsonValue(result, pointer.child(key), transformers);
    }

    /**
     * Returns the specified child value. If this JSON value is not a {@link List} or if no
     * such child exists, then a JSON value containing a {@code null} is returned.
     *
     * @param index index of child element value to return.
     * @return the child value, or a JSON value containing {@code null}.
     * @throws IndexOutOfBoundsException if {@code index} is negative.
     * @throws JsonException if a transformer failed to transform the child value.
     */
    public JsonValue get(int index) throws JsonException {
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
        return new JsonValue(child, pointer.child(index), transformers);
    }
     
    /**
     * Returns the specified child value. If the specified child value does not exist, then
     * {@code null} is returned.
     *
     * @param pointer the JSON pointer identifying the child value to return.
     * @return the child value, or {@code null} if no such value exists.
     * @throws JsonException if a transformer failed to transform the child value.
     */
    public JsonValue get(JsonPointer pointer) throws JsonException {
        JsonValue result = this;
        for (String token : pointer) {
            JsonValue child = result.get(token);
            if (child.isNull() && !result.isDefined(token)) {
                return null; // undefined value yields null, not a JSON value containing null
            }
            result = child;
        }
        return result;
    }

    /**
     * Sets the value of the specified member.
     * <p>
     * If setting a list element, the specified key must be parseable as an unsigned  base-10
     * integer and be less than or equal to the size of the list.
     *
     * @param key the {@code Map} key or {@code List} index identifying the child value to set.
     * @param value the value to assign to the child member.
     * @throws JsonValueException if this JSON value is not a {@code Map} or {@code List}.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public void put(String key, Object value) throws JsonValueException {
        if (key == null) {
            throw new NullPointerException();
        }
        if (isMap()) {
            asMap().put(key, value);
        } else if (isList()) {
            put(toIndex(key), value);
        } else {
            throw new JsonValueException(this, "expecting Map or List");
        }
    }

    /**
     * Sets the value of the specified child list element.
     *
     * @param index the {@code List} index identifying the child value to set.
     * @param value the value to assign to the list element.
     * @throws JsonValueException if this JSON value is not a {@code List}.
     */
    public void put(int index, Object value) throws JsonValueException {
        List<Object> list = required().asList();
        if (index < 0 || index > list.size()) {
            throw new JsonValueException(this, "index out of range");
        } else if (index == list.size()) { // appending to end of list
            list.add(value);
        } else { // replacing existing element
            list.set(index, value);
        }
    }

    /**
     * Sets the value of the value identified by the specified pointer. If doing so would
     * require the creation of a new object or list, a {@code JsonValueException} will be
     * thrown.
     *
     * @param pointer identifies the child value to set.
     * @param value the value to set.
     * @throws JsonValueException if the specified pointer is invalid.
     */
    public void put(JsonPointer pointer, Object value) throws JsonValueException {
        JsonValue jv = this;
        String[] tokens = pointer.toArray();
        for (int n = 0; n < tokens.length -1; n++) {
            jv = jv.get(tokens[n]).required();
        }
        jv.put(tokens[tokens.length - 1], value);
    }

    /**
     * Removes the specified child value. If the specified child value is not defined, calling
     * this method has no effect.
     *
     * @param key the {@code Map} key or {@code List} index identifying the child value to remove.
     */
    public void remove(String key) {
        if (isMap()) {
            ((Map)value).remove(key);
        } else if (isList()) {
            remove(toIndex(key));
        }
    }

    /**
     * Removes the specified child value, shifting any subsequent elements to the left. If the
     * JSON value is not a {@code List}, calling this method has no effect.
     *
     * @param index the {@code List} index identifying the child value to remove.
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
     * Adds the specified value.
     * <p>
     * If adding to a list value, the specified key must be parseable as an unsigned
     * base-10 integer and be less than or equal to the list size. Adding a value to a list
     * shifts any existing elements at or above the specified index to the right by one.
     *
     * @param key the {@code Map} key or {@code List} index to add.
     * @param value the value to add.
     * @throws JsonValueException if not a {@code Map} or {@code List}, the {@code Map} key already exists, or the {@code List} index is out of range.
     */
    public void add(String key, Object value) throws JsonValueException {
        if (isMap()) {
            if (isDefined(key)) {
                throw new JsonValueException(this, key + " already exists");
            }
            asMap().put(key, value);
        } else if (isList()) {
            add(toIndex(key), value);
        } else {
            throw new JsonValueException(this, "expecting Map or List");
        }
    }

    /**
     * Adds the specified value to the list. Adding a value to a list shifts any existing
     * elements at or above the specified index to the right by one.
     *
     * @param index the {@code List} index of the value to add.
     * @param value the value to add.
     * @throws JsonValueException if this JSON value is not a {@code List} or index is out of range.
     */
    public void add(int index, Object value) throws JsonValueException {
        List<Object> list = required().asList();
        if (index < 0 || index > list.size()) {
            throw new JsonValueException(this, "index out of range");
        } else {
            list.add(index, value);
        }
    }

    /**
     * Removes all child values from this JSON value, if it has any.
     */
    public void clear() throws JsonValueException {
        if (isMap()) {
            asMap().clear();
        } else if (isList()) {
            asList().clear();
        }
    }

    /**
     * Returns the number of child values that this JSON value contains.
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
     * Returns the set of keys for this JSON value's child values. If this value is a
     * {@code Map}, then the order of the resulting keys is undefined. If there are no child
     * values, this method returns an empty set.
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
     * Returns an iterator over the child values that this JSON value contains. If this value
     * is a {@code Map}, then the order of the resulting child values is undefined.
     * <p>
     * Note: calls to the {@code next()} method may throw the runtime {@link JsonException}
     * if any transformers fail to execute.
     */
    @Override
    @SuppressWarnings("unchecked")
    public Iterator<JsonValue> iterator() {
        if (isList()) { // optimize for list
            return new Iterator<JsonValue>() {
                int cursor = 0;
                Iterator<Object> i = ((List)value).iterator();
                @Override public boolean hasNext() {
                    return i.hasNext();
                }
                @Override public JsonValue next() {
                    Object element = i.next();
                    return new JsonValue(element, pointer.child(cursor++), transformers);
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        } else {
            return new Iterator<JsonValue>() {
                Iterator<String> i = keys().iterator();
                @Override public boolean hasNext() {
                    return i.hasNext();
                }
                @Override public JsonValue next() {
                    return get(i.next());
                }
                @Override public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Returns a deep copy of this JSON value.
     * <p>
     * This method applies all transformations while traversing the values's children.
     * Consequently, the resulting copy does not inherit the transformers from this value.
     */
    public JsonValue copy() {
        JsonValue result = new JsonValue(value, pointer); // start with shallow copy
        if (isMap()) {
            HashMap<String, Object> map = new HashMap<String, Object>(size());
            for (String key : keys()) {
                map.put(key, get(key).copy().getValue()); // recursive descent
            }
            result.value = map;
        } else if (isList()) {
            ArrayList<Object> list = new ArrayList<Object>(size());
            for (JsonValue element : this) {
                list.add(element.copy().getValue()); // recursive descent
            }
            result.value = list;
        }
        return result;
    }

    /**
     * Returns a string representation of the JSON value. This method does not apply
     * transformations to the value's children.
     */ 
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
                sb.append(new JsonValue(map.get(key)).toString()); // recursive
                if (i.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" }");
        } else if (isList()) {
            sb.append("[ ");
            for (Iterator<Object> i = asList().iterator(); i.hasNext();) {
                sb.append(new JsonValue(i.next()).toString()); // recursive
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
