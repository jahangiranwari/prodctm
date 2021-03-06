package pro.documentum.persistence.jpa.query;

import static org.hamcrest.Matchers.endsWith;

import org.junit.Test;

import pro.documentum.model.jpa.user.DmUser;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class UpperQueryTest extends AbstractQueryTest {

    @Test
    public void testUpper1() throws Exception {
        String q = str(jpql(DmUser.class, "UPPER(userName) = 'dmadmin'"));
        assertThat(q, endsWith("WHERE UPPER(this.user_name)='dmadmin'"));
    }

    @Test
    public void testUpper2() throws Exception {
        String q = str(jpql(DmUser.class, "userName.toUpperCase() = 'dmadmin'"));
        assertThat(q, endsWith("WHERE UPPER(this.user_name)='dmadmin'"));
    }

    @Test
    public void testUpper3() throws Exception {
        String q = str(jpql(DmUser.class, "userName = UPPER('dmadmin')"));
        assertThat(q, endsWith("WHERE this.user_name=UPPER('dmadmin')"));
    }

}
