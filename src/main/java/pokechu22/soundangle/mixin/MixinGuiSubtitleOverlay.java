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
import pokechu22.soundangle.LiteModSoundAngle;
import pokechu22.soundangle.LiteModSoundAngle.CaptionMode;

@Mixin(GuiSubtitleOverlay.class)
public abstract class MixinGuiSubtitleOverlay extends Gui implements ISoundEventListener {
	@Shadow
	private @Final Minecraft client;
	@Shadow
	private @Final List<GuiSubtitleOverlay.Subtitle> subtitles;
	@Shadow
	private boolean enabled;

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

			int numDrawn = 0;
			int maxTextWidth = 0;

			Iterator<GuiSubtitleOverlay.Subtitle> itr = this.subtitles.iterator();

			while (itr.hasNext()) {
				GuiSubtitleOverlay.Subtitle subtitle = itr.next();

				if (subtitle.getStartTime() + LiteModSoundAngle.INSTANCE.displayTime <= Minecraft.getSystemTime()) {
					itr.remove();
				} else {
					maxTextWidth = Math.max(maxTextWidth, this.client.fontRenderer.getStringWidth(subtitle.getString()));
				}
			}

			int boxWidth = maxTextWidth + this.client.fontRenderer.getStringWidth(" -000.0");

			for (GuiSubtitleOverlay.Subtitle subtitle : this.subtitles) {
				String subtitleText = subtitle.getString();

				Vec3d pos = subtitle.getLocation();
				Vec3d offset = new Vec3d(pos.x - playerPos.x, 0, pos.z - playerPos.z).normalize();

				double angle = Math.toDegrees(MathHelper.atan2(offset.x, offset.z));
				if (LiteModSoundAngle.INSTANCE.captionMode == CaptionMode.DEGREES_FROM_PLAYER) {
					angle += client.player.rotationYaw;
				}
				angle = MathHelper.wrapDegrees(angle);
				String angleText = String.format("%.1f", angle);
				int angleWidth = client.fontRenderer.getStringWidth(angleText);

				int fontHeight = this.client.fontRenderer.FONT_HEIGHT;
				int halfHeight = fontHeight / 2;
				int brightness = MathHelper.floor(MathHelper.clampedLerp(255, 75,
						(Minecraft.getSystemTime() - subtitle.getStartTime()) / (float)LiteModSoundAngle.INSTANCE.displayTime));
				int color = 0xFF000000 | brightness << 16 | brightness << 8 | brightness;
				GlStateManager.pushMatrix();
				GlStateManager.translate(resolution.getScaledWidth() - boxWidth - 2,
						resolution.getScaledHeight() - 30 - numDrawn * (fontHeight + 1), 0);
				GlStateManager.scale(1.0F, 1.0F, 1.0F);
				drawRect(-1, -halfHeight - 1, boxWidth + 1, halfHeight + 1, 0xCC000000);
				GlStateManager.enableBlend();

				int textWidth = this.client.fontRenderer.getStringWidth(subtitleText);
				this.client.fontRenderer.drawString(subtitleText, maxTextWidth / 2 - textWidth / 2, -halfHeight, color);
				this.client.fontRenderer.drawString(angleText, boxWidth - angleWidth, -halfHeight, color);

				GlStateManager.popMatrix();
				numDrawn++;
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
}