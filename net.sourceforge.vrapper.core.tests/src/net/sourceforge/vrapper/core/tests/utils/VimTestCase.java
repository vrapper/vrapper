package net.sourceforge.vrapper.core.tests.utils;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.EnumSet;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.Configuration.OptionScope;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.platform.ServiceProvider;
import net.sourceforge.vrapper.platform.SimpleConfiguration;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.DefaultKeyMapProvider;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.TextObjectProvider;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.SimpleRegister;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class VimTestCase {

    @Mock protected Platform platform;
    @Mock protected RegisterManager registerManager;
    @Mock protected ViewportService viewportService;
    @Mock protected UserInterfaceService userInterfaceService;
    @Mock protected FileService fileService;
    @Mock protected HistoryService historyService;
    @Mock protected ServiceProvider serviceProvider;
    @Mock protected PlatformSpecificStateProvider platformSpecificStateProvider;
    protected Configuration configuration;
    protected TestTextContent content;
    protected TestCursorAndSelection cursorAndSelection;
    protected EditorAdaptor adaptor;
    protected SimpleRegister defaultRegister;
    protected SimpleRegister lastEditRegister;
    protected KeyMapProvider keyMapProvider;

    public VimTestCase() {
        super();
    }

    public void initMocks() {
    	DefaultEditorAdaptor.SHOULD_READ_RC_FILE = false;
    	MockitoAnnotations.initMocks(this);
    	cursorAndSelection = spy(new TestCursorAndSelection());
    	content = spy(new TestTextContent(cursorAndSelection));
    	cursorAndSelection.setContent(content);
    	keyMapProvider = spy(new DefaultKeyMapProvider());
    	configuration = spy(new SimpleConfiguration());
    	when(configuration.getNewLine()).thenReturn("\n");
    	for (Option<Boolean> o : Options.BOOLEAN_OPTIONS) {
    	    // Use defaults for local options.
    	    if (EnumSet.of(OptionScope.GLOBAL, OptionScope.DEFAULT).contains(o.getScope())) {
    	        when(configuration.get(o)).thenReturn(Boolean.FALSE);
    	    }
    	}
        //let UIInterface mock print out error messages
    	Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				for (Object argument : invocation.getArguments()) {
					System.err.println(argument);
				}
				return null;
			}
		}).when(userInterfaceService).setErrorMessage(Mockito.anyString());
    	when(userInterfaceService.getCommandLineUI(Mockito.any(EditorAdaptor.class))).thenReturn(new CommandLineUIStub());
    	when(platform.getCursorService()).thenReturn(cursorAndSelection);
    	when(platform.getSelectionService()).thenReturn(cursorAndSelection);
    	when(platform.getModelContent()).thenReturn(content);
    	when(platform.getViewContent()).thenReturn(content);
    	when(platform.getViewportService()).thenReturn(viewportService);
    	ViewPortInformation viewPortInfo = new ViewPortInformation(0, 10);
    	when(viewportService.getViewPortInformation()).thenReturn(viewPortInfo);
    	when(platform.getUserInterfaceService()).thenReturn(userInterfaceService);
    	when(platform.getFileService()).thenReturn(fileService);
    	when(platform.getHistoryService()).thenReturn(historyService);
    	when(platform.getKeyMapProvider()).thenReturn(keyMapProvider);
    	when(platform.getServiceProvider()).thenReturn(serviceProvider);
    	when(platform.getConfiguration()).thenReturn(configuration);
    	when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
    	        .thenReturn(platformSpecificStateProvider);
    	reloadEditorAdaptor();
    	defaultRegister = spy(new SimpleRegister());
    	lastEditRegister = spy(new SimpleRegister());
		when(registerManager.getActiveRegister()).thenReturn(defaultRegister);
		when(registerManager.getLastEditRegister()).thenReturn(lastEditRegister);
		when(registerManager.getRegister(":")).thenReturn(defaultRegister);
		when(fileService.isEditable()).thenReturn(true);

    }

    protected void reloadEditorAdaptor() {
        DefaultEditorAdaptor unwrapped = new DefaultEditorAdaptor(platform, registerManager, false);
        DefaultEditorAdaptor wrapped = spy(unwrapped);
        wrapped.__set_modes(wrapped);
        adaptor = wrapped;
    }

    @Before
    public void setUp() {
    	initMocks();
    }

    public void setBuffer(String text) {
        content.setText(text);
    }

    public String getBuffer() {
        return content.getText();
    }

    public void type(Iterable<KeyStroke> keyStrokes) {
        for (KeyStroke stroke: keyStrokes) {
            if(!adaptor.handleKey(stroke)) {
                typeInUnderlyingEditor(stroke);
            }
        }
    }

    private void typeInUnderlyingEditor(KeyStroke stroke) {
        int offset = cursorAndSelection.getPosition().getModelOffset();
        content.replace(offset, 0, ""+stroke.getCharacter());
    }

}