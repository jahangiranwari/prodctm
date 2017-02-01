package pro.documentum.dfs.remote.pool;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Test;

import com.documentum.thirdparty.javassist.ClassPool;
import com.documentum.thirdparty.javassist.CtClass;
import com.documentum.thirdparty.javassist.CtMethod;
import com.documentum.thirdparty.javassist.Modifier;
import com.emc.documentum.fs.datamodel.core.DataPackage;
import com.emc.documentum.fs.datamodel.core.ObjectIdentity;
import com.emc.documentum.fs.datamodel.core.ObjectIdentitySet;
import com.emc.documentum.fs.datamodel.core.Qualification;
import com.emc.documentum.fs.datamodel.core.context.Identity;
import com.emc.documentum.fs.datamodel.core.context.RepositoryIdentity;
import com.emc.documentum.fs.rt.AuthenticationException;
import com.emc.documentum.fs.rt.context.ContextFactory;
import com.emc.documentum.fs.rt.context.IServiceContext;
import com.emc.documentum.fs.services.core.client.IObjectService;

import pro.documentum.junit.DfcTestSupport;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class ServicePoolTest extends DfcTestSupport {

    @Test
    public void testPool() throws Exception {
        IServicePool pool = ServicePool.getInstance();
        IObjectService service = pool.getService(IObjectService.class, null);
        assertNotNull(service);
        assertNotNull(service.getServiceContext());
        assertTrue(service.getServiceContext() instanceof IServiceContextDecorator);
        assertTrue(service instanceof IPooledService);
        ((IPooledService) service).returnToPool();
        IObjectService service1 = pool.getService(IObjectService.class, null);
        assertFalse(service == service1);
    }

    @Test
    public void testObjectService() throws Exception {
        IServicePool pool = ServicePool.getInstance();
        IServiceContext context = ContextFactory.getInstance().newContext();
        Identity identity = new RepositoryIdentity(getSession()
                .getDocbaseName(), getLoginInfo().getUser(), getLoginInfo()
                .getPassword(), null);
        context.setIdentities(Arrays.asList(identity));
        IObjectService objectService = pool.getService(IObjectService.class,
                context);
        ObjectIdentity<Qualification<String>> objectIdentity = new ObjectIdentity<>(
                new Qualification<>("dm_server_config"), getSession()
                        .getDocbaseName());
        assertEquals(1, objectService.getServiceContext().getIdentityCount());
        assertEquals(getSession().getDocbaseName(),
                ((RepositoryIdentity) objectService.getServiceContext()
                        .getIdentity(0)).getRepositoryName());
        objectService.getServiceContext().addIdentity(identity);
        DataPackage dataPackage = objectService.get(new ObjectIdentitySet(
                objectIdentity), null);
        assertNotNull(dataPackage);

        IServiceContext newContext = ContextFactory.getInstance().newContext();
        identity = new RepositoryIdentity(getSession().getDocbaseName(),
                getLoginInfo().getUser(), RandomStringUtils.random(20), null);
        newContext.setIdentities(Arrays.asList(identity));

        try {
            ServiceContextDecorator.of(objectService).setWrapped(newContext);
        } catch (Exception ex) {
            assertTrue(ex instanceof AuthenticationException);
        }

    }

    public static void main(String[] args) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        String cls = "com.emc.documentum.fs.rt.context.impl.ServiceContextAdapter";
        CtClass cc = pool.get(cls);
        for (CtMethod method : cc.getMethods()) {
            if (!"getDeltaContext".equals(method.getName())) {
                continue;
            }
            int modifiers = method.getModifiers();
            if (!Modifier.isFinal(modifiers)) {
                continue;
            }
            method.setModifiers(Modifier.clear(modifiers, Modifier.FINAL));
        }
        File temp = File.createTempFile("ServiceContextAdapter.class", "");
        FileOutputStream fos = new FileOutputStream(temp);
        fos.write(cc.toBytecode());
        fos.close();
        System.out.println("Written to: " + temp.getAbsolutePath());
    }

}
