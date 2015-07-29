package ru.kontur;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author michael.plusnin
 */
public class SingleYtMacro implements Macro {
    private TemplateRenderer templateRenderer;
    private YtInterface ytInterface;
    private YtConnectionSettingsStorageInterface connSettings;
    private I18nResolver i18n;
    private WebResourceManager webResourceManager;

    public SingleYtMacro(
        TemplateRenderer templateRenderer,
        I18nResolver i18n,
        PluginSettingsFactory pluginSettingsFactory,
        WebResourceManager webResourceManager
    ) {
        this.templateRenderer = templateRenderer;
        this.webResourceManager = webResourceManager;

        PluginSettings pluginSettings = pluginSettingsFactory.createGlobalSettings();
        connSettings = new PluginSettingsYtConnectionSettings(
            pluginSettings,
            Constants.PLUGIN_SETTINGS_BASE_KEY
        );
        CookiesStorageInterface cookiesStorage = new PluginSettingsCookieStorage(
            pluginSettings,
            Constants.PLUGIN_SETTINGS_BASE_KEY
        );
        ytInterface = new YtRest(connSettings, cookiesStorage);
        this.i18n = i18n;
    }

    @Override
    public String execute(
        Map<String, String> params,
        String defaultParam,
        ConversionContext cc
    ) throws MacroExecutionException {
        webResourceManager.requireResource(Constants.PROJECT_BASE_KEY + ":cssResource");

        String issueIdOrUrl = params.get(Constants.ISSUE_ID_OR_URL_KEY);
        if (issueIdOrUrl == null)
            throw new MacroExecutionException(
                    i18n.getText(Constants.PROJECT_BASE_KEY + ".exceptionMessage.issueIdOrUrlNotDefined"));

        String issueIdRE = "^[A-Za-z0-9]+\\-\\d+$";
        if (issueIdOrUrl.matches(issueIdRE)) {
            try {
                return getIssueXhtmlElement(issueIdOrUrl);
            } catch(Exception e) {
                throw new MacroExecutionException(e);
            }
        } else {
            //                      (1   protocol    )     (2 hostname and port                 )
            //                                              (3 hostname            )    (4 port)
            String urlRegexp = "^(?:([A-Za-z\\.0-9-]+)://)?(([^/:]+|(?:\\[[^/]*\\]))(?::(\\w+))?)/"
                    // (                   some path             )       (5 issueId       )    (6 anchor)
                    + "(?:[A-Za-z0-9_\\.~!*'();@&=+$,?%\\[\\]-]*/)*issue/([A-Za-z0-9]+\\-\\d+)(?:#(.*))?$";
            Pattern issueUrlPat = Pattern.compile(urlRegexp);
            Matcher m = issueUrlPat.matcher(issueIdOrUrl);
            if (!m.matches())
                throw new MacroExecutionException(MessageFormat.format(
                        i18n.getText(Constants.PROJECT_BASE_KEY + ".exceptionMessage.notYtIssueUrl"), issueIdOrUrl));
            String issueId = m.group(5);
            if (issueId == null)
                throw new MacroExecutionException(
                        i18n.getText(Constants.PROJECT_BASE_KEY + ".exceptionMessage.issueNotFoundInUrl", issueIdOrUrl));
            try {
                return getIssueXhtmlElement(issueId);
            } catch(Exception e) {
                throw new MacroExecutionException(e);
            }
        }
    }

    private String getIssueXhtmlElement(String issueId) throws Exception {
            Map<String, Object> substitution = new TreeMap<String, Object>();
            substitution.put("id", issueId.toUpperCase());
            IssueUrlComposer urlComposer = new IssueUrlComposer(connSettings.getBaseUrl());
            substitution.put("ref", urlComposer.compose(issueId));

            boolean isIssueExists = ytInterface.checkTheIssueExists(issueId);
            if (isIssueExists) {
                YtIssue issue = ytInterface.getIssue(issueId);
                substitution.put("summary", issue.getSummary());
                substitution.put("status", issue.getResolveTime() == null ?
                        i18n.getText(Constants.PROJECT_BASE_KEY + ".openedStatus") :
                        i18n.getText(Constants.PROJECT_BASE_KEY + ".closedStatus"));
                substitution.put("statusCssType", issue.getResolveTime() == null ? "ytopened" : "ytclosed");
            } else {
                substitution.put("summary", "");
                substitution.put("status", i18n.getText(Constants.PROJECT_BASE_KEY + ".notExistsStatus"));
                substitution.put("statusCssType", "ytnotexists");
            }

            StringWriter sw = new StringWriter();
            templateRenderer.render("yt-single-issue.vm", substitution, sw);
            return sw.toString();
    }

    @Override
    public BodyType getBodyType() {
        return BodyType.NONE;
    }

    @Override
    public OutputType getOutputType() {
        return OutputType.INLINE;
    }
}
