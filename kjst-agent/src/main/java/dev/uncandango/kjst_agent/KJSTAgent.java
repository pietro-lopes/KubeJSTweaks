package dev.uncandango.kjst_agent;

import dev.uncandango.kjst_agent.transformer.DecoderTransformer;
import dev.uncandango.kjst_agent.transformer.FieldDecoderTransformer;
import dev.uncandango.kjst_agent.transformer.OptionalFieldCodecTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;


public class KJSTAgent {
    public static final Logger LOGGER = LoggerFactory.getLogger("KubeJS Tweaks Agent");

    public static void premain(String arg, Instrumentation inst) {
        LOGGER.debug("In premain method");
        inst.addTransformer(new DecoderTransformer("com/mojang/serialization/Decoder"));
        inst.addTransformer(new OptionalFieldCodecTransformer("com/mojang/serialization/codecs/OptionalFieldCodec"));
        inst.addTransformer(new FieldDecoderTransformer("com/mojang/serialization/codecs/FieldDecoder"));
    }
}
