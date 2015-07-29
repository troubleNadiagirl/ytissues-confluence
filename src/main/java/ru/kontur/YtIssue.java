package ru.kontur;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import org.joda.time.DateTime;

/**
 * The methods of this interface also described at
 * http://confluence.jetbrains.com/display/YTD5/Get+an+Issue
 * @author michael.plusnin
 */
public interface YtIssue {
    /**
     * Issue id in database
     * @return id, or <code>null</code> if id not found
     */
    String getId();

    /**
     * If issue was imported from Jira, represents id, that it have in Jira
     * @return jiraId, or <code>null</code> if jiraId not found
     */
    String getJiraId();

    /**
     * @return short name of the issue's project,
     * or <code>null</code> if short name not found
     */
    String getProjectShortName();

    /**
     * @return number of issue in project,
     * or <code>null</code> if number not found
     */
    Integer getNumberInProject();

    /**
     * @return summary of the issue, or <code>null</code> if summary not found
     */
    String getSummary();

    /**
     * @return description of the issue,
     * or <code>null</code> if description not found
     */
    String getDescription();

    /**
     * @return time when issue was created,
     * or <code>null</code> if this time not found
     */
    DateTime getCreateTime();

    /**
     * @return time when issue was last updated,
     * or <code>null</code> if this time not found
     */
    DateTime getUpdateTime();

    /**
     * @return login of the user, that was the last, who updated the issue,
     * or <code>null</code> if this login not found
     */
    String getUpdaterName();

    /**
     * @return If the issue is resolved, shows time,
     * when resolved state was last set to the issue,
     * otherwise <code>null</code>
     */
    DateTime getResolveTime();

    /**
     * @return login of user, who created the issue,
     * or <code>null</code> if this login not found
     */
    String getReporterName();

    /**
     * @return logins of users, that voted for issue,
     * or empty list if voter names not found
     */
    ImmutableList<String> getVoterNames();

    /**
     * @return number of comments in issue
     */
    int getCommentsCount();

    /**
     * @return number of votes for issue
     */
    int getVotesCount();

    /**
     * Returns user group, that has permission to read this issue; if group is not set,
     * it means that any user has access to this issue
     *
     * @return user group, or <code>null</code> if user group not setted
     */
    String getPermittedGroup();

    /**
     * Gets specific fields, objects is specified by implementation
     * Represent any field of the issue including custom fields (depending on
     * name attribute). Number and type of fields depends on project settings.
     * Keys of result is names of fields, non specific fields are excluded from result
     *
     * @return specificFields
     */
    ImmutableListMultimap<String, String> getAllNamedFields();
}
