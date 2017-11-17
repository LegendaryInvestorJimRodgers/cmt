package ndfs.mcndfs_1_naive;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.Thread;
import ndfs.NDFS;

/**
 * Implements the {@link ndfs.NDFS} interface, mostly delegating the work to a
 * worker class.
 */
public class NNDFS implements NDFS {
    //TODO put result variable here and not in Worker, deal with interrupts or
    // conditions and signals
    //OLDCODE:private final Worker worker;
    private Thread[] workers;

    /**
     * Constructs an NDFS object using the specified Promela file.
     *
     * @param promelaFile
     *            the Promela file.
     * @throws FileNotFoundException
     *             is thrown in case the file could not be read.
     */
    public NNDFS(File promelaFile, int nrWorker) throws FileNotFoundException {
        threads = new Thread[nrWorker];
        for(int i=0; i<nrWorker; i++){
            this.workers[i] = new Worker(promelaFile);
        }

        //this.worker = new Worker(promelaFile);
    }

    @Override
    public boolean ndfs() {
        for(int i = 0; i < nrWorker; i++){
            workers[i].start();
        }
        // TODO await condition: cycle found
        // then terminate all other threads
        // and report cycle
        //OLDCODE:return worker.getResult();
    }
}
