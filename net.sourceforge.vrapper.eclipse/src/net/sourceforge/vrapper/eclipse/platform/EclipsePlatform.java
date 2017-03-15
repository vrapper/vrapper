package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.text.ITextViewerExtension6;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.MultiPageEditorSite;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import net.sourceforge.vrapper.eclipse.interceptor.EditorInfo;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.eclipse.keymap.UnionStateProvider;
import net.sourceforge.vrapper.eclipse.mode.UnionModeProvider;
import net.sourceforge.vrapper.eclipse.utils.Utils;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificVolatileStateProvider;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.DefaultKeyMapProvider;
import net.sourceforge.vrapper.vim.DefaultConfigProvider;
import net.sourceforge.vrapper.vim.LocalConfiguration;
import net.sourceforge.vrapper.vim.SimpleLocalConfiguration;
import net.sourceforge.vrapper.vim.TextObjectProvider;

public class EclipsePlatform implements Platform {

    private final EclipseCursorAndSelection cursorAndSelection;
    private final EclipseTextContent textContent;
    private final EclipseFileService fileService;
    private final EclipseViewportService viewportService;
    private final HistoryService historyService;
    private final EclipseServiceProvider serviceProvider;
    private final EclipseUserInterfaceService userInterfaceService;
    private final DefaultKeyMapProvider keyMapProvider;
    private final UnderlyingEditorSettings underlyingEditorSettings;
    private final LocalConfiguration localConfiguration;
    private final AbstractTextEditor underlyingEditor;
    private final HighlightingService highlightingService;
    private final SearchAndReplaceService searchAndReplaceService;
    private final VrapperModeRecorder vrapperModeRecorder;
    private static final Map<String, PlatformSpecificStateProvider> providerCache = new ConcurrentHashMap<String, PlatformSpecificStateProvider>();
    private static final AtomicReference<PlatformSpecificModeProvider> modeProviderCache= new AtomicReference<PlatformSpecificModeProvider>();
    private static final Map<String, PlatformSpecificTextObjectProvider> textObjProviderCache = new ConcurrentHashMap<String, PlatformSpecificTextObjectProvider>();
    private BufferAndTabService bufferAndTabService;

    public EclipsePlatform(EditorInfo partInfo, AbstractTextEditor abstractTextEditor,
            ISourceViewer sourceViewer, GlobalConfiguration sharedConfiguration,
            BufferAndTabService bufferAndTabService) {
        vrapperModeRecorder = new VrapperModeRecorder();
        underlyingEditor = abstractTextEditor;
        underlyingEditorSettings = new AbstractTextEditorSettings(abstractTextEditor, sourceViewer);
        List<DefaultConfigProvider> configProviders =
                Collections.singletonList((DefaultConfigProvider)underlyingEditorSettings);
        this.localConfiguration = new SimpleLocalConfiguration(configProviders, sharedConfiguration);
        this.bufferAndTabService = bufferAndTabService;
        textContent = new EclipseTextContent(sourceViewer);
        cursorAndSelection = new EclipseCursorAndSelection(vrapperModeRecorder, localConfiguration, partInfo, sourceViewer, textContent);
        fileService = new EclipseFileService(abstractTextEditor);
        viewportService = new EclipseViewportService(sourceViewer);
        serviceProvider = new EclipseServiceProvider(abstractTextEditor);
        userInterfaceService = new EclipseUserInterfaceService(
                abstractTextEditor, sourceViewer);
        keyMapProvider = new DefaultKeyMapProvider();
        highlightingService = new EclipseHighlightingService(abstractTextEditor, cursorAndSelection);
        searchAndReplaceService = new EclipseSearchAndReplaceService(sourceViewer, localConfiguration, highlightingService);
        if (sourceViewer instanceof ITextViewerExtension6) {
            final IUndoManager delegate = ((ITextViewerExtension6) sourceViewer)
                    .getUndoManager();
            final EclipseHistoryService manager = new EclipseHistoryService(
                    sourceViewer, delegate);
            sourceViewer.setUndoManager(manager);
            this.historyService = manager;
        } else {
            this.historyService = new DummyHistoryService();
        }
    }

    public VrapperModeRecorder getModeRecorder() {
        return vrapperModeRecorder;
    }

    public AbstractTextEditor getUnderlyingEditor() {
        return underlyingEditor;
    }

    @Override
    public CursorService getCursorService() {
        return cursorAndSelection;
    }

    @Override
    public TextContent getModelContent() {
        return textContent.getModelContent();
    }

    @Override
    public EclipseCursorAndSelection getSelectionService() {
        return cursorAndSelection;
    }

    @Override
    public TextContent getViewContent() {
        return textContent.getViewContent();
    }

    @Override
    public FileService getFileService() {
        return fileService;
    }

    @Override
    public BufferAndTabService getBufferAndTabService() {
        return bufferAndTabService;
    }

    @Override
    public ViewportService getViewportService() {
        return viewportService;
    }

    @Override
    public HistoryService getHistoryService() {
        return historyService;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public UserInterfaceService getUserInterfaceService() {
        return userInterfaceService;
    }

    @Override
    public DefaultKeyMapProvider getKeyMapProvider() {
        return keyMapProvider;
    }

    @Override
    public UnderlyingEditorSettings getUnderlyingEditorSettings() {
        return underlyingEditorSettings;
    }

    @Override
    public LocalConfiguration getConfiguration() {
        return localConfiguration;
    }

    @Override
    public PlatformSpecificStateProvider getPlatformSpecificStateProvider(
            TextObjectProvider textObjectProvider) {
        final String className = underlyingEditor.getClass().getName();
        if (!providerCache.containsKey(className)) {
            providerCache.put(className, buildPlatformSpecificStateProvider(textObjectProvider));
        }
        return providerCache.get(className);
    }

    @Override
    public PlatformSpecificModeProvider getPlatformSpecificModeProvider() {
        if (modeProviderCache.get() == null) {
            final PlatformSpecificModeProvider provider = buildPlatformSpecificModeProvider();
            // Only set this once. Custom modes don't use editor-specific test.
            modeProviderCache.compareAndSet(null, provider);
        }
        return modeProviderCache.get();
    }

    @Override
    public PlatformSpecificTextObjectProvider getPlatformSpecificTextObjectProvider() {
        final String className = underlyingEditor.getClass().getName();
        if (!textObjProviderCache.containsKey(className)) {
            textObjProviderCache.put(className, buildPlatformSpecificTextObjectProvider());
        }
        return textObjProviderCache.get(className);
    }

    @Override
    public SearchAndReplaceService getSearchAndReplaceService() {
        return searchAndReplaceService;
    }

    private PlatformSpecificStateProvider buildPlatformSpecificStateProvider(
            TextObjectProvider textObjectProvider) {
        final IExtensionRegistry registry = org.eclipse.core.runtime.Platform
                .getExtensionRegistry();
        final IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.pssp");
        final List<AbstractEclipseSpecificStateProvider> matched = new ArrayList<AbstractEclipseSpecificStateProvider>();
        final List<PlatformSpecificVolatileStateProvider> matchedVolatile = new ArrayList<PlatformSpecificVolatileStateProvider>();
        for (final IConfigurationElement element : elements) {
            if (VrapperLog.isDebugEnabled()) {
                VrapperLog.debug("Loading Vrapper state provider " + element.getAttribute("name"));
            }
            try {
                final Object gizmo = Utils
                        .createGizmoForElementConditionally(
                                underlyingEditor, "editor-must-subclass",
                                element, "provider-class");
                if (gizmo instanceof AbstractEclipseSpecificStateProvider) {
                    AbstractEclipseSpecificStateProvider provider = (AbstractEclipseSpecificStateProvider)gizmo;
                    provider.configure(element);
                    provider.initializeProvider(textObjectProvider);
                    matched.add(provider);
                    if (VrapperLog.isDebugEnabled()) {
                        VrapperLog.debug("Vrapper state provider " + element.getAttribute("name")
                                + " configured");
                    }
                }
                if (gizmo instanceof PlatformSpecificVolatileStateProvider) {
                    matchedVolatile.add((PlatformSpecificVolatileStateProvider)gizmo);
                }
            } catch (Exception e) {
                VrapperLog.error("Failed to initialize state provider " + element, e);
            }
        }
        Collections.sort(matched);
        return new UnionStateProvider("extensions for "
                + underlyingEditor.getClass().getName(), matched, matchedVolatile);
    }

    private PlatformSpecificModeProvider buildPlatformSpecificModeProvider() {
        final IExtensionRegistry registry = org.eclipse.core.runtime.Platform.getExtensionRegistry();
        final IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.psmp");
        final List<AbstractPlatformSpecificModeProvider> matched = new ArrayList<AbstractPlatformSpecificModeProvider>();
        for (final IConfigurationElement element : elements) {
            try {
                matched.add((AbstractPlatformSpecificModeProvider)
                        element.createExecutableExtension("provider-class"));
            } catch (final Exception e) {
                VrapperLog.error("error while building mode providers", e);
            }
        }
        return new UnionModeProvider("Extension modes", matched);
    }

    private PlatformSpecificTextObjectProvider buildPlatformSpecificTextObjectProvider() {
        final IExtensionRegistry registry = org.eclipse.core.runtime.Platform
                .getExtensionRegistry();
        final IConfigurationElement[] elements = registry
                .getConfigurationElementsFor("net.sourceforge.vrapper.eclipse.pstop");
        final List<PlatformSpecificTextObjectProvider> matched = new ArrayList<PlatformSpecificTextObjectProvider>();
        for (final IConfigurationElement element : elements) {
            final PlatformSpecificTextObjectProvider provider =
                    (PlatformSpecificTextObjectProvider) Utils.createGizmoForElementConditionally(
                            underlyingEditor, "editor-must-subclass",
                            element, "provider-class");
            if (provider != null) {
                matched.add(provider);
            }
        }
        // Priority code is not implemented. If it was, add a SortKey decorator.
        // Collections.sort(matched);
        return new UnionTextObjectProvider("extensions for "
                + underlyingEditor.getClass().getName(), matched);
    }

    public String getEditorType() {
    	IWorkbenchPartSite site = underlyingEditor.getSite();
    	if(site instanceof MultiPageEditorSite) {
    		site = ((MultiPageEditorSite)site).getMultiPageEditor().getSite();
    	}

        return site.getRegisteredName();
    }

    @Override
    public HighlightingService getHighlightingService() {
        return highlightingService;
    }

}
