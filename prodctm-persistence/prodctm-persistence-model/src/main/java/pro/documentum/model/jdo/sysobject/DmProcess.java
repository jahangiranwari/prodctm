package pro.documentum.model.jdo.sysobject;

import javax.jdo.annotations.PersistenceCapable;

import lombok.experimental.Accessors;

/**
 * @author Andrey B. Panfilov <andrey@panfilov.tel>
 */
@PersistenceCapable(table = "dm_process")
@Accessors(chain = true)
public class DmProcess extends DmSysObject {

}
