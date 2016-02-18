package it.confluence.macro;

import com.atlassian.confluence.api.model.content.Content;
import com.atlassian.confluence.it.Page;
import com.atlassian.confluence.pageobjects.page.content.EditContentPage;
import com.atlassian.connect.test.confluence.pageobjects.EditorWithPropertyPanel;
import com.atlassian.connect.test.confluence.pageobjects.ExtensibleMacroPropertyPanel;
import com.atlassian.elasticsearch.shaded.google.common.collect.Lists;
import com.atlassian.plugin.connect.modules.beans.BaseContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.BaseContentMacroModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.pageobjects.RemotePluginDialog;
import com.atlassian.plugin.connect.test.common.pageobjects.RenderedMacro;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import it.confluence.ConfluenceWebDriverTestBase;
import it.confluence.MacroStorageFormatBuilder;

import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.modules.beans.DynamicContentMacroModuleBean.newDynamicContentMacroModuleBean;
import static com.atlassian.plugin.connect.modules.util.ModuleKeyUtils.randomName;
import static com.atlassian.plugin.connect.test.common.servlet.ConnectAppServlets.echoQueryParametersServlet;
import static com.atlassian.plugin.connect.test.confluence.product.ConfluenceTestedProductAccessor.toConfluenceUser;
import static it.confluence.servlet.ConfluenceAppServlets.macroPropertyPanel;
import static it.confluence.servlet.ConfluenceAppServlets.macroPropertyPanelWithDialog;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class TestMacroPropertyPanel extends ConfluenceWebDriverTestBase
{

    public static final String PROPERTY_PANEL_URL = "/render-property-panel";
    public static final String PROPERTY_PANEL_WITH_DIALOG_URL = "/render-property-panel-with-dialog";

    protected static final String PROPERTY_PANEL_MACRO_WITH_DIALOG_NAME = "Property Panel with dialog Macro";
    protected static final String PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY = "property-panel-with-dialog-macro";

    protected static final String PROPERTY_PANEL_MACRO_KEY = "property-panel-macro";
    protected static final String PROPERTY_PANEL_MACRO_NAME = "Property Panel Macro";

    protected static final String PROPERTY_PANEL_MACRO_WITH_CONTROLS_NAME = "Property Panel with controls";
    protected static final String PROPERTY_PANEL_MACRO_WITH_CONTROLS_KEY = "property-panel-macro-with-controls";
    private static List<ControlBean> controls;

    protected static final String DIALOG_KEY = "dialog-key";
    protected static final String DIALOG_NAME = "Dialog";

    protected static final String EDITOR_MACRO_NAME = "Editor Macro";
    protected static final String EDITOR_MACRO_KEY = "editor-macro";

    protected static final String SINGLE_PARAM_ID = "param1";
    protected static final String SINGLE_PARAM_NAME = "Parameter 1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private static ConnectRunner remotePlugin;
    private static String addonKey;

    @BeforeClass
    public static void startConnectAddOn() throws Exception
    {
        final InstallHandlerServlet installHandlerServlet = new InstallHandlerServlet();
        addonKey = AddonTestUtils.randomAddonKey();

        DynamicContentMacroModuleBean propertyPanelMacro = createPropertyPanelMacro(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean propertyPanelWithDialogMacro = createPropertyPanelMacroWithDialog(newDynamicContentMacroModuleBean());
        DynamicContentMacroModuleBean propertyPanelWithCustomControls = createPropertyPanelMacroWithControls(newDynamicContentMacroModuleBean());
        ConnectPageModuleBean propertyPanelDialogPage = createPropertyPanelDialogPage(newPageBean());
        DynamicContentMacroModuleBean editorMacro = createEditorMacro(newDynamicContentMacroModuleBean());


        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .addJWT(installHandlerServlet)
                .setAuthenticationToNone()
                .addScope(ScopeName.ADMIN) // for using ap.request
                .addModules("dynamicContentMacros",
                        propertyPanelMacro,
                        propertyPanelWithDialogMacro,
                        propertyPanelWithCustomControls,
                        editorMacro
                )
                .addModules("generalPages",
                        propertyPanelDialogPage
                )
                .addRoute(PROPERTY_PANEL_URL, macroPropertyPanel())
                .addRoute(PROPERTY_PANEL_WITH_DIALOG_URL, macroPropertyPanelWithDialog())
                .addRoute("/echo/params", echoQueryParametersServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddOn() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void testMacroPropertyPanelContainsIFrame() throws Exception
    {
        String macroBody = "My property panel iframe test";
        String body = new MacroStorageFormatBuilder(PROPERTY_PANEL_MACRO_KEY).richTextBody(macroBody).build();
        Content page = createPage(randomName(PROPERTY_PANEL_MACRO_KEY), body);

        final EditContentPage editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), new Page(page.getId().asLong()));

        EditorWithPropertyPanel editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);
        ExtensibleMacroPropertyPanel propertyPanel = editor.openPropertyPanel(PROPERTY_PANEL_MACRO_KEY);
        assertThat("Property panel has iframe", propertyPanel.hasIframe());
        editorPage.save();
    }

    @Test
    public void testMacroPropertyPanelIFrameChangesParameter() throws Exception
    {
        String macroBody = "My property panel iframe test";

        String body = new MacroStorageFormatBuilder(PROPERTY_PANEL_MACRO_KEY).richTextBody(macroBody).build();
        Content pageContent = createPage(randomName(PROPERTY_PANEL_MACRO_KEY), body);

        final Page page = new Page(pageContent.getId().asLong());

        EditContentPage editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), page);
        EditorWithPropertyPanel editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);
        editor.openPropertyPanel(PROPERTY_PANEL_MACRO_KEY);

        editorPage.save();

        RenderedMacro renderedMacro = confluencePageOperations.findMacroWithIdPrefix(PROPERTY_PANEL_MACRO_KEY);
        assertThat(renderedMacro.getFromQueryString("param1"), is("ThisIsMyGreatNewParamValue"));

        //Now test that parameters can be read

        editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), page);

        editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);

        editor.openPropertyPanel(PROPERTY_PANEL_MACRO_KEY);

        editorPage.save();

        renderedMacro = confluencePageOperations.findMacroWithIdPrefix(PROPERTY_PANEL_MACRO_KEY);
        assertThat(renderedMacro.getFromQueryString("param1"), is("ThisIsMyGreatNewParamValueThisIsMyGreatNewParamValue"));
    }

    @Test
    public void testMacroPropertyPanelDoesNotContainIFrame() throws Exception
    {
        String macroBody = "My property panel iframe test";

        String body = new MacroStorageFormatBuilder(EDITOR_MACRO_KEY).richTextBody(macroBody).build();
        Content page = createPage(randomName(EDITOR_MACRO_KEY), body);

        final EditContentPage editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), new Page(page.getId().asLong()));

        EditorWithPropertyPanel editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);
        final ExtensibleMacroPropertyPanel propertyPanel = editor.openPropertyPanel(EDITOR_MACRO_KEY);
        assertThat("Property panel does not have iframe", !propertyPanel.hasIframe());
        editorPage.save();
    }

    @Test
    public void testMacroPropertyPanelCanLaunchDialog() throws Exception
    {
        String macroBody = "My property panel iframe test";

        String body = new MacroStorageFormatBuilder(PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY).richTextBody(macroBody).build();
        Content pageContent = createPage(randomName(PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY), body);

        EditContentPage editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), new Page(pageContent.getId().asLong()));

        EditorWithPropertyPanel editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);
        final ExtensibleMacroPropertyPanel propertyPanel = editor.openPropertyPanel(PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY);

        RemotePluginDialog dialog = confluencePageOperations.findDialog(ModuleKeyUtils.addonAndModuleKey(addonKey, DIALOG_KEY));

        final long propertyPanelZIndex = propertyPanel.getZIndex();
        final long auiBlanketZIndex = dialog.getAuiBlanketZIndex();

        assertThat("The property panel is below the aui blanket", propertyPanelZIndex < auiBlanketZIndex);

        dialog.submitAndWaitUntilHidden();

        editorPage.save();

        RenderedMacro renderedMacro = confluencePageOperations.findMacroWithIdPrefix(PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY);
        assertThat(renderedMacro.getFromQueryString("param1"), is("ThisIsMyGreatNewParamValue"));
    }

    @Test
    public void testMacroPropertyPanelContainsNewControl() throws Exception
    {
        String macroBody = "My property panel iframe test";
        String body = new MacroStorageFormatBuilder(PROPERTY_PANEL_MACRO_WITH_CONTROLS_KEY).richTextBody(macroBody).build();
        Content page = createPage(randomName(PROPERTY_PANEL_MACRO_WITH_CONTROLS_KEY), body);

        final EditContentPage editorPage = getProduct().loginAndEdit(toConfluenceUser(testUserFactory.basicUser()), new Page(page.getId().asLong()));

        EditorWithPropertyPanel editor = product.getPageBinder().bind(EditorWithPropertyPanel.class);
        ExtensibleMacroPropertyPanel propertyPanel = editor.openPropertyPanel(PROPERTY_PANEL_MACRO_WITH_CONTROLS_KEY);
        for (ControlBean control : controls)
        {
            assertThat("Property panel has control", propertyPanel.hasButton(control.getDisplayName()));
        }
        editorPage.save();
    }

    protected static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createPropertyPanelMacroWithControls(T builder)
    {
        return builder
                .withKey(PROPERTY_PANEL_MACRO_WITH_CONTROLS_KEY)
                .withUrl("/echo/params?" + SINGLE_PARAM_ID + "={" + SINGLE_PARAM_ID + "}")
                .withName(new I18nProperty(PROPERTY_PANEL_MACRO_NAME, null))
                .withPropertyPanel(MacroPropertyPanelBean.newMacroPropertyPanelBean()
                        .withUrl(PROPERTY_PANEL_URL)
                        .withControls(createControlBeans())
                        .build()
                )
                .withParameters(MacroParameterBean.newMacroParameterBean()
                        .withIdentifier(SINGLE_PARAM_ID)
                        .withName(new I18nProperty(SINGLE_PARAM_NAME, null))
                        .withType("string")
                        .build()
                )
                .withEditor(MacroEditorBean.newMacroEditorBean()
                        .withUrl(PROPERTY_PANEL_URL)
                        .withHeight("200px")
                        .withWidth("200px")
                        .build()
                )
                .build();
    }

    private static List<ControlBean> createControlBeans()
    {
        controls =  Lists.newArrayList(ControlBean.newControlBean()
                .withType("button")
                .withKey("charlie-button")
                .withName(new I18nProperty("Let's Charlie", "charlie.button.name"))
                .build());
        return controls;
    }

    public static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createPropertyPanelMacro(T builder)
    {
        return builder
                .withKey(PROPERTY_PANEL_MACRO_KEY)
                .withUrl("/echo/params?" + SINGLE_PARAM_ID + "={" + SINGLE_PARAM_ID + "}")
                .withName(new I18nProperty(PROPERTY_PANEL_MACRO_NAME, null))
                .withPropertyPanel(MacroPropertyPanelBean.newMacroPropertyPanelBean()
                                .withUrl(PROPERTY_PANEL_URL)
                                .build()
                )
                .withParameters(MacroParameterBean.newMacroParameterBean()
                                .withIdentifier(SINGLE_PARAM_ID)
                                .withName(new I18nProperty(SINGLE_PARAM_NAME, null))
                                .withType("string")
                                .build()
                )
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withUrl(PROPERTY_PANEL_URL)
                                .withHeight("200px")
                                .withWidth("200px")
                                .build()
                )
                .build();
    }

    public static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createPropertyPanelMacroWithDialog(T builder)
    {
        return builder
                .withKey(PROPERTY_PANEL_MACRO_WITH_DIALOG_KEY)
                .withUrl("/echo/params?" + SINGLE_PARAM_ID +  "={" + SINGLE_PARAM_ID + "}")
                .withName(new I18nProperty(PROPERTY_PANEL_MACRO_WITH_DIALOG_NAME, null))
                .withPropertyPanel(MacroPropertyPanelBean.newMacroPropertyPanelBean()
                                .withUrl(PROPERTY_PANEL_WITH_DIALOG_URL)
                                .build()
                )
                .withParameters(MacroParameterBean.newMacroParameterBean()
                                .withIdentifier(SINGLE_PARAM_ID)
                                .withName(new I18nProperty(SINGLE_PARAM_NAME, null))
                                .withType("string")
                                .build()
                )
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withUrl(PROPERTY_PANEL_WITH_DIALOG_URL)
                                .withHeight("200px")
                                .withWidth("200px")
                                .build()
                )
                .build();
    }

    protected static ConnectPageModuleBean createPropertyPanelDialogPage(ConnectPageModuleBeanBuilder builder)
    {
        return builder
                .withName(new I18nProperty(DIALOG_NAME, null))
                .withUrl(PROPERTY_PANEL_URL)
                .withKey(DIALOG_KEY)
                .withLocation("none")
                .build();
    }


    public static <T extends BaseContentMacroModuleBeanBuilder<T, B>, B extends BaseContentMacroModuleBean> B createEditorMacro(T builder)
    {
        return builder
                .withKey(EDITOR_MACRO_KEY)
                .withUrl("/echo/params?footy={footy}")
                .withName(new I18nProperty(EDITOR_MACRO_NAME, null))
                .withEditor(MacroEditorBean.newMacroEditorBean()
                                .withUrl("/render-editor")
                                .withHeight("200px")
                                .withWidth("300px")
                                .build()
                )
                .build();
    }
}
