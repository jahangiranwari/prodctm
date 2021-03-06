package pro.documentum.util.objects.changes.attributes.group;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.common.DfException;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class UsersNamesHandler extends AbstractGroupAttributeHandler {

    public UsersNamesHandler() {
        super();
    }

    @Override
    protected boolean doAccept(final Set<String> attrNames) {
        return containsKey(attrNames, "users_names");
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean doApply(final IDfGroup group, final Map<String, ?> values)
        throws DfException {
        List<String> users = (List<String>) values.remove("users_names");
        group.removeAllUsers();
        for (String userName : users) {
            group.addUser(userName);
        }
        return false;
    }

}
