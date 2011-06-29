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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// FEST-Assert
import static org.fest.assertions.Assertions.assertThat;

// TestNG
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Paul C. Bryan
 */
public class JsonPointerTest {

    /** Node encapsulating a map. */
    private JsonNode mapNode;

    /** Node encapsulating a list. */
    private JsonNode listNode;

    // ----- preparation ----------

    @BeforeMethod
    public void beforeMethod() {
        mapNode = new JsonNode(new HashMap());
        listNode = new JsonNode(new ArrayList());
    }

    // ----- parsing unit tests ----------

    @Test
    public void identicalPathEquality() {
        JsonPointer p1 = new JsonPointer("/a/b/c");
        JsonPointer p2 = new JsonPointer("/a/b/c");
        assertThat((Object)p1).isEqualTo((Object)p2);
    }

    @Test
    public void differentPathInequality() {
        JsonPointer p1 = new JsonPointer("/a/b/c");
        JsonPointer p2 = new JsonPointer("/d/e/f");
        assertThat((Object)p1).isNotEqualTo((Object)p2);
    }

    @Test
    public void simpleEscape() throws JsonException {
        JsonPointer p1 = new JsonPointer("/a/%65%73%63%61%70%65");
        JsonPointer p2 = new JsonPointer("/a/escape");
        assertThat((Object)p1).isEqualTo((Object)p2);
    }

    @Test
    public void numericIndexLeadingZeroes() {
        List<Object> list = listNode.asList();
        list.add("a");
        list.add("b");
        list.add("c");
        JsonPointer p1 = new JsonPointer("/0003");
        JsonPointer p2 = new JsonPointer("/3");
        listNode.put(p1, "d");
        assertThat(listNode.get(p2).asString()).isEqualTo("d");
    }

    @Test
    public void parseVsStringChildEquality() {
        JsonPointer p1 = new JsonPointer("/a/b/c");
        JsonPointer p2 = new JsonPointer().child("a").child("b").child("c");
        assertThat((Object)p1).isEqualTo((Object)p2);
    }

    @Test
    public void parseVsIntegerChildEquality() {
        JsonPointer p1 = new JsonPointer("/1/2");
        JsonPointer p2 = new JsonPointer().child(1).child(2);
        assertThat((Object)p1).isEqualTo((Object)p2);
    }

    @Test
    public void slashEncoded() {
        JsonPointer p1 = new JsonPointer().child("a/b").child("c");
        assertThat(p1.toString()).isEqualTo("/a%2Fb/c");
    }

    // ----- manipulation unit tests ----------

    @Test
    @SuppressWarnings("unchecked")
    public void getProperty() {
        Map<String, Object> m = mapNode.asMap();
        m.put("a", (m = new HashMap<String, Object>()));
        m.put("b", (m = new HashMap<String, Object>()));
        m.put("c", "d");
        assertThat(mapNode.get(new JsonPointer("/a/b/c")).asString()).isEqualTo("d");
    }

    @Test
    public void putProperty() {
        mapNode.put(new JsonPointer("/a/b/c"), "d");
        assertThat(mapNode.get("a").get("b").get("c").asString()).isEqualTo("d");
    }

    @Test
    public void deepRemoveProperty() {
        JsonPointer pointer = new JsonPointer("/a/b/c");
        mapNode.put(pointer, "d");
        mapNode.remove(pointer);
        assertThat(mapNode.get("a").isDefined("b")).isTrue();
        assertThat(mapNode.get(new JsonPointer("/a/b")).isDefined("c")).isFalse();
    }

    @Test
    public void shallowRemoveProperty() {
        mapNode.put(new JsonPointer("/a/b/c/d/e"), "f");
        mapNode.remove(new JsonPointer("/a/b/c"));
        assertThat(mapNode.get(new JsonPointer("/a/b")).isMap()).isTrue();
        assertThat(mapNode.get(new JsonPointer("/a/b")).isDefined("c")).isFalse();
    }

    @Test
    public void putArray() {
        listNode.put(new JsonPointer("/0"), "x");
        listNode.put(new JsonPointer("/1"), "y");
        assertThat(listNode.get(0).asString()).isEqualTo("x");
        assertThat(listNode.get(1).asString()).isEqualTo("y");
    }

    @Test
    public void multidimensionalArrays() {
        mapNode.put("a", new ArrayList<Object>());
        mapNode.put(new JsonPointer("/a/0"), new ArrayList<Object>());
        mapNode.put(new JsonPointer("/a/1"), new ArrayList<Object>());
        mapNode.put(new JsonPointer("/a/0/0"), "a00");
        mapNode.put(new JsonPointer("/a/0/1"), "a01");
        mapNode.put(new JsonPointer("/a/1/0"), "a10");
        mapNode.put(new JsonPointer("/a/1/1"), "a11");
        List a0 = mapNode.get("a").get(0).asList();
        assertThat(a0.get(0)).isEqualTo("a00");
        assertThat(a0.get(1)).isEqualTo("a01");
        List a1 = mapNode.get("a").get(1).asList();
        assertThat(a1.get(0)).isEqualTo("a10");
        assertThat(a1.get(1)).isEqualTo("a11");
    }

    // ----- exception unit tests ----------

    @Test(expectedExceptions=JsonException.class)
    public void uriSyntaxException() throws JsonException {
        new JsonPointer("%%%");
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void putRoot() throws JsonNodeException {
        mapNode.put(new JsonPointer(), "foo");
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void removeRoot() throws JsonNodeException {
        mapNode.remove(new JsonPointer());
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void sparseAllocation() throws JsonNodeException {
        listNode.put(new JsonPointer("/2"), "i feel sparse");
    }
}
