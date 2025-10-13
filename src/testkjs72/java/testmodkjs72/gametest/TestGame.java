package testmodkjs72.gametest;

import dev.uncandango.kubejstweaks.KubeJSTweaks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.world.level.Level;
import net.neoforged.testframework.annotation.ForEachTest;
import net.neoforged.testframework.annotation.TestHolder;
import net.neoforged.testframework.gametest.EmptyTemplate;
import net.neoforged.testframework.gametest.ExtendedGameTestHelper;

@ForEachTest(groups = {"test"})
public class TestGame {

    @GameTest
    @EmptyTemplate
    @TestHolder(description = "Testing if I can trigger a test, expanding this later")
    static void testingTheFramework(ExtendedGameTestHelper helper) {

        var vec3 = helper.getBounds().getCenter();
        var center = helper.absolutePos(new BlockPos(1,1,1));
        KubeJSTweaks.LOGGER.info("Center (Vec3) is: {}", vec3);
        KubeJSTweaks.LOGGER.info("Center (Pos) is: {}", center);

        //helper.getLevel().kjs$createExplosion(vec3.x, vec3.y, vec3.z).explosionMode(Level.ExplosionInteraction.TNT).explode();
        KubeJSTweaks.LOGGER.info("Hello from KubeJSTweaks tests!");
        KubeJSTweaks.LOGGER.info("Current thread is: {}", Thread.currentThread().getName());
        helper.fail("Testing!");
    }
}
