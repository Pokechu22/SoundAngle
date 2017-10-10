package pokechu22.soundangle.mixin;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSubtitleOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Mixin(GuiSubtitleOverlay.class)
public abstract class MixinGuiSubtitleOverlay extends Gui implements ISoundEventListener {
	@Shadow
	private @Final Minecraft client;
	@Shadow
	private @Final List<GuiSubtitleOverlay.Subtitle> subtitles;
	@Shadow
	private boolean enabled;

	private static final int DISPLAY_TIME = 3000;

	@Overwrite
	public void renderSubtitles(ScaledResolution resolution) {
		if (!this.enabled && this.client.gameSettings.showSubtitles) {
			this.client.getSoundHandler().addListener(this);
			this.enabled = true;
		} else if (this.enabled && !this.client.gameSettings.showSubtitles) {
			this.client.getSoundHandler().removeListener(this);
			this.enabled = false;
		}

		if (this.enabled && !this.subtitles.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);

			Vec3d playerPos = this.client.player.getPositionEyes(1.0f);
			Vec3d vec3d1 = (new Vec3d(0.0D, 0.0D, -1.0D)).rotatePitch(-this.client.player.rotationPitch * (float)Math.PI/180)
					.rotateYaw(-this.client.player.rotationYaw * (float)Math.PI/180);
			Vec3d vec3d2 = (new Vec3d(0.0D, 1.0D, 0.0D)).rotatePitch(-this.client.player.rotationPitch * (float)Math.PI/180)
					.rotateYaw(-this.client.player.rotationYaw * (float)Math.PI/180);
			Vec3d vec3d3 = vec3d1.crossProduct(vec3d2);

			int numDrawn = 0;
			int maxWidth = 0;

			Iterator<GuiSubtitleOverlay.Subtitle> itr = this.subtitles.iterator();

			while (itr.hasNext()) {
				GuiSubtitleOverlay.Subtitle subtitle = itr.next();

				if (subtitle.getStartTime() + DISPLAY_TIME <= Minecraft.getSystemTime()) {
					itr.remove();
				} else {
					maxWidth = Math.max(maxWidth, this.client.fontRenderer.getStringWidth(subtitle.getString()));
				}
			}

			maxWidth = maxWidth + this.client.fontRenderer.getStringWidth("<") + this.client.fontRenderer.getStringWidth(" ")
					+ this.client.fontRenderer.getStringWidth(">") + this.client.fontRenderer.getStringWidth(" ");

			for (GuiSubtitleOverlay.Subtitle subtitle : this.subtitles) {
				String s = subtitle.getString();
				Vec3d vec3d4 = subtitle.getLocation().subtract(playerPos).normalize();
				double d0 = -vec3d3.dotProduct(vec3d4);
				double d1 = -vec3d1.dotProduct(vec3d4);
				boolean flag = d1 > 0.5D;
				int halfWidth = maxWidth / 2;
				int fontHeight = this.client.fontRenderer.FONT_HEIGHT;
				int halfHeight = fontHeight / 2;
				int lineWidth = this.client.fontRenderer.getStringWidth(s);
				int brightness = MathHelper.floor(MathHelper.clampedLerp(255, 75,
						(Minecraft.getSystemTime() - subtitle.getStartTime()) / (float)DISPLAY_TIME));
				int color = 0xFF000000 | brightness << 16 | brightness << 8 | brightness;
				GlStateManager.pushMatrix();
				GlStateManager.translate(resolution.getScaledWidth() - halfWidth - 2,
						resolution.getScaledHeight() - 30 - numDrawn * (fontHeight + 1), 0);
				GlStateManager.scale(1.0F, 1.0F, 1.0F);
				drawRect(-halfWidth - 1, -halfHeight - 1, halfWidth + 1, halfHeight + 1, 0xCC000000);
				GlStateManager.enableBlend();

				if (!flag) {
					if (d0 > 0.0D) {
						this.client.fontRenderer.drawString(">", halfWidth - this.client.fontRenderer.getStringWidth(">"), -halfHeight,
								color + 0xFF000000);
					} else if (d0 < 0.0D) {
						this.client.fontRenderer.drawString("<", -halfWidth, -halfHeight, color);
					}
				}

				drawCenteredString(this.client.fontRenderer, s, 0, -halfHeight, color);
				this.client.fontRenderer.drawString(s, -lineWidth / 2, -halfHeight, color);
				GlStateManager.popMatrix();
				numDrawn++;
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
}