package ut.ru.kontur.ytclient;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import ru.kontur.ytclient.http.ConnectionException;
import ru.kontur.ytclient.ParseException;
import ru.kontur.ytclient.http.SimpleCookiesStorage;
import ru.kontur.settings.SimpleYtConnectionSettingsStorage;
import ru.kontur.settings.YtConnectionSettingsStorageInterface;
import ru.kontur.ytclient.YtIssue;
import ru.kontur.ytclient.YtRest;

/**
 *
 * @author michael.plusnin
 */
@Ignore // TODO(mp): make up
public class YtRestTest {
    private YtRest ytRest;

    public YtRestTest() {
        YtConnectionSettingsStorageInterface settingsStorage
                = new SimpleYtConnectionSettingsStorage();
        settingsStorage.setBaseUrl("****");
        settingsStorage.setUsername("****");
        settingsStorage.setPassword("****");

        ytRest = new YtRest(settingsStorage, new SimpleCookiesStorage());
    }

    @Test
    public void testCustomFieldType() throws ConnectionException, ParseException {
        Assert.assertEquals("enum[1]", ytRest.getCustomFieldType("Type"));
        Assert.assertEquals(null, ytRest.getCustomFieldType("sdfgdsfg"));
    }

    @Test
    public void testGetEmptyListOfIssues() throws ConnectionException, ParseException {
        List<YtIssue> lst = ytRest.getListOfIssues("project: Qwer", null, 10, 0);
        if (lst == null)
            lst = new LinkedList<YtIssue>();
        Assert.assertEquals(new LinkedList<YtIssue>(), lst);
    }

    @Test
    public void testGetListOfIssues() throws ConnectionException, ParseException {
        List<YtIssue> lst = ytRest.getListOfIssues(
             "project: Asdf created: 2013-06",
             new ArrayList<String>() {{ add("id"); }},
             4,
             3
        );
        String[] ids = new String[] { "ERT-5162", "ERT-5163", "ERT-5164", "ERT-5167" };
        System.out.println(lst);
        Assert.assertEquals(4, lst.size());
        for (int i = 0; i < 4; i++) {
            Assert.assertEquals(ids[i], lst.get(i).getId());
            Assert.assertEquals(1, lst.get(i).getAllNamedFields().size());
        }
    }
}
