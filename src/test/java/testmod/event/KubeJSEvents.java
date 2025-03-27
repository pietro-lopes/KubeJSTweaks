package testmod.event;

import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;

import java.util.HashMap;
import java.util.Map;

public class KubeJSEvents {
    public static final Map<String, RecipesKubeEvent> KUBEJS_EVENTS = new HashMap<>();

    public static void onRecipes(RecipesKubeEvent recipeEvent){
        KUBEJS_EVENTS.put("recipes", recipeEvent);
    }
}
