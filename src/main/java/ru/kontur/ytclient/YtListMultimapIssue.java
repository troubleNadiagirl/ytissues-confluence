package ru.kontur.ytclient;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.util.List;
import org.joda.time.DateTime;

/**
 *
 * @author michael.plusnin
 */
public class YtListMultimapIssue implements YtIssue {
    private static final String ID_KEY                 = "id";
    private static final String JIRA_ID_KEY            = "jiraId";
    private static final String PROJECT_SHORT_NAME_KEY = "projectShortName";
    private static final String NUMBER_IN_PROJECT_KEY  = "numberInProject";
    private static final String SUMMARY_KEY            = "summary";
    private static final String DESCRIPTION_KEY        = "description";
    private static final String CREATED_KEY            = "created";
    private static final String UPDATED_KEY            = "updated";
    private static final String UPDATER_NAME_KEY       = "updaterName";
    private static final String RESOLVED_KEY           = "resolved";
    private static final String REPORTER_NAME_KEY      = "reporterName";
    private static final String VOTER_NAME_KEY         = "voterName";
    private static final String COMMENTS_COUNT_KEY     = "commentsCount";
    private static final String VOTES_KEY              = "votes";
    private static final String PERMITTED_GROUP_KEY    = "permittedGroup";

    ImmutableListMultimap<String, String> parseResults;

    public YtListMultimapIssue(ListMultimap<String, String> listMultimap) {
        parseResults = ImmutableListMultimap.copyOf(listMultimap);
    }

    private String getSingleValue(String key) {
        if (parseResults.containsKey(key)) {
            List<String> listResult = parseResults.get(key);
            if (listResult.size() != 1)
                return null;
            else
                return listResult.get(0);
        } else
            return null;
    }

    private DateTime getTime(String key) {
        String storedTimeInMillisAsString = getSingleValue(key);
        long storedTimeInMillis;
        try {
            storedTimeInMillis = Long.parseLong(storedTimeInMillisAsString);
        } catch(Exception e) {
            return null;
        }
        return new DateTime(storedTimeInMillis);
    }

    private int getIntOrZeroByDefault(String key) {
        String storedValue = getSingleValue(key);
        try {
            return Integer.parseInt(storedValue);
        } catch (Exception e) {
            return 0;
        }
    }

    private ImmutableList<String> getMultiple(String key) {
        if (!parseResults.containsKey(key))
            return ImmutableList.of();
        List<String> storedList = parseResults.get(key);
        return ImmutableList.copyOf(storedList);
    }

    @Override
    public String getId() {
        return getSingleValue(ID_KEY);
    }

    @Override
    public String getJiraId() {
        return getSingleValue(JIRA_ID_KEY);
    }

    @Override
    public String getProjectShortName() {
        return getSingleValue(PROJECT_SHORT_NAME_KEY);
    }

    @Override
    public Integer getNumberInProject() {
        String storedValue = getSingleValue(NUMBER_IN_PROJECT_KEY);
        try {
            return Integer.parseInt(storedValue);
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public String getSummary() {
        return getSingleValue(SUMMARY_KEY);
    }

    @Override
    public String getDescription() {
        return getSingleValue(DESCRIPTION_KEY);
    }

    @Override
    public DateTime getCreateTime() {
        return getTime(CREATED_KEY);
    }

    @Override
    public DateTime getUpdateTime() {
        return getTime(UPDATED_KEY);
    }

    @Override
    public String getUpdaterName() {
        return getSingleValue(UPDATER_NAME_KEY);
    }

    @Override
    public DateTime getResolveTime() {
        return getTime(RESOLVED_KEY);
    }

    @Override
    public ImmutableList<String> getVoterNames() {
        return getMultiple(VOTER_NAME_KEY);
    }

    @Override
    public String getReporterName() {
        return getSingleValue(REPORTER_NAME_KEY);
    }

    @Override
    public int getCommentsCount() {
        return getIntOrZeroByDefault(COMMENTS_COUNT_KEY);
    }

    @Override
    public int getVotesCount() {
        return getIntOrZeroByDefault(VOTES_KEY);
    }

    @Override
    public String getPermittedGroup() {
        return getSingleValue(PERMITTED_GROUP_KEY);
    }

    @Override
    public ImmutableListMultimap<String, String> getAllNamedFields() {
        return parseResults;
    }

    @Override
    public String toString() {
        return parseResults.toString();
    }
}
