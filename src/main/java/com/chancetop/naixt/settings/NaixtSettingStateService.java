package com.chancetop.naixt.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author stephen
 */
@Service
@State(name = "com.chancetop.naixt.settings.NaixtStateService", storages = @Storage("naixt.xml"))
public final class NaixtSettingStateService implements PersistentStateComponent<NaixtSettingState> {
    private final NaixtSettingState state = new NaixtSettingState();

    public NaixtSettingStateService() {}

    public static NaixtSettingStateService getInstance() {
        return ApplicationManager.getApplication().getService(NaixtSettingStateService.class);
    }

    @Override
    public NaixtSettingState getState() {
        return this.state;
    }

    @Override
    public void loadState(@NotNull NaixtSettingState state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }
}
