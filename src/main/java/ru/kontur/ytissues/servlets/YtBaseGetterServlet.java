package ru.kontur.ytissues.servlets;

import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import ru.kontur.ytissues.settings.ConfluenceSettingsStorage;
import ru.kontur.ytissues.settings.SettingsStorage;
import ru.kontur.ytissues.settings.YtSettings;
import scala.Option;

/**
 *
 * @author michael.plusnin
 */
public class YtBaseGetterServlet extends HttpServlet {
    private static Logger logger = Logger.getLogger(YtBaseGetterServlet.class);

    private SettingsStorage settingsStorage;

    public YtBaseGetterServlet(PluginSettingsFactory pluginSettingsFactory) {
        this.settingsStorage = new ConfluenceSettingsStorage(
            pluginSettingsFactory.createGlobalSettings()
        );
    }

    /**
     * Writes to response output stream JSON with
     * { host, port, path, protocol } fields, that getted from YT server url
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        URI ytBaseUrl = null;
        try {
            Option<YtSettings> settings = settingsStorage.ytSettings();
            if (settings.isDefined())
                ytBaseUrl = new URI(settings.get().url());
        } catch (URISyntaxException ex) { ytBaseUrl = null; }

        if (ytBaseUrl == null) {
            logger.warn("Not found yt base url in settings");
            response.getWriter().print("{}");
            response.getWriter().flush();
            return;
        }

        Map<String, String> responseParameters = new TreeMap<String, String>();
        responseParameters.put("host", ytBaseUrl.getHost());
        int port = ytBaseUrl.getPort();
        responseParameters.put("port", port != -1 ? Integer.toString(port) : null);
        responseParameters.put("path", ytBaseUrl.getPath());
        responseParameters.put("protocol", ytBaseUrl.getScheme());

        StringBuilder responseJSON = new StringBuilder();
        responseJSON.append("{");
        Iterator<Entry<String, String>> entryIt = responseParameters.entrySet().iterator();
        while (entryIt.hasNext()) {
            Entry<String, String> entry = entryIt.next();
            responseJSON.append("\"").append(entry.getKey()).append("\":");
            String value = entry.getValue();
            responseJSON.append("\"").append(value != null ? value : "").append("\"");
            if (entryIt.hasNext())
                responseJSON.append(",");
        }
        responseJSON.append("}");

        response.getWriter().write(responseJSON.toString());
        response.getWriter().flush();
    }
}
