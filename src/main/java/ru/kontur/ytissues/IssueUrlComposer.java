package ru.kontur.ytissues;

/**
 *
 * @author michael.plusnin
 */
public class IssueUrlComposer {
    private final String ytBaseUrl;

    public IssueUrlComposer(String ytBaseUrl) {
        this.ytBaseUrl = removeLastSlashes(ytBaseUrl);
    }

    public String compose(String issueId) {
        return ytBaseUrl + "/issue/" + issueId;
    }

    public static String removeLastSlashes(String input) {
        int lastNonSlashPosition = 0;
        for (int i = 0; i < input.length(); i++)
            if (input.charAt(i) != '/')
                lastNonSlashPosition = i;
        return input.substring(0, lastNonSlashPosition + 1);
    }
}
