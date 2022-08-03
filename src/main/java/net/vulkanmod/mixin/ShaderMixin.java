package net.vulkanmod.mixin;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.vulkanmod.Initializer;
import net.vulkanmod.interfaces.ShaderMixed;
import net.vulkanmod.vulkan.Drawer;
import net.vulkanmod.vulkan.Pipeline;
import net.vulkanmod.vulkan.ShaderSPIRVUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Mixin(Shader.class)
public class ShaderMixin implements ShaderMixed {

    @Shadow private GlBlendState blendState;

    private Pipeline pipeline;

    public Pipeline getPipeline() {
        return pipeline;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void create(ResourceFactory factory, String name, VertexFormat format, CallbackInfo ci) {
        String path = "core/" + name;
        try {
            String source = new String(Files.readAllBytes(Paths.get(new URI(ShaderSPIRVUtils.class.getResource("/assets/vulkanmod/shaders/core/" + name + ".json").toExternalForm()))));
            JsonObject jsonObject = JsonHelper.deserialize(source);
            this.blendState = Shader.readBlendState(JsonHelper.getObject(jsonObject, "blend", (JsonObject)null));
        } catch (Exception e){
            e.printStackTrace();
            Initializer.LOGGER.error("Failed to load blendState");
        }
        pipeline = new Pipeline(format, path, new Pipeline.PipelineState(new Pipeline.BlendState(this.blendState), Pipeline.DEFAULT_DEPTH_STATE, Pipeline.DEFAULT_LOGICOP_STATE, Pipeline.DEFAULT_COLORMASK));
    }

    @Inject(method = "loadProgram", at = @At("HEAD"), cancellable = true)
    private static void loadProgram(ResourceFactory factory, Program.Type type, String name, CallbackInfoReturnable<Program> cir) {
        cir.setReturnValue(null);
        cir.cancel();
    }

    @Inject(method = "loadReferences", at = @At("HEAD"), cancellable = true)
    private void loadReferences(CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gl/GlUniform;bindAttribLocation(IILjava/lang/CharSequence;)V"))
    private void bindAttr(int program, int index, CharSequence name) {}

    @Overwrite
    public void bind() {
        RenderSystem.blendEquation(((GlBlendStateAccessor)this.blendState).getFunc());
        if (((GlBlendStateAccessor)this.blendState).getSeparateBlend()) {
            RenderSystem.blendFuncSeparate((((GlBlendStateAccessor)this.blendState).getSrcRgb()), ((GlBlendStateAccessor)this.blendState).getDstRgb(), ((GlBlendStateAccessor)this.blendState).getSrcAlpha(), ((GlBlendStateAccessor)this.blendState).getDstAlpha());
        } else {
            RenderSystem.blendFunc(((GlBlendStateAccessor) this.blendState).getSrcRgb(), ((GlBlendStateAccessor) this.blendState).getDstRgb());
        }
    }

    /**
     * @author
     */
    @Overwrite
    public void close() {
        pipeline.cleanUp();
    }
}
