package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.eclipse.keymap.UnionStateProvider;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.platform.WorkbenchService;
import net.sourceforge.vrapper.utils.DefaultKeyMapProvider;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class EclipsePlatform implements Platform {

    private final EclipseCursorAndSelection cursorAndSelection;
    private final EclipseTextContent textContent;
    private final EclipseFileService fileService;
    private final EclipseViewportService viewportService;
    private final HistoryService historyService;
    private final EclipseServiceProvider serviceProvider;
    private final EclipseUserInterfaceService userInterfaceService;
    private final EclipseWorkbenchService workbenchService;
    private final DefaultKeyMapProvider keyMapProvider;
    private final UnderlyingEditorSettings underlyingEditorSettings;
    private final Configuration configuration;
    private final AbstractTextEditor underlyingEditor;
    private final SearchAndReplaceService searchAndReplaceService;
    private static final Map<String, PlatformSpecificStateProvider> providerCache = new HashMap<String, PlatformSpecificStateProvider>();

    public EclipsePlatform(AbstractTextEditor abstractTextEditor,
            ITextViewer textViewer, Configuration sharedConfiguration) {
        underlyingEditor = abstractTextEditor;
        configuration = sharedConfiguration;
        cursorAndSelection = new EclipseCursorAndSelection(configuration,
                textViewer);
        textContent = new EclipseTextContent(textViewer);
        fileService = new EclipseFileService(abstractTextEditor);
        viewportService = new EclipseViewportService(textViewer);
        serviceProvider = new EclipseServiceProvider(abstractTextEditor);
        userInterfaceService = new EclipseUserInterfaceService(
                abstractTextEditor, textViewer);
        workbenchService = new EclipseWorkbenchService(abstractTextEditor);
        keyMapProvider = new DefaultKeyMapProvider();
        underlyingEditorSettings = new AbstractTextEditorSettings(
                abstractTextEditor);
        searchAndReplaceService = new EclipseSearchAndReplaceService(abstractTextEditor, textViewer);
        if (textViewer instanceof ITextViewerExtension6) {
            IUndoManager delegate = ((ITextViewerExtension6) textViewer)
                    .getUndoManager();
            EclipseHistoryService manager = new EclipseHistoryService(
                    textViewer.getTextWidget(), delegate);
            textViewer.setUndoManager(manager);
            this.historyService = manager;
        } else {
            this.historyService = new DummyHistoryService();
        }
    }

    public CursorService getCursorService() {
        return cursorAndSelection;
    }

    public TextContent getModelContent() {
        return textContent.getModelContent();
    }

    public SelectionService getSelectionService() {
        return cursorAndSelection;
    }

    public TextContent getViewContent() {
        return textContent.getViewContent();
    }

    public FileService getFileService() {
        return fileService;
    }

    public ViewportService getViewportService() {
        return viewportService;
    }

    public HistoryService getHistoryService() {
        return historyService;
    }

    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
    }

    public WorkbenchService getWorkbenchService() {
        return workbenchService;
    }

    public DefaultKeyMapProvider getKeyMapProvider() {
        return keyMapProvider;
    }

    public UnderlyingEditorSettings getUnderlyingEditorSettings() {
        return underlyingEditorSettings;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public PlatformSpecificStateProvider getPlatformSpecificStateProvider() {
        String className = underlyingEditor.getClass().getName();
        if (!providerCache.containsKey(className)) {
            providerCache.put(className, buildPlatformSpecificStateProvider());
        }
        return providerCache.get(className);
    }

    public SearchAndReplaceService getSearchAndReplaceService() {
        return searchAndReplaceService;
    }

    private PlatformSpecificStateProvider buildPlatformSpecificStateProvider() {
        IExtensionRegistry registry = org.eclipse.core.runtime.Platform
                .getExtensionRegistry();
        IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.pssp");
        List<AbstractEclipseSpecificStateProvider> matched = new ArrayList<AbstractEclipseSpecificStateProvider>();
        for (IConfigurationElement element : elements) {
            AbstractEclipseSpecificStateProvider provider = (AbstractEclipseSpecificStateProvider) Utils
                    .createGizmoForElementConditionally(
                            underlyingEditor, "editor-must-subclass",
                            element, "provider-class");
            if (provider != null) {
                matched.add(provider);
            }
        }
        Collections.sort(matched);
        return new UnionStateProvider("extensions for "
                + underlyingEditor.getClass().getName(), matched);
    }

}
