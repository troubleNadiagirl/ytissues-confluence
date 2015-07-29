package ru.kontur;

import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.atlassian.sal.api.user.UserManager;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author michael.plusnin
 */
public class InfoAndSettingsServlet extends HttpServlet {
    private UserManager      userManager;
    private TemplateRenderer templateRenderer;
    private LoginUriProvider loginUriProvider;

    private YtConnectionSettingsStorageInterface connSettings;
    private CookiesStorageInterface              cookies;

    public InfoAndSettingsServlet(
        UserManager           userManager,
        TemplateRenderer      templateRenderer,
        LoginUriProvider      loginUriProvider,
        PluginSettingsFactory pluginSettingsFactory
    ) {
        this.userManager      = userManager;
        this.templateRenderer = templateRenderer;
        this.loginUriProvider = loginUriProvider;

        this.connSettings = new PluginSettingsYtConnectionSettings(
            pluginSettingsFactory.createGlobalSettings(),
            Constants.PLUGIN_SETTINGS_BASE_KEY
        );
        this.cookies = new PluginSettingsCookieStorage(
            pluginSettingsFactory.createGlobalSettings(),
            Constants.PROJECT_BASE_KEY
        );
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!isSysAdminRequest(request)) {
            redirectToLogin(request, response);
            return;
        }

        Map<String, Object> substitution = new TreeMap<String, Object>();

        String url = connSettings.getBaseUrl();
        substitution.put("ytUrl", url != null ? url : "");

        String username = connSettings.getUsername();
        substitution.put("ytUsername", username != null ? username : "");

        response.setContentType("text/html:charset=utf-8");
        templateRenderer.render("admin.vm", substitution, response.getWriter());
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        if (!isSysAdminRequest(request)) {
            redirectToLogin(request, response);
            return;
        }

        String ytUrl = request.getParameter("ytUrl");
        String storedYtUrl = connSettings.getBaseUrl();
        if (ytUrl != null && !ytUrl.isEmpty() && !ytUrl.equals(storedYtUrl)) {
            cookies.erase();
            connSettings.setPassword(null);
            connSettings.setBaseUrl(ytUrl);
        }

        String ytUsername = request.getParameter("ytUsername");
        if (ytUsername != null && !ytUsername.isEmpty()) {
            cookies.erase();
            connSettings.setUsername(ytUsername);
        }

        String ytPassword = request.getParameter("ytPassword");
        if (ytPassword != null && !ytPassword.isEmpty()) {
            cookies.erase();
            connSettings.setPassword(ytPassword);
        }

        response.sendRedirect("ytissues");
    }

    private boolean isSysAdminRequest(HttpServletRequest request) {
        String username = userManager.getRemoteUsername(request);
        return username != null && userManager.isSystemAdmin(username);
    }

    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }

    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        return URI.create(builder.toString());
    }
}
