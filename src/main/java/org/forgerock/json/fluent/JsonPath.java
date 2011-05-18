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
 * Copyright © 2010 ApexIdentity Inc. All rights reserved.
 * Portions Copyrighted 2011 ForgeRock AS.
 */

package org.forgerock.json.fluent;

// Java Standard Edition
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.SourceVersion;

/**
 * Implements the <a href="http://goessner.net/articles/JsonPath/">JSONPath</a> expression
 * syntax for selecting and manipulating nodes in a JSON object model structure.
 * <p>
 * This version currently implements only a subset of JSONPath expressions that concretely
 * reference nodes: the root {@code $}, object property {@code .} {@code []} and array
 * subscript {@code []} operators. A future version is intended to support the full JSONPath
 * expression syntax.
 *
 * @author Paul C. Bryan
 */
public class JsonPath {

    /** A concrete root {@code "$"} JSON path. */
    public static final JsonPath ROOT = new JsonPath();

    /** The concrete elements of the path. */
    private ArrayList<Object> elements = new ArrayList<Object>();

    /**
     * Constructs a root JSON path.
     */
    private JsonPath() {
        // root by virtue of being empty
    }

    /**
     * Constructs a JSON path from a string.
     *
     * @param path a string representation of the JSON path.
     * @throws NullPointerException if {@code path} is {@code null}.
     * @throws JsonPathException if {@code path} is malformed.
     */
    public JsonPath(String path) throws JsonPathException {
        int length = path.length();
        int cursor = 0;
        while (cursor < length) {
            if (path.charAt(cursor) == '[') {
                cursor += parseSubscript(path, cursor);
            }
            else {
                cursor += parseIdentifier(path, cursor);
            }
        }
        if (elements.size() == 0 || !"$".equals(elements.get(0))) {
            throw new JsonPathException("JSONPath must begin with $", path, 0);
        }
        elements.remove(0); // remove explicit root element
    }

    /**
     * Constructs a JSON path from an existing parent path and child property.
     *
     * @param parent the parent JSON path.
     * @param property the child property.
     * @throws NullPointerException if either {@code parent} or {@code property} are {@code null}.
     */
    public JsonPath(JsonPath parent, String property) {
        if (parent == null || property == null) {
            throw new NullPointerException();
        }
        elements.addAll(parent.elements);
        elements.add(property);
    }

    /**
     * Constructs a JSON path from an existing parent path and child subscript.
     *
     * @param parent the parent JSON path.
     * @param subscript the child subscript.
     * @throws NullPointerException if either {@code parent} or {@code subscript} are {@code null}.
     */
    public JsonPath(JsonPath parent, Integer subscript) {
        if (parent == null || subscript == null) {
            throw new NullPointerException();
        }
        elements.addAll(parent.elements);
        elements.add(subscript);
    }

    /**
     * TODO: Description.
     *
     * @param path TODO.
     * @param index TODO.
     * @return TODO.
     * @throws JsonPathException TODO.
     */
    private int parseSubscript(String path, int index) throws JsonPathException {
        int cursor = index + 1; // skip left square bracket
        if (cursor >= path.length()) {
            throw new JsonPathException("illegal subscript", path, index);
        }
        char c = path.charAt(cursor);
        if (c == '\'' || c == '"') { // allows single or double quotation marks 
            return parseQuotedSubscript(path, cursor) + 1; // add left square bracket
        }
        else {
            return parseNumericSubscript(path, cursor) + 1; // add left square bracket
        }
    }

    /**
     * TODO: Description.
     *
     * @param path TODO.
     * @param index TODO.
     * @return TODO.
     * @throws JsonPathException TODO.
     */
    private int parseQuotedSubscript(String path, int index) throws JsonPathException {
        int length = path.length();
        int cursor = index;
        boolean escaped = false;
        char quote = path.charAt(cursor++);
        StringBuilder sb = new StringBuilder();
        while (cursor < length) {
            int cp = path.codePointAt(cursor);
            if (escaped) {
                sb.appendCodePoint(cp);
                escaped = false;
            }
            else if (cp == quote) {
                break;
            }
            else if (cp == '\\') {
                escaped = true;
            }
            else {
                sb.appendCodePoint(cp);
            }
            cursor += Character.charCount(cp);
        }
        if (cursor + 1 >= length || path.charAt(cursor + 1) != ']' || sb.length() == 0) {
            throw new JsonPathException("illegal quoted subscript", path, index);
        }
        elements.add(sb.toString());
        return cursor - index + 2; // add close quote and right square bracket 
    }

    /**
     * TODO: Description.
     *
     * @param path TODO.
     * @param index TODO.
     * @return TODO.
     * @throws JsonPathException TODO.
     */
    private int parseNumericSubscript(String path, int index) throws JsonPathException {
        int length = path.length();
        int cursor = index;
        int subscript = -1;
        while (cursor < length) {
            char c = path.charAt(cursor);
            if (c == ']') {
                break;
            }
            int digit = c - '0';
            if (digit < 0 || digit > 9) {
                throw new JsonPathException("illegal subscript index", path, cursor);
            }
            subscript = (subscript == -1 ? 0 : subscript) * 10 + digit; // add digit
            cursor++;
        }
        if (cursor >= length || path.charAt(cursor) != ']' || subscript < 0) {
            throw new JsonPathException("illegal subscript index", path, index);
        }
        elements.add(new Integer(subscript));
        return cursor - index + 1; // add right square bracket
    }

    /**
     * TODO: Description.
     *
     * @param path TODO.
     * @param index TODO.
     * @return TODO.
     * @throws JsonPathException TODO.
     */
    private int parseIdentifier(String path, int index) throws JsonPathException {
        int length = path.length();
        int cursor = index;
        if (path.charAt(cursor) == '.') {
            cursor++;
        }
        StringBuilder sb = new StringBuilder();
        while (cursor < length) {
            int cp = path.codePointAt(cursor);
            if (cp == '.' || cp == '[') {
                break;
            }
            sb.appendCodePoint(cp); // yes, a forgiving identifier parser
            cursor += Character.charCount(cp);
        }
        if (sb.length() == 0) {
            throw new JsonPathException("illegal identifier", path, index);
        }
        elements.add(sb.toString());
        return cursor - index;
    }

    /**
     * Returns the parent node of this path. This allows the container node to be located
     * so that operations can be performed against it.
     *
     * @param root the root node to apply the path expression to.
     * @return the parent node of this path.
     * @throws JsonNodeException TODO.
     */
    private JsonNode parentNode(JsonNode root) throws JsonNodeException {
        JsonNode node = root;
        for (int n = 0; n < elements.size() - 1; n++) { // exclude child
            node = node.get(elements.get(n));
        }
        return node;
    }

   /**
     * Returns {@code true} if the path expression is a concrete reference to a node. An
     * expression is concrete if it identifies a specific node using only the root {@code $},
     * child {@code .} {@code []} and/or subscript {@code []} operators.
     */ 
    public boolean isConcrete() {
        return true; // currently only concrete path expressions are supported
    }

    /**
     * Returns a set of nodes in the specified root node that match the path expression.
     *
     * @param root the root node to apply the path expression to.
     * @return a list of nodes within the root node that match the path expression.
     * @throws NullPointerException if {@code root} is {@code null}.
     */
    public Set<JsonNode> select(JsonNode root) {
        HashSet<JsonNode> list = new HashSet<JsonNode>(1);
        try {
            JsonNode node = get(root);
            if (node != null) {
                list.add(node);
            }
        }
        catch (JsonNodeException jne) {
            // failure to traverse to the parent means not selected
        }
        return list;
    }

    /**
     * Gets a node specified by the concrete path expression, or {@code null} if the node
     * is not defined.
     * <p>
     * If this path expression is not concrete, then a {@link JsonNodeException} is thrown.
     *
     * @param root the root node to apply the concrete path expression to.
     * @throws JsonNodeException if this path is not concrete or the object structure is incompatible with this path.
     * @return the node specified by the path, or {@code null} if not defined.
     */
    public JsonNode get(JsonNode root) throws JsonNodeException {
        if (root == null) {
            throw new NullPointerException();
        }
        if (!isConcrete()) {
            throw new JsonNodeException(root, "path must be concrete to get a node");
        }
        if (elements.size() == 0) {
            return root; // self-referential
        }
        JsonNode parent = parentNode(root);
        Object key = elements.get(elements.size() - 1);
        return (parent.isDefined(key) ? parent.get(key) : null);
    }

    /**
     * Assigns a value to a node specified by this concrete path expression. Any missing
     * intermediate nodes will be created in the process.
     * <p>
     * If this path expression is not concrete, then a {@link JsonNodeException} is thrown.
     *
     * @param root the root node to apply the concrete path expression to.
     * @param value the value to assign to the node.
     * @throws JsonNodeException if this path is not concrete or the object structure is incompatible with this path.
     * @throws NullPointerException if {@code root} is {@code null}.
     */
    public void put(JsonNode root, Object value) throws JsonNodeException {
        if (root == null) {
            throw new NullPointerException();
        }
        if (!isConcrete()) {
            throw new JsonNodeException(root, "path must be concrete to modify a node");
        }
        if (elements.size() == 0) {
            throw new JsonNodeException(root, "cannot overwrite root object");
        }
        JsonNode node = root;
        for (int n = 0; n < elements.size() - 1; n++) { // traverse up to, not including, child 
            Object key = elements.get(n);
            if (!node.isDefined(key)) { // intermediate node not defined yet
                Object childKey = elements.get(n + 1); // determine node type based on child key
                if (childKey instanceof String) {
                    node.put(key, new HashMap());
                }
                else {
                    node.put(key, new ArrayList());
                }
            }
            node = node.get(key);
        }
        node.put(elements.get(elements.size() - 1), value);
    }

    /**
     * Removes a node specified by this concrete path expression.
     * <p>
     * If this path expression is not concrete, then a {@link JsonNodeException} is thrown.
     *
     * @param root the root node to apply the concrete path expression to.
     * @throws JsonNodeException if this path is not concrete or the object structure is incompatible with this path.
     * @throws NullPointerException if {@code root} is {@code null}.
     */
    public void remove(JsonNode root) throws JsonNodeException {
        if (root == null) {
            throw new NullPointerException();
        }
        if (!isConcrete()) {
            throw new JsonNodeException(root, "path must be concrete to remove a node");
        }
        if (elements.size() == 0) {
            throw new JsonNodeException(root, "cannot remove root object");
        }
        parentNode(root).remove(elements.get(elements.size() - 1));
    }

    /**
     * Returns a string representation of the JSON path.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("$");
        for (Object o : elements) {
            if (o instanceof String) {
                String s = (String)o;
                if (SourceVersion.isIdentifier(s)) { // dot notation for identifier
                    sb.append('.').append(s);
                }
                else {
                    int length = s.length();
                    sb.append("['");
                    for (int n = 0, cp; n < length; n += Character.charCount(cp)) {
                        cp = s.codePointAt(n);
                        if (cp == '\'' || cp == '\\') {
                            sb.append('\\');
                        }
                        sb.appendCodePoint(cp);
                    }
                    sb.append("']");
                }
            }
            else if (o instanceof Integer) {
                sb.append('[').append(((Integer)o).toString()).append(']');
            }
            else {
                throw new IllegalStateException("unknown path element type");
            }
        }
        return sb.toString();
    }

    /**
     * Returns the hash code value for the path.
     */
    @Override
    public int hashCode() {
        return elements.hashCode();
    }

    /**
     * Compares the specified object with the path for equality.
     *
     * @param o object to be compared for equality with the path.
     * @return true if the specified object is equal to the path.
     */
    @Override
    public boolean equals(Object o) {
        return (o == this || (o != null && o instanceof JsonPath && this.elements.equals(((JsonPath)o).elements)));
    }

}

