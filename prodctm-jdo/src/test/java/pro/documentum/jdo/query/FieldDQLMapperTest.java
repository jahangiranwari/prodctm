package pro.documentum.jdo.query;

import org.junit.Test;

import pro.documentum.model.DmUser;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class FieldDQLMapperTest extends AbstractDQLMapperTest {

    @Test
    public void testFieldEq() throws Exception {
        String q = newQuery(DmUser.class, "userName == userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name=this.user_login_name"));
    }

    @Test
    public void testFieldNotEq() throws Exception {
        String q = newQuery(DmUser.class, "userName != userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name!=this.user_login_name"));
    }

    @Test
    public void testFieldGt() throws Exception {
        String q = newQuery(DmUser.class, "userName > userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name>this.user_login_name"));
    }

    @Test
    public void testFieldGtEq() throws Exception {
        String q = newQuery(DmUser.class, "userName >= userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name>=this.user_login_name"));
    }

    @Test
    public void testFieldLt() throws Exception {
        String q = newQuery(DmUser.class, "userName < userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name<this.user_login_name"));
    }

    @Test
    public void testFieldLtEq() throws Exception {
        String q = newQuery(DmUser.class, "userName <= userLoginName");
        assertTrue(q.endsWith("WHERE this.user_name<=this.user_login_name"));
    }

}
