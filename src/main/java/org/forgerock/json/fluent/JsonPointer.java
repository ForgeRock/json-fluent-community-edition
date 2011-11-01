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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TODO: Description.
 *
 * @author Paul C. Bryan
 */
public class JsonPointer implements Iterable<String> {

    /** TODO: Description. */
    private String[] tokens = new String[0];

    /**
     * TODO: Description.
     */
    public JsonPointer() {
        // empty tokens represents pointer to root value
    }

    /**
     * TODO: Description.
     *
     * @param pointer TODO.
     * @throws JsonPointerException TODO.
     */
    public JsonPointer(String pointer) throws JsonException {
        ArrayList<String> list = new ArrayList<String>();
        String[] split = pointer.split("/", -1);
        for (int n = 0; n < split.length; n++) {
            if (n == 0 && split[n].length() == 0) {
                continue; // leading slash ignored
            }
            list.add(decode(split[n]));
        }
        tokens = list.toArray(tokens);
    }

    /**
     * TODO: Description.
     *
     * @param tokens TODO.
     */
    public JsonPointer(String[] tokens) {
        this.tokens = Arrays.copyOf(tokens, tokens.length);
    }

    /**
     * TODO: Description.
     *
     * @param iterable TODO.
     */
    public JsonPointer(Iterable<String> iterable) {
        ArrayList<String> list = new ArrayList<String>();
        for (String element : iterable) {
            list.add(element);
        }
        tokens = list.toArray(tokens);
    }

    /**
     * TODO: Description.
     *
     * @param value TODO.
     * @return TODO.
     */
    private String encode(String value) {
        try {
            return new URI(null, null, null, null, value).toASCIIString().substring(1).replaceAll("/", "%2F");
        } catch (URISyntaxException use) { // shouldn't happen
            throw new IllegalStateException(use.getMessage());
        }
    }

    /**
     * TODO: Description.
     *
     * @param value TODO.
     * @return TODO.
     * @throws JsonException TODO.
     */
    private String decode(String value) throws JsonException {
        try {
            return new URI("#" + value).getFragment();
        } catch (URISyntaxException use) {
            throw new JsonException(use.getMessage());
        }
    }

    /**
     * TODO: Description.
     */
    public int size() {
        return tokens.length;
    }

    /**
     * TODO: Description.
     *
     * @param index TODO.
     * @return TODO.
     * @throws IndexOutOfBoundsException TODO.
     */
    public String get(int index) {
        if (index < 0 || index >= tokens.length) {
            throw new IndexOutOfBoundsException();
        }
        return tokens[index];
    }

    /**
     * TODO: Description.
     */
    public String[] toArray() {
        return Arrays.copyOf(tokens, tokens.length);
    }

    /**
     * TODO: Description.
     */
    public JsonPointer parent() {
        JsonPointer parent = null;
        if (this.tokens.length > 0) {
            parent = new JsonPointer();
            parent.tokens = Arrays.copyOf(this.tokens, this.tokens.length - 1);
        }
        return parent;
    }

    /**
     * TODO: Description.
     */
    public String leaf() {
        return (tokens.length > 0 ? tokens[tokens.length - 1]: null);
    }

    /**
     * TODO: Description.
     *
     * @param child TODO.
     * @return TODO.
     * @throws NullPointerException if {@code child} is {@code null}.
     */
    public JsonPointer child(String child) {
        if (child == null) {
            throw new NullPointerException();
        }
        JsonPointer pointer = new JsonPointer();
        pointer.tokens = Arrays.copyOf(this.tokens, this.tokens.length + 1);
        pointer.tokens[pointer.tokens.length - 1] = child;
        return pointer;
    }

    /**
     * TODO: Description.
     *
     * @param child TODO.
     * @return TODO.
     * @throws IndexOutOfBoundsException if {@code child} is less than zero.
     */
    public JsonPointer child(int child) throws IndexOutOfBoundsException {
        if (child < 0) {
            throw new IndexOutOfBoundsException();
        }
        return child(Integer.toString(child));
    }

    /**
     * TODO: Description.
     */
    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            int cursor = 0;
            @Override public boolean hasNext() {
                return cursor < tokens.length;
            }
            @Override public String next() {
                if (cursor >= tokens.length) {
                    throw new NoSuchElementException();
                }
                return tokens[cursor++];
            }
            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * TODO: Description.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String token : tokens) {
            sb.append('/').append(encode(token));
        }
        return sb.toString();
    }

    /**
     * TODO: Description.
     *
     * @param o TODO.
     * @return TODO.
     */
    @Override
    public boolean equals(Object o) {
        return (o != null && o instanceof JsonPointer && Arrays.equals(tokens, ((JsonPointer)o).tokens));
    }

    /**
     * TODO: Description.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(tokens);
    }
}
