package ru.kontur.ytissues;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.macro.Macro;
import com.atlassian.confluence.macro.MacroExecutionException;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import ru.kontur.ytissues.client.impl.YtClientProxy;
import ru.kontur.ytissues.client.YtClient;
import ru.kontur.ytissues.client.impl.YtClientImpl;
import ru.kontur.ytissues.servlets.InfoAndSettingsServlet;
import ru.kontur.ytissues.settings.ConfluenceSettingsStorage;
import ru.kontur.ytissues.settings.YtSettings;
import scala.Option;
import scala.concurrent.Await;
import scala.concurrent.ExecutionContext$;
import scala.concurrent.duration.Duration;

import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author michael.plusnin
 */
public class SingleYtMacro implements Macro {
    private TemplateRenderer templateRenderer;
    private YtClient ytClient;
    private YtSettings settings;
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

        settings = new ConfluenceSettingsStorage(pluginSettings).ytSettings().get();

        YtClient client = new YtClientImpl(
                settings.toYtClientSettings(),
                ExecutionContext$.MODULE$.global());

        ytClient = new YtClientProxy(settings.toYtProxySettings(),
                client, ExecutionContext$.MODULE$.global());
        this.i18n = i18n;
    }

    public String execute(
        Map<String, String> params,
        String defaultParam,
        ConversionContext cc
    ) throws MacroExecutionException {
        webResourceManager.requireResource(Constants.PROJECT_BASE_KEY() + ":cssResource");

        String issueIdOrUrl = params.get(Constants.ISSUE_ID_OR_URL_KEY());
        if (issueIdOrUrl == null)
            throw new MacroExecutionException(
                    i18n.getText(Constants.PROJECT_BASE_KEY() + ".exceptionMessage.issueIdOrUrlNotDefined"));

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
                        i18n.getText(Constants.PROJECT_BASE_KEY() + ".exceptionMessage.notYtIssueUrl"), issueIdOrUrl));
            String issueId = m.group(5);
            if (issueId == null)
                throw new MacroExecutionException(
                        i18n.getText(Constants.PROJECT_BASE_KEY() + ".exceptionMessage.issueNotFoundInUrl", issueIdOrUrl));
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

        IssueUrlComposer urlComposer = new IssueUrlComposer(settings.url());
        substitution.put("ref", urlComposer.compose(issueId));

        Option<Issue> issueOpt = Await.result(ytClient.getIssue(issueId), Duration.apply(1, TimeUnit.SECONDS));

        if (issueOpt.isDefined()) {
            Issue issue = issueOpt.get();
            substitution.put("summary", issue.summary());
            substitution.put("status", issue.state().isOpened() ?
                    i18n.getText(Constants.PROJECT_BASE_KEY() + ".openedStatus") :
                    i18n.getText(Constants.PROJECT_BASE_KEY() + ".closedStatus"));
            substitution.put("statusCssType", issue.state().isOpened() ? "ytopened" : "ytclosed");
        } else {
            substitution.put("summary", "");
            substitution.put("status", i18n.getText(Constants.PROJECT_BASE_KEY() + ".notExistsStatus"));
            substitution.put("statusCssType", "ytnotexists");
        }

        StringWriter sw = new StringWriter();
        templateRenderer.render("yt-single-issue.vm", substitution, sw);
        return sw.toString();
    }

    public BodyType getBodyType() {
        return BodyType.NONE;
    }

    public OutputType getOutputType() {
        return OutputType.INLINE;
    }
}