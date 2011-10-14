package org.forgerock.json.fluent;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author $author$
 * @version $Revision$ $Date$
 */
public class JsonNodeTest {
    @Test
    public void testPutWithJsonPointer() throws Exception {

        List<String> listObject1 = new ArrayList<String>(3);
        listObject1.add("valueA");
        listObject1.add("valueB");
        listObject1.add("valueC");

        Map<String, Object> mapObject1 = new HashMap<String, Object>();
        mapObject1.put("keyE", "valueE");
        mapObject1.put("keyF", listObject1);
        mapObject1.put("keyG", "valueG");

        Map<String, Object> mapObject2 = new HashMap<String, Object>();
        mapObject2.put("keyH", "valueH");
        mapObject2.put("keyI", "valueI");
        mapObject2.put("keyJ", mapObject1);


        List<String> listObject2 = new ArrayList<String>(3);
        listObject2.add("valueD");
        listObject2.add("valueE");
        listObject2.add("valueF");
        JsonNode listNode = new JsonNode(listObject1);
        JsonNode mapNode = new JsonNode(new HashMap());
        mapNode.put("keyA", "valueA");
        mapNode.put("keyB", "valueB");
        mapNode.put("keyC", "valueC");
        mapNode.add("keyD", listObject2);
        mapNode.add("keyE", mapObject2);


        mapNode.put(new JsonPointer("/keyA"), "testValueA");
        Assert.assertEquals(mapNode.get(new JsonPointer("/keyA")).getValue(), "testValueA");

        listNode.put(new JsonPointer("/1"), "testValueA");
        Assert.assertEquals(listNode.get(new JsonPointer("/1")).getValue(), "testValueA");

        mapNode.put(new JsonPointer("/keyD/0"), "testValueD");
        Assert.assertEquals(mapNode.get(new JsonPointer("/keyD/0")).getValue(), "testValueD");

        mapNode.put(new JsonPointer("/keyE/keyH"), "testValueH");
        Assert.assertEquals(mapNode.get(new JsonPointer("/keyE/keyH")).getValue(), "testValueH");

        mapNode.put(new JsonPointer("/keyE/keyJ/keyF/2"), "testValueH");
        Assert.assertEquals(mapNode.get(new JsonPointer("/keyE/keyJ/keyF/2")).getValue(), "testValueH");

    }
}
