package pokechu22.soundangle;

import com.mumfrey.liteloader.client.gui.GuiCheckbox;
import com.mumfrey.liteloader.modconfig.AbstractConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigPanelHost;import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.gui.GuiPageButtonList.GuiResponder;
import net.minecraft.client.resources.I18n;
import static pokechu22.soundangle.LiteModSoundAngle.CaptionMode.*;

public class SoundAngleConfigPanel extends AbstractConfigPanel implements GuiResponder, GuiSlider.FormatHelper {

	@Override
	public String getPanelTitle() {
		return I18n.format("soundangle.config.title");
	}

	@Override
	public void onPanelHidden() {
	}

	@Override
	protected void addOptions(ConfigPanelHost host) {
		final LiteModSoundAngle mod = host.<LiteModSoundAngle>getMod();

		this.addControl(new GuiCheckbox(0, 0, 32, I18n.format("soundangle.config.rawAngle")), (control) -> {
			control.checked = !control.checked;
			mod.captionMode = (control.checked) ? RAW_DEGREES : DEGREES_FROM_PLAYER;
		}).checked = (mod.captionMode == RAW_DEGREES);

		this.addControl(new GuiSlider(this, 1, 0, 52, I18n.format("soundangle.config.displayTime"), 1, 10, mod.displayTime / 1000F, this), (c) -> {});
	}

	@Override
	public void setEntryValue(int id, boolean value) { }
	@Override
	public void setEntryValue(int id, String value) { }

	@Override
	public void setEntryValue(int id, float value) {
		LiteModSoundAngle.INSTANCE.displayTime = (int)(value * 1000);
	}

	@Override
	public String getText(int id, String name, float value) {
		return I18n.format("soundangle.config.displayTimeFormat", name, (int)(value * 10)/10.0);
	}
}
