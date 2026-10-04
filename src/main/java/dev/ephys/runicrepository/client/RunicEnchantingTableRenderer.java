package dev.ephys.runicrepository.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.ephys.runicrepository.block.RunicEnchantingTableBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.util.Mth;

public class RunicEnchantingTableRenderer implements BlockEntityRenderer<RunicEnchantingTableBlockEntity> {
  private final BookModel bookModel;

  public RunicEnchantingTableRenderer(BlockEntityRendererProvider.Context context) {
    this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
  }

  @Override
  public void render(RunicEnchantingTableBlockEntity be, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay) {
    poseStack.pushPose();
    poseStack.translate(0.5F, 0.75F, 0.5F);

    float time = be.time + partialTick;
    poseStack.translate(0.0F, 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.0F);

    float rotDiff = be.rot - be.oRot;
    while (rotDiff >= (float) Math.PI) {
      rotDiff -= (float) (Math.PI * 2);
    }
    while (rotDiff < -(float) Math.PI) {
      rotDiff += (float) (Math.PI * 2);
    }

    float rot = be.oRot + rotDiff * partialTick;
    poseStack.mulPose(Axis.YP.rotation(-rot));
    poseStack.mulPose(Axis.ZP.rotationDegrees(80.0F));

    float flip = Mth.lerp(partialTick, be.oFlip, be.flip);
    float page1 = Mth.frac(flip + 0.25F) * 1.6F - 0.3F;
    float page2 = Mth.frac(flip + 0.75F) * 1.6F - 0.3F;
    float open = Mth.lerp(partialTick, be.oOpen, be.open);

    this.bookModel.setupAnim(time, Mth.clamp(page1, 0.0F, 1.0F), Mth.clamp(page2, 0.0F, 1.0F), open);
    VertexConsumer consumer = EnchantTableRenderer.BOOK_LOCATION.buffer(buffer, net.minecraft.client.renderer.RenderType::entitySolid);
    this.bookModel.render(poseStack, consumer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);
    poseStack.popPose();
  }
}
