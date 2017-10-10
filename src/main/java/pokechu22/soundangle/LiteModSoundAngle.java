package pokechu22.soundangle;

import java.io.File;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mumfrey.liteloader.Configurable;
import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.modconfig.ConfigPanel;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename="soundangle.json")
public class LiteModSoundAngle implements LiteMod, Configurable {
	@Expose
	@SerializedName("display_time")
	/**
	 * Time to display subtitles for in milliseconds
	 */
	public int displayTime = 3000;

	@Expose
	@SerializedName("caption_mode")
	public CaptionMode captionMode = CaptionMode.DEGREES_FROM_PLAYER;

	public static LiteModSoundAngle INSTANCE;

	@Override
	public String getName() {
		return "SoundAngle";
	}

	@Override
	public String getVersion() {
		return "0.1";
	}

	@Override
	public void init(File configPath) {
		INSTANCE = this;
	}

	@Override
	public void upgradeSettings(String version, File configPath, File oldConfigPath) {

	}

	@Override
	public Class<? extends ConfigPanel> getConfigPanelClass() {
		return SoundAngleConfigPanel.class;
	}

	public static enum CaptionMode {
		RAW_DEGREES,
		DEGREES_FROM_PLAYER
	}
}