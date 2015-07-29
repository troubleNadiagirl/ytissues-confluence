package ru.kontur;

/**
 *
 * @author michael.plusnin
 */
public class IssueUrlComposer {
    private String ytBaseUrl;

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
        String result = new String(input.substring(0, lastNonSlashPosition + 1));
        return result;
    }
}
