package ru.kontur.ytclient;

import ru.kontur.ytclient.http.ConnectionException;

import java.util.List;

/**
 *
 * @author michael.plusnin
 */
public interface YtInterface {
    YtIssue getIssue(String issueId) throws ConnectionException, ParseException;
    boolean checkTheIssueExists(String issueId) throws ConnectionException, ParseException;
    String getCustomFieldType(String fieldName) throws ConnectionException, ParseException;
    List<YtIssue> getListOfIssues(String filter, List<String> withFields,
            int maxIssuesCount, int afterIssues) throws ConnectionException, ParseException;
}
