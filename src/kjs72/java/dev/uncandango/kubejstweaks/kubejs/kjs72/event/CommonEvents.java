package dev.uncandango.kubejstweaks.kubejs.kjs72.event;


import dev.latvian.mods.kubejs.event.EventResult;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.generator.KubeDataGenerator;
import dev.latvian.mods.kubejs.plugin.builtin.event.ServerEvents;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.data.GeneratedDataStage;
import dev.uncandango.kubejstweaks.KubeJSTweaks;
import dev.uncandango.kubejstweaks.kubejs.event.NoOpEventJS;

public class CommonEvents {

    public static void listenKubeEvent(){
        //ServerEvents.RECIPES_AFTER_LOADED.listenJava(ScriptType.SERVER, null, CommonEvents::afterRecipeLoaded);
        KubeJSTweaks.LOGGER.info("Testing");
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
}
