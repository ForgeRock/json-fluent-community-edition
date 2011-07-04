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
        listNode.put(3, "d");
        assertThat(listNode.get(new JsonPointer("/0003")).asString()).isEqualTo("d");
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
    public void array() {
        listNode.put(0, "x");
        listNode.put(1, "y");
        assertThat(listNode.get(new JsonPointer("/0")).getValue()).isEqualTo("x");
        assertThat(listNode.get(new JsonPointer("/1")).getValue()).isEqualTo("y");
    }

    @Test
    public void multiDimensionalArray() {
        mapNode.put("a", new ArrayList<Object>());
        mapNode.get("a").put(0, new ArrayList<Object>());
        mapNode.get("a").get(0).put(0, "a00");
        mapNode.get("a").get(0).put(1, "a01");
        mapNode.get("a").put(1, new ArrayList<Object>());
        mapNode.get("a").get(1).put(0, "a10");
        mapNode.get("a").get(1).put(1, "a11");
        assertThat(mapNode.get(new JsonPointer("/a/0/0")).getValue()).isEqualTo("a00");
        assertThat(mapNode.get(new JsonPointer("/a/0/1")).getValue()).isEqualTo("a01");
        assertThat(mapNode.get(new JsonPointer("/a/1/0")).getValue()).isEqualTo("a10");
        assertThat(mapNode.get(new JsonPointer("/a/1/1")).getValue()).isEqualTo("a11");
    }

    // ----- exception unit tests ----------

    @Test(expectedExceptions=JsonException.class)
    public void uriSyntaxException() throws JsonException {
        new JsonPointer("%%%");
    }
}
