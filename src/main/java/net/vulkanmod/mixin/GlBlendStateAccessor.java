package net.vulkanmod.mixin;

import net.minecraft.client.gl.GlBlendState;
import net.vulkanmod.vulkan.Pipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GlBlendState.class)
public interface GlBlendStateAccessor {
    @Accessor int getSrcRgb();
    @Accessor int getSrcAlpha();
    @Accessor int getDstRgb();
    @Accessor int getDstAlpha();
    @Accessor int getFunc();
    @Accessor boolean getSeparateBlend();
    @Accessor boolean getBlendDisabled();
}
