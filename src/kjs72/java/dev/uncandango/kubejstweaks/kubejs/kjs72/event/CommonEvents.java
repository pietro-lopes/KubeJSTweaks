package dev.uncandango.kubejstweaks.kubejs.event;

import dev.latvian.mods.kubejs.bindings.event.ServerEvents;
import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.data.GeneratedDataStage;

public class CommonEvents {

    public static void listenKubeEvent(){
        //ServerEvents.RECIPES_AFTER_LOADED.listenJava(ScriptType.SERVER, null, CommonEvents::afterRecipeLoaded);
        ServerEvents.GENERATE_DATA.listenJava(ScriptType.SERVER, GeneratedDataStage.LAST,CommonEvents::onGenerateData);
    }

    public static Object onGenerateData(KubeEvent event){
        if (event instanceof KubeDataGenerator generator) {
            if (KJSTEvents.noOp.hasListeners()) {
                KJSTEvents.noOp.post(new NoOpEventJS(generator));
            }
        }
        return EventResult.PASS;
    }

//    public static Object afterRecipeLoaded(KubeEvent event){
//        if (DumpErroringRecipes.isEnabled()) {
//            KubeJSTweaks.LOGGER.info("Dumping erroring recipes!");
//            try {
//                DumpErroringRecipes.dumpToJsonFiles();
//            } catch (IOException e) {
//                KubeJSTweaks.LOGGER.error("Error while dumping json files", e);
//            }
//        }
//        DumpErroringRecipes.disable();
//        return EventResult.PASS;
//    }
}
