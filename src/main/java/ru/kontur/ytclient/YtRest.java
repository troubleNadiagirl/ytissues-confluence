package ru.kontur.ytclient;

import com.google.common.collect.ListMultimap;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import ru.kontur.settings.YtConnectionSettingsStorageInterface;
import ru.kontur.ytclient.http.ConnectionException;
import ru.kontur.ytclient.http.CookiesStorageInterface;

/**
 *
 * @author michael.plusnin
 */
public class YtRest implements YtInterface {
    private static final String LOGIN_LINK = "/rest/user/login";
    private static final String GET_ISSUE_LINK = "/rest/issue/{issue}";
    private static final String CHECK_ISSUE_EXISTS_LINK
            = "/rest/issue/{issue}/exists";
    private static final String GET_CUSTOM_FIELD_TYPE_LINK
            = "/rest/admin/customfield/field/{customFieldName}";
    private CookiesStorageInterface cookiesStorage;
    private YtConnectionSettingsStorageInterface connSettings;

    private class HttpResponseEntry {
        private int responseCode;
        private InputStream in;

        public HttpResponseEntry(int responseCode, InputStream in) {
            this.responseCode = responseCode;
            this.in = in;
        }

        public int getResponseCode() {
            return responseCode;
        }

        public InputStream getInputStream() {
            return in;
        }
    }

    public YtRest(YtConnectionSettingsStorageInterface connSettings,
            CookiesStorageInterface cookiesStorage) {
        this.connSettings   = connSettings;
        this.cookiesStorage = cookiesStorage;
    }

    private void login() throws UnableLoginException {
        int responseCode;
        String responseMess;
        try {
            URL loginUrl = new URL(connSettings.getBaseUrl() + LOGIN_LINK);
            HttpURLConnection connection
                    = (HttpURLConnection)loginUrl.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            OutputStream os = null;
            try {
                os = connection.getOutputStream();
                String postRequestParameters =
                          "login="
                        + URLEncoder.encode(connSettings.getUsername(), "UTF-8")
                        + "&password="
                        + URLEncoder.encode(connSettings.getPassword(), "UTF-8");
                os.write(postRequestParameters.getBytes());
                os.flush();
            } finally {
                if (os != null)
                    os.close();
            }

            ArrayList<String> cookiesEntries = new ArrayList<String>();
            for (Entry<String, List<String>> entry : connection.getHeaderFields().entrySet())
                if ("set-cookie".equalsIgnoreCase(entry.getKey()))
                    cookiesEntries.addAll(entry.getValue());
            cookiesStorage.setCookies(cookiesEntries);

            responseCode = connection.getResponseCode();
            responseMess = connection.getResponseMessage();
        } catch (Exception e) {
            throw new UnableLoginException(e);
        }
        if (responseCode != HttpURLConnection.HTTP_OK)
            throw new UnableLoginException("HTTP " + responseCode
                + ", message: '" + responseMess + "'");
    }

    private HttpResponseEntry restGet(String query) throws ConnectionException {
        Exception lastException = null;
        for (int iteration = 0; iteration < 2; iteration++)
            try {
                URL getUrl = new URL(connSettings.getBaseUrl() + query);
                HttpURLConnection connection =
                        (HttpURLConnection)getUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Cookie", cookiesStorage.getCookies());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED
                        || responseCode == HttpURLConnection.HTTP_FORBIDDEN) {
                    login();
                    continue;
                }

                InputStream in;
                try {
                    in = connection.getInputStream();
                } catch (IOException e) {
                    in = connection.getErrorStream();
                }

                return new HttpResponseEntry(responseCode, in);
            } catch (Exception e) {
                lastException = e;
                continue;
            }
        if (lastException != null)
            throw new ConnectionException(lastException);
        else
            throw new UnableLoginException();
    }

    @Override
    public YtIssue getIssue(String issueId) throws ConnectionException, ParseException {
        HttpResponseEntry httpEntry =
                restGet(GET_ISSUE_LINK.replaceAll("\\{issue\\}", issueId));
        if (httpEntry.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new ConnectionException("Not ok http response code: '"
                    + httpEntry.getResponseCode() + "'");
        InputStream in = httpEntry.getInputStream();
        try {
            return new YtXmlIssue(new BufferedInputStream(in));
        } catch(IOException e) {
            throw new ConnectionException(e);
        } finally {
            tryClose(in);
        }
    }

    @Override
    public boolean checkTheIssueExists(String issueId) throws ConnectionException {
        HttpResponseEntry responseEntry =
                restGet(CHECK_ISSUE_EXISTS_LINK.replaceAll("\\{issue\\}", issueId));
        return responseEntry.getResponseCode() == HttpURLConnection.HTTP_OK;
    }

    @Override
    public String getCustomFieldType(String fieldName) throws ConnectionException, ParseException {
        HttpResponseEntry responseEntry = restGet(GET_CUSTOM_FIELD_TYPE_LINK.replaceAll("\\{customFieldName\\}", fieldName));
        if (responseEntry.getResponseCode() != HttpURLConnection.HTTP_OK)
            return null;

        YtCustomFieldXmlHandler handler = new YtCustomFieldXmlHandler();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser saxParser;
        try {
            saxParser = saxParserFactory.newSAXParser();
            saxParser.parse(responseEntry.getInputStream(), handler);
        } catch (SAXException e) {
            throw new ParseException(e);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } catch (ParserConfigurationException e) {
            throw new ParseException(e);
        } finally {
            tryClose(responseEntry.in);
        }
        return handler.getParsedType();
    }

    @Override
    public List<YtIssue> getListOfIssues(
        String filter,
        List<String> withFields,
        int maxIssuesCount,
        int afterIssues
    ) throws ConnectionException, ParseException {
        // "/rest/issue?{filter}&{with}&{max}&{after}"
        StringBuilder httpUrl = new StringBuilder("/rest/issue?");
        try {
            httpUrl.append("filter=").append(URLEncoder.encode(filter, "utf-8"));
            if (withFields != null)
                for (String withField : withFields)
                    httpUrl.append("&with=").append(URLEncoder.encode(withField, "utf-8"));
            httpUrl.append("&max=").append(maxIssuesCount);
            httpUrl.append("&after=").append(afterIssues);
        } catch (UnsupportedEncodingException e) {
            throw new ConnectionException(e);
        }


        HttpResponseEntry responseEntry = restGet(httpUrl.toString());

        if (responseEntry.responseCode != HttpURLConnection.HTTP_OK)
            return null;

        YtListOfIssuesXmlHandler handler = new YtListOfIssuesXmlHandler();
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser;
        try {
            parser = saxParserFactory.newSAXParser();
            parser.parse(responseEntry.in, handler);
        } catch (ParserConfigurationException e) {
            throw new ParseException(e);
        } catch (SAXException e) {
            throw new ParseException(e);
        } catch (IOException e) {
            throw new ConnectionException(e);
        } finally {
            tryClose(responseEntry.in);
        }

        List<ListMultimap<String, String>> parsedValue = handler.getParsedValue();

        List<YtIssue> result = new ArrayList<YtIssue>(parsedValue.size());
        for (ListMultimap<String, String> issueMultimap : handler.getParsedValue())
            result.add(new YtListMultimapIssue(issueMultimap));

        return result;
    }

    private static void tryClose(Closeable cl) throws ConnectionException {
        try {
            cl.close();
        } catch (IOException e) {
            throw new ConnectionException(e);
        }
    }
}
