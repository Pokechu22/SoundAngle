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
	// Fields that are declared (and initialized and modified) in GuiSubtitleOverlay
	@Shadow
	private @Final Minecraft client;
	@Shadow
	private @Final List<GuiSubtitleOverlay.Subtitle> subtitles;
	@Shadow
	private boolean enabled;

	@Overwrite
	public void renderSubtitles(ScaledResolution resolution) {
		// Register/unregister the sound event listener if the settings changed
		if (!this.enabled && this.client.gameSettings.showSubtitles) {
			this.client.getSoundHandler().addListener(this);
			this.enabled = true;
		} else if (this.enabled && !this.client.gameSettings.showSubtitles) {
			this.client.getSoundHandler().removeListener(this);
			this.enabled = false;
		}

		if (this.enabled && !this.subtitles.isEmpty()) {
			// Render configuration
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);

			// Player's current position as a vector (1.0f as the parameter means no interpolation)
			Vec3d playerPos = this.client.player.getPositionEyes(1.0f);

			// Maximum width of all subtitle text
			int maxTextWidth = 0;

			// Go through all of the current subtitles, to determine the max width and
			// remove any that are too old to be displayed
			Iterator<GuiSubtitleOverlay.Subtitle> itr = this.subtitles.iterator();
			while (itr.hasNext()) {
				GuiSubtitleOverlay.Subtitle subtitle = itr.next();

				if (subtitle.getStartTime() + LiteModSoundAngle.INSTANCE.displayTime <= Minecraft.getSystemTime()) {
					itr.remove();
				} else {
					maxTextWidth = Math.max(maxTextWidth, this.client.fontRenderer.getStringWidth(subtitle.getString()));
				}
			}

			// Max width of all subtitle text plus space for the angle
			int boxWidth = maxTextWidth + this.client.fontRenderer.getStringWidth(" -000.0");

			int numDrawn = 0;

			// Actually draw the subtitles:
			for (GuiSubtitleOverlay.Subtitle subtitle : this.subtitles) {
				String subtitleText = subtitle.getString();

				// Calculate the brightness based off of how old the text is
				// (clampedLerp fades between 255 and 75)
				int brightness = MathHelper.floor(MathHelper.clampedLerp(255, 75,
						(Minecraft.getSystemTime() - subtitle.getStartTime()) / (float)LiteModSoundAngle.INSTANCE.displayTime));
				// Make a color in the form of 0xAARRGGBB - in this case, alpha is set to max (i.e. fully solid)
				// and R, G, and B are all set to brightness
				int color = 0xFF000000 | brightness << 16 | brightness << 8 | brightness;

				// Get the subtitle's location
				Vec3d pos = subtitle.getLocation();

				// atan2 converts x and z to an angle in radians, which we then convert to degrees 
				double angle = Math.toDegrees(MathHelper.atan2(pos.x - playerPos.x, pos.z - playerPos.z));
				// If needed modify the angle to be offset from the player's angle
				if (LiteModSoundAngle.INSTANCE.captionMode == CaptionMode.DEGREES_FROM_PLAYER) {
					angle += client.player.rotationYaw;
				}
				// Wrap the angle so that it's in the range of -180 to 180
				// and then get information needed to display it
				angle = MathHelper.wrapDegrees(angle);
				String angleText = String.format("%.1f", angle);
				int angleWidth = client.fontRenderer.getStringWidth(angleText);

				int fontHeight = this.client.fontRenderer.FONT_HEIGHT;
				int halfHeight = fontHeight / 2;

				// Offset the display position.  I'm not entirely sure why it's set up
				// like this instead of drawing based off of coordinates,
				// but that's the way the original code did it. 
				GlStateManager.pushMatrix();
				GlStateManager.translate(resolution.getScaledWidth() - boxWidth - 2,
						resolution.getScaledHeight() - 30 - numDrawn * (fontHeight + 1), 0);
				GlStateManager.scale(1.0F, 1.0F, 1.0F);
				// Draw the gray background for the current subtitle
				drawRect(-1, -halfHeight - 1, boxWidth + 1, halfHeight + 1, 0xCC000000);
				GlStateManager.enableBlend();

				int textWidth = this.client.fontRenderer.getStringWidth(subtitleText);
				// Draw the subtitle's text, center-aligned with other subtitle text
				this.client.fontRenderer.drawString(subtitleText, maxTextWidth / 2 - textWidth / 2, -halfHeight, color);
				// Draw the angle text, right-aligned
				this.client.fontRenderer.drawString(angleText, boxWidth - angleWidth, -halfHeight, color);

				// Undo the offset to display positions
				GlStateManager.popMatrix();
				numDrawn++;
			}

			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
	}
}