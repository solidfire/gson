/*
 * Copyright (C) 2008 Google Inc.
 * Modifications Copyright (C) 2016 NetApp Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.solidfire.gson.functional;

import com.solidfire.gson.Gson;
import com.solidfire.gson.JsonArray;
import com.solidfire.gson.JsonObject;
import com.solidfire.gson.JsonParseException;
import com.solidfire.gson.JsonParser;
import com.solidfire.gson.JsonPrimitive;
import com.solidfire.gson.JsonSyntaxException;
import com.solidfire.gson.common.TestTypes.BagOfPrimitives;
import com.solidfire.gson.common.TestTypes.Nested;
import com.solidfire.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Functional tests for that use JsonParser and related Gson methods
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonParserTest extends TestCase {
  private Gson gson;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    gson = new Gson();
  }

  public void testParseInvalidJson() {
    try {
      gson.fromJson("[[]", Object[].class);
      fail();
    } catch (JsonSyntaxException expected) { }
  }

  public void testDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertEquals(11, target.intValue);
    assertEquals("foo", target.stringValue);
  }

  public void testBadTypeForDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    JsonArray array = new JsonArray();
    array.add(obj);
    try {
      gson.fromJson(array, BagOfPrimitives.class);
      fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testBadFieldTypeForCustomDeserializerCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    obj.add("longValue", array);

    try {
      gson.fromJson(obj, BagOfPrimitives.class);
      fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testBadFieldTypeForDeserializingCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject primitive1 = new JsonObject();
    primitive1.addProperty("string", "foo");
    primitive1.addProperty("intValue", 11);

    JsonObject obj = new JsonObject();
    obj.add("primitive1", primitive1);
    obj.add("primitive2", array);

    try {
      gson.fromJson(obj, Nested.class);
      fail("Nested has field BagOfPrimitives which is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testChangingCustomTreeAndDeserializing() {
    StringReader json =
      new StringReader("{'stringValue':'no message','intValue':10,'longValue':20}");
    JsonObject obj = (JsonObject) new JsonParser().parse(json);
    obj.remove("stringValue");
    obj.addProperty("stringValue", "fooBar");
    BagOfPrimitives target = gson.fromJson(obj, BagOfPrimitives.class);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
    assertEquals("fooBar", target.stringValue);
  }

  public void testExtraCommasInArrays() {
    Type type = new TypeToken<List<String>>() {}.getType();
    assertEquals(list("a", null, "b", null, null), gson.fromJson("[a,,b,,]", type));
    assertEquals(list(null, null), gson.fromJson("[,]", type));
    assertEquals(list("a", null), gson.fromJson("[a,]", type));
  }

  public void testExtraCommasInMaps() {
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    try {
      gson.fromJson("{a:b,}", type);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  private <T> List<T> list(T... elements) {
    return Arrays.asList(elements);
  }
}
