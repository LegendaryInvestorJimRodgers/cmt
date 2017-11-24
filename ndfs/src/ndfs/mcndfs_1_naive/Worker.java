package ndfs.mcndfs_1_naive;
import java.lang.Thread;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.*;
//import java.lang.Condition;

import graph.Graph;
import graph.GraphFactory;
import graph.State;
//import NNDFS.MonitorObject;
//import NNDFS.ThreadInfo;

/**
 * This is a straightforward implementation of Figure 1 of
 * <a href="http://www.cs.vu.nl/~tcs/cm/ndfs/laarman.pdf"> "the Laarman
 * paper"</a>.
 */
public class Worker extends Thread {

    private final Graph graph;
    private final Colors colors = new Colors();
    public ThreadInfo threadInfo;

    // Throwing an exception is a convenient way to cut off the search in case a
    // cycle is found.
    private static class CycleFoundException extends Exception {
    }

    /**
     * Constructs a Worker object using the specified Promela file.
     *
     *            the Promela file.
     * @throws FileNotFoundException
     *             is thrown in case the file could not be read.
     */
    //public Worker(File promelaFile, int nrWorker, boolean[] tterminationState, Condition ttermination) throws FileNotFoundException {
    public Worker(ThreadInfo threaddInfo) throws FileNotFoundException {
	    this.threadInfo = threaddInfo;
        this.graph = GraphFactory.createGraph(threaddInfo.pFile);
    }

    private void dfsRed(State s) throws CycleFoundException {
	if(Thread.interrupted()){
		throw new InterruptedException();
	}
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.CYAN)) {
                // signal main thread of cycle found
		threadInfo.terminationResult = true;
		synchronized(threadInfo.termination){
			threadInfo.termination.notify();
		}
		return;
                
            } else if (colors.hasColor(t, Color.BLUE)) {
                colors.color(t, Color.RED);
                dfsRed(t);
            }
        }
    }

    private void dfsBlue(State s) throws CycleFoundException {
	if(Thread.interrupted()){
		throw new InterruptedException();
	}
        colors.color(s, Color.CYAN);
        for (State t : graph.post(s)) {
            if (colors.hasColor(t, Color.WHITE)) {
                dfsBlue(t);
            }
        }
        if (s.isAccepting()) {
            dfsRed(s);
            colors.color(s, Color.RED);
        } else {
            colors.color(s, Color.BLUE);
        }
    }

    private void nndfs(State s) throws CycleFoundException {
        dfsBlue(s);
	
	//signal main thread that last worker has finished
	if(getAndIncrement(threadInfo.finishedCount) == threadInfo.nWorker -1){
		threadInfo.terminationResult = false;
		synchronized(threadInfo.termination){
			threadInfo.termination.notify();
		}
	}
    }
    @Override
    public void run() {
        nndfs(graph.getInitialState());
    }

    /*OLDCODE:public boolean getResult() {
        return result;
    }*/
}