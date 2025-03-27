package testmod;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.LinkedHashMap;
import java.util.List;

public final class Utils {

    public static Object unwrap(JsonElement json){
        return switch (json) {
            case JsonObject obj -> resolveObj(obj);
            case JsonArray array -> resolveArray(array);
            case JsonPrimitive prim -> resolvePrimitive(prim);
//            case JsonNull nil -> null;
            default -> throw new IllegalStateException("Unexpected value: " + json);
        };
    }

    private static Object resolvePrimitive(JsonPrimitive prim) {
        if (prim.isBoolean()) return prim.getAsBoolean();
        if (prim.isString()) return prim.getAsString();
        if (prim.isNumber()) {
            var valString = prim.getAsNumber().toString();
            if (valString.endsWith(".0")) return valString.substring(0, valString.length() - 2);
            return valString;
        }
        return null;
    }

    private static LinkedHashMap<String, Object> resolveObj(JsonObject obj){
        var map = new LinkedHashMap<String, Object>();
        for (var entry : obj.entrySet()){
            map.put(entry.getKey(), unwrap(entry.getValue()));
        }
        return map;
    }

    private static List<Object> resolveArray(JsonArray array){
        return array.asList().stream().map(Utils::unwrap).toList();
    }
}

