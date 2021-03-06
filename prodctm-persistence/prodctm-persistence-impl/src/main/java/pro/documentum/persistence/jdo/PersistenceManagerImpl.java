package pro.documentum.persistence.jdo;

import org.datanucleus.api.jdo.JDOPersistenceManager;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.api.jdo.NucleusJDOHelper;
import org.datanucleus.exceptions.NoPersistenceInformationException;
import org.datanucleus.exceptions.NucleusException;
import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.StoreData;
import org.datanucleus.util.Localiser;

import pro.documentum.persistence.common.util.DNMetaData;
import pro.documentum.util.auth.ICredentials;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class PersistenceManagerImpl extends JDOPersistenceManager implements
        ICredentials {

    private String _userName;

    private String _password;

    public PersistenceManagerImpl(final JDOPersistenceManagerFactory pmf,
            final String userName, final String password) {
        super(pmf, userName, password);
        _userName = userName;
        _password = password;
    }

    @Override
    public String getDocbase() {
        return null;
    }

    @Override
    public void setDocbase(final String docbase) {
        // nop
    }

    @Override
    public String getUserName() {
        return _userName;
    }

    @Override
    public String getPassword() {
        return _password;
    }

    @Override
    public void setUserName(final String userName) {
        _userName = userName;
    }

    @Override
    public void setPassword(final String password) {
        _password = password;
    }

    @Override
    public void sync(final ICredentials other) {
        setUserName(other.getUserName());
        setPassword(other.getPassword());
        setDocbase(other.getDocbase());
    }

    @Override
    public final Object newObjectIdInstance(final Class pcClass,
            final Object key) {
        assertIsOpen();
        try {
            if (pcClass == null) {
                throw new NucleusUserException(Localiser.msg("010028"));
            }
            ec.assertClassPersistable(pcClass);
            AbstractClassMetaData cmd = ec.getMetaDataManager()
                    .getMetaDataForClass(pcClass, ec.getClassLoaderResolver());
            if (cmd == null) {
                throw new NoPersistenceInformationException(pcClass.getName());
            }

            StoreData storeData = DNMetaData
                    .getStoreData(ec, pcClass.getName());

            if (storeData == null) {
                throw new NoPersistenceInformationException(pcClass.getName());
            }

            return ec.newObjectId(pcClass, key);
        } catch (NucleusException ne) {
            throw NucleusJDOHelper.getJDOExceptionForNucleusException(ne);
        }
    }

}
