package net.sourceforge.vrapper.core.tests.cases;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.Mockito;

import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.plugin.surround.provider.SurroundModesProvider;
import net.sourceforge.vrapper.plugin.surround.provider.SurroundStateProvider;
import net.sourceforge.vrapper.testutil.CommandTestCase;
import net.sourceforge.vrapper.vim.TextObjectProvider;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class NormalModeTests extends CommandTestCase {
	@Override
	public void setUp() {
		super.setUp();
		adaptor.changeModeSafely(NormalMode.NAME);
	};

	@Override
	protected void reloadEditorAdaptor() {
	    super.reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
        reset(adaptor);
	};

	@Test
    public void testSurroundPlugin_ds() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("dsb"),
                "array[(in",'d',"ex)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[(in",'d',"ex)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[",'(',"index)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[(index",')',"];",
                "array[",'i',"ndex];");
    }

    @Test
    public void testSurroundPlugin_cs() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("cs[b"),
                "fn[ar",'g',"ument];",
                "fn",'(',"argument);");
        checkCommand(forKeySeq("cs)("),
                "fn(ar",'g',"ument);",
                "fn",'('," argument );");
        checkCommand(forKeySeq("cs()"),
                "fn(  ar",'g',"ument  );",
                "fn",'(',"argument);");
    }

	@Test
    public void testSurroundPlugin_cs_input() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        when(platform.getPlatformSpecificModeProvider()).thenReturn(
                (PlatformSpecificModeProvider) new SurroundModesProvider());
        reloadEditorAdaptor();
        checkCommand(forKeySeq("cs[tok<RETURN>"),
                "fn[ar",'g',"ument];",
                "fn",'<',"ok>argument</ok>;");
        checkCommand(forKeySeq("cs)<LT>p><RETURN>"),
                "fn(ar",'g',"ument);",
                "fn",'<',"p>argument</p>;");
        checkCommand(forKeySeq("yswtok<RETURN>"),
                "fn[ar",'g',"ument];",
                "fn[ar",'<',"ok>gument</ok>];");
        checkCommand(forKeySeq("ysE<LT>p<RETURN>"),
                "fn(ar",'g',"ument);",
                "fn(ar",'<',"p>gument);</p>");
    }

	@Test
    public void testSurroundPlugin_cs_replaceTag() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        when(platform.getPlatformSpecificModeProvider()).thenReturn(
                (PlatformSpecificModeProvider) new SurroundModesProvider());
        reloadEditorAdaptor();
        // Simple replaces
        checkCommand(forKeySeq("cst<LT>ok<RETURN>"),
                "<root>\r",' ',"   <property>nill</property>\r</root>",
                "<root>\r    ",'<',"ok>nill</ok>\r</root>");
        checkCommand(forKeySeq("cst<LT>ok<GT><RETURN>"),
                "<root>\r",' ',"   <property>nill</property>\r</root>",
                "<root>\r    ",'<',"ok>nill</ok>\r</root>");
        
        // Replace tag, keep attributes (no <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok<RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok value=\"nill\"></ok>\r</root>");
        
        // Replace tag, remove attributes (see <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok<GT><RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok></ok>\r</root>");
        
        // Replace tag, add extra attributes (no <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok type=\"String\"<RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok type=\"String\" value=\"nill\"></ok>\r</root>");
        
        // Replace tag, overwrite attribute (<GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok type=\"String\"<GT><RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok type=\"String\"></ok>\r</root>");
    }

    @Test
    public void testSurroundPlugin_visual_insertTag() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        when(platform.getPlatformSpecificModeProvider()).thenReturn(
                (PlatformSpecificModeProvider) new SurroundModesProvider());
        reloadEditorAdaptor();
        // Simple replaces

        checkCommand(forKeySeq("evbS<LT>ok<RETURN>"),
                "<div>\r    this is some t",'e',"xt\r</div>",
                "<div>\r    this is some ",'<',"ok>text</ok>\r</div>");

        checkCommand(forKeySeq("viwS<LT>ok<RETURN>"),
                "<div>\r    this is some t",'e',"xt\r</div>",
                "<div>\r    this is some ",'<',"ok>text</ok>\r</div>");
    }

    @Test
    public void testSurroundPlugin_ys() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("ysiwb"),
                "so",'m',"ething",
                "",'(',"something)");

        checkCommand(forKeySeq("ys2iwb"),
                "so",'m',"ething funny",
                "",'(',"something funny)");

        checkCommand(forKeySeq("yssb"),
                "so",'m',"ething funny",
                "",'(',"something funny)");
    }

}
