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

// FEST-Assert
import static org.fest.assertions.Assertions.assertThat;

// TestNG
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Paul C. Bryan
 */
public class JsonPatchTest {

    /** Node encapsulating a map. */
    private JsonNode mapNode;

    /** Node encapsulating a list. */
    private JsonNode listNode;

    /** TODO: Description. */
    private JsonNode n1;

    /** TODO: Description. */
    private JsonNode n2;

    /** TODO: Description. */
    private JsonNode diff;

    // ----- preparation ----------

    @BeforeMethod
    public void beforeMethod() {
        mapNode = new JsonNode(new HashMap());
        listNode = new JsonNode(new ArrayList());
        n1 = null;
        n2 = null;
    }

    // ----- happy path unit tests ----------

    @Test
    public void removeMapItem() {
        n1 = mapNode;
        n1.put("a", "1");
        n1.put("b", "2");
        n2 = n1.copy();
        n2.remove("b");
        diff = JsonPatch.diff(n1, n2);
        assertThat(diff.size()).isEqualTo(1);
        JsonPatch.patch(n1, diff);
        assertThat(JsonPatch.diff(n1, n2).size()).isEqualTo(0);
        assertThat(n1.isDefined("b")).isFalse();
    }

    @Test
    public void addMapItem() {
        n1 = mapNode;
        n1.put("a", "b");
        n2 = n1.copy();
        n2.put("c", "d");
        diff = JsonPatch.diff(n1, n2);
        assertThat(diff.size()).isEqualTo(1);
        JsonPatch.patch(n1, diff);
        assertThat(n1.get("c").getValue()).isEqualTo("d");
        assertThat(JsonPatch.diff(n1, n2).size()).isEqualTo(0);
    }

    @Test
    public void replaceMapItem() {
        n1 = mapNode;
        n1.put("a", "b");
        n1.put("c", "d");
        n2 = n1.copy();
        n2.put("a", "e");
        diff = JsonPatch.diff(n1, n2);
        assertThat(diff.size()).isEqualTo(1);
        JsonPatch.patch(n1, diff);
        assertThat(n1.get("a").getValue()).isEqualTo("e");
        assertThat(JsonPatch.diff(n1, n2).size()).isEqualTo(0);
    }

    @Test
    public void mapDiffNoChanges() {
        n1 = mapNode;
        n1.put("foo", "bar");
        n1.put("boo", "far");
        n2 = n1.copy();
        diff = JsonPatch.diff(n1, n2);
        assertThat(diff.size()).isEqualTo(0);
        JsonPatch.patch(n1, diff);
        assertThat(JsonPatch.diff(n1, n2).size()).isEqualTo(0);
    }

    @Test
    public void listDiffNoChanges() {
        n1 = mapNode;
        n1.put("a", new ArrayList<Object>());
        n1.get("a").put(0, "foo");
        n1.get("a").put(1, "bar");
        n2 = n1.copy();
        diff = JsonPatch.diff(n1, n2);
        assertThat(diff.size()).isEqualTo(0);
        JsonPatch.patch(n1, diff);
        assertThat(JsonPatch.diff(n1, n2).size()).isEqualTo(0);
    }

    // ----- exception unit tests ----------

    @Test(expectedExceptions=JsonNodeException.class)
    public void replaceNonExistentMapItem() {
        n1 = mapNode;
        n1.put("a", "1");
        n2 = n1.copy();
        n2.put("a", "2");
        diff = JsonPatch.diff(n1, n2);
        n1.clear();
        JsonPatch.patch(n1, diff);
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void addExistentMapItem() {
        n1 = mapNode;
        n2 = n1.copy();
        n2.put("a", "b");
        diff = JsonPatch.diff(n1, n2);
        n1 = n2.copy();
        JsonPatch.patch(n1, diff);
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void removeNonExistentMapItem() {
        n1 = mapNode;
        n1.put("a", "1");
        n2 = n1.copy();
        n2.clear();
        diff = JsonPatch.diff(n1, n2);
        n1.clear();
        JsonPatch.patch(n1, diff);
    }
}
