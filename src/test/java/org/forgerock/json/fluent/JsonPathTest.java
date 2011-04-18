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
public class JsonPathTest {

    /** Root node to use in node manipulation testing. */
    private JsonNode root;

    // ----- preparation ----------

    @BeforeMethod
    public void beforeMethod() throws JsonNodeException, JsonPathException {
        root = new JsonNode(new HashMap(), new JsonPath("$"));
    }

    // ----- parsing unit tests ----------

    @Test
    public void identicalPathEquality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a.b.c");
        JsonPath p2 = new JsonPath("$.a.b.c");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void differentPathInequality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a.b.c");
        JsonPath p2 = new JsonPath("$.d.e.f");
        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    public void simpleMixedSyntaxEquality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a.b.c");
        JsonPath p2 = new JsonPath("$.a['b']['c']");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void simpleEscape() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a['\\e\\s\\c\\a\\p\\e']");
        JsonPath p2 = new JsonPath("$.a.escape");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void singleQuotesStringEscape() throws JsonPathException {
        JsonPath p1 = new JsonPath("$[\"b'c\"]");
        JsonPath p2 = new JsonPath("$['b\\'c']");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void doubleQuotesStringEscape() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a[\"b\\\"c\"]");
        JsonPath p2 = new JsonPath("$.a['b\"c']");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void complexSyntaxRepresentationEquality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a_b.c['['][1].d'e.f-g.h.i[2][\"j\"]['k']['\\\'']['\"'][\"\\\"\"]");
        JsonPath p2 = new JsonPath(p1.toString());
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void numericIndexLeadingZeroes() throws JsonPathException {
        JsonPath p1 = new JsonPath("$[00000000001701]");
        JsonPath p2 = new JsonPath("$[1701]");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void numericVsStringSubscriptInequality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$[1701]");
        JsonPath p2 = new JsonPath("$['1701']");
        assertThat(p1).isNotEqualTo(p2);
    }

    @Test
    public void parseVsStringSubscriptEquality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a.b.c");
        JsonPath p2 = new JsonPath(new JsonPath(new JsonPath(new JsonPath("$"), "a"), "b"), "c");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void parseVsIntegerSubscriptEquality() throws JsonPathException {
        JsonPath p1 = new JsonPath("$[1][2]");
        JsonPath p2 = new JsonPath(new JsonPath(new JsonPath("$"), Integer.valueOf(1)), Integer.valueOf(2));
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void escapeBackslashes() throws JsonPathException {
        JsonPath p1 = new JsonPath("$.a['b\\\\c']");
        JsonPath p2 = new JsonPath(new JsonPath(new JsonPath("$"), "a"), "b\\c");
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void slashSlashBackslashEscapeReturnHome() throws JsonPathException {
        JsonPath p = new JsonPath("$['//\\\\\u001b\r\u0001']");
    }

    // ----- manipulation unit tests ----------

    @Test
    public void getProperty() throws JsonNodeException, JsonPathException {
        Map r = root.asMap();
        r.put("x", new HashMap());
        Map x = (Map)r.get("x");
        x.put("y", new HashMap());
        Map y = (Map)x.get("y");
        y.put("z", "foo");
        JsonPath path = new JsonPath("$.x.y.z");
        assertThat(path.get(root).asString()).isEqualTo("foo");
    }

    @Test
    public void putProperty() throws JsonNodeException, JsonPathException {
        JsonPath path = new JsonPath("$.a.b.c");
        path.put(root, "d");
        assertThat(root.get("a").get("b").get("c").asString()).isEqualTo("d");
    }

    @Test
    public void deepRemoveProperty() throws JsonNodeException, JsonPathException {
        new JsonPath("$.a.b.c").put(root, "d");
        assertThat(root.get("a").get("b").isDefined("c")).isTrue();
        assertThat(new JsonPath("$.a.b.c").get(root).isDefined("d")).isFalse();
    }

    @Test
    public void shallowRemoveProperty() throws JsonNodeException, JsonPathException {
        new JsonPath("$.a.b.c.d.e").put(root, "f");
        new JsonPath("$.a.b.c").remove(root);
        assertThat(new JsonPath("$.a.b").get(root).isMap()).isTrue();
        assertThat(new JsonPath("$.a.b").get(root).isDefined("c")).isFalse();
    }

    @Test
    public void putArray() throws JsonNodeException, JsonPathException {
        new JsonPath("$.a[0]").put(root, "x");
        new JsonPath("$.a[1]").put(root, "y");
        new JsonPath("$.a[2]").put(root, "z");
        List l = root.get("a").asList();
        assertThat(l.get(0)).isEqualTo("x");
        assertThat(l.get(1)).isEqualTo("y");
        assertThat(l.get(2)).isEqualTo("z");
    }

    @Test
    public void multidimensionalArrays() throws JsonNodeException, JsonPathException {
        new JsonPath("$.b[0][0]").put(root, "b00");
        new JsonPath("$.b[0][1]").put(root, "b01");
        new JsonPath("$.b[1][0]").put(root, "b10");
        new JsonPath("$.b[1][1]").put(root, "b11");
        List b0 = root.get("b").get(0).asList();
        assertThat(b0.get(0)).isEqualTo("b00");
        assertThat(b0.get(1)).isEqualTo("b01");
        List b1 = root.get("b").get(1).asList();
        assertThat(b1.get(0)).isEqualTo("b10");
        assertThat(b1.get(1)).isEqualTo("b11");
    }

    // ----- exception unit tests ----------

    @Test(expectedExceptions=NullPointerException.class)
    public void nullPath() throws JsonPathException {
        JsonPath p = new JsonPath(null);
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void missingRoot() throws JsonPathException {
        JsonPath p = new JsonPath("a.b");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void trailingFullStop() throws JsonPathException {
        JsonPath p = new JsonPath("$.a.");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void emptyIdentitifer() throws JsonPathException {
        JsonPath p = new JsonPath("$.a..b");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void trailingLeftSquareBracket() throws JsonPathException {
        JsonPath p = new JsonPath("$.a[");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void numericSubscriptMissingRightSquareBracket() throws JsonPathException {
        JsonPath p = new JsonPath("$.a[1");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void quotedSubscriptMissingClosingQuote() throws JsonPathException {
        JsonPath p = new JsonPath("$.a['b]");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void quotedSubscriptMissingRightSquareBracket() throws JsonPathException {
        JsonPath p = new JsonPath("$.a['b'");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void unquotedSubscript() throws JsonPathException {
        JsonPath p = new JsonPath("$.a[b]");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void emptySubscript() throws JsonPathException {
        JsonPath p = new JsonPath("$.a[]");
    }

    @Test(expectedExceptions=JsonPathException.class)
    public void emptyQuotedSubscript() throws JsonPathException {
        JsonPath p = new JsonPath("$.a['']");
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void putRoot() throws JsonNodeException, JsonPathException {
        JsonPath p = new JsonPath("$");
        p.put(root, "foo");
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void removeRoot() throws JsonNodeException, JsonPathException {
        JsonPath p = new JsonPath("$");
        p.remove(root);
    }

    @Test(expectedExceptions=JsonNodeException.class)
    public void spareAllocation() throws JsonNodeException, JsonPathException {
        JsonPath p = new JsonPath("$.a.b.c[20]");
        p.put(root, "i feel sparse");
    }
}
