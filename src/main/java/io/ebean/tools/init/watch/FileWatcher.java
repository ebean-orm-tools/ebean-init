package io.ebean.tools.init.watch;

import io.ebean.tools.init.Detection;
import io.ebean.tools.init.InteractionHelp;
import io.ebean.tools.init.action.DoGenerate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Watches a directory structure and generates finder / query beans.
 */
public class FileWatcher {

  private static final Logger log = LoggerFactory.getLogger(FileWatcher.class);

  private final Set<File> queue = new HashSet<>();

  private final Detection detection;

  private final InteractionHelp help;

  private boolean running;

  public FileWatcher(Detection detection, InteractionHelp help) {
    this.detection = detection;
    this.help = help;
  }

  /**
   * Called when watcher detects file events in the source directory.
   */
  private class Callback implements WatchDirCallback {

    /**
     * Process the file event.
     */
    @Override
    public void event(WatchEvent<Path> event, Path child, String eventKind) {

      log.trace("watch processing event:{} file:{}", eventKind, child);
      if (isDelete(eventKind)) {
        processFileDelete(child);

      } else {
        processFile(child);
      }
    }

    private boolean isDelete(String eventKind) {
      return "ENTRY_DELETE".equals(eventKind);
    }
  }

  private WatchDir watchDir;

  public boolean isRunning() {
    return running;
  }

  /**
   * Register the watch service on the source directory and process any events.
   * <p>
   * This event does not return.
   */
  public void start(Path sourceDirectory) {
    synchronized (queue) {
      running = true;
      log.info("starting watcher ...");
      timer = new Timer();
      watchDir = new WatchDir(sourceDirectory, true, new Callback(), new Skip());
      watchDir.start();
    }
  }

  public void stop() {

    synchronized (queue) {
      running = false;
      if (timer != null) {
        timer.purge();
        timer.cancel();
      }
      if (watchDir != null) {
        log.info("stopping watcher ...");
        watchDir.stop();
        log.info("stopped watcher ...");
        watchDir = null;
      }
    }
  }

  private static class Skip implements WatchDirSkip {

    @Override
    public boolean skip(Path path) {
      if (path.endsWith("finder") || path.endsWith("query")) {
        log.debug("skip path {}", path);
        return true;
      }
      return false;
    }
  }

  /**
   * Ignore hidden files (like git files).
   */
  private boolean isIgnoreFile(Path file) {
    if (file.toFile().isDirectory()) {
      return true;
    }
    return file.getFileName().toString().startsWith(".");
  }

  /**
   * A file was deleted so remove it from the destination.
   */
  private void processFileDelete(Path file) {

//    if (isIgnoreFile(file)) {
//      return;
//    }
    log.info("process delete file or directory " + file);
  }

  private Timer timer;

  private ProcessTask currentTask;

  /**
   * Process a file - copy or process html template.
   */
  private void processFile(Path file) {

    if (isIgnoreFile(file)) {
      log.info("ignore file or directory " + file);
      return;
    }
    synchronized (queue) {
      log.info("process 4 file or directory " + file);
      if (currentTask != null) {
        currentTask.cancel();
      }
      currentTask = new ProcessTask();
      timer.schedule(currentTask, 1000L);
      queue.add(file.toFile());
    }
  }

  class ProcessTask extends TimerTask {

    @Override
    public void run() {

      synchronized (queue) {

        List<File> touchedClasses = new ArrayList<>(queue);

        if (touchedClasses.isEmpty()) {
          log.debug("no touchedClasses to process");

        } else {
          log.info("process v2 files " + touchedClasses);

          DoGenerate doGenerate = new DoGenerate(detection, help);
          doGenerate.setEntityClasses(touchedClasses);
          doGenerate.generateFinders();
          doGenerate.generateQueryBeans();
          queue.clear();

//          File file = touchedClasses.get(0);
//          File parentDir = file.getParentFile();
//          parentDir.setLastModified(System.currentTimeMillis());
        }
      }
    }
  }

}
