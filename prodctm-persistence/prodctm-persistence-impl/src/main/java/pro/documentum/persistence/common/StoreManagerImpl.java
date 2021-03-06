package pro.documentum.persistence.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.Configuration;
import org.datanucleus.ExecutionContext;
import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.Transaction;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.ClassMetaData;
import org.datanucleus.metadata.ClassPersistenceModifier;
import org.datanucleus.store.AbstractStoreManager;
import org.datanucleus.store.NucleusConnection;
import org.datanucleus.store.NucleusConnectionImpl;
import org.datanucleus.store.StoreData;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.ConnectionFactory;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.schema.table.CompleteClassTable;

import com.documentum.fc.client.IDfSession;

import pro.documentum.persistence.common.schema.SchemaHandlerImpl;
import pro.documentum.persistence.common.util.DNMetaData;
import pro.documentum.persistence.common.util.Nucleus;
import pro.documentum.util.auth.ICredentials;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
public class StoreManagerImpl extends AbstractStoreManager {

    public static final String TRANSLATOR_TYPE = PropertyNames.PROPERTY_IDENTITY_STRING_TRANSLATOR_TYPE;

    public static final String IDENTITY_TYPE = PropertyNames.PROPERTY_DATASTORE_IDENTITY_TYPE;

    public static final String NAMING_FACTORY = PropertyNames.PROPERTY_IDENTIFIER_NAMING_FACTORY;

    public static final String PREFIX = "dctm";

    public StoreManagerImpl(final ClassLoaderResolver clr,
            final PersistenceNucleusContext nucleusContext,
            final Map<String, Object> props) {
        super(PREFIX, clr, nucleusContext, props);
        setConfiguration(nucleusContext);
        persistenceHandler = new PersistenceHandlerImpl(this);
        schemaHandler = new SchemaHandlerImpl(this);
    }

    public static String getDocbaseName(final String url) {
        return url.substring(StoreManagerImpl.PREFIX.length() + 1);
    }

    @Override
    public String getNativeQueryLanguage() {
        return "DQL";
    }

    private void setConfiguration(final PersistenceNucleusContext nucleusContext) {
        Configuration configuration = nucleusContext.getConfiguration();
        configuration.setProperty(TRANSLATOR_TYPE, PREFIX);
        configuration.setProperty(IDENTITY_TYPE, PREFIX);
        configuration.setProperty(NAMING_FACTORY, PREFIX);
    }

    @Override
    public NucleusConnection getNucleusConnection(
            final ExecutionContext executionContext) {
        ConnectionFactory connectionFactory = connectionMgr
                .lookupConnectionFactory(primaryConnectionFactoryName);
        ExecutionContext connectionContext = executionContext;
        Map<Object, Object> options = new HashMap<>();
        options.put(ICredentials.class,
                Nucleus.extractLoginInfo(executionContext));
        Transaction transaction = null;
        final boolean enlisted = executionContext.getTransaction().isActive();
        if (enlisted) {
            transaction = executionContext.getTransaction();
        } else {
            connectionContext = null;
        }
        final ManagedConnection managedConnection = connectionFactory
                .getConnection(connectionContext, transaction, options);
        managedConnection.lock();
        Runnable closeRunnable = new Runnable() {
            public void run() {
                managedConnection.unlock();
                if (!enlisted) {
                    managedConnection.close();
                }
            }
        };
        return new NucleusConnectionImpl(managedConnection.getConnection(),
                closeRunnable);
    }

    @Override
    public Collection<String> getSupportedOptions() {
        Set<String> result = new HashSet<>();
        result.add(StoreManager.OPTION_DATASTORE_ID);
        result.add(StoreManager.OPTION_ORM_EMBEDDED_COLLECTION_NESTED);
        result.add(StoreManager.OPTION_ORM_EMBEDDED_ARRAY_NESTED);
        return result;
    }

    @Override
    public Object getStrategyValue(final ExecutionContext ec,
            final AbstractClassMetaData cmd, final int absoluteFieldNumber) {
        DNMetaData.getStoreData(ec, cmd);
        return super.getStrategyValue(ec, cmd, absoluteFieldNumber);
    }

    public void manageClasses(final ClassLoaderResolver clr,
            final String... classNames) {
        throw new UnsupportedOperationException();
    }

    public void manageClasses(final ExecutionContext ec,
            final String... classNames) {
        if (classNames == null) {
            return;
        }
        ManagedConnection mconn = getConnection(ec);
        try {
            IDfSession session = (IDfSession) mconn.getConnection();
            manageClasses(ec.getClassLoaderResolver(), session, classNames);
        } finally {
            mconn.release();
        }
    }

    public void manageClasses(final ClassLoaderResolver clr,
            final IDfSession session, final String... classNames) {
        if (classNames == null) {
            return;
        }

        String[] filteredClassNames = getNucleusContext().getTypeManager()
                .filterOutSupportedSecondClassNames(classNames);

        List<AbstractClassMetaData> abstractClassMetaDatas = getMetaDataManager()
                .getReferencedClasses(filteredClassNames, clr);

        for (AbstractClassMetaData abstractClassMetaData : abstractClassMetaDatas) {
            ClassMetaData classMetaData = (ClassMetaData) abstractClassMetaData;
            if (classMetaData.isAbstract()) {
                continue;
            }
            if (classMetaData.getPersistenceModifier() != ClassPersistenceModifier.PERSISTENCE_CAPABLE) {
                continue;
            }

            String fullClassName = classMetaData.getFullClassName();
            if (storeDataMgr.managesClass(fullClassName)) {
                continue;
            }

            StoreData sd = storeDataMgr.get(fullClassName);
            if (sd != null) {
                continue;
            }
            CompleteClassTable table = new CompleteClassTable(this,
                    classMetaData, null);
            sd = newStoreData(classMetaData, clr);
            sd.setTable(table);
            registerStoreData(sd);
        }
    }

}
