package ut.ru.kontur;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.joda.time.DateTime;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import ru.kontur.YtXmlIssue;

/**
 *
 * @author michael.plusnin
 */
public class YtXmlIssueUnitTest {
    @Test
    public void test()
    {
        YtXmlIssue issue = getYtXmlIssue();
        assertEquals("HBR-63", issue.getId());
        assertEquals(null,     issue.getJiraId());
        assertEquals("HBR",    issue.getProjectShortName());
        assertEquals(new Integer(63), issue.getNumberInProject());
        assertEquals("summary", issue.getSummary());
        assertEquals("description", issue.getDescription());
        assertEquals(new DateTime(1262171005630L), issue.getCreateTime());
        assertEquals(new DateTime(1267630833573L), issue.getUpdateTime());
        assertEquals("rootU", issue.getUpdaterName());
        assertEquals("rootR", issue.getReporterName());
        Assert.assertArrayEquals(new String[0], issue.getVoterNames().toArray());
        assertEquals(2, issue.getCommentsCount());
        assertEquals(0, issue.getVotesCount());
        assertEquals(null, issue.getPermittedGroup());

        ListMultimap<String, String> specFields = ArrayListMultimap.create();
        specFields.put("Priority",  "Normal");
        specFields.put("Type",      "Bug");
        specFields.put("State",     "Won't fix");
        specFields.put("Assignee",  "qwer");
        specFields.put("Subsystem", "Configuration");

        List<String> fixVersions = Arrays.asList(
                new String[] { "2.0", "2.0.5", "2.0.7" });
        specFields.putAll("Fix versions", fixVersions);

        List<String> cf = Arrays.asList(new String[] {"0", "!"} );
        specFields.putAll("cf", cf);

        specFields.put("scf", "1265835603000");

        List<String> links = Arrays.asList(new String[] { "HBR-62", "HBR-57",
                "HBR-54", "HBR-49", "HBR-51", "HBR-49" });
        specFields.putAll("links", links);

        ImmutableListMultimap<String, String> allFields = issue.getAllNamedFields();
        for (Entry<String, String> entry : specFields.entries())
            Assert.assertTrue(entry.toString() + " not contains",
                    allFields.containsEntry(entry.getKey(), entry.getValue()));
    }

    @Test
    public void testId()
    {
        YtXmlIssue issue = getYtXmlIssue();
        assertEquals("HBR-63", issue.getId());
    }

    private YtXmlIssue getYtXmlIssue() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<issue id=\"HBR-63\">\n" +
        "    <field name=\"attachments\">\n" +
        "        <value url=\"/_persistent/uploadFile.html?file=45-46&amp;v=0&amp;c=true\">uploadFile.html</value>\n" +
        "    </field>\n" +
        "    <comment id=\"42-306\" author=\"root\" issueId=\"HBR-63\" deleted=\"false\" text=\"comment 1!\" shownForIssueAuthor=\"false\"\n" +
        "             created=\"1267030230127\">\n" +
        "        <replies/>\n" +
        "    </comment>\n" +
        "    <comment id=\"42-307\" author=\"root\" issueId=\"HBR-63\" deleted=\"false\" text=\"comment 2?\" shownForIssueAuthor=\"false\"\n" +
        "             created=\"1267030238721\" updated=\"1267030230127\">\n" +
        "        <replies/>\n" +
        "    </comment>\n" +
        "    <field name=\"Priority\">\n" +
        "        <value>Normal</value>\n" +
        "    </field>\n" +
        "    <field name=\"Type\">\n" +
        "        <value>Bug</value>\n" +
        "    </field>\n" +
        "    <field name=\"State\">\n" +
        "        <value>Won't fix</value>\n" +
        "    </field>\n" +
        "    <field name=\"Assignee\">\n" +
        "        <value>qwer</value>\n" +
        "    </field>\n" +
        "    <field name=\"Subsystem\">\n" +
        "        <value>Configuration</value>\n" +
        "    </field>\n" +
        "    <field name=\"Fix versions\">\n" +
        "        <value>2.0</value>\n" +
        "        <value>2.0.5</value>\n" +
        "        <value>2.0.7</value>\n" +
        "    </field>\n" +
        "    <field name=\"cf\">\n" +
        "        <value>0</value>\n" +
        "        <value>!</value>\n" +
        "    </field>\n" +
        "    <field name=\"scf\">\n" +
        "        <value>1265835603000</value>\n" +
        "    </field>\n" +
        "    <field name=\"links\">\n" +
        "        <value type=\"Depend\" role=\"depends on\">HBR-62</value>\n" +
        "        <value type=\"Duplicate\" role=\"duplicates\">HBR-57</value>\n" +
        "        <value type=\"Duplicate\" role=\"is duplicated by\">HBR-54</value>\n" +
        "        <value type=\"Relates\" role=\"relates to\">HBR-49</value>\n" +
        "        <value type=\"Relates\" role=\"is related to\">HBR-51</value>\n" +
        "        <value type=\"Depend\" role=\"is required for\">HBR-49</value>\n" +
        "    </field>\n" +
        "    <field name=\"projectShortName\">\n" +
        "        <value>HBR</value>\n" +
        "    </field>\n" +
        "    <field name=\"numberInProject\">\n" +
        "        <value>63</value>\n" +
        "    </field>\n" +
        "    <field name=\"summary\">\n" +
        "        <value>summary</value>\n" +
        "    </field>\n" +
        "    <field name=\"description\">\n" +
        "        <value>description</value>\n" +
        "    </field>\n" +
        "    <field name=\"created\">\n" +
        "        <value>1262171005630</value>\n" +
        "    </field>\n" +
        "    <field name=\"updated\">\n" +
        "        <value>1267630833573</value>\n" +
        "    </field>\n" +
        "    <field name=\"updaterName\">\n" +
        "        <value>rootU</value>\n" +
        "    </field>\n" +
        "    <field name=\"resolved\">\n" +
        "        <value>1267030108251</value>\n" +
        "    </field>\n" +
        "    <field name=\"reporterName\">\n" +
        "        <value>rootR</value>\n" +
        "    </field>\n" +
        "    <field name=\"commentsCount\">\n" +
        "        <value>2</value>\n" +
        "    </field>\n" +
        "    <field name=\"votes\">\n" +
        "        <value>0</value>\n" +
        "    </field>\n" +
        "</issue>";
        YtXmlIssue result = null;
        try {
            result = new YtXmlIssue(new ByteArrayInputStream(xml.getBytes()));
            Assert.assertTrue(true);
        } catch (Exception e) {
            Assert.assertFalse(e.getMessage(), true);
        }
        return result;
    }
}
